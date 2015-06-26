package com.lafarge.truckmix.decoder.states;

import com.lafarge.truckmix.decoder.actions.*;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.ProgressListener;
import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.utils.CRC16Modbus;
import com.lafarge.truckmix.utils.Convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * The sixth and last state of a message. Will send the decoded event though the MessageReceivedListener if we pass
 * valid bytes (i.e. computed crc (thanks to Message object) match with the crc received),
 * otherwise will return HeaderState.
 */
public final class CrcState extends State {

    /** The number of bytes of the crc part of a message */
    private static final int CRC_NB_BYTES = 2;

    private byte[] crcToMatch;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    /**
     * Constructs a CrcState.
     *
     * @see State(Message, MessageReceivedListener, ProgressListener)
     */
    public CrcState(Message message, MessageReceivedListener messageListener, ProgressListener progressListener) throws IOException {
        super(message, messageListener, progressListener);
        this.crcToMatch = computeCrc(message);
    }

    @Override
    public State decode(InputStream in) throws IOException {
        int read = in.read();

        if (read == -1) {
            return this;
        } else {
            progressListener.willProcessByte(ProgressListener.ProgressState.STATE_CRC, (byte) read);
            out.write(read);
            if (!isCrcFoundMatch(out.toByteArray())) {
                progressListener.parsingFailed(ProgressListener.ParsingError.ERROR_PARSING_CRC, (byte) read);
                return new HeaderState(messageListener, progressListener).decode(in);
            }
            if (out.size() < CRC_NB_BYTES) {
                return decode(in);
            } else {
                getAction(getType()).decode(message.data);
                return new HeaderState(messageListener, progressListener).decode(in);
            }
        }
    }

    @Override
    protected void saveBuffer() {} // Nothing to do here.

    /** Helper to check if computed crc match with given crc bytes by bytes */
    private boolean isCrcFoundMatch(byte[] crcToTest) {
        for (int i = 0; i < crcToTest.length; i++) {
            if (crcToTest[i] != crcToMatch[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compute crc to valid message for a given message.
     *
     * @param message The message we want to compute the crc.
     * @return The crc
     */
    private byte[] computeCrc(State.Message message) throws IOException {
        CRC16Modbus crc = new CRC16Modbus();
        for (byte b : message.getMessageBytes()) {
            crc.update((int)b);
        }
        return crc.getCrcBytes();
    }

    /**
     * Return an action to do for a given message identifier. Should be called only if the message decoded is valid.
     *
     * @param type The message identifier
     * @return An action to perform @see ReadAction
     */
    private ReadAction getAction(int type) {
        Map.Entry<String, Protocol.Spec> entry = getSpec(type);

        String s = entry.getKey();
        if (s.equals(Protocol.TRAME_SLUMP_COURANT)) {
            return new SlumpUpdated(messageListener);
        } else if (s.equals(Protocol.TRAME_VOLUME_EAU_AJOUTE_PLUS_MODE)) {
            return new WaterAdded(messageListener);
        } else if (s.equals(Protocol.TRAME_TEMPERATURE_COURANTE)) {
            return new TemperatureUpdated(messageListener);
        } else if (s.equals(Protocol.TRAME_NOTIFICATION_PASSAGE_EN_MALAXAGE)) {
            return new MixingModeActivated(messageListener);
        } else if (s.equals(Protocol.TRAME_NOTIFICATION_PASSAGE_EN_VIDANGE)) {
            return new UnloadingModeActivated(messageListener);
        } else if (s.equals(Protocol.TRAME_NOTIFICATION_DEBUT_AJOUT_EAU)) {
            return new WaterAdditionBegan(messageListener);
        } else if (s.equals(Protocol.TRAME_NOTIFICATION_FIN_AJOUT_EAU)) {
            return new WaterAdditionEnd(messageListener);
        } else if (s.equals(Protocol.TRAME_NOTIFICATION_PARAMETRES_STATIQUES_RECUS)) {
            return new TruckParametersReceived(messageListener);
        } else if (s.equals(Protocol.TRAME_NOTIFICATION_PARAMETRES_DYNAMIQUES_RECUS)) {
            return new DeliveryParametersReceived(messageListener);
        } else if (s.equals(Protocol.TRAME_NOTIFICATION_ACCEPTATION_LIVRAISON_RECUE)) {
            return new DeliveryValidationReceived(messageListener);
        } else if (s.equals(Protocol.TRAME_NOTIFICATION_FRANCHISSEMENT_TRANSITION)) {
            return new StateChanged(messageListener);
        } else if (s.equals(Protocol.TRAME_DEMANDE_AUTORISATION_AJOUT_EAU)) {
            return new WaterAdditionRequest(messageListener);
        } else if (s.equals(Protocol.TRAME_DEMANDE_PARAMETRES_STATIQUES)) {
            return new TruckParametersRequest(messageListener);
        } else if (s.equals(Protocol.TRAME_DEMANDE_PARAMETRES_DYNAMIQUES)) {
            return new DeliveryParametersRequest(messageListener);
        } else if (s.equals(Protocol.TRAME_DEMANDE_ACCEPTATION_LIVRAISON)) {
            return new DeliveryValidationRequest(messageListener);
        } else if (s.equals(Protocol.TRAME_TRACE_DEBUG)) {
            return new TraceDebug(messageListener);
        } else if (s.equals(Protocol.TRAME_DONNEES_BRUTES)) {
            return new RawData(messageListener);
        } else if (s.equals(Protocol.TRAME_DONNEES_DERIVEES)) {
            return new DerivedData(messageListener);
        } else if (s.equals(Protocol.TRAME_DONNEES_INTERNES)) {
            return new InternData(messageListener);
        } else if (s.equals(Protocol.TRAME_DONNEES_CALIBRATION)) {
            return new CalibrationData(messageListener);
        } else if (s.equals(Protocol.TRAME_NOTIFICATION_ERREUR_EAU_MAX)) {
            return new AlarmWaterMax(messageListener);
        } else if (s.equals(Protocol.TRAME_NOTIFICATION_ERREUR_ECOULEMENT)) {
            return new AlarmFlowageError(messageListener);
        } else if (s.equals(Protocol.TRAME_NOTIFICATION_ERREUR_COMPTAGE)) {
            return new AlarmCountingError(messageListener);
        } else if (s.equals(Protocol.TRAME_NOTIFICATION_AJOUT_EAU_BLOQUE)) {
            return new AlarmWaterAdditionBlocked(messageListener);
        } else if (s.equals(Protocol.TRAME_NOTIFICATION_CAPTEUR_PRESSION_ENTREE_DECONNECTE)) {
            return new SensorInputConnectionChanged(messageListener);
        } else if (s.equals(Protocol.TRAME_NOTIFICATION_CAPTEUR_PRESSION_SORTIE_DECONNECTE)) {
            return new SensorOutputConnectionChanged(messageListener);
        } else if (s.equals(Protocol.TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MIN)) {
            return new SensorSpeedThresholdMin(messageListener);
        } else if (s.equals(Protocol.TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MAX)) {
            return new SensorSpeedThresholdMax(messageListener);
        } else {
            throw new RuntimeException("The given type doesn't have an associated action");
        }
    }
}
