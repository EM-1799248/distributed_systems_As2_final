/*
AggregationServer.java: Manages incoming GET and PUT requests.
It uses the Lamport Clock to track the order of requests and stores data in a dataStore.
JSON data is parsed using Gson.

- Checks data
- distributes to clients as they request it
- accepts updates from content servers
- stores data persistently until content server no longer in contact, or has not been in contact for 30 seconds
- can have multiple clients sent GET requests
- removes content from server that has not been updated in 30 sec (be efficient)
- when storage file is created, returns '201-HTTP_CREATED'
- when updates, returns '200' if unsuccessful, '201' if successful
- any request other than GET and PUT returns '400'
- sending no content to server returns '204'
- incorrect JSON returns '500'
- default port is 4567, but accepts single command line arg for port number
- contains the ag server's main method
- maintains a lamport clock
 */

import java.io.*;
import java.net.*;
import java.util.*;
import com.google.gson.Gson;

public class AggregationServer {

    private static final int DEFAULT_PORT = 4567;
    private static final int TIMEOUT = 30 * 1000; // 30 seconds timeout
    private static final Gson gson = new Gson();
    private static Map<String, DataEntry> dataStore = new HashMap<>();
    private static LamportClock clock = new LamportClock();

    public static void main(String[] args) {
        System.out.println("line 36");

        // set the port to a user-specified value if provided, or a default value if not
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        // create a server socket bound to the port that will listen for incoming client connections
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Aggregation Server is running on port " + port);

            // Enters an infinite loop, continuously waiting for and accepting new client connections
            while (true) {
                System.out.println("line 47");

                // Waits for a client to connect, then create a new Socket object when i client connects
                try (Socket clientSocket = serverSocket.accept()) {
                    // Call method that handles client connection
                    handleClient(clientSocket);
                } catch (IOException e) {
                    // If error occurs, catch exception, and print error message to  console
                    System.err.println("Error handling client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            // If error occurs, catch exception, and print error message to  console
            System.err.println("Could not start server: " + e.getMessage());
        }
    }

    // Takes a socket that represents the connection between the server and the client
    // Throw an input/output exception during the communication process
    private static void handleClient(Socket clientSocket) throws IOException {
        System.out.println("line 67");

        // Reads raw byte stream from client and converts into characters
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        // Send a response back to the client
        OutputStream out = clientSocket.getOutputStream();

        // Read in the first line of the client's request
        String requestLine = in.readLine();
        if (requestLine == null) return; // if request is empty, return

        // Request line is split by spaces into an array
        String[] requestParts = requestLine.split(" ");
        String requestType = requestParts[0];
        String requestPath = requestParts[1];

        if (requestType.equals("GET")) {
            System.out.println("line 84");
            handleGetRequest(out, requestPath);
        } else if (requestType.equals("PUT")) {
            System.out.println("line 87");
            handlePutRequest(in, out, requestPath);
        } else {
            System.out.println("line 90");
            sendResponse(out, "400 Bad Request", "Invalid requestType");
        }
    }


    private static void handleGetRequest(OutputStream out, String requestPath) throws IOException {
        System.out.println("line 97");
        clock.tick(); // Lamport Clock tick for GET

        // check if the server has any data associated with the requested path.
        if (dataStore.containsKey(requestPath)) {
            DataEntry entry = dataStore.get(requestPath); // retrieve the data associated with the requested path
            String jsonResponse = gson.toJson(entry); // converts object into a JSON string
            sendResponse(out, "200 OK", jsonResponse);
        } else {
            sendResponse(out, "404 Not Found", "Data not found");
        }
    }

    private static void handlePutRequest(BufferedReader in, OutputStream out, String path) throws IOException {
        System.out.println("line 111");
        clock.tick(); // Lamport Clock tick for PUT

        StringBuilder body = new StringBuilder(); // store the content of the request body in JSON format
        String line;
        // Reads the incoming data from the client line-by-line
        while ((line = in.readLine()) != null && !line.isEmpty()) { // end loop when no more data or line is empty
            body.append(line);
        }

        // If the server receives an empty body (no data was sent by the client) respond with a 204
        if (body.isEmpty()) {
            sendResponse(out, "204 No Content", "No data received");
            return;
        }

        // Convert JSON data in the request body to a DataEntry object
        try {
            DataEntry dataEntry = gson.fromJson(body.toString(), DataEntry.class);
            dataStore.put(path, dataEntry); // Persist data in memory by associating the path with the DataEntry object
            sendResponse(out, "201 Created", "Data stored successfully");
        } catch (Exception e) {
            System.err.println("Error processing PUT request: " + e.getMessage());
            sendResponse(out, "500 Internal Server Error", "Invalid JSON");
        }
    }

    private static void sendResponse(OutputStream out, String status, String message) throws IOException {
        System.out.println("line 138");

        // Construct the HTTP response string that the client can interpret
        String response = "HTTP/1.1 " + status + "\r\n" +               // HTTP status code and associated message indicating success or failure
                "Content-Length: " + message.length() + "\r\n" +        // specifying the length (in bytes) of the message body
                "\r\n" +                                                // separates the headers from the actual response body
                message;                                                // send back response body
        out.write(response.getBytes()); // sends the constructed HTTP response string as bytes through the output stream to the client
    }

    // Data structure to hold entries
    static class DataEntry {
        String source;     // source of data (Content Server)
        String content;
        long timestamp;

        public DataEntry(String source, String content, long timestamp) {
            this.source = source;
            this.content = content;
            this.timestamp = timestamp;
        }
    }
}
