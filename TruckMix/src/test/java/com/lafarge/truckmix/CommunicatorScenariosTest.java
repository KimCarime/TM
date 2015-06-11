package com.lafarge.truckmix;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.Communicator;
import com.lafarge.truckmix.communicator.Scheduler;
import com.lafarge.truckmix.communicator.listeners.CommunicatorBytesListener;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;

@RunWith(value = Parameterized.class)
public class CommunicatorScenariosTest {

    private interface Step {}

    private static class Message implements Step {
        final String description;
        final byte[] packets;

        public Message(String description, byte[] packets) {
            this.description = description;
            this.packets = packets;
        }
    }

    private static class Action implements Step {
        final String action;
        final Map<String, Object> values;

        private Action(String action, Map<String, Object> values) {
            this.action = action;
            this.values = values;
        }
    }

    private static class Wait implements Step {
        final int waitInSec;

        public Wait(int waitInSec) {
            this.waitInSec = waitInSec;
        }
    }

    private static class Connected implements Step {
        final boolean connected;

        public Connected(boolean connected) {
            this.connected = connected;
        }
    }

    private final String description;
    private final List<Step> steps;
    private final List<byte[]> packetsToSend;

    public CommunicatorScenariosTest(String description, List<Step> steps, List<byte[]> packetsToSend) {
        this.description = description;
        this.steps = steps;
        this.packetsToSend = packetsToSend;
    }

    @SuppressWarnings("unchecked")
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> scenarios() {
        Gson gson = new Gson();
        Type token = new TypeToken<Map<String, Object>>(){}.getType();
        Reflections reflections = new Reflections("scenarios.communication", new ResourcesScanner());
        Set<String> files = reflections.getResources(Pattern.compile(".*\\.json"));
        List<Object[]> scenarios = new LinkedList<Object[]>();
        for (String file : files) {
            Object[] values = new Object[3];
            Map<String, Object> test = gson.fromJson(new InputStreamReader(CommunicatorScenariosTest.class.getResourceAsStream("/" + file)), token);
            values[0] = test.get("description");

            List<Map<String, Object>> steps_def = (List<Map<String, Object>>) test.get("scenario");
            List<Step> steps = new LinkedList<Step>();
            for (Map<String, Object> step_def : steps_def) {
                Step newStep = null;
                if (step_def.containsKey("bluetoothConnected")) {
                    newStep = new Connected((Boolean) step_def.get("bluetoothConnected"));
                } else if (step_def.containsKey("action")) {
                    newStep = new Action((String) step_def.get("action"), (Map<String, Object>) step_def.get("values"));
                } else if (step_def.containsKey("wait_in_sec")) {
                    newStep = new Wait(((Double)step_def.get("wait_in_sec")).intValue());
                } else if (step_def.containsKey("packets")) {
                    ByteArrayOutputStream frame = new ByteArrayOutputStream();
                    for (String b : (List<String>) step_def.get("packets")) {
                        frame.write(Integer.decode(b));
                    }
                    newStep = new Message((String) step_def.get("description"), frame.toByteArray());
                }

                if (newStep != null) {
                    steps.add(newStep);
                }
            }
            values[1] = steps;

            List<List<String>> results_def = (List<List<String>>) test.get("result_packets_to_send");
            List<byte[]> results = new LinkedList<byte[]>();
            for (List<String> result_def : results_def) {
                ByteArrayOutputStream frame = new ByteArrayOutputStream();
                for (String b : result_def) {
                    frame.write(Integer.decode(b));
                }
                results.add(frame.toByteArray());
            }
            values[2] = results;

            scenarios.add(values);
        }
        return scenarios;
    }

