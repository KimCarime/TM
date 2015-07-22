package com.lafarge.truckmix.decoder.listeners;

import com.lafarge.truckmix.utils.Convert;

public class LoggedProgressListener implements ProgressListener {
    @Override
    public void willDecode(byte[] buff) {
        System.out.println("  will decode: " + Convert.bytesToHex(buff));
    }

    public void willProcessByte(ProgressState state, byte b) {
        System.out.println("    " + state.toString() + " will process byte: " + Convert.byteToHex(b));
    }

    @Override
    public void parsingFailed(ParsingError errorType, byte b) {
        System.out.println("      parsing failed: " + errorType.toString() + " because of byte " + Convert.byteToHex(b));
    }

    @Override
    public void timeout() {
        System.out.println("      previous state has expired -> reset states to Header");
    }
}
