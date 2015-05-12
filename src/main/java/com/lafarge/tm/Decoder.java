package com.lafarge.tm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;

public class Decoder {
    public static final int STATE_EXPIRATION_DELAY_IN_SEC = 1;

    private final MessageReceivedListener listener;
    private State state;
    private Date lastDecodeDate;

    private static final Logger logger = LoggerFactory.getLogger(Decoder.class);

    public Decoder(MessageReceivedListener listener) {
        this.listener = listener;
        this.state = new HeaderState();
    }

    public void decode(byte[] in) throws IOException {
        logger.info("will decode: {}", bytesToHex(in));

        if (stateHasExpired()) {
            logger.info("previous state has expired -> reset to HeaderState");
            this.state = new HeaderState();
        }
        this.state = this.state.decode(new ByteArrayInputStream(in));
    }

    private boolean stateHasExpired() {
        boolean previousStateHasExpired = false;
        Date now = new Date();

        if (this.lastDecodeDate != null) {
            long diffInSec = (now.getTime() - this.lastDecodeDate.getTime()) / 1000;

            logger.debug("diff between now and last decode: {}s", diffInSec);
            previousStateHasExpired = (diffInSec > STATE_EXPIRATION_DELAY_IN_SEC);
        }
        this.lastDecodeDate = now;
        logger.debug("did set lastDecodeDate to {}s", now.getTime());
        return previousStateHasExpired;
    }

    public static abstract class State {
        protected Message message;

        public State() {
        }

        public State(Message message) {
            this.message = message;
        }

        abstract public State decode(InputStream in) throws IOException;
        abstract protected void saveBuffer();

        public static final class Message {
            public byte header;
            public byte version;
            public byte typeMsb;
            public byte typeLsb;
            public byte sizeMsb;
            public byte sizeLsb;
            public byte[] data;

            public byte[] getMessageBytes() throws IOException {
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                out.write(this.header);
                out.write(this.version);
                out.write(this.typeMsb);
                out.write(this.typeLsb);
                out.write(this.sizeMsb);
                out.write(this.sizeLsb);
                out.write(this.data);
                return out.toByteArray();
            }
        }

        protected int buffToInt(byte[] buffer) {
            return (int)ByteBuffer
                    .wrap(buffer)
                    .getShort();
        }

        protected byte[] intToBuff(int i) {
            return ByteBuffer
                    .allocate(4)
                    .putShort((short)i) // Hack: message are only with two bytes
                    .array();
        }
    }

    /**
     * Header
     */
    public static final class HeaderState extends State {
        public HeaderState() {
            this.message = new Message();
        }

        @Override
        public State decode(InputStream in) throws IOException {
            int read = in.read();

            switch (read) {
                case -1:
                    logger.info("[HeaderState] end of buffer -> waiting...");
                    return this;
                case Protocol.HEADER:
                    logger.info("[HeaderState] did received header byte -> continue to VersionState");
                    saveBuffer();
                    return new VersionState(this.message)
                            .decode(in);
                default:
                    logger.warn("[HeaderState] did received incorrect byte -> trying next byte");
                    return this.decode(in);
            }
        }

        @Override
        protected void saveBuffer() {
            this.message.header = (byte)Protocol.HEADER;
        }
    }

    /**
     *  Version
     */
    public static final class VersionState extends State {
        public VersionState(Message message) {
            super(message);
        }

        @Override
        public State decode(InputStream in) throws IOException {
            int read = in.read();

            switch (read) {
                case -1:
                    logger.info("[VersionState] end of buffer -> waiting...");
                    return this;
                case Protocol.VERSION:
                    logger.info("[VersionState] did received version byte -> continue to SizeState");
                    saveBuffer();
                    return new TypeState(this.message)
                            .decode(in);
                default:
                    logger.warn("[VersionState] did received incorrect byte");
                    return new HeaderState();
            }
        }

        @Override
        protected void saveBuffer() {
            this.message.version = (byte)Protocol.VERSION;
        }
    }

    /**
     *  Type
     */
    public static final class TypeState extends State {
        public static final int TYPE_NB_BYTES = 2;

        private final byte[] buffer = new byte[TYPE_NB_BYTES];
        private int totalRead = 0;

        public TypeState(Message message) {
            super(message);
        }

        @Override
        public State decode(InputStream in) throws IOException {
            int read = in.read(this.buffer, this.totalRead, TYPE_NB_BYTES - this.totalRead);

            switch (read) {
                case -1:
                    logger.info("[TypeState] end of buffer -> waiting...");
                    return this;
                default:
                    this.totalRead += read;
                    return nextState(in);
            }
        }

        @Override
        protected void saveBuffer() {
            this.message.typeMsb = this.buffer[0];
            this.message.typeLsb = this.buffer[1];
        }

