package com.lafarge.tm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(value = Parameterized.class)
public class ScenariosTest {

    private static class Result {
        String message;
        List<Object> data;

        public Result(String message, List<Object> data) {
            this.message = message;
            this.data = data;
        }
    }

    private String scenario;
    private List<byte[]> frames;
    private List<Result> results;

    public ScenariosTest(String scenario, List<byte[]> frames, List<Result> results) {
        this.scenario = scenario;
        this.frames = frames;
        this.results = results;
    }

    @SuppressWarnings("unchecked")
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> scenarios() {
        Gson gson = new Gson();
        Type token = new TypeToken<Map<String, Object>>(){}.getType();
        Reflections reflections = new Reflections("scenarios", new ResourcesScanner());
        Set<String> files = reflections.getResources(Pattern.compile(".*\\.json"));
        List<Object[]> scenarios = new LinkedList<>();
        for (String file : files) {
            Object[] values = new Object[3];
            Map<String, Object> test = gson.fromJson(new InputStreamReader(ScenariosTest.class.getResourceAsStream("/" + file)), token);
            values[0] = test.get("description");
            List<List<String>> frames_def = (List<List<String>>)test.get("packets");
            List<byte[]> frames = new LinkedList<>();
            for (List<String> frame_def : frames_def) {
                ByteArrayOutputStream frame = new ByteArrayOutputStream();
                for (String b : frame_def) {
                    frame.write(Integer.decode(b));
                }
                frames.add(frame.toByteArray());
            }
            values[1] = frames;
            List<Map<String, Object>> results_def = (List<Map<String, Object>>)test.get("results");
            List<Result> results = new ArrayList<>(results_def.size());
            for (Map<String, Object> result_def : results_def) {
                results.add(new Result((String)result_def.get("message"), (List<Object>)result_def.get("data")));
            }
            values[2] = results;
            scenarios.add(values);
        }
        return scenarios;
    }

