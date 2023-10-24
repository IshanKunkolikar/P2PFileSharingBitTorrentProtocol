import java.util.LinkedHashMap;
import java.util.Map;

public class Peer {
    private int portNo;
    private boolean fileExists;
    private int id;
    private String host;


    public Peer(int portNo, boolean fileExists, int id, String host) {
        this.portNo = portNo;
        this.fileExists = fileExists;
        this.id = id;
        this.host = host;
    }

    public int fetchPortNo() {
        return portNo;
    }

    public boolean isFileExisting() {
        return fileExists;
    }

    public int fetchPeerId() {
        return id;
    }

    public String fetchHost() {
        return host;
    }

    private Map<Integer, Peer> completePeerMapping = new LinkedHashMap<>();

    public String fetchDetails() {
        return "portNo-" + portNo + "\n" +
               "fileExists-"+ fileExists + "\n" +
               "id-" + id + "\n" +
               "host-" + host + "\n";             
    }

    public Map<Integer, Peer> getCompletePeerMapping() {
        return this.completePeerMapping;
    }
}
