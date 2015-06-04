package com.lafarge.truckmix.decoder.listeners;

/**
 *  Implement this interface to obtain processing information from the Decoder
 */
public interface ProgressListener {
    enum ProgressState {
        STATE_HEADER,
        STATE_VERSION,
        STATE_TYPE,
        STATE_SIZE,
        STATE_DATA,
        STATE_CRC
    }

    enum ParsingError {
        ERROR_PARSING_HEADER,
        ERROR_PARSING_VERSION,
        ERROR_PARSING_TYPE,
        ERROR_PARSING_SIZE,
        ERROR_PARSING_DATA,
        ERROR_PARSING_DATA_BOOLEAN_TYPE,
        ERROR_PARSING_CRC,
        ERROR_PARSING_TIMEOUT
    }

    void timeout();
    void willDecode(byte[] buff);
    void willProcessByte(ProgressState state,  byte b);
    void parsingFailed(ParsingError errorType, byte b);
}
