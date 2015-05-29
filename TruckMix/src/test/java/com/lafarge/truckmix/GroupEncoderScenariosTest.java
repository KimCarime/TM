package com.lafarge.truckmix;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lafarge.truckmix.utils.Convert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;

import static org.junit.Assert.assertArrayEquals;

@RunWith(value = Parameterized.class)
public class GroupEncoderScenariosTest {

    private static class Message {
        final String type;
        final Map<String, Object> values;

        public Message(String type, Map<String, Object> values) {
            this.type = type;
            this.values = values;
        }
    }

    private final String scenario;
    private final Message message;
    private final byte[] result;

    public GroupEncoderScenariosTest(String scenario, Message message, byte[] result) {
        this.scenario = scenario;
        this.message = message;
        this.result = result;
    }

    @SuppressWarnings("unchecked")
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> scenarios() {
        Gson gson = new Gson();
        Type token = new TypeToken<Map<String, Object>>() {}.getType();
        Reflections reflections = new Reflections("scenarios.group_encoder", new ResourcesScanner());
        Set<String> files = reflections.getResources(Pattern.compile(".*\\.json"));
        List<Object[]> scenarios = new LinkedList<Object[]>();
        for (String file : files) {
            System.out.println("scenario: " + file);
            Object[] values = new Object[3];
            Map<String, Object> test = gson.fromJson(new InputStreamReader(GroupEncoderScenariosTest.class.getResourceAsStream("/" + file)), token);
            values[0] = test.get("description");

            Map<String, Object> message_def = (Map<String, Object>) test.get("message");
            values[1] = new Message((String) message_def.get("type"), (Map<String, Object>) message_def.get("values"));

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
    public void scenario() {
        GroupEncoder encoder = new GroupEncoder(null);
        final byte[] found;
        System.out.println("running: " + scenario);
        System.out.println("  testing: " + message.type + "\nExpected result: " + Convert.bytesToHex(result));
        if (message.type.equals("TRUCK_PARAMETERS")) {
            TruckParameters parameters = new TruckParameters(
                    ((Double) message.values.get("T1")),
                    ((Double) message.values.get("A11")),
                    ((Double) message.values.get("A12")),
                    ((Double) message.values.get("A13")),
                    ((Double) message.values.get("magnetQuantity")).intValue(),
                    ((Double) message.values.get("timePump")).intValue(),
                    ((Double) message.values.get("timeDelayDriver")).intValue(),
                    ((Double) message.values.get("pulseNumber")).intValue(),
                    ((Double) message.values.get("flowmeterFrequency")).intValue(),
                    TruckParameters.CommandPumpMode.valueOf(((String) message.values.get("commandPumpMode"))),
                    ((Double) message.values.get("calibrationInputSensorA")),
                    ((Double) message.values.get("calibrationInputSensorB")),
                    ((Double) message.values.get("calibrationOutputSensorA")),
                    ((Double) message.values.get("calibrationOutputSensorB")),
                    ((Double) message.values.get("openingTimeEV1")).intValue(),
                    ((Double) message.values.get("openingTimeVA1")).intValue(),
                    ((Double) message.values.get("toleranceCounting")).intValue(),
                    ((Double) message.values.get("waitingDurationAfterWaterAddition")).intValue(),
                    ((Double) message.values.get("maxDelayBeforeFlowage")).intValue(),
                    ((Double) message.values.get("maxFlowageError")).intValue(),
                    ((Double) message.values.get("maxCountingError")).intValue());
            found = encoder.truckParameters(parameters);
        } else if (message.type.equals("DELIVERY_PARAMETERS")) {
            DeliveryParameters parameters = new DeliveryParameters(
                    ((Double) message.values.get("targetSlump")).intValue(),
                    ((Double) message.values.get("maxWater")).intValue(),
                    ((Double) message.values.get("loadVolume")).intValue());
            found = encoder.deliveryParameters(parameters);
        } else {
            throw new RuntimeException("this group doesn't exist: " + message.type);
        }
        assertArrayEquals(found, result);
    }
}
