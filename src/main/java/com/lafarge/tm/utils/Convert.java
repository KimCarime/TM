package com.lafarge.tm.utils;

import java.nio.ByteBuffer;

public class Convert {

    private final static char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    private Convert() {}

    public static int bytesToInt(byte[] buffer) {
        int value = 0;
        
        for (byte b : buffer) {
            value = (value << 8) + (b & 0xff);
        }
        return value;
    }

    public static float bytesToFloat(byte[] buffer) {
        return ByteBuffer.wrap(buffer).getFloat();
    }

    public static byte[] intToBytes(int i) {
        return ByteBuffer.allocate(4).putShort((short) i).array();
    }

    public static String byteToHex(byte b) {
        int v = b & 0xff;

        return new String(new char[]{ HEX_DIGITS[v >> 4], HEX_DIGITS[v & 0xf] });
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xff;

            buf.append(HEX_DIGITS[v >> 4]);
            buf.append(HEX_DIGITS[v & 0xf]);
            if (i < bytes.length - 1) {
                buf.append(" ");
            }
        }
        return buf.toString();
    }
}
