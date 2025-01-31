## Information Retrieval Engine - Client Server

### Intro 

I have built an InformationRetrievalEngine system which demonstrates the implementation of three important concepts of distributed systems which are application layering, mulithreading and client-server architecture. This Information Retrieval Engine supports indexing or traversing files from a specified input folder and also supports searching of indexed or traversed documents when a user gives a multiple term query. Upon providing search words, this application will search those words across all the traversed and read files and finally returns a list of top documents contaning those search words in the order of their frequency. Some of the sourcecode was carried on from my previous multithreaded version of InformationRetrievalEngine, but in this project I have added the support for client-server architecture. The communication medium used between client and server were POSIX sockets. Implementing this project using client and server components gives us huge advantage in modularity and maintainability of our application. The client will have access to the datasets and is responsible for partial indexing of datasets where datasets were divided based on number of clients, the number of clients is implemented in a way where each client is treated as a thread and maintanance of these clients that is creation of threads will be done by dispatcher. Server side is responsible for maintaining our hashtables DocumentMap and termInvertedIndex, server receives partial index from client and replies the client with its requested information, the requested information is that information which relies on our hashtables documentMap and termInvertedIndex. This is a brief description of my project.Below is few instructions on how to run my application and also my directory structure.

### System Design

This class diagram illustrates the core architecture of the search engine system.

```mermaid
classDiagram
    %% Benchmark classes
    class FileRetrievalBenchmark {
        +main(String[] args) void$
    }

    class BenchmarkWorker {
        <<implements Runnable>>
        -ClientProcessingEngine engine
        -String datasetPath
        -String serverIP
        -String serverPort
        +BenchmarkWorker(String serverIP, String serverPort, String datasetPath)
        +run() void
        +search(String query) void
        +disconnect() void
    }

    %% Entry point classes
    class FileRetrievalServer {
        +main(String[] args) void$
    }

    class FileRetrievalClient {
        +main(String[] args) void$
    }

    %% Interface classes
    class ServerAppInterface {
        -ServerProcessingEngine engine
        +ServerAppInterface(ServerProcessingEngine engine)
        +readCommands() void
    }

    class ClientAppInterface {
        -ClientProcessingEngine engine
        +ClientAppInterface(ClientProcessingEngine engine)
        +readCommands() void
    }

    %% Worker classes
    class Dispatcher {
        <<implements Runnable>>
        -ServerProcessingEngine engine
        -int serverPort
        +Dispatcher(ServerProcessingEngine engine)
        +run() void
    }

    class IndexWorker {
        <<implements Runnable>>
        -IndexStore store
        -Socket clientSocket
        -ServerProcessingEngine engine
        -BufferedReader in
        -PrintWriter out
        -String clientID
        +IndexWorker(IndexStore store, ServerProcessingEngine engine, Socket clientSocket)
        +run() void
        -handleIndexRequest(String message) void
        -handleSearchRequest(String message) void
        -handleQuitRequest() void
    }

    %% Core Engine classes
    class ServerProcessingEngine {
        -IndexStore store
        -Thread dispatcherThread
        -ServerSocket serverSocket
        -ArrayList~String~ connectedClients
        -ExecutorService workerPool
        -volatile boolean isRunning
        +ServerProcessingEngine(IndexStore store)
        +initialize(int serverPort) void
        +spawnWorker(Socket clientSocket) void
        +shutdown() void
        +addConnectedClient(String clientID) void
        +getConnectedClients() ArrayList~String~
    }
    
    class ClientProcessingEngine {
        -String clientID
        -Socket socket
        -BufferedReader in
        -PrintWriter out
        +ClientProcessingEngine(String clientID)
        +IndexResult indexFiles(String folderPath)
        +SearchResult searchFiles(ArrayList~String~ terms)
        +connect(String serverIP, String serverPort) void
        +disconnect() void
    }

    %% Storage class
    class IndexStore {
        -HashMap~String,Long~ documentMap
        -HashMap~String,ArrayList~DocFreqPair~~ termInvertedIndex
        -ReentrantLock documentMapLock
        -ReentrantLock termInvertedIndexLock
        -long documentNumber
        +putDocument(String documentPath) long
        +getDocument(long documentNumber) String
        +updateIndex(long documentNumber, HashMap~String,Long~ wordFrequencies) void
        +lookupIndex(String term) ArrayList~DocFreqPair~
    }

    %% Data classes
    class DocFreqPair {
        +long documentNumber
        +long wordFrequency
        +DocFreqPair(long documentNumber, long wordFrequency)
    }

    class IndexResult {
        +double executionTime
        +long totalBytesRead
        +IndexResult(double executionTime, long totalBytesRead)
    }
    
    class SearchResult {
        +double excutionTime
        +ArrayList~DocPathFreqPair~ documentFrequencies
        +SearchResult(double executionTime, ArrayList~DocPathFreqPair~ documentFrequencies)
    }
    
    class DocPathFreqPair {
        +String documentPath
        +long wordFrequency
        +DocPathFreqPair(String documentPath, long wordFrequency)
    }

    %% External classes
    class Socket {
        <<external>>
    }
    
    class ServerSocket {
        <<external>>
    }
    
    class BufferedReader {
        <<external>>
    }
    
    class PrintWriter {
        <<external>>
    }

    class ReentrantLock {
        <<external>>
    }

    class ExecutorService {
        <<external>>
    }

    %% Benchmark relationships
    FileRetrievalBenchmark ..> BenchmarkWorker : creates
    FileRetrievalBenchmark --> ExecutorService : uses
    BenchmarkWorker --> ClientProcessingEngine : uses
    
    %% Server-side relationships
    FileRetrievalServer ..> IndexStore : creates
    FileRetrievalServer ..> ServerProcessingEngine : creates
    FileRetrievalServer ..> ServerAppInterface : creates
    ServerProcessingEngine --> IndexStore : uses
    ServerProcessingEngine --> ServerSocket : uses
    ServerProcessingEngine --> ExecutorService : uses
    ServerProcessingEngine ..> Dispatcher : creates
    ServerProcessingEngine ..> IndexWorker : creates
    Dispatcher --> ServerSocket : uses
    IndexWorker --> IndexStore : uses
    IndexWorker --> Socket : uses
    IndexWorker --> BufferedReader : uses
    IndexWorker --> PrintWriter : uses
    
    %% Client-side relationships
    FileRetrievalClient ..> ClientAppInterface : creates
    FileRetrievalClient ..> ClientProcessingEngine : creates
    ClientAppInterface --> ClientProcessingEngine : uses
    ClientProcessingEngine --> Socket : uses
    ClientProcessingEngine --> BufferedReader : uses
    ClientProcessingEngine --> PrintWriter : uses
    ClientProcessingEngine ..> IndexResult : creates
    ClientProcessingEngine ..> SearchResult : creates
    SearchResult --> DocPathFreqPair : contains
    
    %% Storage relationships
    IndexStore --> ReentrantLock : uses
    IndexStore --> DocFreqPair : uses
```

