package com.lafarge.tm.states;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.ProgressListener;
import com.lafarge.tm.Protocol;
import com.lafarge.tm.actions.*;
import com.lafarge.tm.utils.CRC16Modbus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.lafarge.tm.utils.Convert.bytesToHex;

public final class CrcState extends State {
    public static final int CRC_NB_BYTES = 2;

    private byte[] crcToMatch;

    private final byte[] buffer = new byte[CRC_NB_BYTES];
    private int totalRead = 0;

    public CrcState(Message message, MessageReceivedListener messageListener, ProgressListener progressListener) throws IOException {
        super(message, messageListener, progressListener);
        this.crcToMatch = computeCrc(message);
    }

    @Override
    public State decode(InputStream in) throws IOException {
        int read = in.read(this.buffer, this.totalRead, CRC_NB_BYTES - this.totalRead);

        switch (read) {
            case -1:
                return this;
            default:
                this.totalRead += read;
                return nextState(in);
        }
    }

    @Override
    protected void saveBuffer() {
        // Do nothing here
    }

    State nextState(InputStream in) throws IOException {
        State next = null;

        switch (this.totalRead) {
            case 1:
                if (checkIfFirstByteMatchWithCrc(this.buffer[0])) {
                    next = this;
                } else {
                    logger.warn("[CrcState] first byte received of crc doesn't match");
                }
                break;
            case CRC_NB_BYTES:
                if (checkIfCrcMatch(this.buffer)) {
                    MessageType action = getAction(getType());
                    if (action != null) {
                        action.decode(message.data);
                    }
                    next = new HeaderState(messageListener, progressListener).decode(in);
                } else {
                    logger.warn("[CrcState] crc received doesn't match");
                }
                break;
            default:
                throw new RuntimeException("The impossible happened: the nb bytes read is not conform to the protocol");
        }
        return (next != null) ? next : new HeaderState(messageListener, progressListener);
    }

    private byte[] computeCrc(State.Message message) throws IOException {
        CRC16Modbus crc = new CRC16Modbus();

        for (byte b : message.getMessageBytes()) {
            crc.update((int)b);
        }
        return crc.getCrcBytes();
    }

    private boolean checkIfFirstByteMatchWithCrc(byte byteToTest) {
        logger.debug("[CrcState] received byte: {}, expected byte: {}", String.format("0x%02X", byteToTest), String.format("0x%02X", this.crcToMatch[0]));
        return byteToTest == this.crcToMatch[0];
    }

    private boolean checkIfCrcMatch(byte[] crcToTest) {
        logger.debug("[CrcState] received crc: {}, expected crc: {}", bytesToHex(crcToTest), bytesToHex(this.crcToMatch));
        return (crcToTest[0] == this.crcToMatch[0] && crcToTest[1] == this.crcToMatch[1]);
    }

    protected Map.Entry<String, Protocol.Spec> getSpec(int messageType) {
        for (Map.Entry<String, Protocol.Spec> entry : Protocol.constants.entrySet()) {
            Protocol.Spec spec = entry.getValue();
            if (spec.address == messageType) {
                return entry;
            }
        }
        return null;
    }

    private MessageType getAction(int messageType) {
        Map.Entry<String, Protocol.Spec> entry = getSpec(messageType);

        switch (entry.getKey()) {
            case Protocol.TRAME_SLUMP_COURANT:
                return new SlumpUpdated(messageListener);
            case Protocol.TRAME_VOLUME_EAU_AJOUTE_PLUS_MODE:
                return new WaterAdded(messageListener);
            case Protocol.TRAME_NOTIFICATION_PASSAGE_EN_MALAXAGE:
                return new MixingModeActivated(messageListener);
            case Protocol.TRAME_NOTIFICATION_PASSAGE_EN_VIDANGE:
                return new UnloadingModeActivated(messageListener);
            case Protocol.TRAME_NOTIFICATION_DEBUT_AJOUT_EAU:
                return new WaterAdditionBegan(messageListener);
            case Protocol.TRAME_NOTIFICATION_FIN_AJOUT_EAU:
                return new WaterAdditionEnd(messageListener);
            case Protocol.TRAME_NOTIFICATION_PARAMETRES_STATIQUES_RECUS:
                return new TruckParametersReceived(messageListener);
            case Protocol.TRAME_NOTIFICATION_PARAMETRES_DYNAMIQUES_RECUS:
                return new DeliveryParametersReceived(messageListener);
            case Protocol.TRAME_NOTIFICATION_ACCEPTATION_LIVRAISON_RECUE:
                return new DeliveryValidationReceived(messageListener);
            case Protocol.TRAME_NOTIFICATION_FRANCHISSEMENT_TRANSITION:
                return new StateChanged(messageListener);
            case Protocol.TRAME_DEMANDE_AUTORISATION_AJOUT_EAU:
                return new WaterAdditionRequest(messageListener);
            case Protocol.TRAME_DEMANDE_PARAMETRES_STATIQUES:
                return new TruckParametersRequest(messageListener);
            case Protocol.TRAME_DEMANDE_PARAMETRES_DYNAMIQUES:
                return new DeliveryParametersRequest(messageListener);
            case Protocol.TRAME_DEMANDE_ACCEPTATION_LIVRAISON:
                return new DeliveryValidationRequest(messageListener);
            case Protocol.TRAME_TRACE_DEBUG:
                return new TraceDebug(messageListener);
            case Protocol.TRAME_DONNEES_BRUTES:
                return new RawData(messageListener);
            case Protocol.TRAME_DONNEES_DERIVEES:
                return new DerivedData(messageListener);
            case Protocol.TRAME_DONNEES_INTERNES:
                return new InternData(messageListener);
            case Protocol.TRAME_DONNEES_CALIBRATION:
                return new CalibrationData(messageListener);
            case Protocol.TRAME_NOTIFICATION_ERREUR_EAU_MAX:
                return new ErrorWaterMax(messageListener);
            case Protocol.TRAME_NOTIFICATION_ERREUR_ECOULEMENT:
                return new ErrorFlowage(messageListener);
            case Protocol.TRAME_NOTIFICATION_ERREUR_COMPTAGE:
                return new ErrorCounting(messageListener);
            case Protocol.TRAME_NOTIFICATION_AJOUT_EAU_BLOQUE:
                return new WaterAdditionLocked(messageListener);
            case Protocol.TRAME_NOTIFICATION_CAPTEUR_PRESSION_ENTREE_DECONNECTE:
                return new InputSensorStateChanged(messageListener);
            case Protocol.TRAME_NOTIFICATION_CAPTEUR_PRESSION_SORTIE_DECONNECTE:
                return new OutputSensorStateChanged(messageListener);
            case Protocol.TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MIN:
                return new SensorSpeedThresholdMin(messageListener);
            case Protocol.TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MAX:
                return new SensorSpeedThresholdMax(messageListener);
        }
        return null;
    }
}
