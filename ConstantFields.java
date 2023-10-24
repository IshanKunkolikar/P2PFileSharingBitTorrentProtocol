public class ConstantFields {

    public static final Integer PEER_LENGTH = 32;
    public static final String PEER_HEADER = "P2PFileSharingBitTorrentProtocol";
    public static final Integer PEER_HEADER_START = 0;
    public static final Integer PEER_HEADER_FIELD = 18;
    public static final Integer PEER_ZERO_BITS_START = 18;
    public static final Integer PEER_ZERO_BITS_FIELD = 10;
    public static final Integer PEER_PEER_ID_START = 28;
    public static final Integer PEER_PEER_ID_FIELD = 4;

    public static final Integer PEER_MESSAGE_LENGTH_START_INDEX = 0;
    public static final Integer PEER_MESSAGE_LENGTH_FIELD_INDEX = 4;
    public static final Integer PEER_MESSAGE_TYPE_START_INDEX = 4;
    public static final Integer PEER_MESSAGE_TYPE_FIELD_INDEX = 1;

    public static final Integer PEER_PIECE_START = 0;
    public static final Integer PEER_PIECE_INDEX = 4;

    public static enum MessageForm {
        CHOKING(0), UNCHOKING(1);

        private final int messageValue;

        private MessageForm(int messageValue) {
            
            this.messageValue = messageValue;
        }

        public int getValue() {
            
            return this.messageValue;
        }

        public MessageForm defaultMessageForm = null;
        public static MessageForm getMessageFormByValue(int messageValue) {
            MessageForm defaultMessageForm = null;
            for (MessageForm messageForm: MessageForm.values()) {
                if (messageForm.getValue() == messageValue) {
                    return messageForm;
                }
            }
            return defaultMessageForm;
        }
    }
    
}
