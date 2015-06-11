package com.lafarge.truckmix.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Utility class that help to convert primitives to bytes and vice versa.
 */
public class Convert {

    private final static char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    private Convert() {}

    /**
     * Convert an int into an array of bytes
     *
     * @param value The value to convert
     * @return The array of bytes
     */
    public static byte[] intToBytes(int value) {
        return intToBytes(value, Integer.SIZE/8);
    }

    /**
     * Convert an int into an array of bytes for given length.
     *
     * @param value The value to convert
     * @param byteCount The size of the returned array
     * @return The array of bytes
     */
    public static byte[] intToBytes(int value, int byteCount) {
        byte[] result = new byte[byteCount];
        for (int i = 0; i < byteCount; i++) {
            result[byteCount - 1 - i] = (byte) (value >> i * 8);
        }
        return result;
    }

    /**
     * Convert an array of bytes into an int.
     *
     * @param bytes The array of bytes to convert
     * @return The result
     */
    public static int bytesToInt(byte[] bytes) {
        int value = 0;
        for (byte b : bytes) {
            value = (value << 8) + (b & 0xff);
        }
        return value;
    }

    /**
     * Convert a float to an array of bytes.
     *
     * @param value The value to convert
     * @return The array of bytes
     */
    public static byte[] floatToBytes(float value) {
        return ByteBuffer.allocate(Float.SIZE/8).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
    }

    /**
     * Convert an array of bytes into a float.
     *
     * @param bytes The array of bytes to convert
     * @return The result
     */
    public static float bytesToFloat(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getFloat();
    }

    /**
     * Convert a double to an array of bytes.
     *
     * @param value The value to convert
     * @return The array of bytes
     */
    public static byte[] doubleToBytes(double value) {
        return ByteBuffer.allocate(Double.SIZE/8).order(ByteOrder.LITTLE_ENDIAN).putDouble(value).array();
    }

    /**
     * Convert an array of bytes into a double.
     *
     * @param bytes The array of bytes to convert
     * @return The result
     */
    public static double bytesToDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    /**
     * Convert a byte into a String representation.
     *
     * @param b The byte
     * @return The string representation
     */
    public static String byteToHex(byte b) {
        int v = b & 0xff;

        return new String(new char[]{ HEX_DIGITS[v >> 4], HEX_DIGITS[v & 0xf] });
    }

    /**
     * Convert an array of bytes into a String representation.
     *
     * @param bytes The array of bytes
     * @return The string representation
     */
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
