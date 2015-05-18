package com.lafarge.tm;

import com.lafarge.tm.utils.CRC16Modbus;

import java.io.IOException;
import java.io.InputStream;

import static com.lafarge.tm.utils.Convert.bytesToHex;

public final class CrcState extends State {
    public static final int CRC_NB_BYTES = 2;

    private byte[] crcToMatch;

    private final byte[] buffer = new byte[CRC_NB_BYTES];
    private int totalRead = 0;

    public CrcState(Message message, MessageReceivedListener messageListener, ProgressListener progressListener) throws IOException {
        super(message, messageListener, progressListener);
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
                } else {
                    logger.warn("[CrcState] first byte received of crc doesn't match");
                }
                break;
            case CRC_NB_BYTES:
                if (checkIfCrcMatch(this.buffer)) {
                    switch (getType()) {
                        case Protocol.TRAME_SLUMP_COURANT:
                            new SlumpUpdated(messageListener).decode(message.data);
                            break;
                        default:
                            throw new RuntimeException("Unknow buffer type " + getType());
                    }
                    next = new HeaderState(messageListener, progressListener);
                } else {
                    logger.warn("[CrcState] crc received doesn't match");
                }
                break;
            default:
                throw new RuntimeException("The impossible happened: the nb bytes read is not conform to the protocol");
        }
        return (next != null) ? next : new HeaderState(messageListener, progressListener);
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
