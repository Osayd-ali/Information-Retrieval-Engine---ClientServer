package csc435.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Dispatcher implements Runnable {
    private ServerProcessingEngine engine;
    private int serverPort;  // The port to listen on
    public Dispatcher(ServerProcessingEngine engine) {
        this.engine = engine;
    }
    
    @Override
    public void run() {
        // TO-DO create a TCP/IP socket and listen for new connections
        // TO-DO When new connection comes through create a new Index Worker thread for the new connection
        // TO-DO Use the engine spawnWorker method to create a new Index Worker thread
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Server is listening on port " + serverPort);
            // Continuously listen for new client connections
            while (true) {
                // Accept new client connection
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                // Use the engine's spawnWorker method to create a new IndexWorker thread
                engine.spawnWorker(clientSocket);  // Create a new worker thread for this connection
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error in Dispatcher: Unable to listen on port " + serverPort);
        }
    }
}
