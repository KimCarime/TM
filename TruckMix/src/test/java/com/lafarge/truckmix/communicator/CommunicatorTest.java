package com.lafarge.truckmix.communicator;

import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.listeners.CommunicatorBytesListener;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;
import com.lafarge.truckmix.encoder.Encoder;
import com.lafarge.truckmix.encoder.listeners.MessageSentListener;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.*;

public class CommunicatorTest {

    private SchedulerMock scheduler;
    private Encoder encoder;
    private Communicator communicator;
    private CommunicatorBytesListener bytesListener;
    private CommunicatorListener communicatorListener;
    private final LoggerListener loggerListener = new LoggerListener() {
        @Override
        public void log(String log) {
            System.out.println(log);
        }
    };

    @Before
    public void setup() {
        scheduler = new SchedulerMock();
        encoder = new Encoder(mock(MessageSentListener.class));
        bytesListener = mock(CommunicatorBytesListener.class);
        communicatorListener = mock(CommunicatorListener.class);
        communicator = new Communicator(bytesListener, communicatorListener, loggerListener, scheduler);
    }

    @Test
    public void should_send_truckParameters_on_request_in_every_state() {
        final TruckParameters parameters = new TruckParameters(3.4563, 563.376, -39.844, 4.3254, 24, 15, 120, 45, 60, TruckParameters.CommandPumpMode.SEMI_AUTO, 2.5, 0.0, 2.5, 0.0, 3, 180, 10, 90, 64, 5, 6);
        final byte[] truckParametersRequestBytes = new byte[]{(byte) 0xC0, 0x01, 0x50, 0x02, 0x00, 0x00, (byte) 0x9C, 0x1B};
        final byte[] truckParametersBytes = encoder.truckParameters(parameters);
        final List<byte[]> result = new LinkedList<byte[]>();

        CommunicatorBytesListener bytesListener = new CommunicatorBytesListener() {
            @Override
            public void send(byte[] bytes) {
                if (Arrays.equals(bytes, truckParametersBytes)) {
                    result.add(bytes);
                }
            }
        };
        Communicator communicator = new Communicator(bytesListener, communicatorListener, loggerListener);
        communicator.setTruckParameters(parameters);
        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        communicator.received(truckParametersRequestBytes);
        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION);
        communicator.received(truckParametersRequestBytes);
        communicator.setState(Communicator.State.DELIVERY_IN_PROGRESS);
        communicator.received(truckParametersRequestBytes);