        private State nextState(InputStream in) throws IOException {
            State next = null;

            switch (this.totalRead) {
                case 1:
                    if (checkIfFirstByteOfTypeMessageExist(this.buffer[0])) {
                        logger.info("[TypeState] first byte match with a known type -> waiting for next byte");
                        next = this;
                    } else {
                        logger.warn("[TypeState] did received incorrect byte -> reset to HeaderState");
                    }
                    break;
                case TYPE_NB_BYTES:
                    int messageTypeFound = buffToInt(this.buffer);

                    if (checkIfTypeMessageExist(messageTypeFound)) {
                        logger.info("[TypeState] received message type exist -> continue to SizeState");
                        saveBuffer();
                        next = new SizeState(messageTypeFound, this.message)
                                .decode(in);
                    } else {
                        logger.warn("[TypeState] received type doesn't exit -> reset to HeaderState");
                    }
                    break;
                default:
                    assert false : "The impossible happened: the nb bytes read is not conform to the protocol";
                    break;
            }
            return (next != null) ? next : new HeaderState();
        }

        private boolean checkIfFirstByteOfTypeMessageExist(byte firstByteToTest) {
            for (Map.Entry<Integer, Protocol.Pair> entry : Protocol.constants.entrySet()) {
                int message = entry.getKey();
                byte firstByteToMatch = this.intToBuff(message)[0];

                logger.debug("[TypeState] received byte: {}, expected byte: {}", String.format("0x%02X", firstByteToTest), String.format("0x%02X", firstByteToMatch));
                if (firstByteToTest == firstByteToMatch) {
                    return true;
                }
            }
            return false;
        }

