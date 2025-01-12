package csc435.app;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class IndexWorker implements Runnable {
    private IndexStore store;
    private Socket clientSocket;
    private ServerProcessingEngine engine;  // Reference to the ServerProcessingEngine
    private BufferedReader in;   // To receive messages from the client
    private PrintWriter out;     // To send messages to the client
    private String clientID;

    public IndexWorker(IndexStore store, ServerProcessingEngine engine, Socket clientSocket) {
        this.store = store;
        this.engine = engine;
        this.clientSocket = clientSocket;
        try {
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);  // Auto-flush after each write
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // TO-DO receive a message from the client
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("CLIENT_ID")) {
                    // Extract and store the client ID
                    clientID = message.split(" ")[1];
                    System.out.println("Client ID received: " + clientID);

                    // Store the client ID in the ServerProcessingEngine
                    synchronized (engine) {
                        engine.addConnectedClient(clientID);
                    }
                } else if (message.startsWith("INDEX REQUEST")) {
                    handleIndexRequest(message);
                } else if (message.startsWith("SEARCH REQUEST")) {
                    handleSearchRequest(message);
                } else if (message.equals("QUIT")) {
                    handleQuitRequest();
                    break;  // Exit the loop to stop the worker
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Clean up and close resources when done
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
        // TO-DO if the message is an INDEX REQUEST, then
        //       extract the document path, client ID and word frequencies from the message(s)
        //       get the document number associated with the document path (call putDocument)
        //       update the index store with the word frequencies and the document number
        //       return an acknowledgement INDEX REPLY message
        // TO-DO if the message is a SEARCH REQUEST, then
        //       extract the terms from the message
        //       for each term get the pairs of documents and frequencies from the index store
        //       combine the returned documents and frequencies from all of the specified terms
        //       sort the document and frequency pairs and keep only the top 10
        //       for each document number get from the index store the document path
        //       return a SEARCH REPLY message containing the top 10 results
        // TO-DO if the message is a QUIT message, then finish running
        // Handle INDEX REQUEST from the client
        private void handleIndexRequest(String message) {
            try {
                // Example format: "INDEX REQUEST clientID filePath word1:freq1 word2:freq2 ..."
                String[] parts = message.split(" ");
                String clientID = parts[1];
                String documentPath = parts[2];

                // Extract word frequencies
                HashMap<String, Long> wordFrequencies = new HashMap<>();
                for (int i = 3; i < parts.length; i++) {
                    String[] wordFreq = parts[i].split(":");
                    if (wordFreq.length == 2) {
                        wordFrequencies.put(wordFreq[0], Long.parseLong(wordFreq[1]));
                    }
                }

                // Get the document number associated with the document path
                long documentNumber = store.putDocument(documentPath);

                // Update the index store with the word frequencies for the document
                store.updateIndex(documentNumber, wordFrequencies);

                // Send an INDEX REPLY back to the client
                out.println("INDEX REPLY " + documentPath + " successfully indexed.");
            } catch (Exception e) {
                e.printStackTrace();
                out.println("INDEX REPLY FAILED");
            }
        }

    // Handle SEARCH REQUEST from the client
    private void handleSearchRequest(String message) {
        try {
            // Example format: "SEARCH REQUEST term1 term2 term3 ..."
            String[] parts = message.split(" ");
            ArrayList<String> terms = new ArrayList<>();
            for (int i = 1; i < parts.length; i++) {
                terms.add(parts[i].toLowerCase());  // Ensure case-insensitive search
            }
            // This map will store document numbers and their combined frequency for all terms
            HashMap<Long, Long> documentFrequencyMap = new HashMap<>();
            // For each term, get the pairs of documents and frequencies from the index store
            for (String term : terms) {
                ArrayList<DocFreqPair> termResults = store.lookupIndex(term);
                for (DocFreqPair pair : termResults) {
                    documentFrequencyMap.merge(pair.documentNumber, pair.wordFrequency, Long::sum);
                }
            }
            // Convert the documentFrequencyMap to the final results list
            ArrayList<DocPathFreqPair> results = new ArrayList<>();
            for (Long docNum : documentFrequencyMap.keySet()) {
                String docPath = store.getDocument(docNum);
                if (docPath != null) {
                    results.add(new DocPathFreqPair(docPath, documentFrequencyMap.get(docNum)));
                }
            }
            // Sort results by frequency in descending order and limit to top 10
            results.sort((a, b) -> Long.compare(b.wordFrequency, a.wordFrequency));
            results = new ArrayList<>(results.subList(0, Math.min(10, results.size())));
            // Send SEARCH REPLY back to the client with the top 10 results
            StringBuilder reply = new StringBuilder("SEARCH REPLY ");
            for (DocPathFreqPair result : results) {
                reply.append(result.documentPath).append(":").append(result.wordFrequency).append(" ");
            }
            out.println(reply.toString().trim());  // Send the search reply to the client

        } catch (Exception e) {
            e.printStackTrace();
            out.println("SEARCH REPLY FAILED");
        }
    }
    // Handle QUIT request from the client
    private void handleQuitRequest() {
        System.out.println("Client disconnected: " + clientSocket.getInetAddress());
    }
}