        assertThat(result, hasSize(3));
        for (int i = 0; i < result.size(); i++) {
            assertArrayEquals(result.get(i), truckParametersBytes);
        }
    }

    @Test
    public void should_send_deliveryParameters_on_request_in_WAITING_DELIVERY_NOTE_ACCEPTATION_and_DELIVERY_IN_PROGRESS_states() {
        final DeliveryParameters params = new DeliveryParameters(150, 0, 6);
        final byte[] deliveryParametersRequestBytes = new byte[]{(byte) 0xC0, 0x01, 0x50, 0x03, 0x00, 0x00, (byte) 0xCD, (byte) 0xDB};
        final byte[] deliveryParametersBytes = encoder.deliveryParameters(params);
        final List<byte[]> result = new LinkedList<byte[]>();

        Communicator communicator = new Communicator(new CommunicatorBytesListener() {
            @Override
            public void send(byte[] bytes) {
                if (Arrays.equals(bytes, deliveryParametersBytes)) {
                    result.add(bytes);
                }
            }
        }, communicatorListener, loggerListener, scheduler);
        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        communicator.received(deliveryParametersRequestBytes);
        communicator.deliveryNoteReceived(params);
        communicator.received(deliveryParametersRequestBytes);
        communicator.setState(Communicator.State.DELIVERY_IN_PROGRESS);
        communicator.received(deliveryParametersRequestBytes);

        assertThat(result, hasSize(2));
        for (int i = 0; i < result.size(); i++) {
            assertArrayEquals(result.get(i), deliveryParametersBytes);
        }
    }

    @Test
    public void should_stop_timer_when_receive_deliveryParametersRequest() {
        final byte[] deliveryParametersRequestBytes = new byte[]{(byte)0xC0, 0x01, 0x50 ,0x03 ,0x00, 0x00, (byte)0xCD, (byte)0xDB};
        final byte[] endOfDeliveryBytes = encoder.endOfDelivery();
        CommunicatorBytesListener bytesListener = mock(CommunicatorBytesListener.class);
        Communicator communicator = new Communicator(bytesListener, communicatorListener, loggerListener, scheduler);

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        scheduler.flush();
        verify(bytesListener, times(2)).send(endOfDeliveryBytes);
        communicator.received(deliveryParametersRequestBytes);
        scheduler.flush();
        verify(bytesListener, times(2)).send(endOfDeliveryBytes);
    }

    @Test
    public void should_inform_waterAdditionRequest_only_in_DELIVERY_IN_PROGRESS_state() {
        final byte[] addWaterRequestBytes = new byte[]{(byte) 0xC0, 0x01, 0x50, 0x01, 0x00, 0x01, 0x0B, 0x5B, 0x7A};

        CommunicatorListener communicatorListener = mock(CommunicatorListener.class);
        Communicator communicator = new Communicator(bytesListener, communicatorListener, loggerListener, scheduler);

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        communicator.received(addWaterRequestBytes);
        verify(communicatorListener, never()).waterAdditionRequest(11);
        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION);
        communicator.received(addWaterRequestBytes);
        verify(communicatorListener, never()).waterAdditionRequest(11);
        communicator.setState(Communicator.State.DELIVERY_IN_PROGRESS);
        communicator.received(addWaterRequestBytes);
        verify(communicatorListener).waterAdditionRequest(11);
    }

    @Test
    public void should_send_endOfDelivery_for_each_tick_in_WAITING_FOR_DELIVERY_NOTE_state() {
        final byte[] endOfDeliveryBytes = encoder.endOfDelivery();
        final int n = 2;

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        scheduler.flush(n);
        verify(bytesListener, times(n + 1)).send(endOfDeliveryBytes);
        assertThat(scheduler.hasSchedule(), is(true));
    }

    @Test (expected = IllegalStateException.class)
    public void should_throw_exception_when_setting_new_delivery_parameters_if_internal_state_different_from_WAITING_FOR_DELIVERY_NOTE() {
        communicator.setState(Communicator.State.DELIVERY_IN_PROGRESS);
        communicator.deliveryNoteReceived(new DeliveryParameters(42, 42, 42));
    }

    @Test (expected = IllegalArgumentException.class)
    public void should_throw_exception_when_passing_null_truck_parameters() {
        communicator.setTruckParameters(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void should_throw_exception_when_passing_null_delivery_parameters() {
        communicator.deliveryNoteReceived(null);
    }

    @Test
    public void should_stop_and_restart_correctly_timer_in_WAITING_FOR_DELIVERY_NOTE_state_and_send_endOfDelivery_for_each_tick() {
        final byte[] endOfDeliveryBytes = encoder.endOfDelivery();
        final int n = 1;
        final int j = 1;

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        scheduler.flush();
        verify(bytesListener, times(n + 1)).send(endOfDeliveryBytes);
        communicator.setState(Communicator.State.DELIVERY_IN_PROGRESS);
        scheduler.flush();
        verify(bytesListener, times(n + 1)).send(encoder.endOfDelivery()); // No additional invocations
        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        scheduler.flush();
        verify(bytesListener, times(n + j + 2)).send(endOfDeliveryBytes);
    }

    @Test
    public void should_not_stop_timer_in_WAITING_FOR_DELIVERY_NOTE_ACCEPTATION_when_sync_was_not_done() {
        final byte[] endOfDeliveryBytes = encoder.endOfDelivery();

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION);
        scheduler.flush();
        verify(bytesListener, times(2)).send(endOfDeliveryBytes);
    }

    @Test
    public void should_stop_timer_in_WAITING_FOR_DELIVERY_NOTE_ACCEPTATION_when_sync_was_done() {
        final byte[] deliveryParametersRequestBytes = new byte[]{(byte) 0xC0, 0x01, 0x50, 0x03, 0x00, 0x00, (byte) 0xCD, (byte) 0xDB};
        final byte[] endOfDeliveryBytes = encoder.endOfDelivery();

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        communicator.received(deliveryParametersRequestBytes);
        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION);
        scheduler.flush();
        verify(bytesListener).send(endOfDeliveryBytes);
    }

    @Test
    public void should_not_start_timer_when_setConnected_to_true_when_sync_was_done() {
        final byte[] deliveryParametersRequestBytes = new byte[]{(byte) 0xC0, 0x01, 0x50, 0x03, 0x00, 0x00, (byte) 0xCD, (byte) 0xDB};
        final byte[] endOfDeliveryBytes = encoder.endOfDelivery();

        communicator.setConnected(false);
        communicator.received(deliveryParametersRequestBytes);
        communicator.setConnected(true);
        verify(bytesListener, never()).send(endOfDeliveryBytes);
    }

    @Test
    public void should_start_timer_when_setConnected_to_true_only_if_sync_was_not_done() {
        final byte[] endOfDeliveryBytes = encoder.endOfDelivery();

        communicator.setConnected(false);
        communicator.setConnected(true);
        scheduler.flush();
        verify(bytesListener, times(2)).send(endOfDeliveryBytes);
    }
}
