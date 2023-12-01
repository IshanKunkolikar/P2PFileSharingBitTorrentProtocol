import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.Delayed;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SelectOptimisticallyUnchokedExecutor implements Runnable {
    int peerId;
    PeerNode peerNode;
    Map<Integer, Peer> dataPeerCfg;
    Random createRandom;

    Logger LOGGER = LogManager.getLogger(TorrentService.class);

    //constructs the executor and initializes peer node
    public SelectOptimisticallyUnchokedExecutor(int id, PeerNode peerNode, Map<Integer, Peer> dataPeerCfg) {
        this.peerId = id;
        this.peerNode = peerNode;
        this.dataPeerCfg = dataPeerCfg;
        createRandom = new Random();
    }

    // Choose one choked peer at random to be an optimistically unchoked neighbour.
    public int ChooseUnchokedPeer(List<Integer> chokedPeerNodes) {
        int optimistic = 0;
        try {
            if (!chokedPeerNodes.isEmpty()) {
                optimistic = chokedPeerNodes.get(createRandom.nextInt(chokedPeerNodes.size()));
                peerNode.setOptimisticNeighboringPeer(optimistic);
                LOGGER.info("{}: Peer {} has the optimistically unchoked neighbor {}", fetchCurrTime(), this.peerId, optimistic);
            }
        } catch (Exception excep) {
            excep.printStackTrace();
        }

        return optimistic;
    }

    public static String fetchCurrTime() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now());
    }

    //checks the chokedPeerNodes and set them as unchoked
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

    //sets the optimistic node as unchoked
    public void setUnchokedNode(int optimistic){
        // get torrent service from the optimistic neighbour
        TorrentService TorrentService = peerNode.getPeerTorrentService(optimistic);
        TorrentService.pingNeighborWithMessage(ConstantFields.MessageForm.UNCHOKE);
    }

    @Override
    public void run() {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        // Retrieval of the list of Peers which are choked
        List<Integer> chokedPeerNodes = new ArrayList<>();

        try {
            for (int i : this.dataPeerCfg.keySet()) {
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

    //constructs the data block piece
    public Piece(int val) {
        this.currIndex = val;
        this.prevIndex = -1;
        this.createTime = LocalDateTime.now();
    }

    // updates the indexes of the piece block
    public void newIndex(int val){
        this.prevIndex = this.currIndex;
        this.currIndex = val;
    }

    //fetches the currrent piece block index
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
