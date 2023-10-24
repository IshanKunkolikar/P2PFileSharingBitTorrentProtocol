import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SelectOptimisticallyUnchokedExecutor implements Runnable {
    int peerId;
    PeerNode peerNode;
    Peer peer;
    Random createRandom;

    public SelectOptimisticallyUnchokedExecutor(int id, PeerNode peerNode, Peer peer) {
        this.peerId = id;
        this.peerNode = peerNode;
        this.peer = peer;
        createRandom = new Random();
    }

    // Choose one choked peer at random to be an optimistically unchoked neighbour.
    public int ChooseUnchokedPeer(List<Integer> chokedPeerNodes) {
        int optimistic = 0;
        try {
            if (!chokedPeerNodes.isEmpty()) {
                optimistic = chokedPeerNodes.get(createRandom.nextInt(chokedPeerNodes.size()));
                peerNode.setOptimisticNeighboringPeer(optimistic);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return optimistic;
    }

    public void ConfirmUnchokedPeer(List<Integer> chokedPeerNodes , int optimistic)
    {
        try {
            //  checking if the nodes exist and replacing if necessary
            while(optimistic < 0)
            {
                optimistic = ChooseUnchokedPeer(chokedPeerNodes);
            }
            SetUnchokedNode(optimistic);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SetUnchokedNode(int optimistic){

        // get torrent service from the optimistic neighbour
        TorrentService TorrentService = peerNode.getPeerTorrentService(optimistic);
        TorrentService.pingNeighborWithMessage(ConstantFields.MessageForm.UNCHOKING);
    }

    @Override
    public void run() {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        // Retrieval of the list of Peers which are choked
        List<Integer> chokedPeerNodes = new ArrayList<>();

        try {
            for (int i : this.peer.getCompletePeerMapping().keySet()) {
                if ((!peerNode.getPreferredNeighboringPeers().contains(i)))
                    chokedPeerNodes.add(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int optimistic = ChooseUnchokedPeer(chokedPeerNodes);
        ConfirmUnchokedPeer(chokedPeerNodes, optimistic);

    }
}
