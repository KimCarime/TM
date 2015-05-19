package com.lafarge.tm.states;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.lafarge.tm.utils.Convert.buffToInt;

public abstract class State {

    protected Logger logger;

    protected final MessageReceivedListener messageListener;
    protected final ProgressListener progressListener;

    protected Message message;
    private long lastDecode;

    public State(Message message, MessageReceivedListener messageListener, ProgressListener progressListener) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.message = message;
        this.messageListener = messageListener;
        this.progressListener = progressListener;
    }

    public abstract State decode(InputStream in) throws IOException;

    protected abstract void saveBuffer();

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

    public int getType() {
        return buffToInt(new byte[]{this.message.typeMsb, this.message.typeLsb});
    }

}
