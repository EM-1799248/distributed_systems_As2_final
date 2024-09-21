import java.io.IOException;
import java.net.ServerSocket;

public class AggregationServer2 {

    // Final instance variable for ServerSocket
    private final ServerSocket serverSocket;

    // Constructor to initialize the ServerSocket with the specified port
    public AggregationServer2(int port) throws IOException {
        // Initialize the final serverSocket variable
        this.serverSocket = new ServerSocket(port);
        System.out.println("Server started on port: " + port);
    }

    public static void main() {
        int port = 4567; // Default port

        try {
            // Create an instance of AggregationServer with the specified port
            AggregationServer2 server = new AggregationServer2(port);
            // Further logic to handle client connections goes here
        } catch (IOException e) {
            System.err.println("Failed to start the server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Additional methods for handling client connections, etc.
}
