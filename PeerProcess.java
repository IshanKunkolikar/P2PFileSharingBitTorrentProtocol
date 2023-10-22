import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class PeerProcess {

   public static final String CURR_DIRECTORY = System.getProperty("user.dir");
   public static final String COMMON_CONFIG = "Common.cfg";
   public static final String PEER_INFO_CONFIG = "PeerInfo.cfg";

   public static boolean isValidFilePath(String path) {
      return path != null && path.length() > 0;
   }

   private static String buildFullFilePath(String filePath) {
      return CURR_DIRECTORY + File.separator + filePath;
   }

   private static List<String> readLinesFromFile(String fullPath) throws IOException {
      List<String> lines = new ArrayList<>();

      try (BufferedReader reader = new BufferedReader(new FileReader(fullPath))) {
         String line;
         while ((line = reader.readLine()) != null) {
            lines.add(line);
         }
      }
      return lines;
   }

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

   private static void createPeerDirectory(int peerId, String currDir) {
      try {
          String peerPath = Paths.get(currDir, String.format("peer_%d", peerId)).toString();
          Files.createDirectories(Paths.get(peerPath));
      } catch (IOException error) {
         error.printStackTrace();
      }
  }
  
  private static void copyInputFile(int peerId, String inputFileName, Boolean fileExists) {
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


   private static void buildDirectory(String fileName, String currDir, PeerInfoCfg.PeerInfo peerData) throws Exception {
      createPeerDirectory(peerData.getId(), currDir);
      copyInputFile(peerData.getId(), fileName, peerData.getHasFile());
   }

   public static void main(String[] peerProcessArguments){
        //Getting the peer id from arguments
        int peerId = Integer.parseInt(peerProcessArguments[0]);

         // Read PeerInfo.cfg
         List<String> dataPeerCfg = readFile(PEER_INFO_CONFIG);
         PeerInfoCfg peerInfoCfgFile = new PeerInfoCfg();
         peerInfoCfgFile.parse(dataPeerCfg);

        // Read Common.cfg
        List<String> dataCommonCfg = readFile(COMMON_CONFIG);
        CommonCfg commonCfgFile = new CommonCfg();
        commonCfgFile.parse(dataCommonCfg);

        // Create folder structure
        try {
            buildDirectory(commonCfgFile.getFileName(), CURR_DIRECTORY, peerInfoCfgFile.getPeer(peerId));
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
}
