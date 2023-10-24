import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class TorrentService implements Runnable {
    PeerNode peerNode1;
    PeerNode peerNode2;

    int peerNode1Id;
    int peerNode2Id;

    Socket torrentSocket;
    ExecutorService fixedThreadPoolExecutor;

    OutputStream outputChannel;
    InputStream inputChannel;

    ScheduledExecutorService scheduledThreadPoolExecutor;

    String bitfieldData;
    String fileDataPieces;

    //constricts and initializes peers and schedulers
    public TorrentService(int peerNode1Id, int peerNode2Id, PeerNode peerNode1, PeerNode peerNode2, Socket torrentSocket, ExecutorService fixedThreadPoolExecutor,
                          ScheduledExecutorService scheduledThreadPoolExecutor, OutputStream outputChannel,
                          String bitfieldData, String fileDataPieces) throws IOException {
        this.peerNode1Id = peerNode1Id;
        this.peerNode1 = peerNode1;
        this.peerNode2Id = peerNode2Id;
        this.peerNode2 = peerNode2;
        this.torrentSocket = torrentSocket;
        this.fixedThreadPoolExecutor = fixedThreadPoolExecutor;
        this.scheduledThreadPoolExecutor = scheduledThreadPoolExecutor;
        this.bitfieldData = bitfieldData;
        this.fileDataPieces = fileDataPieces;
        this.inputChannel = this.torrentSocket.getInputStream();
        this.outputChannel = this.torrentSocket.getOutputStream();
    }

    @Override
    public void run() {
        // start handshake
        peerNode1.addPeerTorrentService(peerNode2Id, this);
    }

    public Socket getTorrentSocket() {
        return this.torrentSocket;
    }

    // sends message to the neighbors
    public void pingNeighborWithMessage(ConstantFields.MessageForm messageForm) {
        try {
            this.fixedThreadPoolExecutor.execute(
                    new TorrentMessenger(this.torrentSocket.getOutputStream(), getMessageText(messageForm, new byte[0]))
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // gets the message
    public static byte[] getMessageText(ConstantFields.MessageForm messageType, byte[] messageContentPayload) {
        int messageTextLength = messageContentPayload != null ? messageContentPayload.length : 0;
        byte[] messageData = getNewByte(messageTextLength);
        int messageCounter = combineMsg2InMsg1(messageData, convertIntegerToByteCollection(messageTextLength), 0);
        messageCounter = messageCounter+1;
        messageData[messageCounter] = (byte) messageType.getValue();
        if (checkIfMessageLengthIsValid(messageTextLength)) {
            combineMsg2InMsg1(messageData, messageContentPayload, messageCounter);
        }
        return messageData;
    }

    private static byte[] getNewByte(int messageTextLength) {
        return new byte[ConstantFields.PEER_MESSAGE_LENGTH_FIELD_INDEX + ConstantFields.PEER_MESSAGE_TYPE_FIELD_INDEX + messageTextLength];
    }

    public static boolean checkIfMessageLengthIsValid(int messageTextLength){
        return messageTextLength>0;
    }

    // merge one array with another array
    public static int combineMsg2InMsg1(byte[] msgData1, byte[] msgData2, int initialIndex) {
        for (byte msgContent : msgData2) {
            msgData1[initialIndex++] = msgContent;
        }
        return initialIndex;
    }

    //converts Integer length To a Byte array
    public static byte[] convertIntegerToByteCollection(int length) {
        return ByteBuffer.allocate(4).putInt(length).array();
    }
}
