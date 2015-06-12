package com.lafarge.truckmix.decoder.listeners;

/**
 * This interface is used to have processing information from the Decoder.
 */
public interface ProgressListener {
    /** List of state of the Decoder */
    enum ProgressState {
        STATE_HEADER,
        STATE_VERSION,
        STATE_TYPE,
        STATE_SIZE,
        STATE_DATA,
        STATE_CRC
    }

    /** List of possible error type */
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

    /**
     * Triggered when the Decoder received the second part of a message after a delay.
     * @see com.lafarge.truckmix.decoder.Decoder.STATE_EXPIRATION_DELAY_MILLIS
     */
    void timeout();

    /**
     * Triggered before the Decoder process given bytes
     *
     * @param buff The buffer that will be decoded
     */
    void willDecode(byte[] buff);

    /**
     * Inform about the byte that will be process in the current state of the Decoder. Useful for development.
     *
     * @param state The current state of the Decoder
     * @param b The byte that will be process
     */
    void willProcessByte(ProgressState state,  byte b);

    /**
     * Inform there was an error while processing bytes in the current state of the Decoder. Useful for development.
     *
     * @param errorType The type of error
     * @param b The byte responsible of the error
     */
    void parsingFailed(ParsingError errorType, byte b);
}
