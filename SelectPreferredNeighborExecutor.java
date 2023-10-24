import java.util.Map;
import java.util.Set;

public class SelectPreferredNeighborExecutor implements Runnable {
    public int peerId;

    int CHOKEDPEER = 0;

    int UNCHOKEDPEER = 1;

    private PeerNode peerNode;

    public SelectPreferredNeighborExecutor(int peerId, PeerNode peerNode) {
        this.peerId = peerId;
        this.peerNode = peerNode;
    }

    public boolean checkIfThreadIsNotInterrupted() {
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

    //check each neighbor type
    private void validatePeerNeighbors() {
        try {
            for (Map.Entry<Integer, TorrentService> peerEntry : getPeerEntries()) {
                int peerId = peerEntry.getKey();
                TorrentService torrentService = peerEntry.getValue();
                validateExtractedPeers(peerId, torrentService);
            }
        } catch (Exception excep) {
            excep.printStackTrace();
        }

    }

    //send unchoke message is peer is neighbor else choke the peer
    private void validateExtractedPeers(int peerId, TorrentService torrentService) {
        try {
            if (checkIfPeerIsNeighbor(peerId)) {
                torrentService.pingNeighborWithMessage(ConstantFields.MessageForm.UNCHOKING);
            } else if (checkIfPeerIsOptimisticNeighbor(peerId)) {
                return;
            } else {
                torrentService.pingNeighborWithMessage(ConstantFields.MessageForm.CHOKING);
            }
        } catch (Exception excep) {
            excep.printStackTrace();
        }

    }

    private Set<Map.Entry<Integer, TorrentService>> getPeerEntries() {
        return peerNode.getPeerTorrentService().entrySet();
    }

    //checks if the thread is interrupted
    public boolean checkIfThreadIsInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    //checks if the given peer Id is a neighbor
    public boolean checkIfPeerIsNeighbor(int peerId) {
        return peerNode.getPreferredNeighboringPeers().contains(peerId);
    }

    //checks if the given peer id is a optimistic neighbor
    public boolean checkIfPeerIsOptimisticNeighbor(int peerId) {
        return peerNode.getOptimisticNeighboringPeer().get() == peerId;
    }

    public boolean checkExistanceOfNeighbors(int peerId) {
        boolean answer = false;
        try {
            for (Map.Entry<Integer, TorrentService> peerEntry : getPeerEntries()) {
               if(peerEntry.getKey() == peerId){
                   answer = true;
               }
            }
        } catch (Exception excep) {
            excep.printStackTrace();
        }
        return answer;
    }
}
