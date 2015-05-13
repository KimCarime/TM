package com.lafarge.tm;

import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class DecoderTest {

    @Test @Ignore
    public void decode_parts() throws IOException {
        byte[] msg1 = "AAAAAAAAAAA".getBytes("UTF-8");
        byte[] msg2 = "AAAAABBBBBB".getBytes("UTF-8");
        byte[] msg3 = "BBCCCCCCCCC".getBytes("UTF-8");

        final List<String> received = new LinkedList<>();
        MessageReceivedListener callback = mock(MessageReceivedListener.class);

        Decoder decoder = new Decoder(callback);
        decoder.decode(msg1);
        assertThat(received, hasSize(0));
        decoder.decode(msg2);
        assertThat(received, hasSize(1));
        assertThat(received.get(0), equalTo("AAAAAAAAAAAAAAAA"));
        decoder.decode(msg3);
        assertThat(received, hasSize(3));
        assertThat(received.get(0), equalTo("AAAAAAAAAAAAAAAA"));
        assertThat(received.get(1), equalTo("BBBBBBBB"));
        assertThat(received.get(2), equalTo("CCCCCCCCC"));
    }

    @Test @Ignore
    public void should_decode_message() throws IOException {
        final MessageReceivedListener callback = mock(MessageReceivedListener.class);

        Decoder decoder = new Decoder(callback);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0xC0); // Header
        out.write(0x01); // Version
        out.write(0x10); // TRAME_SLUMP_COURANT
        out.write(0x01);
        out.write(0x00); // Size
        out.write(0x02);
        out.write(0x00); // Data
        out.write(0xEE);
        out.write(0x42); // CRC
        out.write(0x47);

        decoder.decode(out.toByteArray());
        verify(callback, only()).slumpUpdated(12);
    }

    /**
     *  Expiration
     */
    @Test
    public void current_state_should_expire_after_one_second_between_two_decode() throws NoSuchFieldException, IllegalAccessException, IOException {
        Decoder decoder = new Decoder(null);

        decoder.decode(new byte[]{(byte) Protocol.HEADER});

        Field dateField = Decoder.class.getDeclaredField("lastDecodeDate");
        dateField.setAccessible(true);
        dateField.set(decoder, new Date(1430991523413L));

        decoder.decode(new byte[]{(byte) Protocol.VERSION});

        Field stateField = Decoder.class.getDeclaredField("state");
        stateField.setAccessible(true);
        assertThat(stateField.get(decoder), instanceOf(Decoder.HeaderState.class));
    }

    @Test
    public void current_state_should_not_expire_between_two_decode() throws NoSuchFieldException, IllegalAccessException, IOException {
        Decoder decoder = new Decoder(null);

        decoder.decode(new byte[]{(byte) Protocol.HEADER});
        decoder.decode(new byte[]{ (byte)Protocol.VERSION });

        Field stateField = Decoder.class.getDeclaredField("state");
        stateField.setAccessible(true);
        assertThat(stateField.get(decoder), instanceOf(Decoder.TypeState.class));
    }

    /**
     *  Header state
     */
    @Test
    public void header_state_accepts_correct_header_and_returns_version_state() throws IOException {
        Decoder.HeaderState state = new Decoder(null).new HeaderState();
        Decoder.State actual;

        actual = state.decode(new ByteArrayInputStream(new byte[]{ (byte)Protocol.HEADER }));
        assertThat(actual, instanceOf(Decoder.VersionState.class));
    }

    @Test
    public void header_state_returns_itself_if_buffer_empty() throws IOException {
        Decoder.HeaderState state = new Decoder(null).new HeaderState();
        Decoder.State actual;

        actual = state.decode(new ByteArrayInputStream(new byte[0]));
        assertThat(actual, is((Decoder.State) state));
    }

    @Test
    public void header_state_reject_all_invalid_header_bytes_and_stays_current() throws IOException {
        Decoder.HeaderState state = new Decoder(null).new HeaderState();
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{ 0x42, 0x42, 0x42, 0x42 });
        Decoder.State actual = state.decode(in);
        assertThat(actual, is((Decoder.State)state));
        assertThat(in.read(), is(-1));
    }

    /**
     *  Version state
     */
    @Test
    public void version_state_accepts_correct_version_and_returns_type_state() throws IOException {
        Decoder.VersionState state = new Decoder(null).new VersionState(new Decoder.State.Message());
        Decoder.State actual;

        actual = state.decode(new ByteArrayInputStream(new byte[]{ (byte)Protocol.VERSION }));
        assertThat(actual, instanceOf(Decoder.TypeState.class));
    }

    /**
     *  Type state
     */
    @Test
    public void type_state_should_accept_a_correct_message_and_return_size_state() throws IOException {
        Decoder.TypeState state = new Decoder(null).new TypeState(new Decoder.State.Message());
        Decoder.State actual;

        // TRAME_SLUMP_COURANT
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x10, 0x01 }));
        assertThat(actual, instanceOf(Decoder.SizeState.class));
    }

    @Test
    public void type_state_should_accept_first_byte_but_not_second_and_return_header_state() throws IOException {
        Decoder.TypeState state = new Decoder(null).new TypeState(new Decoder.State.Message());
        Decoder.State actual;

        // First byte of TRAME_SLUMP_COURANT and an unknown byte
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x10, 0x42 }));
        assertThat(actual, instanceOf(Decoder.HeaderState.class));
    }

    @Test
    public void type_state_should_accept_first_then_second_byte_and_return_size_state() throws IOException {
        Decoder.TypeState state = new Decoder(null).new TypeState(new Decoder.State.Message());
        Decoder.State actual;

        // First byte of TRAME_SLUMP_COURANT
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x10 }));
        assertThat(actual, is((Decoder.State) state));

        // Second byte of TRAME_SLUMP_COURANT
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x01 }));
        assertThat(actual, instanceOf(Decoder.SizeState.class));
    }

    @Test
    public void type_state_should_accept_first_then_refuse_second_byte_and_return_header_state() throws IOException {
        Decoder.TypeState state = new Decoder(null).new TypeState(new Decoder.State.Message());
        Decoder.State actual;

        // First byte of TRAME_SLUMP_COURANT
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x10 }));
        assertThat(actual, is((Decoder.State) state));

        // Unknown byte
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x42 }));
        assertThat(actual, instanceOf(Decoder.HeaderState.class));
    }

    @Test
    public void type_state_should_refuse_first_byte_and_return_header_state() throws IOException {
        Decoder.TypeState state = new Decoder(null).new TypeState(new Decoder.State.Message());
        Decoder.State actual;

        // Unknown byte
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x42 }));
        assertThat(actual, instanceOf(Decoder.HeaderState.class));
    }

    /**
     *  Size state
     */
    @Test
    public void size_state_should_accept_a_size_that_match_with_current_type_state_and_return_data_state() throws IOException {
        Decoder.SizeState state = new Decoder(null).new SizeState(Protocol.TRAME_TRACE_DONNEES_BRUTE, new Decoder.State.Message());
        Decoder.State actual;

        // Bytes of TRAME_DONNEES_BRUTES's size
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x00, 0x0D }));
        assertThat(actual, instanceOf(Decoder.DataState.class));
    }

    @Test
    public void size_state_should_refuse_a_size_that_doesnt_match_with_current_type_state_and_return_header_state() throws IOException {
        Decoder.SizeState state = new Decoder(null).new SizeState(Protocol.TRAME_TRACE_DONNEES_BRUTE, new Decoder.State.Message());
        Decoder.State actual;

        // First byte of TRAME_DONNEES_BRUTES's size and an unknown byte
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x00, 0x42 }));
        assertThat(actual, instanceOf(Decoder.HeaderState.class));
    }

    @Test
    public void size_state_should_accept_a_size_that_match_with_current_type_state_and_return_data_state_in_two_decode() throws IOException {
        Decoder.SizeState state = new Decoder(null).new SizeState(Protocol.TRAME_TRACE_DONNEES_BRUTE, new Decoder.State.Message());
        Decoder.State actual;

        // First byte of TRAME_DONNEES_BRUTES's size
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x00 }));
        assertThat(actual, is((Decoder.State) state));

        // Second byte of TRAME_DONNEES_BRUTES's size
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x0D }));
        assertThat(actual, instanceOf(Decoder.DataState.class));
    }

    @Test
    public void size_state_should_refuse_a_size_that_doesnt_match_with_current_type_state_and_return_header_state_in_two_decode() throws IOException {
        Decoder.SizeState state = new Decoder(null).new SizeState(Protocol.TRAME_TRACE_DONNEES_BRUTE, new Decoder.State.Message());
        Decoder.State actual;

        // First byte of TRAME_DONNEES_BRUTES 's size
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x00 }));
        assertThat(actual, is((Decoder.State)state));

        // Unknown byte
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x42 }));
        assertThat(actual, instanceOf(Decoder.HeaderState.class));
    }

    @Test
    public void size_state_should_refuse_first_byte_and_return_header_state() throws IOException {
        Decoder.SizeState state = new Decoder(null).new SizeState(Protocol.TRAME_TRACE_DONNEES_BRUTE, new Decoder.State.Message());
        Decoder.State actual;

        // Unknown byte
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x42 }));
        assertThat(actual, instanceOf(Decoder.HeaderState.class));
    }

    @Test
    public void size_state_should_return_crc_state_if_size_found_is_equal_to_zero_and_match_with_type_size() throws IOException {
        Decoder.State.Message message = new Decoder.State.Message();
        message.data = new byte[] { 0x00 };

        Decoder.SizeState state = new Decoder(null).new SizeState(Protocol.TRAME_NOTIFICATION_PASSAGE_EN_MALAXAGE, message);
        Decoder.State actual;

        // Bytes of TRAME_NOTIFICATION_PASSAGE_EN_MALAXAGE's size
        actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x00, 0x00 }));
        assertThat(actual, instanceOf(Decoder.CrcState.class));
    }

    /**
     *  Data state
     */
    @Test
    public void data_state_should_accept_bytes_until_the_expected_nb_bytes_is_reach_and_return_crc_state() throws IOException {
        final int randomSize = new Random().nextInt(15) + 1;
        Decoder.DataState state = new Decoder(null).new DataState(randomSize, new Decoder.State.Message());

        Decoder.State actual = null;
        for (int i = 0; i < randomSize; i++) {
            actual = state.decode(new ByteArrayInputStream(new byte[]{ 0x42 }));
            if (i < randomSize - 1) {
                assertThat(actual, is((Decoder.State) state));
            }
        }
        assertThat(actual, instanceOf(Decoder.CrcState.class));
    }

    /**
     *  CRC State
     */
    @Test
    public void crc_state_should_accept_crc_and_return_null() throws IOException {
        Decoder.State.Message message = new Decoder.State.Message();
        message.header = (byte)Protocol.HEADER;
        message.version = (byte)Protocol.VERSION;
        message.typeMsb = 0x10;
        message.typeLsb = 0x01;
        message.sizeMsb = 0x00;
        message.sizeLsb = 0x02;
        message.data = new byte[]{ 0x00, (byte)0xEE };

        MessageReceivedListener listener = mock(MessageReceivedListener.class);
        Decoder.CrcState state = new Decoder(listener).new CrcState(message);
        Decoder.State actual;

        // Send a valid CRC
        actual = state.decode(new ByteArrayInputStream(new byte[]{ (byte)0x42, (byte)0x47 }));
        assertThat(actual, instanceOf(Decoder.HeaderState.class));
        verify(listener)
        ;
    }

    @Test
    public void crc_state_should_refuse_crc_and_return_header_state() throws IOException {
        Decoder.State.Message message = new Decoder.State.Message();
        message.header = (byte)Protocol.HEADER;
        message.version = (byte)Protocol.VERSION;
        message.typeMsb = 0x10;
        message.typeLsb = 0x01;
        message.sizeMsb = 0x00;
        message.sizeLsb = 0x02;
        message.data = new byte[]{ 0x00, (byte)0xEE };

        Decoder.CrcState state = new Decoder(null).new CrcState(message);
        Decoder.State actual;

        // Send a wrong CRC
        actual = state.decode(new ByteArrayInputStream(new byte[]{ (byte)0x42, (byte)0x42 }));
        assertThat(actual, instanceOf(Decoder.HeaderState.class));
    }
}
