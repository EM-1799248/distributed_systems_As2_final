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
            if (!jsonResponse.isEmpty()) {
                DataEntry entry = gson.fromJson(jsonResponse.toString(), DataEntry.class);
                // Print all fields of the DataEntry based on data file structure
                System.out.println("ID: " + entry.id);
                System.out.println("Name: " + entry.name);
                System.out.println("State: " + entry.state);
                System.out.println("Time Zone: " + entry.time_zone);
                System.out.println("Latitude: " + entry.lat);
                System.out.println("Longitude: " + entry.lon);
                System.out.println("Local Date Time: " + entry.local_date_time);
                System.out.println("Local Date Time Full: " + entry.local_date_time_full);
                System.out.println("Air Temperature: " + entry.air_temp);
                System.out.println("Apparent Temperature: " + entry.apparent_t);
                System.out.println("Cloud: " + entry.cloud);
                System.out.println("Dew Point: " + entry.dewpt);
                System.out.println("Pressure: " + entry.press);
                System.out.println("Relative Humidity: " + entry.rel_hum);
                System.out.println("Wind Direction: " + entry.wind_dir);
                System.out.println("Wind Speed (km/h): " + entry.wind_spd_kmh);
                System.out.println("Wind Speed (knots): " + entry.wind_spd_kt);
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    // Data structure to hold entries
    static class DataEntry {
        String id; // Added fields to match your data structure
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
        double rel_hum;
        String wind_dir;
        double wind_spd_kmh;
        double wind_spd_kt;

        // Default constructor for Gson
        public DataEntry() {}
    }
}