    @Test
    public void scenario() throws InterruptedException {
        System.out.println("running: " + this.description);
        final List<byte[]> results = new LinkedList<byte[]>();
        Scheduler scheduler = new Scheduler(100 + 5);
        Communicator communicator = new Communicator(new CommunicatorBytesListener() {
            @Override
            public void send(byte[] bytes) {
                results.add(bytes);
            }
        }, mock(CommunicatorListener.class), new LoggerListener() {
            @Override
            public void log(String log) {
                System.out.println(log);
            }
        }, scheduler);

        for (Step step : this.steps) {
            if (step instanceof Connected) {
                System.out.println("--------- TEST: should change connection state");
                communicator.setConnected(((Connected) step).connected);
            } else if (step instanceof Wait) {
                System.out.println("--------- TEST: should wait " + ((Wait)step).waitInSec + " sec");
                Thread.sleep(((Wait) step).waitInSec*10);
            } else if (step instanceof Message) {
                System.out.println("--------- TEST: should received: " + ((Message) step).description);
                communicator.received(((Message) step).packets);
            } else if (step instanceof Action) {
                if (((Action) step).action.equals("init")) {
                    System.out.println("--------- TEST: should set truck parameters");
                    communicator.setTruckParameters(new TruckParameters(
                            ((Double) ((Action) step).values.get("T1")),
                            ((Double) ((Action) step).values.get("A11")),
                            ((Double) ((Action) step).values.get("A12")),
                            ((Double) ((Action) step).values.get("A13")),
                            ((Double) ((Action) step).values.get("magnetQuantity")).intValue(),
                            ((Double) ((Action) step).values.get("timePump")).intValue(),
                            ((Double) ((Action) step).values.get("timeDelayDriver")).intValue(),
                            ((Double) ((Action) step).values.get("pulseNumber")).intValue(),
                            ((Double) ((Action) step).values.get("flowmeterFrequency")).intValue(),
                            TruckParameters.CommandPumpMode.valueOf(((String) ((Action) step).values.get("commandPumpMode"))),
                            ((Double) ((Action) step).values.get("calibrationInputSensorA")),
                            ((Double) ((Action) step).values.get("calibrationInputSensorB")),
                            ((Double) ((Action) step).values.get("calibrationOutputSensorA")),
                            ((Double) ((Action) step).values.get("calibrationOutputSensorB")),
                            ((Double) ((Action) step).values.get("openingTimeEV1")).intValue(),
                            ((Double) ((Action) step).values.get("openingTimeVA1")).intValue(),
                            ((Double) ((Action) step).values.get("toleranceCounting")).intValue(),
                            ((Double) ((Action) step).values.get("waitingDurationAfterWaterAddition")).intValue(),
                            ((Double) ((Action) step).values.get("maxDelayBeforeFlowage")).intValue(),
                            ((Double) ((Action) step).values.get("maxFlowageError")).intValue(),
                            ((Double) ((Action) step).values.get("maxCountingError")).intValue()));

                } else if (((Action) step).action.equals("deliveryNoteReceived")) {
                    System.out.println("--------- TEST: should receive delivery note");
                    communicator.deliveryNoteReceived(new DeliveryParameters(
                            ((Double) ((Action) step).values.get("targetSlump")).intValue(),
                            ((Double) ((Action) step).values.get("maxWater")).intValue(),
                            ((Double) ((Action) step).values.get("loadVolume")).intValue()));

                } else if (((Action) step).action.equals("acceptDelivery")) {
                    System.out.println("--------- TEST: should accept delivery");
                    communicator.acceptDelivery((Boolean) ((Action) step).values.get("accepted"));

                } else if (((Action) step).action.equals("allowWaterAddition")) {
                    System.out.println("--------- TEST: should allow water addition");
                    communicator.allowWaterAddition((Boolean) ((Action) step).values.get("allowed"));

                } else if (((Action) step).action.equals("endDelivery")) {
                    System.out.println("--------- TEST: should end delivery");
                    communicator.endDelivery();

                } else if (((Action) step).action.equals("changeExternalDisplayState")) {
                    System.out.println("--------- TEST: should change external display state");
                    communicator.changeExternalDisplayState((Boolean) ((Action) step).values.get("activated"));

                } else {
                    System.out.println("--------- TEST: action not defined: " + ((Action) step).action);

                }
            }
        }
        scheduler.reset();

        assertThat(results, hasSize(this.packetsToSend.size()));
        for (int i = 0; i < this.packetsToSend.size(); i++) {
            assertArrayEquals(results.get(i), this.packetsToSend.get(i));
        }
    }
}
