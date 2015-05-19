package com.lafarge.tm;

public class TruckParametersRequest extends MessageType {
    public TruckParametersRequest(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        if (data != null) {
            checkIfDataLengthIsValid(data.length, Protocol.TRAME_DEMANDE_PARAMETRES_STATIQUES);
        }

        // Inform listener
        if (listener != null) {
            listener.truckParametersRequest();
        }
    }
}
