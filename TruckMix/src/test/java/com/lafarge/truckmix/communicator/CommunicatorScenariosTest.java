package com.lafarge.truckmix.communicator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.events.Event;
import com.lafarge.truckmix.communicator.listeners.CommunicatorBytesListener;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.EventListener;
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
import static org.hamcrest.Matchers.is;
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
    private final List<Event> events;

    public CommunicatorScenariosTest(String description, List<Step> steps, List<byte[]> packetsToSend, List<Event> events) {
        this.description = description;
        this.steps = steps;
        this.packetsToSend = packetsToSend;
        this.events = events;
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
            Object[] values = new Object[4];
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

            List<Map<String, Object>> events_def = (List<Map<String, Object>>) test.get("events");
            List<Event> eventResults = new LinkedList<Event>();
            for (Map<String, Object> event_def : events_def) {

                Event event;
                Event.EventId eventId = Event.EventId.getEnum(((Double) event_def.get("id")).intValue());
                Double value = (Double) event_def.get("value");
                if (eventId == Event.EventId.ROTATION_SPEED || eventId == Event.EventId.INPUT_PRESSURE || eventId == Event.EventId
                        .OUTPUT_PRESSURE) {
                    event = new Event<Float>(eventId, value.floatValue());
                } else {
                    event = new Event<Integer>(eventId, value.intValue());
                }
                eventResults.add(event);
            }
            values[3] = eventResults;

            scenarios.add(values);
        }
        return scenarios;
    }

    @Test
    public void scenario() throws InterruptedException {
        System.out.println("running: " + this.description + "\n");
        final List<byte[]> results = new LinkedList<byte[]>();
        final List<Event> eventResults = new LinkedList<Event>();
        CountingSchedulerMock scheduler = new CountingSchedulerMock(Communicator.RESET_STATE_IN_MILLIS);
        Communicator communicator = new Communicator(
                new CommunicatorBytesListener() {
                    @Override
                    public void send(byte[] bytes) {
                        results.add(bytes);
                    }
                },
                mock(CommunicatorListener.class),
                new LoggerListener() {
                    @Override
                    public void log(String log) {
                        System.out.println(log);
                    }
                },
                new EventListener() {
                    @Override
                    public void onNewEvents(Event event) {
                        eventResults.add(event);
                    }
                },
                scheduler);
        communicator.setQualityTrackingActivated(true);
        communicator.setWaterRequestAllowed(true);

        for (Step step : this.steps) {
            if (step instanceof Connected) {
                communicator.setConnected(((Connected) step).connected);
            } else if (step instanceof Wait) {
                System.out.println("--------- TEST: should wait " + ((Wait)step).waitInSec + " sec");
                scheduler.forward(((Wait) step).waitInSec * 1000);
            } else if (step instanceof Message) {
                communicator.received(((Message) step).packets);
            } else if (step instanceof Action) {
                if (((Action) step).action.equals("init")) {
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
                    communicator.deliveryNoteReceived(new DeliveryParameters(
                            ((Double) ((Action) step).values.get("targetSlump")).intValue(),
                            ((Double) ((Action) step).values.get("maxWater")).intValue(),
                            ((Double) ((Action) step).values.get("loadVolume")).intValue()));

                } else if (((Action) step).action.equals("acceptDelivery")) {
                    communicator.acceptDelivery((Boolean) ((Action) step).values.get("accepted"));

                } else if (((Action) step).action.equals("allowWaterAddition")) {
                    communicator.allowWaterAddition((Boolean) ((Action) step).values.get("allowed"));

                } else if (((Action) step).action.equals("endDelivery")) {
                    communicator.endDelivery();

                } else if (((Action) step).action.equals("changeExternalDisplayState")) {
                    communicator.changeExternalDisplayState((Boolean) ((Action) step).values.get("activated"));

                } else {
                    System.out.println("--------- TEST: action not defined: " + ((Action) step).action);

                }
            }
        }
        scheduler.reset();

        System.out.println("\nChecking packets to send:");
        assertThat(results, hasSize(this.packetsToSend.size()));
        for (int i = 0; i < this.packetsToSend.size(); i++) {
            assertArrayEquals(results.get(i), this.packetsToSend.get(i));
        }
        System.out.println("-> packages to send: OK");

        System.out.println("\nChecking events:");
        System.out.println("Expected:");
        for (Event event : this.events) {
            System.out.println("EVENT: {id: " + event.id.getIdValue() + ", value: " + event.value + "}");
        }
        System.out.println("Result:");
        for (Event event : eventResults) {
            System.out.println("EVENT: {id: " + event.id.getIdValue() + ", value: " + event.value + "}");
        }

        assertThat(eventResults, hasSize(this.events.size()));
        for (int i = 0; i < this.events.size(); i++) {
            assertThat(eventResults.get(i).id, is(this.events.get(i).id));
            assertThat(eventResults.get(i).value, is(this.events.get(i).value));
        }
        System.out.println("-> events to send: OK");
    }
}
