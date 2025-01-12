package csc435.app;

import java.io.IOException;
import java.util.UUID;

public class FileRetrievalClient
{
    public static void main(String[] args)
    {
        String clientID = UUID.randomUUID().toString();  // Generate a unique Client ID
        ClientProcessingEngine engine = new ClientProcessingEngine(clientID);
        ClientAppInterface appInterface = new ClientAppInterface(engine);
        
        // read commands from the user
        try {
            appInterface.readCommands();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
