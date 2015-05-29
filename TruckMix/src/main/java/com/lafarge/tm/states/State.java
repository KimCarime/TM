package com.lafarge.tm.states;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.ProgressListener;
import com.lafarge.tm.Protocol;
import com.lafarge.tm.utils.Convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public abstract class State {

    final MessageReceivedListener messageListener;
    final ProgressListener progressListener;

    final Message message;

    State(Message message, MessageReceivedListener messageListener, ProgressListener progressListener) {
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

    int getType() {
        return Convert.bytesToInt(new byte[]{this.message.typeMsb, this.message.typeLsb});
    }

    Map.Entry<String, Protocol.Spec> getSpec(int type) {
        for (Map.Entry<String, Protocol.Spec> entry : Protocol.constants.entrySet()) {
            Protocol.Spec spec = entry.getValue();
            if (spec.address == type) {
                return entry;
            }
        }
        throw new RuntimeException("Can't find a spec in the protocol for the given type");
    }
}
