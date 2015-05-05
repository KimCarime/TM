package com.lafarge.tm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Decoder {

    public static final int HEADER = 0xC0;
    public static final int VERSION = 0x01;

    private final MessageReceivedListener listener;

    public Decoder(MessageReceivedListener listener) {
        this.listener = listener;
        this.state = new HeaderState();
    }

    public void decode(byte[] in) throws IOException {
        this.state = this.state.decode(new ByteArrayInputStream(in));
    }


    private State state;

    public interface State {
        public State decode(InputStream in) throws IOException;
    }

    public static class HeaderState implements State {

        @Override
        public State decode(InputStream in) throws IOException {
            int read = in.read();
            if (read == -1) {
                return this;
            } else if (read == HEADER) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                buffer.write(read);
                State next = new VersionState(buffer);
                return next.decode(in);
            } else {
                return this.decode(in);
            }
        }
    }

    public static class VersionState implements State {
        final ByteArrayOutputStream buffer;

        public VersionState(ByteArrayOutputStream buffer) {
            this.buffer = buffer;
        }

        @Override
        public State decode(InputStream in) throws IOException {
            int read = in.read();
            if (read == -1) {
                return this;
            } else if (read == VERSION) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                buffer.write(read);
                State next = new VersionState(buffer);
                return next.decode(in);
            } else {
                return null;
            }
        }
    }

}
