package com.lafarge.tm.states;

import com.lafarge.tm.MessageReceivedListener;
import com.lafarge.tm.ProgressListener;
import com.lafarge.tm.Protocol;
import com.lafarge.tm.actions.*;
import com.lafarge.tm.utils.CRC16Modbus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public final class CrcState extends State {

    private static final int CRC_NB_BYTES = 2;

    private byte[] crcToMatch;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

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
            if (progressListener != null) {
                progressListener.willProcessByte(ProgressListener.State.STATE_CRC, (byte) read);
            }
            out.write(read);
            if (!isCrcFoundMatch(out.toByteArray())) {
                if (progressListener != null) {
                    progressListener.parsingFailed(ProgressListener.ParsingError.ERROR_PARSING_CRC, (byte) read);
                }
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

    private boolean isCrcFoundMatch(byte[] crcToTest) {
        for (int i = 0; i < crcToTest.length; i++) {
            if (crcToTest[i] != crcToMatch[i]) {
                return false;
            }
        }
        return true;
    }

    private byte[] computeCrc(State.Message message) throws IOException {
        CRC16Modbus crc = new CRC16Modbus();
        for (byte b : message.getMessageBytes()) {
            crc.update((int)b);
        }
        return crc.getCrcBytes();
    }

    private ReadAction getAction(int type) {
        Map.Entry<String, Protocol.Spec> entry = getSpec(type);

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
                return new AlarmWaterMax(messageListener);
            case Protocol.TRAME_NOTIFICATION_ERREUR_ECOULEMENT:
                return new AlarmFlowageError(messageListener);
            case Protocol.TRAME_NOTIFICATION_ERREUR_COMPTAGE:
                return new AlarmCountingError(messageListener);
            case Protocol.TRAME_NOTIFICATION_AJOUT_EAU_BLOQUE:
                return new AlarmWaterAdditionBlocked(messageListener);
            case Protocol.TRAME_NOTIFICATION_CAPTEUR_PRESSION_ENTREE_DECONNECTE:
                return new SensorInputConnectionChanged(messageListener);
            case Protocol.TRAME_NOTIFICATION_CAPTEUR_PRESSION_SORTIE_DECONNECTE:
                return new SensorOutputConnectionChanged(messageListener);
            case Protocol.TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MIN:
                return new SensorSpeedThresholdMin(messageListener);
            case Protocol.TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MAX:
                return new SensorSpeedThresholdMax(messageListener);
            default:
                throw new RuntimeException("The given type doesn't have an associated action");
        }
    }
}
