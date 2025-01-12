package csc435.app;

import java.lang.System;
import java.util.Scanner;
import java.util.ArrayList;

public class ServerAppInterface {
    private ServerProcessingEngine engine;

    public ServerAppInterface(ServerProcessingEngine engine) {
        this.engine = engine;
    }

    public void readCommands() {
        // TO-DO implement the read commands method
        Scanner sc = new Scanner(System.in);
        String command;
        
        while (true) {
            System.out.print("> ");
            
            // read from command line
            command = sc.nextLine().trim();

            // if the command is quit, terminate the program       
            if (command.equalsIgnoreCase("quit")) {
                System.out.println("Shutting down the server...");
                engine.shutdown();
		System.out.println("Server has been shut down.");
                break;
            }

            // if the command begins with list, list all the connected clients
            if (command.equalsIgnoreCase("list")) {
                // TO-DO call the getConnectedClients method from the server to retrieve the clients information
                // TO-DO print the clients information
                ArrayList<String> clientsInformation = engine.getConnectedClients();
                if (clientsInformation.isEmpty()) {
                    System.out.println("No clients are currently connected.");
                } else {
                    System.out.println("Connected Clients:");
                    for (String client : clientsInformation) {
                        System.out.println("- " + client);
                    }
                }
                continue;
            }

            System.out.println("unrecognized command!");
        }

        sc.close();
    }
}
