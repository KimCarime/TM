package com.lafarge.tm.utils;

import java.nio.ByteBuffer;

public class Convert {

    private Convert() {
    }

    public static int buffToInt(byte[] buffer) {
        return (int) ByteBuffer
                .wrap(buffer)
                .getShort();
    }

    public static byte[] intToBuff(int i) {
        return ByteBuffer
                .allocate(4)
                .putShort((short) i) // Hack: message are only with two bytes
                .array();
    }

    final protected static char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

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