    @Test
    public void scenario() throws IOException {
        MessageReceivedListener messageListener = mock(MessageReceivedListener.class);
        ProgressListener progressListener = mock(ProgressListener.class);
        Decoder decoder = new Decoder(messageListener, progressListener);
        System.out.println("running: " + scenario);
        for (byte[] frame : frames) {
            decoder.decode(frame);
        }

        for (Result result : results) {
            switch (result.message) {
                case Protocol.TRAME_NOTIFICATION_ERREUR_COMPTAGE:
                    verify(messageListener).countingError();
                    break;
                case Protocol.TRAME_NOTIFICATION_ERREUR_ECOULEMENT:
                    verify(messageListener).flowageError();
                    break;
                case Protocol.TRAME_NOTIFICATION_ERREUR_EAU_MAX:
                    verify(messageListener).waterMaxError();
                    break;
                case Protocol.TRAME_NOTIFICATION_AJOUT_EAU_BLOQUE:
                    verify(messageListener).waterAdditionLocked();
                    break;
                case Protocol.TRAME_NOTIFICATION_DEBUT_AJOUT_EAU:
                    verify(messageListener).waterAdditionBegan();
                    break;
                case Protocol.TRAME_DONNEES_CALIBRATION:
                    verify(messageListener).calibrationData(
                            ((Double)result.data.get(0)).floatValue(),
                            ((Double)result.data.get(1)).floatValue(),
                            ((Double)result.data.get(2)).floatValue());
                    break;
                case Protocol.TRAME_TRACE_DEBUG:
                    verify(messageListener).traceDebug((String)result.data.get(0));
                    break;
                case Protocol.TRAME_NOTIFICATION_ACCEPTATION_LIVRAISON_RECUE:
                    verify(messageListener).deliveryValidationReceived();
                    break;
                case Protocol.TRAME_DEMANDE_ACCEPTATION_LIVRAISON:
                    verify(messageListener).deliveryValidationRequest();
                    break;
                case Protocol.TRAME_DONNEES_DERIVEES:
                    verify(messageListener).derivedData(
                            MessageReceivedListener.RotationDirection.valueOf((String)result.data.get(0)),
                            (Boolean)result.data.get(1),
                            ((Double)result.data.get(2)).intValue(),
                            ((Double)result.data.get(3)).intValue());
                    break;
                case Protocol.TRAME_NOTIFICATION_PARAMETRES_DYNAMIQUES_RECUS:
                    verify(messageListener).deliveryParametersReceived();
                    break;
                case Protocol.TRAME_DEMANDE_PARAMETRES_DYNAMIQUES:
                    verify(messageListener).deliveryParametersRequest();
                    break;
                case Protocol.TRAME_NOTIFICATION_FIN_AJOUT_EAU:
                    verify(messageListener).waterAdditionEnd();
                    break;
                case Protocol.TRAME_NOTIFICATION_CAPTEUR_PRESSION_ENTREE_DECONNECTE:
                    verify(messageListener).inputSensorStateChanged((Boolean)result.data.get(0));
                    break;
                case Protocol.TRAME_DONNEES_INTERNES:
                    verify(messageListener).internData(
                            (Boolean)result.data.get(0),
                            (Boolean)result.data.get(1),
                            (Boolean)result.data.get(2),
                            (Boolean)result.data.get(3),
                            (Boolean)result.data.get(4),
                            (Boolean)result.data.get(5));
                    break;
                case Protocol.TRAME_NOTIFICATION_PASSAGE_EN_MALAXAGE:
                    verify(messageListener).mixingModeActivated();
                    break;
                case Protocol.TRAME_NOTIFICATION_PASSAGE_EN_VIDANGE:
                    verify(messageListener).unloadingModeActivated();
                    break;
                case Protocol.TRAME_NOTIFICATION_CAPTEUR_PRESSION_SORTIE_DECONNECTE:
                    verify(messageListener).outputSensorStateChanged((Boolean)result.data.get(0));
                    break;
                case Protocol.TRAME_DONNEES_BRUTES:
                    verify(messageListener).rawData(
                            ((Double)result.data.get(0)).intValue(),
                            ((Double)result.data.get(1)).intValue(),
                            ((Double)result.data.get(2)).intValue(),
                            (Boolean)result.data.get(3));
                    break;
                case Protocol.TRAME_SLUMP_COURANT:
                    verify(messageListener).slumpUpdated(((Double)result.data.get(0)).intValue());
                    break;
                case Protocol.TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MAX:
                    verify(messageListener).speedSensorHasExceedMaxThreshold((Boolean)result.data.get(0));
                    break;
                case Protocol.TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MIN:
                    verify(messageListener).speedSensorHasExceedMinThreshold((Boolean)result.data.get(0));
                    break;
                case Protocol.TRAME_NOTIFICATION_FRANCHISSEMENT_TRANSITION:
                    verify(messageListener).stateChanged(
                            ((Double)result.data.get(0)).intValue(),
                            ((Double)result.data.get(1)).intValue());
                    break;
                case Protocol.TRAME_NOTIFICATION_PARAMETRES_STATIQUES_RECUS:
                    verify(messageListener).truckParametersReceived();
                    break;
                case Protocol.TRAME_DEMANDE_PARAMETRES_STATIQUES:
                    verify(messageListener).truckParametersRequest();
                    break;
                case Protocol.TRAME_VOLUME_EAU_AJOUTE_PLUS_MODE:
                    verify(messageListener).waterAdded(
                            ((Double)result.data.get(0)).intValue(),
                            MessageReceivedListener.WaterAdditionMode.valueOf((String)result.data.get(1)));
                    break;
                case Protocol.TRAME_DEMANDE_AUTORISATION_AJOUT_EAU:
                    verify(messageListener).waterAdditionRequest(((Double)result.data.get(0)).intValue());
                    break;
                default:
                    throw new IllegalArgumentException("unknown result message " + result.message);
            }
        }
    }
}
