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
    private static final Map<String, DataEntry> dataStore = Collections.synchronizedMap(new HashMap<>()); // Thread-safe map
    private static final LamportClock clock = new LamportClock();

    public static void main(String[] args) {
        // Set the port to a user-specified value if provided, or a default value if not
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Aggregation Server is running on port " + port);

            // Enters an infinite loop, continuously waiting for and accepting new client connections
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleClient(clientSocket);
                } catch (IOException e) {
                    System.err.println("Error handling client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = clientSocket.getOutputStream();

        String requestLine = in.readLine();
        if (requestLine == null) return; // if request is empty, return

        String[] requestParts = requestLine.split(" ");
        String requestType = requestParts[0];
        String requestPath = requestParts[1];

        if (requestType.equals("GET")) {
            handleGetRequest(out, requestPath);
        } else if (requestType.equals("PUT")) {
            handlePutRequest(in, out, requestPath);
        } else {
            sendResponse(out, "400 Bad Request", "Invalid request type");
        }
    }

    private static void handleGetRequest(OutputStream out, String requestPath) throws IOException {
        clock.tick(); // Lamport Clock tick for GET

        if (dataStore.containsKey(requestPath)) {
            DataEntry entry = dataStore.get(requestPath);
            String jsonResponse = gson.toJson(entry);
            sendResponse(out, "200 OK", jsonResponse);
        } else {
            sendResponse(out, "404 Not Found", "Data not found");
        }
    }

    private static void handlePutRequest(BufferedReader in, OutputStream out, String path) throws IOException {
        clock.tick(); // Lamport Clock tick for PUT

        StringBuilder body = new StringBuilder();
        String line;

        // Read incoming data from the client line-by-line
        while ((line = in.readLine()) != null) {
            if (line.isEmpty()) break; // End loop when line is empty
            body.append(line);
        }

        // If the server receives an empty body, respond with 204
        if (body.isEmpty()) {
            sendResponse(out, "204 No Content", "No data received");
            return;
        }

        // Convert JSON data in the request body to a DataEntry object
        try {
            DataEntry dataEntry = gson.fromJson(body.toString(), DataEntry.class);
            dataStore.put(path, dataEntry);
            sendResponse(out, "201 Created", "Data stored successfully");
        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace for debugging
            sendResponse(out, "500 Internal Server Error", "Invalid JSON");
        }
        out.flush(); // Ensure the response is sent immediately
    }

    private static void sendResponse(OutputStream out, String status, String message) throws IOException {
        String response = "HTTP/1.1 " + status + "\r\n" +
                "Content-Length: " + message.length() + "\r\n" +
                "\r\n" +
                message;
        out.write(response.getBytes());
        out.flush(); // Ensure the response is sent immediately
    }

    // Data structure to hold entries
    static class DataEntry {
        String id;
        String name;
        String state;
        String time_zone;
        double lat;
        double lon;
        String local_date_time;
        String local_date_time_full;
        double air_temp;
        double apparent_t;
        String cloud;
        double dewpt;
        double press;
        int rel_hum;
        String wind_dir;
        double wind_spd_kmh;
        double wind_spd_kt;
    }
}