### Directory Structure

After cloning this repository you will need to follow a specific directory structure to run the program.

* Enter into app-java directory: "cd app-java"
* Here, when you type "ls" you will be able to see datasets directory, java build file and src directory where my source code exists.
* You can acces my code if you want to by following the specific path: "cd src/main/java/csc435/app"
* After entering into the above path, type "ls" and you will find my source code files. If you wish to enter any source code file type "vi filename.java".

### Requirements

If you are implementing your solution in Java you will need to have Java 21.x and Maven 3.8.x installed on your systems.
On Ubuntu 24.04 LTS you can install Java and Maven using the following commands:

```
sudo apt install maven openjdk-21-jdk
```

### Setup

There are 3 datasets (dataset1_client_server, dataset2_client_server, dataset3_client_server) that you need to use to evaluate the indexing performance of your solution.
Before you can evaluate your solution you need to download the datasets. You can download the datasets from the following link:

https://depauledu-my.sharepoint.com/:f:/g/personal/aorhean_depaul_edu/Ej4obLnAKMdFh1Hidzd1t1oBHY7IvgqXoLdKRg-buoiisw?e=SWLALa

After you finished downloading the datasets copy them to the dataset directory (create the directory if it does not exist).
Here is an example on how you can copy Dataset1 to the remote machine and how to unzip the dataset:

```
remote-computer$ mkdir datasets
local-computer$ scp dataset1_client_server.zip cc@<remote-ip>:<path-to-repo>/datasets/.
remote-computer$ cd <path-to-repo>/datasets
remote-computer$ unzip dataset1_client_server.zip
```

### Java solution
#### How to build/compile


To build the Java solution use the following commands:
```
First clone the remote repository from GitHub
Enter directory: cd csc-435-pa4-Osayd-ali
cd app-java
mvn compile
mvn package
```

#### How to run application

To run the Java server (after you build the project) use the following command:
```
Port No. I used is 12345
java -cp target/app-java-1.0-SNAPSHOT.jar csc435.app.FileRetrievalServer <port>
```

