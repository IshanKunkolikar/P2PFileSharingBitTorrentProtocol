import java.util.Map;
import java.util.Set;

public class SelectPreferredNeighborExecutor implements Runnable {
    public int peerId;

    int CHOKEPEER = 0;

    int UNCHOKEPEER = 1;

    private PeerNode peerNode;

    public SelectPreferredNeighborExecutor(int peerId, PeerNode peerNode) {
        this.peerId = peerId;
        this.peerNode = peerNode;
    }

    public boolean checkIfThreadIsNotInterrupted(){
        return !Thread.currentThread().isInterrupted();
    }

    @Override
    public void run() {
        if (checkIfThreadIsInterrupted())
            return;
        this.peerNode.selectNeighborAgain();
        //get the preferred neighboring peers
        Set<Integer> preferredNeighboringPeers = this.peerNode.getPreferredNeighboringPeers();
        //validate neighbor peer type
        validatePeerNeighbors();
    }

    private void validatePeerNeighbors() {
        for (Map.Entry<Integer, TorrentService> peerEntry : getPeerEntries()) {
            int peerId = peerEntry.getKey();
            TorrentService torrentService = peerEntry.getValue();
            validateExtractedPeers(peerId, torrentService);
        }
    }

    private void validateExtractedPeers(int peerId, TorrentService torrentService) {
        if (checkIfPeerIsNeighbor(peerId)) {
            torrentService.pingNeighborWithMessage(ConstantFields.MessageForm.UNCHOKING);
        } else if (checkIfPeerIsOptimisticNeighbor(peerId)) {
            return;
        } else {
            torrentService.pingNeighborWithMessage(ConstantFields.MessageForm.CHOKING);
        }
    }

    private Set<Map.Entry<Integer, TorrentService>> getPeerEntries() {
        return peerNode.getPeerTorrentService().entrySet();
    }

    //checks if the thread is interrupted
    public boolean checkIfThreadIsInterrupted(){
        return Thread.currentThread().isInterrupted();
    }

    //checks if the given peer Id is a neighbor
    public boolean checkIfPeerIsNeighbor(int peerId){
        return peerNode.getPreferredNeighboringPeers().contains(peerId);
    }

    //checks if the given peer id is a optimistic neighbor
    public boolean checkIfPeerIsOptimisticNeighbor(int peerId){
        return peerNode.getOptimisticNeighboringPeer().get() == peerId;
    }
}
