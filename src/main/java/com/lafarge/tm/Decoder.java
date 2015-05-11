package com.lafarge.tm;

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

    public Decoder(MessageReceivedListener listener) {
        this.listener = listener;
        this.state = new HeaderState();
    }

    public void decode(byte[] in) throws IOException {
        this.resetStateIfLastWasExpired();
        this.state = this.state.decode(new ByteArrayInputStream(in));
    }

    private void resetStateIfLastWasExpired() {
        Date now = new Date();

        if (this.lastDecodeDate != null) {
            long diffInSec = (now.getTime() - this.lastDecodeDate.getTime()) / 1000;

            if (diffInSec > STATE_EXPIRATION_DELAY_IN_SEC) {
                this.state = new HeaderState();
            }
        }
        this.lastDecodeDate = now;
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
                    return this;
                case Protocol.HEADER:
                    saveBuffer();
                    return new VersionState(this.message)
                            .decode(in);
                default:
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
                    return this;
                case Protocol.VERSION:
                    saveBuffer();
                    return new TypeState(this.message)
                            .decode(in);
                default:
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

        private byte[] buffer = new byte[TYPE_NB_BYTES];
        private int totalRead = 0;

        public TypeState(Message message) {
            super(message);
        }

        @Override
        public State decode(InputStream in) throws IOException {
            int read = in.read(this.buffer, this.totalRead, TYPE_NB_BYTES - this.totalRead);

            switch (read) {
                case -1:
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
                        next = this;
                    }
                    break;
                case TYPE_NB_BYTES:
                    int messageTypeFound = buffToInt(this.buffer);

                    if (checkIfTypeMessageExist(messageTypeFound)) {
                        saveBuffer();
                        next = new SizeState(messageTypeFound, this.message)
                                .decode(in);
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

                if (firstByteToTest == firstByteToMatch) {
                    return true;
                }
            }
            return false;
        }

        private boolean checkIfTypeMessageExist(int messageTypeToTest) {
            for (Map.Entry<Integer, Protocol.Pair> entry : Protocol.constants.entrySet()) {
                int typeMessageToMatch = entry.getKey();

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

        private int messageType;

        private byte[] buffer = new byte[SIZE_NB_BYTES];
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
                    if (checkIfFirstByteMatchForGivenState(this.buffer[0], this.messageType)) {
                        next = this;
                    }
                    break;
                case SIZE_NB_BYTES:
                    int sizeFound = buffToInt(this.buffer);

                    if (checkIfSizeMatchForGivenState(sizeFound, this.messageType)) {
                        saveBuffer();
                        if (sizeFound > 0) {
                            next = new DataState(sizeFound, null)
                                    .decode(in);
                        } else {
                            next = new CrcState(this.message)
                                    .decode(in);
                        }
                    }
                    break;
                default:
                    assert false : "The impossible happened: the nb bytes read is not conform to the protocol";
            }
            return (next != null) ? next : new HeaderState();
        }

        private boolean checkIfFirstByteMatchForGivenState(byte firstByteToTest, int stateToMatch) {
            Protocol.Pair pair = Protocol.constants.get(stateToMatch);

            if (pair != null) {
                byte firstByteToMatch = intToBuff(pair.size)[0];
                return firstByteToTest == firstByteToMatch;
            } else {
                assert true : "The impossible happened: The state wasn't recognize";
                return false;
            }
        }

        private boolean checkIfSizeMatchForGivenState(int sizeToTest, int stateToMatch) {
            Protocol.Pair pair = Protocol.constants.get(stateToMatch);

            if (pair != null) {
                int sizeToMatch = pair.size;
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
        private byte[] buffer;
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
                return this;
            } else {
                this.totalRead += read;
                if (this.totalRead == this.expectedSize) {
                    saveBuffer();
                    return new CrcState(this.message)
                            .decode(in);
                } else {
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

        private byte[] buffer = new byte[CRC_NB_BYTES];
        private int totalRead = 0;

        public CrcState(Message message) throws IOException {
            super(message);
            this.crcToMatch = computeCrc(message);
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
                    }
                    break;
                case CRC_NB_BYTES:
                    if (checkIfCrcMatch(this.buffer)) {
                        next = new EndState();
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
            return byteToTest == this.crcToMatch[0];
        }

        private boolean checkIfCrcMatch(byte[] crcToTest) {
            return (crcToTest[0] == this.crcToMatch[0] &&
                    crcToTest[1] == this.crcToMatch[1]);
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
}
