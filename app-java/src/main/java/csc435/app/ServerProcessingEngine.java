package csc435.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerProcessingEngine {
    private IndexStore store;
    // TO-DO keep track of the Dispatcher thread
    private Thread dispatcherThread;
    // TO-DO keep track of the Index Worker threads
    private ServerSocket serverSocket;          // ServerSocket to accept client connections
    private ArrayList<String> connectedClients;
    // TO-DO keep track of the clients information
    private ExecutorService workerPool;         // Thread pool for managing worker threads
    private volatile boolean isRunning;         // To handle graceful shutdown

    public ServerProcessingEngine(IndexStore store) {
        this.store = store;
        this.connectedClients = new ArrayList<>();
        this.workerPool = Executors.newCachedThreadPool();  // A pool for worker threads
        this.isRunning = true;
    }

    public void initialize(int serverPort) {
        // TO-DO create and start the Dispatcher thread
        dispatcherThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(serverPort);
                System.out.println("Server listening on port " + serverPort);

                while (isRunning) {
                    try {
                        // Accept new client connections
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Accepted connection from clientIP: " + clientSocket.getInetAddress() + " on port: " + clientSocket.getPort());
                        // Spawn a new worker for each connected client
                        spawnWorker(clientSocket);

                        // Keep track of connected clients
                        connectedClients.add(clientSocket.getInetAddress().toString());
                    } catch (IOException e){
                        if (isRunning) {
                            System.err.println("Error accepting connection: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Ensure the server socket is closed when the server shuts down
                if (serverSocket != null && !serverSocket.isClosed()) {
                    try {
                        System.out.println("Closing ServerSocket...");
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Dispatcher has stopped.");
            }
        });
        dispatcherThread.start();  // Start the Dispatcher thread
    }

    public void spawnWorker(Socket clientSocket) {
        // TO-DO create and start a new Index Worker thread
        Runnable worker = new IndexWorker(store, this,  clientSocket);
        workerPool.execute(worker);  // Use the thread pool to execute the worker
    }

    public void shutdown() {
        System.out.println("Shutting down the server...");
        // TO-DO signal the Dispatcher thread to shutdown
        // TO-DO join the Dispatcher and Index Worker threads
        isRunning = false;
        // Close the ServerSocket to stop accepting new connections
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                System.out.println("Closing server socket...");
                serverSocket.close();  // Close the server socket to unblock the accept() call
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Wait for the dispatcher thread to finish
        if (dispatcherThread != null) {
            try {
                System.out.println("Waiting for the dispatcher thread to finish...");
                dispatcherThread.join();  // Wait for the Dispatcher thread to finish
                System.out.println("Dispatcher thread has finished.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Shutdown the worker pool and wait for workers to finish
        workerPool.shutdownNow();  // Force shutdown of all workers
        System.out.println("Worker pool has been shut down.");
        System.out.println("Server has shut down.");
    }
    public void addConnectedClient(String clientID) {
        connectedClients.add(clientID);
    }

    public ArrayList<String> getConnectedClients() {
        // TO-DO return the connected clients information
        return new ArrayList<>(connectedClients);
    }
}

