package com.lafarge.truckmix.actions;

import com.lafarge.truckmix.MessageReceivedListener;
import com.lafarge.truckmix.Protocol;
import com.lafarge.truckmix.utils.Convert;

public class InternData extends ReadAction {
    public InternData(MessageReceivedListener listener) {
        super(listener);
    }

    @Override
    public void decode(byte[] data) {
        // Check data length
        checkIfDataLengthIsValid(data.length, Protocol.TRAME_DONNEES_INTERNES);

        // Extract parameters
        byte inSensorConnectedByte = data[0];
        byte outSensorConnectedByte = data[1];
        byte speedTooLowByte = data[2];
        byte speedTooHighByte = data[3];
        byte commandEP1ActivatedByte = data[4];
        byte commandVA1ActivatedByte = data[5];

        // Check types
        checkIfBooleanByteIsValid(inSensorConnectedByte, "L'octet correspondant à la donnée etat connexion capteur Pe dans la trame donnees internes est d une valeur invalide : " + Convert.byteToHex(inSensorConnectedByte));
        checkIfBooleanByteIsValid(outSensorConnectedByte, "L'octet correspondant à la donnée etat connexion capteur Ps dans la trame donnees internes est d une valeur invalide : " + Convert.byteToHex(outSensorConnectedByte));
        checkIfBooleanByteIsValid(speedTooLowByte, "L'octet correspondant à la donnée etat vitesse limite basse dans la trame donnees internes est d une valeur invalide : " + Convert.byteToHex(speedTooLowByte));
        checkIfBooleanByteIsValid(speedTooHighByte, "L'octet correspondant à la donnée etat vitesse limite haute dans la trame donnees internes est d une valeur invalide : " + Convert.byteToHex(speedTooHighByte));
        checkIfBooleanByteIsValid(commandEP1ActivatedByte, "L'octet correspondant à la donnée etat commande EP1 dans la trame donnees internes est d une valeur invalide : " + Convert.byteToHex(commandEP1ActivatedByte));
        checkIfBooleanByteIsValid(commandVA1ActivatedByte, "L'octet correspondant à la donnée etat commande VA1 dans la trame donnees internes est d une valeur invalide : " + Convert.byteToHex(commandVA1ActivatedByte));

        // Decode parameters
        boolean inSensorConnected = (data[0] == 0x00);
        boolean outSensorConnected = (data[1] == 0x00);
        boolean speedTooLow = (data[2] != 0x00);
        boolean speedTooHigh = (data[3] != 0x00);
        boolean commandEP1Activated = (data[4] == 0x00);
        boolean commandVA1Activated = (data[5] == 0x00);

        // Inform listener
        if (listener != null) {
            listener.internData(inSensorConnected, outSensorConnected, speedTooLow, speedTooHigh, commandEP1Activated, commandVA1Activated);
        }
    }
}
