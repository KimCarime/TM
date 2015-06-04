package com.lafarge.truckmix.encoder;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lafarge.truckmix.common.Protocol;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.utils.Convert;
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

import static org.junit.Assert.assertArrayEquals;

@RunWith(value = Parameterized.class)
public class EncoderScenariosTest {

    private static class Message {
        final String type;
        final Object value;

        public Message(String type, Object value) {
            this.type = type;
            this.value = value;
        }
    }

    private final String scenario;
    private final Message message;
    private final byte[] result;

    public EncoderScenariosTest(String scenario, Message message, byte[] result) {
        this.scenario = scenario;
        this.message = message;
        this.result = result;
    }

    @SuppressWarnings("unchecked")
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> scenarios() {
        Gson gson = new Gson();
        Type token = new TypeToken<Map<String, Object>>() {}.getType();
        Reflections reflections = new Reflections("scenarios.encoder", new ResourcesScanner());
        Set<String> files = reflections.getResources(Pattern.compile(".*\\.json"));
        List<Object[]> scenarios = new LinkedList<Object[]>();
        for (String file : files) {
            System.out.println("scenario: " + file);
            Object[] values = new Object[3];
            Map<String, Object> test = gson.fromJson(new InputStreamReader(EncoderScenariosTest.class.getResourceAsStream("/" + file)), token);
            values[0] = test.get("description");

            Map<String, Object> message_def = (Map<String, Object>) test.get("message");
            values[1] = new Message((String) message_def.get("type"), message_def.get("value"));

            List<String> result_def = (List<String>) test.get("result");
            ByteArrayOutputStream frame = new ByteArrayOutputStream();
            for (String b : result_def) {
                frame.write(Integer.decode(b));
            }
            values[2] = frame.toByteArray();

            scenarios.add(values);
        }
        return scenarios;
    }

    @Test
    public void scenario() throws IOException {
        final Encoder encoder = new Encoder();
        final byte[] found;
        System.out.println("running: " + scenario);
        System.out.println("  testing: " + message.type + "\nExpected result: " + Convert.bytesToHex(result));
        if (message.type.equals(Protocol.TRAME_SLUMP_CIBLE)) {
            found = encoder.targetSlump(((Double) message.value).intValue());

        } else if (message.type.equals(Protocol.TRAME_VOLUME_EAU_MAXIMUM)) {
            found = encoder.maximumWater(((Double) message.value).intValue());

        } else if (message.type.equals(Protocol.TRAME_AUTORISATION_REFUS_AJOUT_EAU)) {
            found = encoder.waterAdditionPermission((Boolean) message.value);

        } else if (message.type.equals(Protocol.TRAME_ACTIVATION_INHIBITION_AFFICHEUR)) {
            found = encoder.changeExternalDisplayState((Boolean) message.value);

        } else if (message.type.equals(Protocol.TRAME_NOTIFICATION_FIN_DECHARGEMENT)) {
            found = encoder.endOfDelivery();

        } else if (message.type.equals(Protocol.TRAME_NOTIFICATION_ACCEPTATION_COMMANDE)) {
            found = encoder.beginningOfDelivery();

        } else if (message.type.equals(Protocol.TRAME_VOLUME_CHARGE)) {
            found = encoder.loadVolume((Double) message.value);

        } else if (message.type.equals(Protocol.TRAME_PARAMETRE_T1)) {
            found = encoder.parameterT1((Double) message.value);

        } else if (message.type.equals(Protocol.TRAME_PARAMETRE_A11)) {
            found = encoder.parameterA11((Double) message.value);

        } else if (message.type.equals(Protocol.TRAME_PARAMETRE_A12)) {
            found = encoder.parameterA12((Double) message.value);

        } else if (message.type.equals(Protocol.TRAME_PARAMETRE_A13)) {
            found = encoder.parameterA13((Double) message.value);

        } else if (message.type.equals(Protocol.TRAME_PARAMETRE_NOMBRE_D_AIMANTS)) {
            found = encoder.magnetQuantity(((Double) message.value).intValue());

        } else if (message.type.equals(Protocol.TRAME_PARAMETRE_TEMPS_AVANT_COULANT)) {
            found = encoder.timePump(((Double) message.value).intValue());

        } else if (message.type.equals(Protocol.TRAME_PARAMETRE_TEMPO_ATTENTE_REPONSE_CONDUCTEUR)) {
            found = encoder.timeDelayDriver(((Double) message.value).intValue());

        } else if (message.type.equals(Protocol.TRAME_NOMBRE_D_IMPULSIONS_PAR_LITRE)) {
            found = encoder.pulseNumber(((Double) message.value).intValue());

        } else if (message.type.equals(Protocol.TRAME_FREQUENCE_DEBITMETRE)) {
            found = encoder.flowmeterFrequency(((Double) message.value).intValue());

        } else if (message.type.equals(Protocol.TRAME_MODE_DE_COMMANDE_POMPE)) {
            found = encoder.commandPumpMode(TruckParameters.CommandPumpMode.valueOf((String) message.value));

        } else if (message.type.equals(Protocol.TRAME_FACTEUR_A_CAPTEUR_PRESSION_ENTREE)) {
            found = encoder.calibrationInputSensorA((Double) message.value);

        } else if (message.type.equals(Protocol.TRAME_FACTEUR_B_CAPTEUR_PRESSION_ENTREE)) {
            found = encoder.calibrationInputSensorB((Double) message.value);

        } else if (message.type.equals(Protocol.TRAME_FACTEUR_A_CAPTEUR_PRESSION_SORTIE)) {
            found = encoder.calibrationOutputSensorA((Double) message.value);

        } else if (message.type.equals(Protocol.TRAME_FACTEUR_B_CAPTEUR_PRESSION_SORTIE)) {
            found = encoder.calibrationOutputSensorB((Double) message.value);

        } else if (message.type.equals(Protocol.TRAME_DUREE_ATTENTE_OUVERTURE_EV1_VA1)) {
            found = encoder.openingTimeEV1(((Double) message.value).intValue());

        } else if (message.type.equals(Protocol.TRAME_DUREE_ATTENTE_FERMETURE_VA1_EV1)) {
            found = encoder.openingTimeVA1(((Double) message.value).intValue());

        } else if (message.type.equals(Protocol.TRAME_TOLERANCE_DE_COMPTAGE)) {
            found = encoder.countingTolerance(((Double) message.value).intValue());

        } else if (message.type.equals(Protocol.TRAME_DUREE_ATTENTE_APRES_AJOUT_EAU)) {
            found = encoder.waitingDurationAfterWaterAddition(((Double) message.value).intValue());

        } else if (message.type.equals(Protocol.TRAME_DELAI_MAXIMUM_AVANT_ECOULEMENT)) {
            found = encoder.maxDelayBeforeFlowage(((Double) message.value).intValue());

        } else if (message.type.equals(Protocol.TRAME_NOMBRE_MAX_ERREURS_ECOULEMENT)) {
            found = encoder.maxFlowageError(((Double) message.value).intValue());

        } else if (message.type.equals(Protocol.TRAME_NOMBRE_MAX_ERREURS_COMPTAGE)) {
            found = encoder.maxCountingError(((Double) message.value).intValue());

        } else {
            throw new IllegalArgumentException("unknown protocol type: " + message.type);
        }
        assertArrayEquals(found, result);
    }
}
