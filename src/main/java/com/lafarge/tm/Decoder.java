package com.lafarge.tm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

        protected class Message {
            public int header = -1;
            public int version = -1;
            public int typeMsb = -1;
            public int typeLsb = -1;
            public int dataMsb = -1;
            public int dataLsb = -1;
            public int[] data = null;
            public int crcLsb = -1;
            public int crcMsb = -1;
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
                    if (this.message != null) {
                        this.message.header = read;
                    }
                    return new VersionState(this.message)
                            .decode(in);

                default:
                    return this.decode(in);
            }

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
                    if (this.message != null) {
                        this.message.version = read;
                    }
                    return new TypeState(this.message)
                            .decode(in);

                default:
                    return new HeaderState();
            }
        }
    }

    /**
     *  Type
     */
    public static final class TypeState extends State {
        public static final int TYPE_SIZE = 2;

        private byte[] mBuffer = new byte[TYPE_SIZE];
        private int mTotalReaded = 0;

        public TypeState(Message message) {
            super(message);
        }

        @Override
        public State decode(InputStream in) throws IOException {
            int read = in.read(mBuffer, mTotalReaded, TYPE_SIZE - mTotalReaded);

            switch (read) {
                case -1:
                    return this;

                default:
                    mTotalReaded += read;

                    if (mTotalReaded == 1) {
                        return checkIfFirstByteExist(mBuffer[0]) ? this : new HeaderState();
                    } else if (mTotalReaded == TYPE_SIZE) {
                        return checkIfMessageExist(mBuffer) ? new SizeState(null).decode(in) : new HeaderState();
                    } else {
                        assert false : "The impossible happened";
                        return null;
                    }
            }
        }

        private boolean checkIfFirstByteExist(byte firstByte) {
            for (Map.Entry<Integer, Protocol.Pair> entry : Protocol.constants.entrySet()) {
                int message = entry.getKey();
                byte[] bytes = ByteBuffer
                        .allocate(4)
                        .putShort((short)message) // Hack: message are only with two bytes
                        .array();
                byte firstByOfMessage = bytes[0];

                if (firstByte == firstByOfMessage) {
                    return true;
                }
            }
            return false;
        }

        private boolean checkIfMessageExist(byte[] bytes) {
            int messageToTest = (int)ByteBuffer.wrap(bytes).getShort();

            for (Map.Entry<Integer, Protocol.Pair> entry : Protocol.constants.entrySet()) {
                int message = entry.getKey();

                if (messageToTest == message) {
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
        public SizeState(Message message) {
            super(message);
        }

        @Override
        public State decode(InputStream in) throws IOException {
            int read = in.read();

            switch (read) {
                case -1:
                    return this;
                default:
                    if (this.message.typeMsb == -1) {
                        // TODO: Check typeMsb
                        this.message.typeMsb = read;
                        return this.decode(in);
                    } else if (this.message.typeLsb == -1) {
                        // TODO: Check typeLsb
                        this.message.typeLsb = read;
                        return new DataState(this.message)
                                .decode(in);
                    } else {
                        assert true : "The impossible happend";
                    }
                    return new HeaderState();
            }
        }
    }

    /**
     *  Data
     */
    public static final class DataState extends State {
        private int size = 0;
        private byte[] buffer;

        public DataState(Message message) {
            super(message);
        }

        @Override
        public State decode(InputStream in) throws IOException {
            int read = in.read();

            buffer = new byte[2];
            int ret = in.read(buffer, 0, this.size);

            return null;
        }
    }

    /**
     *  CRC
     */
    public static final class CrcState extends State {
        public CrcState() {

        }

        @Override
        public State decode(InputStream in) throws IOException {
            return null;
        }
    }
}
