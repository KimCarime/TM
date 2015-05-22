package com.lafarge.tm.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Convert {

    private final static char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    private Convert() {}

    // Int
    public static byte[] intToBytes(int value) {
        return intToBytes(value, Integer.SIZE/8);
    }

    public static byte[] intToBytes(int value, int byteCount) {
        byte[] result = new byte[byteCount];
        for (int i = 0; i < byteCount; i++) {
            result[byteCount - 1 - i] = (byte) (value >> i * 8);
        }
        return result;
    }

    public static int bytesToInt(byte[] buffer) {
        int value = 0;
        for (byte b : buffer) {
            value = (value << 8) + (b & 0xff);
        }
        return value;
    }

    // Float
    public static byte[] floatToBytes(float value) {
        return ByteBuffer.allocate(Float.SIZE/8).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
    }

    public static float bytesToFloat(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getFloat();
    }

    // Double
    public static byte[] doubleToBytes(double value) {
        return ByteBuffer.allocate(Double.SIZE/8).order(ByteOrder.LITTLE_ENDIAN).putDouble(value).array();
    }

    public static double bytesToDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    // Hexa string
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
