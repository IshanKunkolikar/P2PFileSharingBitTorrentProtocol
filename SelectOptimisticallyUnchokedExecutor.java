import java.util.concurrent.Delayed;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
        } catch (Exception excep) {
            excep.printStackTrace();
        }

        return optimistic;
    }

    public void confirmUnchokedPeer(List<Integer> chokedPeerNodes , int optimistic)
    {
        try {
            //  checking if the nodes exist and replacing if necessary
            while(optimistic < 0)
            {
                optimistic = ChooseUnchokedPeer(chokedPeerNodes);
            }
            setUnchokedNode(optimistic);

        } catch (Exception excep) {
            excep.printStackTrace();
        }
    }

    public void setUnchokedNode(int optimistic){
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
        } catch (Exception excep) {
            excep.printStackTrace();
        }

        int optimistic = ChooseUnchokedPeer(chokedPeerNodes);
        confirmUnchokedPeer(chokedPeerNodes, optimistic);

    }
}

//creates piece blocks from the data blocks
abstract class Piece implements Delayed {
    private int currIndex;
    private LocalDateTime createTime;

    private int prevIndex;

    public Piece(int val) {
        this.currIndex = val;
        this.prevIndex = -1;
        this.createTime = LocalDateTime.now();
    }

    public void newIndex(int val){
        this.prevIndex = this.currIndex;
        this.currIndex = val;
    }

    public int getCurrIndex() {
        return currIndex;
    }

    //compares the delay time
    public boolean compareTime(Delayed otherIndex) {
        long result = 0;
        try{
             result = this.getDelay(TimeUnit.MILLISECONDS) - otherIndex.getDelay(TimeUnit.MILLISECONDS);
        }
        catch(Exception excep){
            excep.printStackTrace();
        }

        if (result < 0) {
            return false;
        }

        return true;
    }

    //compares the message index change
    public int compareChange(){
        int ress = 0;
        int curr = this.currIndex;
        int prev = this.prevIndex;

        try{
            if(prev==-1)
            {
                return -1;
            }
            else{
                ress = curr - prev;
            }
        }
        catch(Exception excep){
            excep.printStackTrace();
        }

        return ress;
    }
}
