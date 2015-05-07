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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DecoderTest {

    @Test @Ignore
    public void decode_parts() throws IOException {
        byte[] msg1 = "AAAAAAAAAAA".getBytes("UTF-8");
        byte[] msg2 = "AAAAABBBBBB".getBytes("UTF-8");
        byte[] msg3 = "BBCCCCCCCCC".getBytes("UTF-8");

        final List<String> received = new LinkedList<>();
        MessageReceivedListener callback = new MessageReceivedListener() {
            @Override
            public void messagesReceived(String message) {
                received.add(message);
            }
        };

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
        final MessageReceivedListener callback = new MessageReceivedListener() {
            @Override
            public void messagesReceived(String message) {

            }
        };

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
        decoder.decode(new byte[]{(byte) Protocol.VERSION});

        Field stateField = Decoder.class.getDeclaredField("state");
        stateField.setAccessible(true);
        assertThat(stateField.get(decoder), instanceOf(Decoder.TypeState.class));
    }

    /**
     *  Header state
     */
    @Test
    public void header_state_accepts_correct_header_and_returns_version_state() throws IOException {
        Decoder.HeaderState state = new Decoder.HeaderState();

        Decoder.State actual = state.decode(new ByteArrayInputStream(new byte[]{(byte) Protocol.HEADER}));
        assertThat(actual, instanceOf(Decoder.VersionState.class));
    }

    @Test
    public void header_state_returns_itself_if_buffer_empty() throws IOException {
        Decoder.HeaderState state = new Decoder.HeaderState();
        Decoder.State actual = state.decode(new ByteArrayInputStream(new byte[0]));
        assertThat(actual, is((Decoder.State) state));
    }

    @Test
    public void header_state_reject_all_invalid_header_bytes_and_stays_current() throws IOException {
        Decoder.HeaderState state = new Decoder.HeaderState();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0x42); // Unknown bytes
        out.write(0x42);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Decoder.State actual = state.decode(in);
        assertThat(actual, is((Decoder.State)state));
        assertThat(in.read(), is(-1));
    }

    /**
     *  Version state
     */
    @Test
    public void version_state_accepts_correct_version_and_returns_type_state() throws IOException {
        Decoder.VersionState state = new Decoder.VersionState(null);
        Decoder.State actual = state.decode(new ByteArrayInputStream(new byte[]{(byte)Protocol.VERSION}));
        assertThat(actual, instanceOf(Decoder.TypeState.class));
    }

    /**
     *  Type state
     */
    @Test
    public void type_state_should_accept_a_correct_message_and_return_size_state() throws IOException {
        Decoder.TypeState state = new Decoder.TypeState(null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0x10); // TRAME_SLUMP_COURANT
        out.write(0x01);

        Decoder.State actual = state.decode(new ByteArrayInputStream(out.toByteArray()));
        assertThat(actual, instanceOf(Decoder.SizeState.class));
    }

    @Test
    public void type_state_should_accept_first_byte_but_not_second_and_return_header_state() throws IOException {
        Decoder.TypeState state = new Decoder.TypeState(null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0x10); // first byte of TRAME_SLUMP_COURANT
        out.write(0x42); // Unknown byte

        Decoder.State actual = state.decode(new ByteArrayInputStream(out.toByteArray()));
        assertThat(actual, instanceOf(Decoder.HeaderState.class));
    }

    @Test
    public void type_state_should_accept_first_then_second_byte_and_return_size_state() throws IOException {
        Decoder.TypeState state = new Decoder.TypeState(null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0x10); // first byte of TRAME_SLUMP_COURANT

        Decoder.State actual = state.decode(new ByteArrayInputStream(out.toByteArray()));
        assertThat(actual, is((Decoder.State)state));

        out.reset();

        out.write(0x01); // second byte of TRAME_SLUMP_COURANT
        actual = state.decode(new ByteArrayInputStream(out.toByteArray()));
        assertThat(actual, instanceOf(Decoder.SizeState.class));
    }

    @Test
    public void type_state_should_accept_first_then_refuse_second_byte_and_return_header_state() throws IOException {
        Decoder.TypeState state = new Decoder.TypeState(null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0x10); // first byte of TRAME_SLUMP_COURANT

        Decoder.State actual = state.decode(new ByteArrayInputStream(out.toByteArray()));
        assertThat(actual, is((Decoder.State)state));

        out.reset();

        out.write(0x42); // unknown byte
        actual = state.decode(new ByteArrayInputStream(out.toByteArray()));
        assertThat(actual, instanceOf(Decoder.HeaderState.class));
    }
}
