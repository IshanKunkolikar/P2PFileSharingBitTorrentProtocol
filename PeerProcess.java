import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ExecutorService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class PeerProcess {

   //declaring constants
   public static final String CURR_DIRECTORY = System.getProperty("user.dir");
   public static final String COMMON_CONFIG = "Common.cfg";
   public static final String PEER_INFO_CONFIG = "PeerInfo.cfg";

   public static Map<Integer, Peer> peerMap = new LinkedHashMap<>();
   public static int prefNeighborsCount;
   public static int pieceCount;
	public static int unchokingTime;
   public static int optUnchokingTime;
   public static String commonCfgFileName;
   public static int sizeOfFile;
   public static int sizeOfPiece;

   //checking if the input path provided is valid
   public static boolean isValidFilePath(String path) {
      return path != null && path.length() > 0;
   }

   //building the full file path using the current directory and path given
   private static String buildFullFilePath(String filePath) {
      return CURR_DIRECTORY + File.separator + filePath;
   }

   //reading the passed file line by line and storing the data in a list
   public static List<String> readLinesFromFile(String fullPath) throws IOException {
      List<String> lines = new ArrayList<>();

      try (BufferedReader reader = new BufferedReader(new FileReader(fullPath))) {
         String line;
         while ((line = reader.readLine()) != null) {
            lines.add(line);
         }
      }
      return lines;
   }

   //reading the file provided in the path
   public static List<String> readFile(String path) {
      List<String> content = new ArrayList<>();
      if (isValidFilePath(path)) {
         String fullPath = buildFullFilePath(path);

         try {
            content = readLinesFromFile(fullPath);
         } catch (IOException error) {
            error.printStackTrace();
         }
      }
      return content;
   }

   //adding peer details to the peer map
   public static void addPeerInfo(int portNo, boolean fileExists, int id, String host) {
      Peer peerData = new Peer(portNo, fileExists, id, host);
      peerMap.put(id, peerData);
  }

  //parsing the peer config data line by line and adding it to the map
   public static void parsePeerLine(String line) {
      String[] values = line.split(" ");
      addPeerInfo(Integer.parseInt(values[0]), Integer.parseInt(values[1]) == 1, Integer.parseInt(values[2]), values[3]);
  }
   
  //parsing the config data from the peer config file
  public static void parsePeerData(List<String> configLines) {
      for(String line : configLines) {
         parsePeerLine(line);
      }
  }

  //fetching specific peer data from the peer map
  public static Peer fetchPeer(int id) {
      return peerMap.get(id);
   }

   //Returning all peers as a peer map
   public static Map<Integer, Peer> getPeers() {
      return peerMap;
   }

  //setting the prefNeighborsCount
  public static void setPrefNeighborsCount(String line) {
      prefNeighborsCount = extractValue(line);
   }

   //setting the unchoking time interval
   public static void setUnchokingTime(String line) {
      unchokingTime = extractValue(line);
   }

   //setting the optimistic unhocking time interval
   public static void setOptUnchokingTime(String line) {
      optUnchokingTime = extractValue(line);
   }

   //setting the file name for the common config file
   public static void setcommonCfgFileName(String line) {
      commonCfgFileName = line.split(" ")[1];
   }

   //setting the file size
   public static void setFileSize(String line) {
      sizeOfFile = extractValue(line);
   }

   //setting the piece size
   public static void setPieceSize(String line) {
      sizeOfPiece = extractValue(line);
   }

   //computing the piece count using file and piece size
   public static void calculatePieceCount() {
      double sizeOfFileInDouble = (double) sizeOfFile;
      double sizeOfPieceInDouble = (double) sizeOfPiece;
      pieceCount = (int) Math.ceil(sizeOfFileInDouble / sizeOfPieceInDouble);
   }

   //extracting the value from the config line by splitting it by " "
   public static int extractValue(String line) {
      return Integer.parseInt(line.split(" ")[1]);
   }

   //checking if the common config file has any lines
   public static boolean isValidConfig(List<String> configLines) {
   return configLines != null && configLines.size() == 6;
   }

   //parsing the common config file data
   public static void parseCommonCfgData(List<String> configLines) {
      if (isValidConfig(configLines)) {
          setPrefNeighborsCount(configLines.get(0));
          setUnchokingTime(configLines.get(1));
          setOptUnchokingTime(configLines.get(2));
          setcommonCfgFileName(configLines.get(3));
          setFileSize(configLines.get(4));
          setPieceSize(configLines.get(5));
          calculatePieceCount();
      }
  }
  
   //creating the peer directory having peer id and current directory
   public static void createPeerDirectory(int peerId, String currDir) {
      try {
          String peerPath = Paths.get(currDir, String.format("peer_%d", peerId)).toString();
          Files.createDirectories(Paths.get(peerPath));
      } catch (IOException error) {
         error.printStackTrace();
      }
  }

  //copy input file from source to destination
  public static void copyInputFile(int peerId, String inputFileName, Boolean fileExists) {
      if (fileExists) {
          try {
            Path src = Paths.get(CURR_DIRECTORY, inputFileName);
            Path dest = Paths.get(CURR_DIRECTORY, String.format("peer_%d", peerId), inputFileName);
            Files.copy(src, dest);
          } catch (IOException error) {
            error.printStackTrace();
          }
      }
  }

   //building the directory by creating a peer directory and copying the input file
   public static void buildDirectory(String fileName, String currDir, Peer peerData) throws Exception {
      createPeerDirectory(peerData.fetchPeerId(), currDir);
      copyInputFile(peerData.fetchPeerId(), fileName, peerData.isFileExisting());
   }

   //implementing concurrency
   public void initializePeerAndSchedulers() {
      ExecutorService executorService = createFixedThreadPool(8);
      ScheduledExecutorService scheduler = createScheduledThreadPool(8);
  }
  
  //intializing ExecutorService
  private ExecutorService createFixedThreadPool(int numThreads) {
      return Executors.newFixedThreadPool(numThreads);
  }
  
  //initializing ScheduledExecutorService
  private ScheduledExecutorService createScheduledThreadPool(int numThreads) {
      return Executors.newScheduledThreadPool(numThreads);
  }

   public static void main(String[] peerProcessArguments){
        //Getting the peer id from arguments
        int peerId = Integer.parseInt(peerProcessArguments[0]);

         List<String> dataPeerCfg = readFile(PEER_INFO_CONFIG);
         PeerProcess.parsePeerData(dataPeerCfg);

        List<String> dataCommonCfg = readFile(COMMON_CONFIG);
        PeerProcess.parseCommonCfgData(dataCommonCfg);

        try {
            buildDirectory(commonCfgFileName, CURR_DIRECTORY, PeerProcess.fetchPeer(peerId));
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
}
