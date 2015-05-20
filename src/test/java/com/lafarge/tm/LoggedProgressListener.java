package com.lafarge.tm;

import com.lafarge.tm.utils.Convert;

public class LoggedProgressListener implements ProgressListener {
    @Override
    public void willDecode(byte[] buff) {
        System.out.println("  will decode: " + Convert.bytesToHex(buff));
    }

    public void willProcessByte(State state, byte b) {
        System.out.println("    " + state.toString() + " will process byte: " + Convert.byteToHex(b));
    }

    @Override
    public void parsingFailed(ParsingError errorType, byte b) {
        System.out.println("      parsing failed: " + errorType.toString() + " because of byte " + Convert.byteToHex(b));
    }

    @Override
    public void processingFailed(ProcessError errorType) {
        System.out.println("      processing failed: " + errorType.toString());
    }

    @Override
    public void timeout() {
        System.out.println("      previous state has expired -> reset states to Header");
    }
}
