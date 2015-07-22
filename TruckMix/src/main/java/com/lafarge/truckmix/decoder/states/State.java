package com.lafarge.truckmix.decoder.states;

import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.ProgressListener;
import com.lafarge.truckmix.utils.Convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Abstract class of the State.
 */
public abstract class State {

    // Listeners
    final MessageReceivedListener messageListener;
    final ProgressListener progressListener;

    // Message decoded during process
    final Message message;

    /**
     * Construct a State
     *
     * @param message The current message state, this message should be pass at each step.
     * @param messageListener The listener that will be called when a valid message is decoded.
     * @param progressListener The listener to follow progression of the parsing
     */
    State(Message message, MessageReceivedListener messageListener, ProgressListener progressListener) {
        this.message = message;
        this.messageListener = messageListener;
        this.progressListener = progressListener;
    }

    /**
     * Subclasses should implement to decode bytes. If, according to its need, bytes is valid then subclasses should
     * return next State, or initial state to reset parser.
     *
     * @param in The bytes to process
     * @return The next state
     * @throws IOException
     */
    public abstract State decode(InputStream in) throws IOException;

    /**
     * Subclass should call this method to save the current buffer for crc calculation.
     */
    protected abstract void saveBuffer();

    /**
     * During process, this object is passed at each step to follow progression. At the end, we use it to compute the
     * crc to valid the message.
     */
    public static final class Message {
        public byte header;
        public byte version;
        public byte typeMsb;
        public byte typeLsb;
        public byte sizeMsb;
        public byte sizeLsb;
        public byte[] data;

        /** Return the complete buffer of the messages */
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

    /** Return the message identifier to treat some specific case */
    int getType() {
        return Convert.bytesToInt(new byte[]{this.message.typeMsb, this.message.typeLsb});
    }

    /**
     * Return the specification of a message
     *
     * @param type The message identifier
     * @return The specification of the given identifier
     */
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
