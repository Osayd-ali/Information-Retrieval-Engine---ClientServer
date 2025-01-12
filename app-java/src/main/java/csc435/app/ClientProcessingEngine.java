package csc435.app;

import java.io.*;
import java.net.Socket;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class IndexResult {
    public double executionTime;
    public long totalBytesRead;

    public IndexResult(double executionTime, long totalBytesRead) {
        this.executionTime = executionTime;
        this.totalBytesRead = totalBytesRead;
    }
}

class DocPathFreqPair {
    public String documentPath;
    public long wordFrequency;

    public DocPathFreqPair(String documentPath, long wordFrequency) {
        this.documentPath = documentPath;
        this.wordFrequency = wordFrequency;
    }
}

class SearchResult {
    public double excutionTime;
    public ArrayList<DocPathFreqPair> documentFrequencies;

    public SearchResult(double executionTime, ArrayList<DocPathFreqPair> documentFrequencies) {
        this.excutionTime = executionTime;
        this.documentFrequencies = documentFrequencies;
    }
}

public class ClientProcessingEngine {
    // TO-DO keep track of the connection (socket)
    private String clientID;
    private Socket socket;
    private BufferedReader in; //For receiving server messages
    private PrintWriter out; //For sending messages to the server
    public ClientProcessingEngine(String clientID) {
        this.clientID = clientID;
    }

