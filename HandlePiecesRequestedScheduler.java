import java.util.BitSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.DelayQueue;

public class HandlePiecesRequestedScheduler implements Runnable {
    private PeerNode peerNode;
//    private final Bitfield bitfield;

    public HandlePiecesRequestedScheduler(PeerNode peerNode) {
        this.peerNode = peerNode;
//        this.bitfield = peer.getBitfield();
    }

    @Override
    public void run() {
        try {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

//            DelayQueue<Piece> piecesRequested = bitfield.getDelayQueue();
//            Piece expiredPieceIndex = piecesRequested.poll();

            while (true) {
//                if (Objects.isNull(expiredPieceIndex)) {
//                    break;
//                }

//                bitfield.removeTimedOutPieceIndex(expiredPieceIndex.getCurrIndex());

                for (Map.Entry<Integer, BitSet> entry : peerNode.getPeerBitfields().entrySet()) {
                    BitSet bitset = entry.getValue();
//                    if (bitset.get(expiredPieceIndex.getIndex())) {
//                        TorrentService ep = peerNode.getPeerTorrentService(entry.getKey());
//                        ep.pingNeighborWithMessage(ConstantFields.MessageForm.INTERESTED);
//                    }
                }
//                expiredPieceIndex = piecesRequested.poll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
