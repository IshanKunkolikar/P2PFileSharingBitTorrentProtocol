public class ConstantFields {

    public static final int PEER_LENGTH = 30;
    public static final String PEER_HEADER = "P2PFileSharingBitTorrentProtocol";
    public static final int PEER_HEADER_FRONT = 0;
    public static final int PEER_HEADER_FIELD = 8;
    public static final int PEER_ZERO_BITS_FRONT = 8;
    public static final int PEER_ZERO_BITS_FIELD = 1;
    public static final int PEER_PEER_ID_FRONT = 20;
    public static final int PEER_PEER_ID_FIELD = 0;

    public static final int PEER_MESSAGE_LENGTH_FRONT_INDEX = 0;
    public static final int PEER_MESSAGE_LENGTH_FIELD_INDEX = 7;
    public static final int PEER_MESSAGE_TYPE_FRONT_INDEX = 7;
    public static final int PEER_MESSAGE_TYPE_FIELD_INDEX = 3;

    public static final int PEER_PIECE_FRONT = 0;
    public static final int PEER_PIECE_INDEX = 7;

    public static enum MessageForm {
        CHOKING(0), UNCHOKING(1);

        private final int messageValue;

        private MessageForm(int messageValue) {
            
            this.messageValue = messageValue;
        }

        public int getMessageVal() {
            return this.messageValue;
        }

        public MessageForm defaultMessageForm = null;
        public static MessageForm getMessageFormByValue(int messageValue) {
            MessageForm defaultMessageForm = null;
            try{
                for (MessageForm messageForm: MessageForm.values()) {
                    if (checkIfMessageIsSame(messageValue, messageForm)) {
                        return messageForm;
                    }
                }
            }
            catch(Exception excep){
                excep.printStackTrace();
            }
            return defaultMessageForm;
        }

        ///validates if messageValue and MessageForm is same
        private static boolean checkIfMessageIsSame(int messageValue, MessageForm messageForm) {
            return messageForm.getMessageVal() == messageValue;
        }
    }
    
}
