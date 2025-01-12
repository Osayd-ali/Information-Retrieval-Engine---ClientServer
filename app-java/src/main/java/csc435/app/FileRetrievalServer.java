package csc435.app;

public class FileRetrievalServer
{
    public static void main( String[] args )
    {
        // TO-DO change server port to a non-privileged port from args[0]
        if (args.length < 1) {
            System.out.println("Error: Please provide a port number as the first argument.");
            return;
        }
        int serverPort;
        try {
            // Parse the server port from args[0]
            serverPort = Integer.parseInt(args[0]);

            // Ensure that the port is a non-privileged port (above 1024)
            if (serverPort <= 1024 || serverPort > 65535) {
                System.out.println("Error: Please provide a valid non-privileged port number (above 1024).");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid port number. Please provide a valid integer.");
            return;
        }

        IndexStore store = new IndexStore();
        ServerProcessingEngine engine = new ServerProcessingEngine(store);
        ServerAppInterface appInterface = new ServerAppInterface(engine);
        
        // create a thread that creates and server TCP/IP socket and listens to connections
        engine.initialize(serverPort);

        // read commands from the user
        appInterface.readCommands();
    }
}
