package com.lafarge.truckmix.decoder;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.decoder.listeners.LoggedMessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.LoggedProgressListener;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.decoder.listeners.ProgressListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.mockito.Mockito.anyByte;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(value = Parameterized.class)
public class DecoderScenariosTest {

    private static class Result {
        final String message;
        final List<Object> data;

        public Result(String message, List<Object> data) {
            this.message = message;
            this.data = data;
        }
    }

    private static class Error {
        final String error;
        final int count;

        public Error(String error, int count) {
            this.error = error;
            this.count = count;
        }
    }

    private final String scenario;
    private final List<byte[]> frames;
    private final List<Result> results;

    public DecoderScenariosTest(String scenario, List<byte[]> frames, List<Result> results) {
        this.scenario = scenario;
        this.frames = frames;
        this.results = results;
    }

    @SuppressWarnings("unchecked")
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> scenarios() {
        Gson gson = new Gson();
        Type token = new TypeToken<Map<String, Object>>(){}.getType();
        Reflections reflections = new Reflections("scenarios.decoder", new ResourcesScanner());
        Set<String> files = reflections.getResources(Pattern.compile(".*\\.json"));
        List<Object[]> scenarios = new LinkedList<Object[]>();
        for (String file : files) {
            Object[] values = new Object[3];
            Map<String, Object> test = gson.fromJson(new InputStreamReader(DecoderScenariosTest.class.getResourceAsStream("/" + file)), token);
            values[0] = test.get("description");
            List<List<String>> frames_def = (List<List<String>>) test.get("packets");
            List<byte[]> frames = new LinkedList<byte[]>();
            for (List<String> frame_def : frames_def) {
                ByteArrayOutputStream frame = new ByteArrayOutputStream();
                for (String b : frame_def) {
                    frame.write(Integer.decode(b));
                }
                frames.add(frame.toByteArray());
            }
            values[1] = frames;
            List<Map<String, Object>> results_def = (List<Map<String, Object>>) test.get("results");
            List<Object> results = new ArrayList<Object>(results_def.size());
            for (Map<String, Object> result_def : results_def) {
                if (result_def.get("message") != null) {
                    results.add(new Result((String) result_def.get("message"), (List<Object>) result_def.get("data")));
                } else if (result_def.get("error") != null) {
                    results.add(new Error((String) result_def.get("error"), ((Double) result_def.get("count")).intValue()));
                }
            }
            values[2] = results;
            scenarios.add(values);
        }
        return scenarios;
    }

    @Test
    public void scenario() throws IOException {
        LoggedMessageReceivedListener messageListener = spy(LoggedMessageReceivedListener.class);
        LoggedProgressListener progressListener = spy(LoggedProgressListener.class);
        Decoder decoder = new Decoder(messageListener, progressListener);
        System.out.println("running: " + scenario);
        for (byte[] frame : frames) {
            decoder.decode(frame);
        }

        for (Object object : results) {
            if (object instanceof Result) {
                verifyResult((Result) object, messageListener);
            } else if (object instanceof Error) {
                verifyError((Error) object, progressListener);
            }
        }
    }

    private void verifyError(Error error, ProgressListener progressListener) {
        verify(progressListener, times(error.count)).parsingFailed(eq(ProgressListener.ParsingError.valueOf(error.error)), anyByte());
    }

