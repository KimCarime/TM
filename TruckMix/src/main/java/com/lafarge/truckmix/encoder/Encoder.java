package com.lafarge.truckmix.encoder;

import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.encoder.listeners.MessageSentListener;
import com.lafarge.truckmix.utils.CRC16Modbus;
import com.lafarge.truckmix.utils.Convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The Encoder part of the library, this class is responsible of encoding messages to send to the Wirma.
 */
public class Encoder {

    private MessageSentListener messageSentListener;

    /**
     * Constructor
     *
     * @param messageSentListener Listener about message actually encoded
     * @throws IllegalArgumentException If messageSentListener is null
     */
    public Encoder(MessageSentListener messageSentListener) {
        if (messageSentListener == null) throw new IllegalArgumentException("messageSentListener can't be null");
        this.messageSentListener = messageSentListener;
    }

    public byte[] targetSlump(int value) {
        byte[] result = encode(Protocol.TRAME_SLUMP_CIBLE, value);
        messageSentListener.targetSlump(value, result);
        return result;
    }

    public byte[] maximumWater(int value) {
        byte[] result = encode(Protocol.TRAME_VOLUME_EAU_MAXIMUM, value);
        messageSentListener.maximumWater(value, result);
        return result;
    }

    public byte[] waterAdditionPermission(boolean isAllowed) {
        byte[] result = encode(Protocol.TRAME_AUTORISATION_REFUS_AJOUT_EAU, (byte) (!isAllowed ? 0x00 : 0xFF));
        messageSentListener.waterAdditionPermission(isAllowed, result);
        return result;
    }

    public byte[] changeExternalDisplayState(boolean isActivated) {
        byte[] result = encode(Protocol.TRAME_ACTIVATION_INHIBITION_AFFICHEUR, (byte) (!isActivated ? 0x00 : 0xFF));
        messageSentListener.changeExternalDisplayState(isActivated, result);
        return result;
    }

    public byte[] endOfDelivery() {
        byte[] result = encode(Protocol.TRAME_NOTIFICATION_FIN_DECHARGEMENT);
        messageSentListener.endOfDelivery(result);
        return result;
    }

    public byte[] beginningOfDelivery() {
        byte[] result = encode(Protocol.TRAME_NOTIFICATION_ACCEPTATION_COMMANDE);
        messageSentListener.beginningOfDelivery(result);
        return result;
    }

    public byte[] loadVolume(double value) {
        byte[] result = encode(Protocol.TRAME_VOLUME_CHARGE, (value));
        messageSentListener.loadVolume(value, result);
        return result;
    }

    public byte[] parameterT1(double value) {
        byte[] result = encode(Protocol.TRAME_PARAMETRE_T1, (value));
        messageSentListener.parameterT1(value, result);
        return result;
    }

    public byte[] parameterA11(double value) {
        byte[] result = encode(Protocol.TRAME_PARAMETRE_A11, (value));
        messageSentListener.parameterA11(value, result);
        return result;
    }

    public byte[] parameterA12(double value) {
        byte[] result = encode(Protocol.TRAME_PARAMETRE_A12, (value));
        messageSentListener.parameterA12(value, result);
        return result;
    }

    public byte[] parameterA13(double value) {
        byte[] result = encode(Protocol.TRAME_PARAMETRE_A13, (value));
        messageSentListener.parameterA13(value, result);
        return result;
    }

    public byte[] magnetQuantity(int value) {
        byte[] result = encode(Protocol.TRAME_PARAMETRE_NOMBRE_D_AIMANTS, (value));
        messageSentListener.magnetQuantity(value, result);
        return result;
    }

    public byte[] timePump(int value) {
        byte[] result = encode(Protocol.TRAME_PARAMETRE_TEMPS_AVANT_COULANT, (value));
        messageSentListener.timePump(value, result);
        return result;
    }

    public byte[] timeDelayDriver(int value) {
        byte[] result = encode(Protocol.TRAME_PARAMETRE_TEMPO_ATTENTE_REPONSE_CONDUCTEUR, (value));
        messageSentListener.timeDelayDriver(value, result);
        return result;
    }

    public byte[] pulseNumber(int value) {
        byte[] result = encode(Protocol.TRAME_NOMBRE_D_IMPULSIONS_PAR_LITRE, (value));
        messageSentListener.pulseNumber(value, result);
        return result;
    }

    public byte[] flowmeterFrequency(int value) {
        byte[] result = encode(Protocol.TRAME_FREQUENCE_DEBITMETRE, (value));
        messageSentListener.flowmeterFrequency(value, result);
        return result;
    }

    public byte[] commandPumpMode(TruckParameters.CommandPumpMode commandPumpMode) {
        byte[] result = encode(Protocol.TRAME_MODE_DE_COMMANDE_POMPE, (byte) (commandPumpMode == TruckParameters.CommandPumpMode.SEMI_AUTO ? 0x00 : 0xFF));
        messageSentListener.commandPumpMode(commandPumpMode, result);
        return result;
    }