    public IndexResult indexFiles(String folderPath) throws FileNotFoundException, IOException {
        IndexResult result = new IndexResult(0.0, 0);
        // TO-DO get the start time
        long startTime = System.currentTimeMillis();
        long bytesIndexed = 0;
        LinkedList<File> dirsToProcess = new LinkedList<>(); //Creating a queue for directories to process
        // TO-DO crawl the folder path and extract all file paths
        File rootDir = new File(folderPath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            System.out.println("Invalid directory: " + folderPath);
            return new IndexResult(0, 0);
        }
        dirsToProcess.add(new File(folderPath));

        while (!dirsToProcess.isEmpty()) {
            File currentDir = dirsToProcess.poll();
            File[] filesInlist = currentDir.listFiles(); //Storing all the files we get through the folder path in a file array.
            if (filesInlist == null) {//When no files are found in list
                System.out.println("No files found or unable to access directory: " + folderPath);
                return new IndexResult(0, 0);
            }
            System.out.println("Starting indexing of " + filesInlist.length + " files in directory: " + folderPath);
            for (File file : filesInlist) { //Traversing all the files in list
                if (file.isFile()) { //Making sure the path being given is a file
                    if (file.getName().equals(".DS_Store")){
                        System.out.println("Skipping hidden file: "+file.getPath());
                        continue;
                    }
                    System.out.println("file being processed: " + file.getPath());
                    long fileSize = file.length();
                    bytesIndexed += fileSize; //Updating the total bytes indexed for all the files by adding up each file's size.
                    // Extract word frequencies from the file (client responsibility)
                    HashMap<String, Long> wordFrequencies = getWordFrequencies(file.getPath());
                    // Preparing and sending INDEX REQUEST to the server
                    sendIndexRequest(clientID, file.getPath(), wordFrequencies);
                    //Wait for Index Reply from server
                    String reply = receiveMessage();
		    if (reply == null) {
 		        System.out.println("No reply received from the server. It might be disconnected.");
		    } 
		    else if (!reply.startsWith("INDEX REPLY")) {
                        System.out.println("Failed to receive INDEX REPLY from server for file: " + file.getPath());
                    } else {
                        System.out.println("File indexed successfully: " + file.getPath());
                    }
                } else if (file.isDirectory()) {
                    //System.out.println("Entering directory: " + file.getPath());
                    dirsToProcess.add(file);
                }
            }
        }

        // TO-DO for each file extract all alphanumeric terms that are larger than 2 characters
        //       and count their frequencies
        // TO-DO increment the total number of bytes read
        // TO-DO for each file prepare an INDEX REQUEST message and send to the server
        //       the document path, the client ID and the word frequencies
        // TO-DO receive for each INDEX REQUEST message an INDEX REPLY message
        // TO-DO get the stop time and calculate the execution time
        // TO-DO return the execution time and the total number of bytes read
        long stopTime = System.currentTimeMillis();
        result = new IndexResult((stopTime-startTime)/1000.0, bytesIndexed);
        return result;
    }
    private HashMap<String, Long> getWordFrequencies(String filePath) {
        HashMap<String, Long> wordCounts = new HashMap<>();
        List<String> stopwords = Arrays.asList("and", "the", "is", "in", "of", "a", "to", "it");
        try {
            // Check if the file is a .DS_Store or other system files and skip it
            if (filePath.endsWith(".DS_Store")) {
                System.out.println("Skipping system file: " + filePath);
                return wordCounts;
            } // For below line, .lines method takes in the file path and returns the lines from the file as a stream.
            try (Stream<String> lines = Files.lines(Paths.get(filePath))) { // This is the main logic of getting word frequency
                lines.forEach(line -> //Performing an operation for each element of this stream of lines.
                        Arrays.stream(line.split("[^a-zA-Z0-9]+"))
                                .map(String::toLowerCase)
                                .filter(word -> word.length() > 2) //filtering only those words that have a length greater than 2.
                                .filter(word -> !stopwords.contains(word)) //filtering those word which are not stop words.
                                .forEach(word -> wordCounts.merge(word, 1L, Long::sum))
                );
            }
        } catch (MalformedInputException e) {
            System.err.println("Skipping file due to encoding issues: " + filePath);
        }

        catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
        }
        return wordCounts;
    }
    private void sendIndexRequest(String clientID, String filePath, HashMap<String, Long> wordFrequencies) {
        StringBuilder request = new StringBuilder("INDEX REQUEST");
        // Append the Client ID
        request.append(clientID).append(" ");
        // Append the file path
        request.append(filePath).append(" ");
        // Append the word frequencies in a key-value format
        wordFrequencies.forEach((word, freq) -> request.append(word).append(":").append(freq).append(" "));
        // Send the request to the server
        sendMessage(request.toString());
    }
    // Helper method to send messages to the server
    private void sendMessage(String message) {
        out.println(message);  // Sends the message to the server via socket output stream
    }
    private String receiveMessage() throws IOException {
        return in.readLine();  // Assume this is using the BufferedReader from the socket
    }

    public SearchResult searchFiles(ArrayList<String> terms) {
        SearchResult result = new SearchResult(0.0, new ArrayList<DocPathFreqPair>());
        // TO-DO get the start time
        // TO-DO prepare a SEARCH REQUEST message that includes the search terms and send it to the server
        // TO-DO receive one or more SEARCH REPLY messages with the results of the search query
        // TO-DO get the stop time and calculate the execution time
        // TO-DO return the execution time and the top 10 documents and frequencies
        long startTime = System.currentTimeMillis();
        // Validate the search terms
        if (terms == null || terms.size() < 1) {
            throw new IllegalArgumentException("The search query must contain at least 1 term");
        }
        // Prepare and send the SEARCH REQUEST to the server
        sendSearchRequest(terms);
        // Receive the SEARCH REPLY from the server
        ArrayList<DocPathFreqPair> results = new ArrayList<>();
        try {
            results = receiveSearchReply(terms);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to receive search results from server.");
        }
        // Stop measuring the execution time
        long stopTime = System.currentTimeMillis();
        double executionTime2 = (stopTime - startTime) / 1000.0;
        result.excutionTime = executionTime2;
        result.documentFrequencies = new ArrayList<>(results);
        return new SearchResult(executionTime2, new ArrayList<>(results));
    }
    private void sendSearchRequest(ArrayList<String> terms) {
        StringBuilder request = new StringBuilder("SEARCH REQUEST ");
        // Append each term to the search request
        for (String term : terms) {
            request.append(term.toLowerCase()).append(" ");  // Ensure case-insensitivity
        }
        // Send the request to the server (reusing sendMessage method)
        sendMessage(request.toString().trim());  // Send the trimmed request to avoid extra spaces
    }
    private ArrayList<DocPathFreqPair> receiveSearchReply(ArrayList<String> terms) throws IOException {
        // Declare the results list to store the top 10 results
        ArrayList<DocPathFreqPair> results = new ArrayList<>();
        String reply = receiveMessage();  // Receives the entire SEARCH REPLY from the server
        if (reply != null && !reply.isEmpty()) {
            // Example reply format: "SEARCH REPLY <docPath1>:<frequency1> <docPath2>:<frequency2> ..."
            String[] docEntries = reply.split(" ");
            for (String entry : docEntries) {
                if (entry.equals("SEARCH") || entry.equals("REPLY")) {
                    continue;  // Skip the "SEARCH REPLY" keywords
                }
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    String docPath = parts[0];
                    long wordFrequency = Long.parseLong(parts[1]);
                    results.add(new DocPathFreqPair(docPath, wordFrequency));
                }
            }
        }
        // Return the results (already sorted by the server)
        return results;
    }
    public void connect(String serverIP, String serverPort) {
        // TO-DO implement connect to server
        // create a new TCP/IP socket and connect to the server
        try {
            // Create a new TCP/IP socket to connect to the server
            socket = new Socket(serverIP, Integer.parseInt(serverPort));
            // Initialize the input and output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);  // Auto-flush after each write
            // Connection successful message
            System.out.println("Connected to server " + serverIP + ":" + serverPort);
            // Send the client ID to the server
            out.println("CLIENT_ID " + clientID);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to the server.");
        }
    }

    public void disconnect() {
        // TO-DO implement disconnect from server
        // TO-DO send a QUIT message to the server
        // close the TCP/IP socket
        try {
            // Send a QUIT message to the server before disconnecting
            sendMessage("QUIT");
            // Close the input and output streams
            in.close();
            out.close();
            // Close the socket
            socket.close();
            System.out.println("Disconnected from the server.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to disconnect from the server.");
        }
    }
}