    private void verifyResult(Result result, MessageReceivedListener messageListener) {
        if (result.message.equals(Protocol.TRAME_NOTIFICATION_ERREUR_COMPTAGE)) {
            verify(messageListener).alarmCountingError();

        } else if (result.message.equals(Protocol.TRAME_NOTIFICATION_ERREUR_ECOULEMENT)) {
            verify(messageListener).alarmFlowageError();

        } else if (result.message.equals(Protocol.TRAME_NOTIFICATION_ERREUR_EAU_MAX)) {
            verify(messageListener).alarmWaterMax();

        } else if (result.message.equals(Protocol.TRAME_NOTIFICATION_AJOUT_EAU_BLOQUE)) {
            verify(messageListener).alarmWaterAdditionBlocked();

        } else if (result.message.equals(Protocol.TRAME_NOTIFICATION_DEBUT_AJOUT_EAU)) {
            verify(messageListener).waterAdditionBegan();

        } else if (result.message.equals(Protocol.TRAME_DONNEES_CALIBRATION)) {
            verify(messageListener).calibrationData(
                    ((Double) result.data.get(0)).floatValue(),
                    ((Double) result.data.get(1)).floatValue(),
                    ((Double) result.data.get(2)).floatValue());

        } else if (result.message.equals(Protocol.TRAME_TRACE_DEBUG)) {
            verify(messageListener).traceDebug((String) result.data.get(0));

        } else if (result.message.equals(Protocol.TRAME_NOTIFICATION_ACCEPTATION_LIVRAISON_RECUE)) {
            verify(messageListener).deliveryValidationReceived();

        } else if (result.message.equals(Protocol.TRAME_DEMANDE_ACCEPTATION_LIVRAISON)) {
            verify(messageListener).deliveryValidationRequest();

        } else if (result.message.equals(Protocol.TRAME_DONNEES_DERIVEES)) {
            verify(messageListener).derivedData(
                    MessageReceivedListener.RotationDirection.valueOf((String) result.data.get(0)),
                    (Boolean) result.data.get(1),
                    ((Double) result.data.get(2)).intValue(),
                    ((Double) result.data.get(3)).intValue());

        } else if (result.message.equals(Protocol.TRAME_NOTIFICATION_PARAMETRES_DYNAMIQUES_RECUS)) {
            verify(messageListener).deliveryParametersReceived();

        } else if (result.message.equals(Protocol.TRAME_DEMANDE_PARAMETRES_DYNAMIQUES)) {
            verify(messageListener).deliveryParametersRequest();

        } else if (result.message.equals(Protocol.TRAME_NOTIFICATION_FIN_AJOUT_EAU)) {
            verify(messageListener).waterAdditionEnd();

        } else if (result.message.equals(Protocol.TRAME_NOTIFICATION_CAPTEUR_PRESSION_ENTREE_DECONNECTE)) {
            verify(messageListener).inputSensorConnectionChanged((Boolean) result.data.get(0));

        } else if (result.message.equals(Protocol.TRAME_DONNEES_INTERNES)) {
            verify(messageListener).internData(
                    (Boolean) result.data.get(0),
                    (Boolean) result.data.get(1),
                    (Boolean) result.data.get(2),
                    (Boolean) result.data.get(3),
                    (Boolean) result.data.get(4),
                    (Boolean) result.data.get(5));

        } else if (result.message.equals(Protocol.TRAME_NOTIFICATION_PASSAGE_EN_MALAXAGE)) {
            verify(messageListener).mixingModeActivated();

        } else if (result.message.equals(Protocol.TRAME_NOTIFICATION_PASSAGE_EN_VIDANGE)) {
            verify(messageListener).unloadingModeActivated();

        } else if (result.message.equals(Protocol.TRAME_NOTIFICATION_CAPTEUR_PRESSION_SORTIE_DECONNECTE)) {
            verify(messageListener).outputSensorConnectionChanged((Boolean) result.data.get(0));

        } else if (result.message.equals(Protocol.TRAME_DONNEES_BRUTES)) {
            verify(messageListener).rawData(
                    ((Double) result.data.get(0)).intValue(),
                    ((Double) result.data.get(1)).intValue(),
                    ((Double) result.data.get(2)).intValue(),
                    (Boolean) result.data.get(3));

        } else if (result.message.equals(Protocol.TRAME_SLUMP_COURANT)) {
            verify(messageListener).slumpUpdated(((Double) result.data.get(0)).intValue());

        } else if (result.message.equals(Protocol.TRAME_TEMPERATURE_COURANTE)) {
            verify(messageListener).temperatureUpdated(((Double) result.data.get(0)).floatValue());

        } else if (result.message.equals(Protocol.TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MAX)) {
            verify(messageListener).speedSensorHasExceedMaxThreshold((Boolean) result.data.get(0));

        } else if (result.message.equals(Protocol.TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MIN)) {
            verify(messageListener).speedSensorHasExceedMinThreshold((Boolean) result.data.get(0));

        } else if (result.message.equals(Protocol.TRAME_NOTIFICATION_FRANCHISSEMENT_TRANSITION)) {
            verify(messageListener).stateChanged(
                    ((Double) result.data.get(0)).intValue(),
                    ((Double) result.data.get(1)).intValue());

        } else if (result.message.equals(Protocol.TRAME_NOTIFICATION_PARAMETRES_STATIQUES_RECUS)) {
            verify(messageListener).truckParametersReceived();

        } else if (result.message.equals(Protocol.TRAME_DEMANDE_PARAMETRES_STATIQUES)) {
            verify(messageListener).truckParametersRequest();

        } else if (result.message.equals(Protocol.TRAME_VOLUME_EAU_AJOUTE_PLUS_MODE)) {
            verify(messageListener).waterAdded(
                    ((Double) result.data.get(0)).intValue(),
                    MessageReceivedListener.WaterAdditionMode.valueOf((String) result.data.get(1)));

        } else if (result.message.equals(Protocol.TRAME_DEMANDE_AUTORISATION_AJOUT_EAU)) {
            verify(messageListener).waterAdditionRequest(((Double) result.data.get(0)).intValue());

        } else {
            throw new IllegalArgumentException("unknown result message " + result.message);
        }
    }
}
