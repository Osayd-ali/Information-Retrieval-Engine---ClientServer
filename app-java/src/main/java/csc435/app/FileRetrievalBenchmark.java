package csc435.app;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class BenchmarkWorker implements Runnable {
    private ClientProcessingEngine engine;
    private String datasetPath;
    private String serverIP;
    private String serverPort;


    public BenchmarkWorker(String serverIP, String serverPort, String datasetPath) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.datasetPath = datasetPath;
    }
    // TO-DO declare a ClientProcessingEngine

    @Override
    public void run() {
        // TO-DO create a ClientProcessinEngine
        String clientID = java.util.UUID.randomUUID().toString();
        engine = new ClientProcessingEngine(clientID);
        // TO-DO connect the ClientProcessingEngine to the server
        System.out.println("Connecting to server: " + serverIP + ":" + serverPort);
        engine.connect(serverIP, serverPort);
        // TO-DO index the dataset
        System.out.println("Indexing dataset: " + datasetPath);
        try {
            IndexResult result = engine.indexFiles(datasetPath);
            System.out.println("Completed indexing " + result.totalBytesRead + " bytes of data");
            System.out.println("Completed indexing in " + result.executionTime + " seconds");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void search(String query) {
        // TO-DO perform search operations on the ClientProcessingEngine
        System.out.println("Performing search query: " + query);
        // TO-DO print the results and performance
        SearchResult searchResult = engine.searchFiles(new ArrayList<>(java.util.Arrays.asList(query.split(" "))));
        System.out.println("Search completed in " + searchResult.excutionTime + " seconds");
        System.out.println("Search results (top 10): ");
        for (DocPathFreqPair pair : searchResult.documentFrequencies) {
            System.out.println(pair.documentPath + ": " + pair.wordFrequency);
        }
    }

    public void disconnect() {
        // TO-DO disconnect the ClientProcessingEngine from the server
        engine.disconnect();
        System.out.println("Client disconnected.");
    }
}

public class FileRetrievalBenchmark {
    public static void main(String[] args)
    {
        if (args.length < 4) {
            System.out.println("Usage: java FileRetrievalBenchmark <num_clients> <server_ip> <server_port> <dataset_paths...>");
            return;
        }
	try{ 
        String serverIP = args[0];
        String serverPort = args[1];
	int numberOfClients = Integer.parseInt(args[2]);
        ArrayList<String> clientsDatasetPath = new ArrayList<>();
        for (int i = 3; i < args.length; i++) {
            clientsDatasetPath.add(args[i]);
        }
        long startTime = System.currentTimeMillis();

        // TO-DO extract the arguments from args
        // TO-DO measure the execution start time
        // TO-DO create and start benchmark worker threads equal to the number of clients
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfClients);
        ArrayList<BenchmarkWorker> workers = new ArrayList<>();
        for (int i = 0; i < numberOfClients; i++) {
            BenchmarkWorker worker = new BenchmarkWorker(serverIP, serverPort, clientsDatasetPath.get(i));
            workers.add(worker);
            executorService.execute(worker);
        }
        // TO-DO join the benchmark worker threads
        // Step 8: Wait for all threads to finish indexing
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // TO-DO measure the execution stop time and print the performance
        long stopTime = System.currentTimeMillis();
        double totalTime = (stopTime - startTime) / 1000.0;
        System.out.println("Total indexing time for all clients: " + totalTime + " seconds");
        // TO-DO run search queries on the first client (benchmark worker thread number 1)
        if (!workers.isEmpty()) {
            System.out.println("\nRunning search queries on the first client:");
            workers.get(0).search("Worms");
            workers.get(0).search("distortion AND adaptation");
        }
        // TO-DO disconnect all clients (all benchmakr worker threads)
        for (BenchmarkWorker worker : workers) {
            worker.disconnect();
        }

        System.out.println("Benchmark completed.");
	}catch (NumberFormatException e){
            System.out.println("Error: Invalid number format in arguments.");
            e.printStackTrace();
        }
    }
}
