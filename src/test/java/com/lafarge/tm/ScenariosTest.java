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
        List<Object> datas;

        public Result(String message, List<Object> datas) {
            this.message = message;
            this.datas = datas;
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
                results.add(new Result((String)result_def.get("message"), (List<Object>)result_def.get("datas")));
            }
            values[2] = results;
            scenarios.add(values);
        }
        return scenarios;
    }

    @Test @Ignore
    public void scenario() throws IOException {
        MessageReceivedListener messageListener = mock(MessageReceivedListener.class);
        ProgressListener progressListener = mock(ProgressListener.class);
        Decoder decoder = new Decoder(messageListener, progressListener);
        System.out.println("running " + scenario);
        for (byte[] frame : frames) {
            decoder.decode(frame);
        }

        for (Result result : results) {
            switch (result.message) {
                case "TRAME_NOTIFICATION_ERREUR_COMPTAGE":
                    break;
                case "TRAME_NOTIFICATION_ERREUR_ECOULEMENT":
                    break;
                case "TRAME_NOTIFICATION_ERREUR_EAU_MAX":
                    break;
                case "TRAME_NOTIFICATION_AJOUT_EAU_BLOQUE":
                    break;
                case "TRAME_NOTIFICATION_DEBUT_AJOUT_EAU":
                    break;
                case "TRAME_DONNEES_CALIBRATION":
                    break;
                case "TRAME_TRACE_DEBUG":
                    break;
                case "TRAME_NOTIFICATION_ACCEPTATION_LIVRAISON_RECUE":
                    break;
                case "TRAME_ATTENTE_ACCEPTATION_LIVRAISON":
                    break;
                case "TRAME_DONNEES_DERIVEES":
                    break;
                case "TRAME_NOTIFICATION_PARAMETRES_DYNAMIQUES_RECUS":
                    break;
                case "TRAME_DEMANDE_PARAMETRES_DYNAMIQUES":
                    break;
                case "TRAME_NOTIFICATION_FIN_AJOUT_EAU":
                    break;
                case "TRAME_NOTIFICATION_CAPTEUR_PRESSION_ENTREE_DECONNECTE":
                    break;
                case "TRAME_DONNES_INTERNES":
                    break;
                case "TRAME_NOTIFICATION_PASSAGE_EN_MALAXAGE":
                    break;
                case "TRAME_NOTIFICATION_PASSAGE_EN_VIDANGE":
                    break;
                case "TRAME_NOTIFICATION_CAPTEUR_PRESSION_SORTIE_DECONNECTE":
                    break;
                case "TRAME_DONNEES_BRUTES":
                    break;
                case "TRAME_SLUMP_COURANT":
                    verify(messageListener).slumpUpdated(((Double)result.datas.get(0)).intValue());
                    break;
                case "TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MAX":
                    break;
                case "TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MIN":
                    break;
                case "TRAME_NOTIFICATION_FRANCHISSEMENT_TRANSITION":
                    break;
                case "TRAME_NOTIFICATION_PARAMETRES_STATIQUES_RECUS":
                    break;
                case "TRAME_DEMANDE_PARAMETRES_STATIQUES":
                    break;
                case "TRAME_VOLUME_EAU_AJOUTE_PLUS_MODE":
                    break;
                case "TRAME_DEMANDE_AUTORISATION_AJOUT_EAU":
                    break;
                default:
                    throw new IllegalArgumentException("unknown result message " + result.message);
            }
        }
    }
}
