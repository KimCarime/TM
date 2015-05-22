package com.lafarge.tm;

import com.lafarge.tm.utils.CRC16Modbus;
import com.lafarge.tm.utils.Convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Encoder {

    public Encoder() {
    }

    public byte[] targetSlump(int value) {
        return encode(Protocol.TRAME_SLUMP_CIBLE, value);
    }

    public byte[] maximumWater(int value) {
        return encode(Protocol.TRAME_VOLUME_EAU_MAXIMUM, value);
    }

    public byte[] waterAdditionPermission(boolean isAllowed) {
        return encode(Protocol.TRAME_AUTORISATION_REFUS_AJOUT_EAU, (byte) (!isAllowed ? 0x00 : 0xFF));
    }

    public byte[] displayActivated(boolean isActivated) {
        return encode(Protocol.TRAME_ACTIVATION_INHIBITION_AFFICHEUR, (byte) (!isActivated ? 0x00 : 0xFF));
    }

    public byte[] endOfDelivery() {
        return encode(Protocol.TRAME_NOTIFICATION_FIN_DECHARGEMENT);
    }

    public byte[] beginningOfDelivery() {
        return encode(Protocol.TRAME_NOTIFICATION_ACCEPTATION_COMMANDE);
    }

    public byte[] loadVolume(double value) {
        return encode(Protocol.TRAME_VOLUME_CHARGE, value);
    }

    public byte[] parameterT1(double value) {
        return encode(Protocol.TRAME_PARAMETRE_T1, value);
    }

    public byte[] parameterA11(double value) {
        return encode(Protocol.TRAME_PARAMETRE_A11, value);
    }

    public byte[] parameterA12(double value) {
        return encode(Protocol.TRAME_PARAMETRE_A12, value);
    }

    public byte[] parameterA13(double value) {
        return encode(Protocol.TRAME_PARAMETRE_A13, value);
    }

    public byte[] magnetQuantity(int value) {
        return encode(Protocol.TRAME_PARAMETRE_NOMBRE_D_AIMANTS, value);
    }

    public byte[] timePump(int value) {
        return encode(Protocol.TRAME_PARAMETRE_TEMPS_AVANT_COULANT, value);
    }

    public byte[] timeDelayDriver(int value) {
        return encode(Protocol.TRAME_PARAMETRE_TEMPO_ATTENTE_REPONSE_CONDUCTEUR, value);
    }

    public byte[] pulseNumber(int value) {
        return encode(Protocol.TRAME_NOMBRE_D_IMPULSIONS_PAR_LITRE, value);
    }

    public byte[] debimeterFrequency(int value) {
        return encode(Protocol.TRAME_FREQUENCE_DEBITMETRE, value);
    }

    public byte[] commandPumpMode(TruckParameters.CommandPumpMode commandPumpMode) {
        return encode(Protocol.TRAME_MODE_DE_COMMANDE_POMPE, (byte) (commandPumpMode == TruckParameters.CommandPumpMode.SEMI_AUTO ? 0x00 : 0xFF));
    }

    public byte[] calibrationInputSensorA(double value) {
        return encode(Protocol.TRAME_FACTEUR_A_CAPTEUR_PRESSION_ENTREE, value);
    }

    public byte[] calibrationOutputSensorA(double value) {
        return encode(Protocol.TRAME_FACTEUR_A_CAPTEUR_PRESSION_SORTIE, value);
    }

    public byte[] calibrationInputSensorB(double value) {
        return encode(Protocol.TRAME_FACTEUR_B_CAPTEUR_PRESSION_ENTREE, value);
    }

    public byte[] calibrationOutputSensorB(double value) {
        return encode(Protocol.TRAME_FACTEUR_B_CAPTEUR_PRESSION_SORTIE, value);
    }

    public byte[] openingTimeEV1(int value) {
        return encode(Protocol.TRAME_DUREE_ATTENTE_OUVERTURE_EV1_VA1, value);
    }

    public byte[] openingTimeVA1(int value) {
        return encode(Protocol.TRAME_DUREE_ATTENTE_FERMETURE_VA1_EV1, value);
    }

    public byte[] countingTolerance(int value) {
        return encode(Protocol.TRAME_TOLERANCE_DE_COMPTAGE, value);
    }

    public byte[] waitingDurationAfterWaterAddition(int value) {
        return encode(Protocol.TRAME_DUREE_ATTENTE_APRES_AJOUT_EAU, value);
    }

    public byte[] maxDelayBeforeFlowage(int value) {
        return encode(Protocol.TRAME_DELAI_MAXIMUM_AVANT_ECOULEMENT, value);
    }

    public byte[] maxFlowageError(int value) {
        return encode(Protocol.TRAME_NOMBRE_MAX_ERREURS_ECOULEMENT, value);
    }

    public byte[] maxCountingError(int value) {
        return encode(Protocol.TRAME_NOMBRE_MAX_ERREURS_COMPTAGE, value);
    }
    
    private byte[] encode(String type) {
        return encode(type, null);
    }

    private byte[] encode(String type, int value) {
        return encode(type, Convert.intToBytes(value, Protocol.constants.get(type).size));
    }

    private byte[] encode(String type, double value) {
        return encode(type, Convert.doubleToBytes(value));
    }

    private byte[] encode(String type, byte dataByte) {
        return encode(type, new byte[]{dataByte});
    }

    private byte[] encode(String type, byte[] dataBytes) {
        Protocol.Spec spec = Protocol.constants.get(type);

        System.out.println("Will encode: " + type + ((dataBytes != null) ? " with bytes: " + Convert.bytesToHex(dataBytes) : ""));

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
            // TODO: The test should be `dataBytes.length != spec.size` and not `<`
            if (dataBytes.length != spec.size) {
                throw new RuntimeException("The size of dataBytes doesn't match with the spec of " + type + ", given: " + dataBytes.length + ", expected: " + spec.size);
            }
            result.write(dataBytes, 0, spec.size);
        }

        CRC16Modbus crc = new CRC16Modbus();
        crc.update(result.toByteArray(), 0, result.size());
        try {
            result.write(crc.getCrcBytes());
            System.out.println("Encoding result: " + Convert.bytesToHex(result.toByteArray()));
            return result.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
