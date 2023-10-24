import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class PeerNode {
    private final int peerId;
    private final List<String> dataCommonCfg;

    private final List<String> dataPeerCfg;

    private final ExecutorService fixedThreadPoolExecutor;

    private final ScheduledExecutorService scheduledThreadPoolExecutor;

    private final AtomicInteger optimisticNeighboringPeer = new AtomicInteger(-1);

    private final int prefNeighborsCount;

    private final Map<Integer, Integer> downloadingSpeedMap = new ConcurrentHashMap<>();

    private final Set<Integer> preferredNeighboringPeers = ConcurrentHashMap.newKeySet();

    private final Set<Integer> interestedNeighboringPeers = ConcurrentHashMap.newKeySet();

    private final Map<Integer, TorrentService> peerTorrentServices = new ConcurrentHashMap<>();

    //constructs a peer node
    public PeerNode(int peerId, List<String> dataCommonCfg, List<String> dataPeerCfg, ExecutorService fixedThreadPoolExecutor, ScheduledExecutorService scheduledThreadPoolExecutor, int prefNeighborsCount) {
        this.peerId = peerId;
        this.dataCommonCfg = dataCommonCfg;
        this.dataPeerCfg = dataPeerCfg;
        this.fixedThreadPoolExecutor = fixedThreadPoolExecutor;
        this.scheduledThreadPoolExecutor = scheduledThreadPoolExecutor;
        this.prefNeighborsCount = prefNeighborsCount;
    }
    
    //re-selecting preferred neighbors every p seconds
    public void selectNeighborAgain() {
        // Resetting the un-choked peer
        resetUnchockedPeer();

        //iterate over the download rates and initialize downloadingSpeedMap
        for (int pId : downloadingSpeedMap.keySet()){
            this.downloadingSpeedMap.put(pId, 0);
        }

        selectprefNeighborsCount();
    }

    // Select prefNeighborsCount with highest download speed and are interested in peer data
    public void selectprefNeighborsCount(){
        // get the download rates of each peer and sort the peer in descending download rate order
        List<Integer> sortedPeersBasedOnDownloadRate = sortPeerBasedOnDownloadRate();

        int pnCount = 0;
        int i = 0;
        while (pnCount < prefNeighborsCount && i < this.interestedNeighboringPeers.size()) {
            int currentPeerNode = sortedPeersBasedOnDownloadRate.get(i);
            if (checkIfInterestedNeighborHasCurrentPeer(currentPeerNode)) {
                this.preferredNeighboringPeers.add(currentPeerNode);
                pnCount++;
            }
            i++;
        }
    }

    public boolean checkIfInterestedNeighborHasCurrentPeer(int currentPeerNode){
        return this.interestedNeighboringPeers.contains(currentPeerNode);
    }

    private void resetUnchockedPeer() {
        //clearing the downloading rates and list of un-choked peers
        this.preferredNeighboringPeers.clear();
    }

    public List<Integer> sortPeerBasedOnDownloadRate() {
        List<Map.Entry<Integer, Integer>> sortedDownloadingSpeedMap = getSortedDownloadingSpeedMap();
        List<Integer> orderedPeersBasedOnSpeed = new ArrayList<>();
        populatePeersOnDownloadRate(sortedDownloadingSpeedMap, orderedPeersBasedOnSpeed);
        return orderedPeersBasedOnSpeed;
    }

    private static void populatePeersOnDownloadRate(List<Map.Entry<Integer, Integer>> sortedDownloadingSpeedMap, List<Integer> orderedPeersBasedOnSpeed) {
        for(Map.Entry<Integer, Integer> entry : sortedDownloadingSpeedMap) {
            orderedPeersBasedOnSpeed.add(entry.getKey());
        }
    }

    // sorts the neighbors according to download rate
    public List<Map.Entry<Integer, Integer>> getSortedDownloadingSpeedMap(){
        List<Map.Entry<Integer, Integer>> sortedDownloadingSpeedMap = new ArrayList<>(downloadingSpeedMap.entrySet());
        sortedDownloadingSpeedMap.sort(Map.Entry.comparingByValue());
        return sortedDownloadingSpeedMap;
    }

    public Set<Integer> getPreferredNeighboringPeers() {
        return this.preferredNeighboringPeers;
    }

    //increment the interested neighbor
    public int checkInterestedNeighborCount(int currentPeerNode){
        return this.prefNeighborsCount +1;
    }

    // reset the peer count
    private void resetNeighboringPeerCount() {
        //clearing the downloading rates and list of un-choked peers
        this.preferredNeighboringPeers.clear();
    }

    public void setOptimisticNeighboringPeer(int neighboringPeer) {
        this.optimisticNeighboringPeer.set(neighboringPeer);
    }

    public AtomicInteger getOptimisticNeighboringPeer() {
        return this.optimisticNeighboringPeer;
    }

    public Map<Integer, TorrentService> getPeerTorrentService() {
        return this.peerTorrentServices;
    }

    //adding new peer service
    public void addPeerTorrentService(int peerId, TorrentService torrentService) {
        this.peerTorrentServices.put(peerId, torrentService);
        this.downloadingSpeedMap.put(peerId, 0);
    }
}
