import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.BitSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class BitValues {
    private final BitSet bitval;
    private final int pieceCount;
    private final CommonConfig commonConfig;
    private final ReadWriteLock lockOnReadWrite = new ReentrantReadWriteLock();
    private final Lock lockOnread = lockOnReadWrite.readLock();
    private final Lock lockOnWrite = lockOnReadWrite.writeLock();
    private final Set<Integer> piecesReq = ConcurrentHashMap.newKeySet();
    private final DelayQueue<Piece> piecesReqQueue = new DelayQueue<>();

    public BitValues(CommonConfig commonConfig, BitSet bitval) {
        this.bitval = bitval;
        this.commonConfig = commonConfig;
        this.pieceCount = this.commonConfig.getPieceCount();
    }

    public void readLock() {
        this.lockOnread.lock();
    }

    public void readUnlock() {
        this.lockOnread.unlock();
    }

    public BitSet getBitval() {
        return this.bitval;
    }

    public boolean allPiecesReceived() {
        int nextClearIndex = bitval.nextClearBit(0);
        return nextClearIndex == -1 || nextClearIndex >= pieceCount;
    }

    public void removeTimedOutPiece(int index) {
        this.piecesReq.remove(index);
    }

    public DelayQueue<Piece> fetchDelayInQueue() {
        return this.piecesReqQueue;
    }

    public boolean hasInterest(BitSet peerBit) {
        try {
            this.lockOnread.lock();
            return fetchIndexOfNextInterestedPiece(peerBit) != -1;
        } finally {
            this.lockOnread.unlock();
        }
    }

    public int fetchIndexOfNextInterestedPiece(BitSet peerBit) {
        for (int i = peerBit.nextSetBit(0); i != -1; i = peerBit.nextSetBit(i + 1)) {
            //If the piece is already present or requested::
            if (piecesReq.contains(i)) {
                continue;
            }
            //If the piece the peer has is interesting::
            if (!bitval.get(i)) {
                return i;
            }
        }
        return -1;
    }

    public void settingRecdPieceIndex(int index) {
        try {
            this.lockOnWrite.lock();
            bitval.set(index);
            piecesReq.remove(index);
        } finally {
            this.lockOnWrite.unlock();
        }
    }

    public void addingInPiecesReq(int pieceIndex) {
        this.piecesReq.add(pieceIndex);
        // this.piecesReqQueue.add(new Piece(pieceIndex));
    }

}
