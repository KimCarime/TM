package com.lafarge.truckmix.utils;

/**
 * Just a helper to perform tests
 */
public class MessageReceivedFactory {
    public static byte[] createTruckParametersRequestMessage() {
        return new byte[]{(byte) 0xC0, 0x01, 0x50, 0x02, 0x00, 0x00, (byte) 0x9C, 0x1B};
    }

    public static byte[] createDeliveryParametersRequestMessage() {
        return new byte[]{(byte) 0xC0, 0x01, 0x50, 0x03, 0x00, 0x00, (byte) 0xCD, (byte) 0xDB};
    }

    public static byte[] createWaterAdditionRequestMessage() {
        return new byte[]{(byte) 0xC0, 0x01, 0x50, 0x01, 0x00, 0x01, 0x0B, 0x5B, 0x7A};
    }

    public static byte[] createSlumpUpdatedMessage() {
        return new byte[]{(byte) 0xC0, 0x01, 0x10, 0x01, 0x00, 0x02, 0x00, (byte) 0xEE, 0x42, 0x47};
    }
}