    public byte[] calibrationInputSensorA(double value) {
        byte[] result = encode(Protocol.TRAME_FACTEUR_A_CAPTEUR_PRESSION_ENTREE, (value));
        messageSentListener.calibrationInputSensorA(value, result);
        return result;
    }

    public byte[] calibrationOutputSensorA(double value) {
        byte[] result = encode(Protocol.TRAME_FACTEUR_A_CAPTEUR_PRESSION_SORTIE, (value));
        messageSentListener.calibrationOutputSensorA(value, result);
        return result;
    }

    public byte[] calibrationInputSensorB(double value) {
        byte[] result = encode(Protocol.TRAME_FACTEUR_B_CAPTEUR_PRESSION_ENTREE, (value));
        messageSentListener.calibrationInputSensorB(value, result);
        return result;
    }

    public byte[] calibrationOutputSensorB(double value) {
        byte[] result = encode(Protocol.TRAME_FACTEUR_B_CAPTEUR_PRESSION_SORTIE, (value));
        messageSentListener.calibrationOutputSensorB(value, result);
        return result;
    }

    public byte[] openingTimeEV1(int value) {
        byte[] result = encode(Protocol.TRAME_DUREE_ATTENTE_OUVERTURE_EV1_VA1, (value));
        messageSentListener.openingTimeEV1(value, result);
        return result;
    }

    public byte[] openingTimeVA1(int value) {
        byte[] result = encode(Protocol.TRAME_DUREE_ATTENTE_FERMETURE_VA1_EV1, (value));
        messageSentListener.openingTimeVA1(value, result);
        return result;
    }

    public byte[] countingTolerance(int value) {
        byte[] result = encode(Protocol.TRAME_TOLERANCE_DE_COMPTAGE, (value));
        messageSentListener.countingTolerance(value, result);
        return result;
    }

    public byte[] waitingDurationAfterWaterAddition(int value) {
        byte[] result = encode(Protocol.TRAME_DUREE_ATTENTE_APRES_AJOUT_EAU, (value));
        messageSentListener.waitingDurationAfterWaterAddition(value, result);
        return result;
    }

    public byte[] maxDelayBeforeFlowage(int value) {
        byte[] result = encode(Protocol.TRAME_DELAI_MAXIMUM_AVANT_ECOULEMENT, (value));
        messageSentListener.maxDelayBeforeFlowage(value, result);
        return result;
    }

    public byte[] maxFlowageError(int value) {
        byte[] result = encode(Protocol.TRAME_NOMBRE_MAX_ERREURS_ECOULEMENT, (value));
        messageSentListener.maxFlowageError(value, result);
        return result;
    }

    public byte[] maxCountingError(int value) {
        byte[] result = encode(Protocol.TRAME_NOMBRE_MAX_ERREURS_COMPTAGE, (value));
        messageSentListener.maxCountingError(value, result);
        return result;
    }

    public byte[] fake() {
        byte[] result = encode(Protocol.TRAME_BIDON, new byte[]{(byte)0xDE, (byte)0xAD, (byte)0xDE, (byte)0xAD,
                (byte)0xDE, (byte)0xAD, (byte)0xDE, (byte)0xAD});
        messageSentListener.fake(result);
        return result;
    }

    /** @see Encoder#encode(String, byte[]) */
    private byte[] encode(String type) {
        return encode(type, null);
    }

    /** @see Encoder#encode(String, byte[]) */
    private byte[] encode(String type, int value) {
        return encode(type, Convert.intToBytes(value, Protocol.constants.get(type).size));
    }

    /** @see Encoder#encode(String, byte[]) */
    private byte[] encode(String type, double value) {
        return encode(type, Convert.doubleToBytes(value));
    }

    /** @see Encoder#encode(String, byte[]) */
    private byte[] encode(String type, byte dataByte) {
        return encode(type, new byte[]{dataByte});
    }

    /**
     * Encode a valid message for a given type with its data.
     *
     * @param type The type of the message
     * @param dataBytes The data bytes to encode
     * @return The message encoded to send to the Wirma.
     * @throws RuntimeException If dataBytes length isn't equal with spec size of given type
     */
    private byte[] encode(String type, byte[] dataBytes) {
        Protocol.Spec spec = Protocol.constants.get(type);

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write(Protocol.HEADER);
        result.write(Protocol.VERSION);

        byte[] typeBytes = Convert.intToBytes(spec.address, 2);
        result.write(typeBytes[0]);
        result.write(typeBytes[1]);

        byte[] sizeBytes = Convert.intToBytes(spec.size, 2);
        result.write(sizeBytes[0]);
        result.write(sizeBytes[1]);

        if (spec.size > 0) {
            if (dataBytes.length != spec.size) {
                throw new RuntimeException("The size of dataBytes doesn't match with the spec of " + type + ", given: " + dataBytes.length + ", expected: " + spec.size);
            }
            result.write(dataBytes, 0, spec.size);
        }

        CRC16Modbus crc = new CRC16Modbus();
        crc.update(result.toByteArray(), 0, result.size());
        try {
            result.write(crc.getCrcBytes());
            return result.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
