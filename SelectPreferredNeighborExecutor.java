import java.util.Map;
import java.util.Set;

public class SelectPreferredNeighborExecutor implements Runnable {
    public int peerId;

    int CHOKE = 0;

    int UNCHOKE = 1;

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

        for (Map.Entry<Integer, TorrentService> entry : peerNode.getPeerTorrentService().entrySet()) {
            Integer peerId = entry.getKey();
            TorrentService torrentService = entry.getValue();
            if (checkIfPeerIsNeighbor(peerId)) {
                torrentService.pingNeighborWithMessage(ConstantFields.MessageForm.UNCHOKING);
            } else if (checkIfPeerIsOptimisticNeighbor(peerId)) {
                continue;
            } else {
                torrentService.pingNeighborWithMessage(ConstantFields.MessageForm.CHOKING);
            }
        }
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
