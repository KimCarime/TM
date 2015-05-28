package com.lafarge.tm;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class CommunicatorTest {

    private Encoder encoder;
    private Communicator communicator;
    private CommunicatorListener communicatorListener;
    private LoggerListener loggerListener;

    @Before
    public void setup() {
        encoder = new Encoder();
        communicatorListener = mock(CommunicatorListener.class);
        communicator = new Communicator(this.communicatorListener, new LoggerListener() {
            @Override
            public void log(String log) {
                System.out.println(log);
            }
        });
    }

    @Test
    public void should_send_truckParameters_on_request_in_every_state() {
        byte[] truckParametersRequestBytes = new byte[]{(byte) 0xC0, 0x01, 0x50, 0x02, 0x00, 0x00, (byte) 0x9C, 0x1B};
        byte[] truckParametersBytes = new byte[]{0x00, 0x01, 0x2A};
        TruckParameters parameters = new TruckParameters(3.4563, 563.376, -39.844, 4.3254, 24, 15, 120, 45, 60, TruckParameters.CommandPumpMode.SEMI_AUTO, 2.5, 0.0, 2.5, 0.0, 3, 180, 10, 90, 64, 5, 6);

        communicator.setTruckParameters(parameters);

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        communicator.received(truckParametersRequestBytes);
        verify(communicatorListener).send(truckParametersBytes);

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION);
        communicator.received(truckParametersRequestBytes);
        verify(communicatorListener).send(truckParametersBytes);

        communicator.setState(Communicator.State.DELIVERY_IN_PROGRESS);
        communicator.received(truckParametersRequestBytes);
        verify(communicatorListener).send(truckParametersBytes);
    }

    @Test
    public void should_sendDeliveryParameters_on_request_in_WAITING_DELIVERY_NOTE_ACCEPTATION_and_DELIVERY_IN_PROGRESS_states() {
        byte[] deliveryParametersRequestBytes = new byte[]{(byte) 0xC0, 0x01, 0x50, 0x03, 0x00, 0x00, (byte) 0xCD, (byte) 0xDB};
        byte[] deliveryParametersBytes = new byte[]{(byte) 0xC0};

        communicator.deliveryNoteReceived(new DeliveryParameters(150, 0, 6));

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        communicator.received(deliveryParametersRequestBytes);
        verify(communicatorListener, never()).send(deliveryParametersBytes);

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION);
        communicator.received(deliveryParametersRequestBytes);
        verify(communicatorListener).send(deliveryParametersBytes);

        communicator.setState(Communicator.State.DELIVERY_IN_PROGRESS);
        communicator.received(deliveryParametersRequestBytes);
        verify(communicatorListener).send(deliveryParametersBytes);
    }

    @Test
    public void should_stop_send_periodicaly_endOfDelivery_when_receive_deliveryParametersRequest() {
        final byte[] deliveryParametersRequestBytes = new byte[]{(byte)0xC0, 0x01, 0x50 ,0x03 ,0x00, 0x00, (byte)0xCD, (byte)0xDB};
        final byte[] endOfDeliveryBytes = encoder.endOfDelivery();

        final int n = 3;
        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        verify(communicatorListener, after((int) (n * Communicator.RESET_STATE_IN_MILLIS) + 100).times(n + 1)).send(endOfDeliveryBytes);

        communicator.received(deliveryParametersRequestBytes);
        verify(communicatorListener, after((int) (n * Communicator.RESET_STATE_IN_MILLIS) + 100).times(n + 1)).send(endOfDeliveryBytes);
    }
    
    @Test
    public void should_send_addWaterPermission_on_request_only_in_DELIVERY_IN_PROGRESS_state() {
        final boolean accepted = true;
        final byte[] addWaterRequestBytes = new byte[]{(byte) 0xC0, 0x01, 0x50, 0x01, 0x00, 0x01, 0x0B, 0x5B, 0x7A};
        final byte[] addWaterPermissionBytes = encoder.waterAdditionPermission(accepted);

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        communicator.received(addWaterRequestBytes);
        verify(communicatorListener, never()).waterAdditionRequest(11);
        verify(communicatorListener, never()).send(addWaterPermissionBytes);

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION);
        communicator.received(addWaterRequestBytes);
        verify(communicatorListener, never()).waterAdditionRequest(11);
        verify(communicatorListener, never()).send(addWaterPermissionBytes);

        communicator.setState(Communicator.State.DELIVERY_IN_PROGRESS);
        communicator.received(addWaterRequestBytes);
        verify(communicatorListener).waterAdditionRequest(11);
        verify(communicatorListener).send(addWaterPermissionBytes);
    }

    @Test
    public void should_send_endOfDelivery_for_each_tick_in_WAITING_FOR_DELIVERY_NOTE_state() {
        final byte[] endOfDeliveryBytes = encoder.endOfDelivery();
        final int n = 3;

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        verify(communicatorListener, after((int) (n * Communicator.RESET_STATE_IN_MILLIS) + 100).times(n + 1)).send(endOfDeliveryBytes);
    }

    @Test
    public void should_cancel_timer_in_other_state_than_WAITING_FOR_DELIVERY_NOTE_state_and_send_endOfDelivery_for_each_tick() {
        final byte[] endOfDeliveryBytes = encoder.endOfDelivery();
        final int n = 1;

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        verify(communicatorListener, after((int) (n * Communicator.RESET_STATE_IN_MILLIS) + 100).times(n + 1)).send(endOfDeliveryBytes);

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION);
        verify(communicatorListener, after(10 * 1000).times(n + 1)).send(encoder.endOfDelivery()); // No additional invocations

        communicator.setState(Communicator.State.DELIVERY_IN_PROGRESS);
        verify(communicatorListener, after((int) (n * Communicator.RESET_STATE_IN_MILLIS)).times(1)).send(encoder.beginningOfDelivery());
    }

    @Test
    public void should_cancel_and_restart_correctly_timer_in_WAITING_FOR_DELIVERY_NOTE_state_and_send_endOfDelivery_for_each_tick() {
        final byte[] endOfDeliveryBytes = encoder.endOfDelivery();
        final int n = 5;
        final int j = 3;

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        verify(communicatorListener, after((int) (n * Communicator.RESET_STATE_IN_MILLIS) + 100).times(n + 1)).send(endOfDeliveryBytes);

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE_ACCEPTATION);
        verify(communicatorListener, after((int) (5 * Communicator.RESET_STATE_IN_MILLIS)).times(n + 1)).send(encoder.endOfDelivery()); // No additional invocations

        communicator.setState(Communicator.State.WAITING_FOR_DELIVERY_NOTE);
        verify(communicatorListener, after((int) (j * Communicator.RESET_STATE_IN_MILLIS) + 100).times(n + j + 2)).send(endOfDeliveryBytes);
    }
}
