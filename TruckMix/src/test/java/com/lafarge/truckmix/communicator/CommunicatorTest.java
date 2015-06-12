package com.lafarge.truckmix.communicator;

import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.events.Event;
import com.lafarge.truckmix.communicator.listeners.CommunicatorBytesListener;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.EventListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;
import com.lafarge.truckmix.encoder.Encoder;
import com.lafarge.truckmix.encoder.listeners.MessageSentListener;
import com.lafarge.truckmix.utils.MessageReceivedFactory;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
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
    private EventListener eventListener;
    private TruckParameters truckParameters;
    private DeliveryParameters deliveryParameters;

    @Before
    public void setup() {
        truckParameters = new TruckParameters(3.4563, 563.376, -39.844, 4.3254, 24, 15, 120, 45, 60, TruckParameters.CommandPumpMode.SEMI_AUTO, 2.5, 0.0, 2.5, 0.0, 3, 180, 10, 90, 64, 5, 6);
        deliveryParameters = new DeliveryParameters(150, 0, 6);

        scheduler = new SchedulerMock();
        encoder = new Encoder(mock(MessageSentListener.class));
        bytesListener = mock(CommunicatorBytesListener.class);
        communicatorListener = mock(CommunicatorListener.class);
        eventListener = mock(EventListener.class);
        communicator = new Communicator(bytesListener, communicatorListener, loggerListener, eventListener, scheduler);
        communicator.setConnected(true);
        reset(bytesListener); // drop the first fake and endOfDelivery message for test coherence.
    }

    @Test
    public void should_send_truckParameters_on_request() {
        communicator.setTruckParameters(truckParameters);
        communicator.received(MessageReceivedFactory.createTruckParametersRequestMessage());
        verify(bytesListener).send(encoder.fake());
        verify(bytesListener).send(encoder.truckParameters(truckParameters));
    }

    @Test
    public void should_not_send_deliveryParameters_on_request_in_WAITING_FOR_DELIVERY_NOTE() {
        communicator.received(MessageReceivedFactory.createDeliveryParametersRequestMessage());
        verify(bytesListener, never()).send((byte[]) any());
    }

    @Test
    public void should_send_deliveryParameters_on_request_in_WAITING_DELIVERY_NOTE_ACCEPTATION_state() {
        communicator.deliveryNoteReceived(deliveryParameters);
        communicator.received(MessageReceivedFactory.createDeliveryParametersRequestMessage());
        verify(bytesListener).send(encoder.fake());
        verify(bytesListener).send(encoder.deliveryParameters(deliveryParameters));
    }

    @Test
    public void should_send_deliveryParameters_on_request_in_DELIVERY_IN_PROGRESS() {
        communicator.deliveryNoteReceived(deliveryParameters);
        communicator.acceptDelivery(true);
        communicator.received(MessageReceivedFactory.createDeliveryParametersRequestMessage());
        communicator.deliveryNoteReceived(deliveryParameters);
        verify(bytesListener, times(2)).send(encoder.fake());
        verify(bytesListener).send(encoder.beginningOfDelivery());
        verify(bytesListener).send(encoder.deliveryParameters(deliveryParameters));
    }

    @Test
    public void should_stop_timer_when_receive_deliveryParametersRequest() {
        communicator.received(MessageReceivedFactory.createDeliveryParametersRequestMessage());
        scheduler.flush();
        verify(bytesListener, never()).send((byte[]) any());
    }

    @Test
    public void should_inform_waterAdditionRequest_only_in_DELIVERY_IN_PROGRESS_state() {
        communicator.setWaterRequestAllowed(true);
        communicator.received(MessageReceivedFactory.createWaterAdditionRequestMessage());
        verify(communicatorListener, never()).waterAdditionRequest(11);
        communicator.deliveryNoteReceived(deliveryParameters);
        communicator.received(MessageReceivedFactory.createDeliveryParametersRequestMessage());
        communicator.received(MessageReceivedFactory.createWaterAdditionRequestMessage());
        verify(communicatorListener, never()).waterAdditionRequest(11);
        communicator.acceptDelivery(true);
        communicator.received(MessageReceivedFactory.createWaterAdditionRequestMessage());
        verify(communicatorListener).waterAdditionRequest(11);
    }

    @Test
    public void should_send_endOfDelivery_for_each_tick_in_WAITING_FOR_DELIVERY_NOTE_state() {
        final int n = 10;
        scheduler.flush(n);
        verify(bytesListener, times(n)).send(encoder.fake());
        verify(bytesListener, times(n)).send(encoder.endOfDelivery());
        assertThat(scheduler.hasSchedule(), is(true));
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
        scheduler.flush();
        verify(bytesListener, times(1)).send(encoder.fake());
        verify(bytesListener, times(1)).send(encoder.endOfDelivery());
        communicator.deliveryNoteReceived(deliveryParameters);
        communicator.received(MessageReceivedFactory.createDeliveryParametersRequestMessage());
        scheduler.flush();
        verify(bytesListener, times(2)).send(encoder.fake());
        verify(bytesListener, times(1)).send(encoder.endOfDelivery()); // No additional invocations
        communicator.acceptDelivery(false);
        scheduler.flush();
        verify(bytesListener, times(4)).send(encoder.fake());
        verify(bytesListener, times(3)).send(encoder.endOfDelivery());
    }

    @Test
    public void should_stop_timer_in_WAITING_FOR_DELIVERY_NOTE_ACCEPTATION_when_sync_was_done() {
        communicator.received(MessageReceivedFactory.createDeliveryParametersRequestMessage());
        scheduler.flush();
        verify(bytesListener, never()).send((byte[]) any());
    }

    @Test
    public void should_start_timer_if_setConnected_to_true_only_if_sync_was_not_done() {
        communicator.setConnected(false);
        communicator.setConnected(true);
        scheduler.flush();
        verify(bytesListener, times(2)).send(encoder.fake());
        verify(bytesListener, times(2)).send(encoder.endOfDelivery());
    }

    @Test
    public void should_not_start_timer_when_setConnected_to_true_if_sync_was_done() {
        communicator.received(MessageReceivedFactory.createDeliveryParametersRequestMessage());
        communicator.setConnected(false);
        communicator.setConnected(true);
        scheduler.flush();
        verify(bytesListener, never()).send((byte[]) any());
    }

    @Test
    public void should_not_send_water_addition_request_if_feature_is_not_activated() {
        communicator.setWaterRequestAllowed(false);
        communicator.received(MessageReceivedFactory.createWaterAdditionRequestMessage());
        verify(communicatorListener, never()).waterAdditionRequest(anyInt());
    }

    @Test
    public void should_not_send_events_if_feature_is_not_activated() {
        communicator.setQualityTrackingActivated(false);
        communicator.deliveryNoteReceived(deliveryParameters);
        communicator.received(MessageReceivedFactory.createDeliveryParametersRequestMessage());
        communicator.acceptDelivery(true);
        for (int i = 0; i < 10; i++) {
            communicator.received(MessageReceivedFactory.createSlumpUpdatedMessage());
        }
        verify(eventListener, never()).onNewEvents((Event) any());
    }
}
