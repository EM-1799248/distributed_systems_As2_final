/*
Reads data from a local file, converts it into JSON using Gson, and sends it to the aggregation server via a PUT request.

- makes PUT requests to ag server and uploads new data to it (replaces old data)
- can have multiple content servers
- start up, read command line for server name and port number, and also location of a file in system local to server that contains fields to be assembled into JSON and uploaded to Ag server
- maintains a lamport clock
 */

import java.io.*;
import java.net.*;
import com.google.gson.Gson;

public class ContentServer {

    private static final Gson gson = new Gson();
    private static LamportClock clock = new LamportClock();

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: ContentServer <server> <port> <filePath>");
            return;
        }

        String server = args[0];
        int port = Integer.parseInt(args[1]);
        String filePath = args[2];

        try (Socket socket = new Socket(server, port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader fileReader = new BufferedReader(new FileReader(filePath));

            // Create data entry from file
            String source = "ContentServer";
            StringBuilder content = new StringBuilder();
            String line;

            // Read each line from the file and append it to content
            while ((line = fileReader.readLine()) != null) {
                content.append(line).append("\n"); // Keep the newline characters for formatting
            }
            DataEntry dataEntry = new DataEntry(source, content.toString(), System.currentTimeMillis());

            // Send PUT request
            clock.tick(); // Lamport Clock tick for PUT
            out.println("PUT / HTTP/1.1");
            out.println("Content-Type: application/json"); // Specify content type
            out.println("Content-Length: " + gson.toJson(dataEntry).length()); // Specify content length
            out.println(); // End headers
            out.println(gson.toJson(dataEntry)); // Send data as JSON

            System.out.println("Data sent successfully to the aggregation server.");

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    static class DataEntry {
        String source;
        String content;
        long timestamp;

        public DataEntry(String source, String content, long timestamp) {
            this.source = source;
            this.content = content;
            this.timestamp = timestamp;
        }
    }
}