        private boolean checkIfTypeMessageExist(int messageTypeToTest) {
            for (Map.Entry<Integer, Protocol.Pair> entry : Protocol.constants.entrySet()) {
                int typeMessageToMatch = entry.getKey();

                logger.debug("[TypeState] received type: {}, expected type: {}", bytesToHex((intToBuff(messageTypeToTest))), bytesToHex((intToBuff(typeMessageToMatch))));
                if (messageTypeToTest == typeMessageToMatch) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     *  Size
     */
    public static final class SizeState extends State {
        public static final int SIZE_NB_BYTES = 2;

        private final int messageType;

        private final byte[] buffer = new byte[SIZE_NB_BYTES];
        private int totalRead = 0;

        public SizeState(int messageType, Message message) {
            super(message);
            this.messageType = messageType;
        }

        @Override
        public State decode(InputStream in) throws IOException {
            int read = in.read(this.buffer, this.totalRead, SIZE_NB_BYTES - this.totalRead);

            switch (read) {
                case -1:
                    logger.info("[SizeState] end of buffer -> waiting...");
                    return this;
                default:
                    this.totalRead += read;
                    return nextState(in);
            }
        }

        @Override
        protected void saveBuffer() {
            this.message.sizeMsb = this.buffer[0];
            this.message.sizeLsb = this.buffer[1];
        }

        private State nextState(InputStream in) throws IOException {
            State next = null;

            switch (this.totalRead) {
                case 1:
                    if (checkIfSizeMatchForGivenMessageType(this.buffer[0], this.messageType)) {
                        logger.info("[SizeState] first byte match with current type's size -> waiting for next byte");
                        next = this;
                    } else {
                        logger.warn("[SizeState] the first byte doesn't match with the current type's size");
                    }
                    break;
                case SIZE_NB_BYTES:
                    int sizeFound = buffToInt(this.buffer);

                    if (checkIfSizeMatchForGivenMessageType(sizeFound, this.messageType)) {
                        saveBuffer();
                        if (sizeFound > 0) {
                            logger.info("[SizeState] received size match with current type's size -> continue to DataState");
                            next = new DataState(sizeFound, null)
                                    .decode(in);
                        } else {
                            logger.info("[SizeState] received size match with current type's size -> continue to CrcState (no need for Data)");
                            next = new CrcState(this.message)
                                    .decode(in);
                        }
                    } else {
                        logger.warn("[SizeState] the received size doesn't match with current type");
                    }
                    break;
                default:
                    assert false : "The impossible happened: the nb bytes read is not conform to the protocol";
            }
            return (next != null) ? next : new HeaderState();
        }

        private boolean checkIfSizeMatchForGivenMessageType(byte firstByteToTest, int stateToMatch) {
            Protocol.Pair pair = Protocol.constants.get(stateToMatch);

            if (pair != null) {
                byte firstByteToMatch = intToBuff(pair.size)[0];
                logger.debug("[SizeState] received byte: {}, expected byte: {}", String.format("0x%02X", firstByteToTest), String.format("0x%02X", firstByteToMatch));
                return firstByteToTest == firstByteToMatch;
            } else {
                assert true : "The impossible happened: The state wasn't recognize";
                return false;
            }
        }

        private boolean checkIfSizeMatchForGivenMessageType(int sizeToTest, int stateToMatch) {
            Protocol.Pair pair = Protocol.constants.get(stateToMatch);

            if (pair != null) {
                int sizeToMatch = pair.size;
                logger.debug("[SizeState] received size: {}, expected size: {}", sizeToTest, sizeToMatch);
                return sizeToTest == sizeToMatch;
            } else {
                assert false : "The impossible happened: The state wasn't recognize";
                return false;
            }
        }
    }

    /**
     *  Data
     */
    public static final class DataState extends State {
        private final byte[] buffer;
        private int expectedSize = 0;
        private int totalRead = 0;

        public DataState(int size, Message message) {
            super(message);
            this.expectedSize = size;
            this.buffer = new byte[size];
        }

        @Override
        public State decode(InputStream in) throws IOException {
            int read = in.read(this.buffer, this.totalRead, this.expectedSize - this.totalRead);

            if (read == -1) {
                logger.info("[DataState] end of buffer -> waiting...");
                return this;
            } else {
                this.totalRead += read;
                if (this.totalRead == this.expectedSize) {
                    logger.info("[DataState] did received all bytes -> continue to CrcState", this.totalRead, this.expectedSize);
                    saveBuffer();
                    return new CrcState(this.message)
                            .decode(in);
                } else {
                    logger.info("[DataState] -> did received {}/{} data bytes -> waiting...", this.totalRead, this.expectedSize);
                    return this;
                }
            }
        }

        @Override
        protected void saveBuffer() {
            this.message.data = buffer;
        }
    }

    /**
     *  CRC
     */
    public static final class CrcState extends State {
        public static final int CRC_NB_BYTES = 2;

        private byte[] crcToMatch;

        private final byte[] buffer = new byte[CRC_NB_BYTES];
        private int totalRead = 0;

        public CrcState(Message message) throws IOException {
            super(message);
            this.crcToMatch = computeCrc(message);

            String hex = bytesToHex(message.getMessageBytes());
            System.out.println(hex); // prints "7F0F00"
        }

        @Override
        public State decode(InputStream in) throws IOException {
            int read = in.read(this.buffer, this.totalRead, CRC_NB_BYTES - this.totalRead);

            switch (read) {
                case -1:
                    return this;
                default:
                    this.totalRead += read;
                    return nextState();
            }
        }

        @Override
        protected void saveBuffer() {
            // Do nothing here
        }

        State nextState() {
            State next = null;

            switch (this.totalRead) {
                case 1:
                    if (checkIfFirstByteMatchWithCrc(this.buffer[0])) {
                        next = this;
                    } else {
                        logger.warn("[CrcState] first byte received of crc doesn't match");
                    }
                    break;
                case CRC_NB_BYTES:
                    if (checkIfCrcMatch(this.buffer)) {
                        next = new EndState();
                    } else {
                        logger.warn("[CrcState] crc received doesn't match");
                    }
                    break;
                default:
                    assert false : "The impossible happened: the nb bytes read is not conform to the protocol";
            }
            return (next != null) ? next : new HeaderState();
        }

        private byte[] computeCrc(State.Message message) throws IOException {
            CRC16Modbus crc = new CRC16Modbus();

            for (byte b : message.getMessageBytes()) {
                crc.update((int)b);
            }
            return crc.getCrcBytes();
        }

        private boolean checkIfFirstByteMatchWithCrc(byte byteToTest) {
            logger.debug("[CrcState] received byte: {}, expected byte: {}", String.format("0x%02X", byteToTest), String.format("0x%02X", this.crcToMatch[0]));
            return byteToTest == this.crcToMatch[0];
        }

        private boolean checkIfCrcMatch(byte[] crcToTest) {
            logger.debug("[CrcState] received crc: {}, expected crc: {}", bytesToHex(crcToTest), bytesToHex(this.crcToMatch));
            return (crcToTest[0] == this.crcToMatch[0] && crcToTest[1] == this.crcToMatch[1]);
        }
    }

    /**
     *  End
     */
    public static final class EndState extends State {
        @Override
        public State decode(InputStream in) throws IOException {
            return null;
        }

        @Override
        protected void saveBuffer() {

        }
    }

    final protected static char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
        public static String bytesToHex(byte[] bytes) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xff;

            buf.append(HEX_DIGITS[v >> 4]);
            buf.append(HEX_DIGITS[v & 0xf]);
            if (i < bytes.length - 1) {
                buf.append(" ");
            }
        }
        return buf.toString();
    }
}