To run the Java client (after you build the project) use the following command:
```
java -cp target/app-java-1.0-SNAPSHOT.jar csc435.app.FileRetrievalClient
```

To run the Java benchmark (after you build the project) use the following command:
```
java -cp target/app-java-1.0-SNAPSHOT.jar csc435.app.FileRetrievalBenchmark <server IP> <server port> <number of clients> [<dataset path>]
```

#### Example (2 clients and 1 server)

**Step 1:** start the server:

Server
```
Note: If while running dataset3, the server happens to crash. Then please do quit the server close the command prompt and restart the server and then repeat the same for client terminal as well. The program will run just fine.

java -cp target/app-java-1.0-SNAPSHOT.jar csc435.app.FileRetrievalServer 12345
>
```

**Step 2:** start the clients and connect them to the server:

Client 1
```
java -cp target/app-java-1.0-SNAPSHOT.jar csc435.app.FileRetrievalClient
> connect 127.0.0.1 12345
Connection successful!
```

Client 2
```
java -cp target/app-java-1.0-SNAPSHOT.jar csc435.app.FileRetrievalClient
> connect 127.0.0.1 12345
Connection successful!
```

**Step 3:** list the connected clients on the server:

Server
```
> list
client1: 127.0.0.1 5746
client2: 127.0.0.1 9677
```

**Step 4:** index files from the clients:

Client 1
```
> index ../datasets/dataset1_client_server/2_clients/client_1
Completed indexing 68383239 bytes of data
Completed indexing in 2.974 seconds
```

Client 2
```
> index ../datasets/dataset1_client_server/2_clients/client_2
Completed indexing 65864138 bytes of data
Completed indexing in 2.386 seconds
```

**Step 5:** search files from the clients:

Client 1
```
> search at
Search completed in 0.4 seconds
Search results (top 10 out of 0):
> search Worms
Search completed in 2.8 seconds
Search results (top 10 out of 12):
* client1:folder4/Document10553.txt:4
* client1:folder3/Document1043.txt:4
* client2:folder7/Document1091.txt:3
* client1:folder3/Document10383.txt:3
* client2:folder7/folderB/Document10991.txt:2
* client2:folder8/Document11116.txt:1
* client2:folder5/folderB/Document10706.txt:1
* client2:folder5/folderB/Document10705.txt:1
* client2:folder5/folderA/Document10689.txt:1
* client1:folder4/Document1051.txt:1
```

Client 2
```
> search distortion AND adaptation
Search completed in 3.27 seconds
Search results (top 10 out of 4):
* client2:folder7/folderC/Document10998.txt:6
* client1:folder4/Document10516.txt:3
* client2:folder8/Document11159.txt:2
* client2:folder8/Document11157.txt:2
```

**Step 6:** close and disconnect the clients:

Client 1
```
> quit
```

Client 2
```
> quit
```

**Step 7:** close the server:

Server
```
> quit
```

#### Example (benchmark with 2 clients and 1 server)

**Step 1:** start the server:

Server
```
java -cp target/app-java-1.0-SNAPSHOT.jar csc435.app.FileRetrievalServer 12345
>
```

**Step 2:** start the benchmark:

Benchmark
```
java -cp target/app-java-1.0-SNAPSHOT.jar csc435.app.FileRetrievalBenchmark 127.0.0.1 12345 2 ../datasets/dataset1_client_server/2_clients/client_1 ../datasets/dataset1_client_server/2_clients/client_2
Completed indexing 134247377 bytes of data
Completed indexing in 6.015 seconds
Searching at
Search completed in 0.4 seconds
Search results (top 10 out of 0):
Searching Worms
Search completed in 2.8 seconds
Search results (top 10 out of 12):
* client1:folder4/Document10553.txt:4
* client1:folder3/Document1043.txt:4
* client2:folder7/Document1091.txt:3
* client1:folder3/Document10383.txt:3
* client2:folder7/folderB/Document10991.txt:2
* client2:folder8/Document11116.txt:1
* client2:folder5/folderB/Document10706.txt:1
* client2:folder5/folderB/Document10705.txt:1
* client2:folder5/folderA/Document10689.txt:1
* client1:folder4/Document1051.txt:1
Searching distortion AND adaptation
Search completed in 3.27 seconds
Search results (top 10 out of 4):
* client2:folder7/folderC/Document10998.txt:6
* client1:folder4/Document10516.txt:3
* client2:folder8/Document11159.txt:2
* client2:folder8/Document11157.txt:2
```

**Step 3:** close the server:

Server
```
> quit
```
