/*
Sends GET requests to the aggregation server, receives the response, and prints it.

- client contacts aggregation server through RESTful API
- can have multiple clients
- displays the received data
- starts up, reads command line for server name, port number (url format) and optional station ID
- send GET req to Ag server for weather data
- data stripped of JSON format, and displayed one at a time with attribute and value
- main method
- Possible formats for the server name and port number include "http://servername.domain.domain:portnumber", "http://servername:portnumber" (with implicit domain information) and "servername:portnumber" (with implicit domain and protocol information).
- output does not need hyperlinks
- maintains a lamport clock
 */

import java.io.*;
import java.net.*;
import com.google.gson.Gson;

public class GETClient {

    private static final Gson gson = new Gson();
    private static LamportClock clock = new LamportClock();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: GETClient <server:port> [<stationID>]");
            return;
        }

        String[] serverParts = args[0].split(":");
        String server = serverParts[0];
        int port = serverParts.length > 1 ? Integer.parseInt(serverParts[1]) : 4567; // Default port
        String stationID = args.length == 2 ? args[1] : "/";

        try (Socket socket = new Socket(server, port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send GET request
            clock.tick(); // Lamport Clock tick for GET
            out.println("GET " + stationID + " HTTP/1.1");
            out.println(); // Empty line to end request

            // Read response
            String responseLine;
            boolean isHeader = true;
            StringBuilder jsonResponse = new StringBuilder();
            while ((responseLine = in.readLine()) != null) {
                if (isHeader) {
                    if (responseLine.isEmpty()) {
                        isHeader = false; // End of headers
                    }
                } else {
                    jsonResponse.append(responseLine);
                }
            }

            // Process and display JSON response
            if (jsonResponse.length() > 0) {
                DataEntry entry = gson.fromJson(jsonResponse.toString(), DataEntry.class);
                System.out.println("Source: " + entry.source);
                System.out.println("Content: " + entry.content);
                System.out.println("Timestamp: " + entry.timestamp);
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
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
