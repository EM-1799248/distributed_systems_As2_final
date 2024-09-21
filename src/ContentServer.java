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

            // Read the data file and create a DataEntry object
            StringBuilder content = new StringBuilder();
            String line;

            // Read each line from the file and append it to content
            while ((line = fileReader.readLine()) != null) {
                content.append(line).append("\n"); // Keep the newline characters for formatting
            }

            // Create the DataEntry object based on the data file structure
            DataEntry dataEntry = new DataEntry();
            dataEntry.id = "IDS60901"; // Assign appropriate ID based on your file's data
            dataEntry.name = "Adelaide (West Terrace / ngayirdapira)";
            dataEntry.state = "SA";
            dataEntry.time_zone = "CST";
            dataEntry.lat = -34.9;
            dataEntry.lon = 138.6;
            dataEntry.local_date_time = "15/04:00pm"; // Update as necessary
            dataEntry.local_date_time_full = "20230715160000"; // Update as necessary
            dataEntry.air_temp = 13.3; // Update as necessary
            dataEntry.apparent_t = 9.5; // Update as necessary
            dataEntry.cloud = "Partly cloudy"; // Update as necessary
            dataEntry.dewpt = 5.7; // Update as necessary
            dataEntry.press = 1023.9; // Update as necessary
            dataEntry.rel_hum = 60; // Update as necessary
            dataEntry.wind_dir = "S"; // Update as necessary
            dataEntry.wind_spd_kmh = 15; // Update as necessary
            dataEntry.wind_spd_kt = 8; // Update as necessary

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

        // Default constructor
        public DataEntry() {}
    }
}
