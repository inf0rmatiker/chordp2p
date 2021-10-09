package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Message {

    private static final Logger log = LoggerFactory.getLogger(Message.class);

    public String hostname, ipAddress;
    public Integer port;
    public byte[] marshaledBytes;

    public enum MessageType {
        REGISTER_PEER_REQUEST, REGISTER_PEER_RESPONSE
    }

    /**
     * Writes an integer to the output stream.
     *
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @param value            The int we are writing.
     * @throws IOException If fails to write to the DataOutputStream
     */
    public static void writeInt(DataOutputStream dataOutputStream, int value) throws IOException {
        dataOutputStream.writeInt(value);
    }

    /**
     * Reads and returns an integer to the input stream.
     *
     * @param dataInputStream The DataInputStream we are reading from.
     * @return The int we read.
     * @throws IOException If fails to read from the DataInputStream
     */
    public static int readInt(DataInputStream dataInputStream) throws IOException {
        return dataInputStream.readInt();
    }

    /**
     * Reads a string from the DataInputStream passed in as follows:
     * 1. Reads the string length as an integer.
     * 2. Reads the string bytes; creates and returns a string from said bytes.
     *
     * @param dataInputStream The DataInputStream containing the bytes we are reading.
     * @return The String, whose length is specified before the string bytes.
     * @throws IOException If fails to read from DataInputStream
     */
    public static String readString(DataInputStream dataInputStream) throws IOException {
        int stringLength = dataInputStream.readInt();
        byte[] stringBytes = new byte[stringLength];
        dataInputStream.readFully(stringBytes, 0, stringLength);
        return new String(stringBytes);
    }

    /**
     * Writes a string to the DataOutputString passed in as follows:
     * 1. Writes the string length as an integer
     * 2. Writes the string bytes
     *
     * @param dataOutputStream DataOutputStream containing the byte array we are writing to
     * @param value            The String value we are writing to the byte array
     * @throws IOException If fails to write to DataOutputStream
     */
    public static void writeString(DataOutputStream dataOutputStream, String value) throws IOException {
        dataOutputStream.writeInt(value.length());
        dataOutputStream.writeBytes(value);
    }

    /**
     * Reads a string array from the DataInputStream passed in as follows:
     * 1. Reads the array length as an integer n.
     * 2. Allocates a string array of size n.
     * 3. Iterates n times, reading a string each time into the string array.
     *
     * @param dataInputStream The DataInputStream containing the bytes we are reading.
     * @throws IOException If fails to read from DataInputStream
     */
    public static String[] readStringArray(DataInputStream dataInputStream) throws IOException {
        int count = readInt(dataInputStream);
        String[] array = new String[count];
        for (int i = 0; i < count; i++) {
            array[i] = readString(dataInputStream);
        }
        return array;
    }

    /**
     * Writes a string array to the DataOutputString passed in as follows:
     * 1. Writes the string array length (n) as an integer.
     * 2. Iterates n times, writing a string from the array to the stream each time.
     *
     * @param dataOutputStream DataOutputStream containing the byte array we are writing to
     * @param values           The String values we are writing to the byte array
     * @throws IOException If fails to write to DataOutputStream
     */
    public static void writeStringArray(DataOutputStream dataOutputStream, String[] values) throws IOException {
        writeInt(dataOutputStream, values.length);
        for (String value : values) {
            writeString(dataOutputStream, value);
        }
    }

    /**
     * Reads a string List from the DataInputStream passed in as follows:
     * 1. Reads the array length as an integer n.
     * 2. Allocates a string array of size n.
     * 3. Iterates n times, reading a string each time into the string List.
     *
     * @param dataInputStream The DataInputStream containing the bytes we are reading.
     * @throws IOException If fails to read from DataInputStream
     */
    public static List<String> readStringList(DataInputStream dataInputStream) throws IOException {
        int count = readInt(dataInputStream);
        List<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(readString(dataInputStream));
        }
        return list;
    }

    /**
     * Writes a string List to the DataOutputString passed in as follows:
     * 1. Writes the string List length (n) as an integer.
     * 2. Iterates n times, writing a string from the List to the stream each time.
     *
     * @param dataOutputStream DataOutputStream containing the byte array we are writing to
     * @param values           The String values we are writing to the byte array
     * @throws IOException If fails to write to DataOutputStream
     */
    public static void writeStringList(DataOutputStream dataOutputStream, List<String> values) throws IOException {
        writeInt(dataOutputStream, values.size());
        for (String value : values) {
            writeString(dataOutputStream, value);
        }
    }

    /**
     * Writes a Set of Strings to the output stream.
     *
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @param values           The Set<String> we are writing.
     * @throws IOException If fails to write to DataOutputStream
     */
    public static void writeStringSet(DataOutputStream dataOutputStream, Set<String> values) throws IOException {
        writeInt(dataOutputStream, values.size());
        for (String value : values) {
            writeString(dataOutputStream, value);
        }
    }

    /**
     * Reads a Set of Strings from the input stream.
     *
     * @param dataInputStream The DataInputStream we are reading from.
     * @return The Set of Strings we read
     * @throws IOException If fails to read from the DataInputStream
     */
    public static Set<String> readStringSet(DataInputStream dataInputStream) throws IOException {
        int count = readInt(dataInputStream);
        Set<String> values = new HashSet<>();
        for (int i = 0; i < count; i++) {
            values.add(readString(dataInputStream));
        }
        return values;
    }

    /**
     * Converts an integer to a MessageType enum
     *
     * @param type integer type
     * @return MessageType enum
     */
    public static MessageType typeFromInteger(int type) {
        switch (type) {
            case 0:
                return MessageType.REGISTER_PEER_REQUEST;
            case 1:
                return MessageType.REGISTER_PEER_RESPONSE;
            default:
                return null;
        }
    }

    // --- Static helper functions ---

    /**
     * Converts a MessageType enum to an integer
     *
     * @param type MessageType enum
     * @return integer type
     */
    public static Integer integerFromType(MessageType type) {
        if (type == null) {
            throw new NullPointerException("MessageType cannot be null!");
        }
        switch (type) {
            case REGISTER_PEER_REQUEST:
                return 0;
            case REGISTER_PEER_RESPONSE:
                return 1;
            default:
                return -1;
        }
    }

    public abstract MessageType getType();

    public String getHostname() {
        return hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public byte[] getMarshaledBytes() {
        return marshaledBytes;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Message)) return false;
        Message otherMessage = (Message) other;
        return (this.getType().equals(otherMessage.getType()) &&
                this.getHostname().equals(otherMessage.getHostname()) &&
                this.getIpAddress().equals(otherMessage.getIpAddress()) &&
                this.getPort().equals(otherMessage.getPort())
        );
    }

    /**
     * Marshals/packs the object header fields into the message's byte array representation.
     * This is a partial implementation of the full marshaling process; subclasses are expected to complete this.
     * The message header is represented as follows:
     * - message type (int 4 bytes)
     * - hostname length (int 4 bytes)
     * - hostname string (char[] n bytes)
     * - ip length (int 4 bytes)
     * - ip string (char[] n bytes)
     * - port (int 4 bytes)
     *
     * @param dataOutputStream The DataOutputStream we are writing to.
     */
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(integerFromType(this.getType()));
        writeString(dataOutputStream, this.hostname);
        writeString(dataOutputStream, this.ipAddress);
        dataOutputStream.writeInt(this.port);
    }

    /**
     * Marshals all the fields of this object into the byte array field.
     *
     * @throws IOException If unable to write to the output stream
     */
    public void marshal() throws IOException {
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));
        marshal(dataOutStream);
        collectByteStream(dataOutStream, byteOutStream);
        dataOutStream.close();
        byteOutStream.close();
    }

    /**
     * Unmarshals/unpacks the header fields from the message's byte array into the instance variables.
     * The message header is represented as follows:
     * - hostname length (int 4 bytes)
     * - hostname string (char[] n bytes)
     * - ip length (int 4 bytes)
     * - ip string (char[] n bytes)
     * - port (int 4 bytes)
     *
     * @throws IOException If fails to read from DataInputStream
     */
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        this.hostname = readString(dataInputStream);
        this.ipAddress = readString(dataInputStream);
        this.port = dataInputStream.readInt();
    }

    /**
     * Completes the marshaling process by flushing the DataOutputStream to the ByteArrayOutputStream, then
     * collecting as a byte array into the object's marshalledBytes field. Lastly, the streams are closed.
     *
     * @param dataOutStream The DataOutputStream we have been writing to.
     * @param byteOutStream The ByteArrayOutputStream we have been flushing bytes to.
     * @throws IOException If fails to write to DataOutputStream
     */
    public void collectByteStream(DataOutputStream dataOutStream, ByteArrayOutputStream byteOutStream)
            throws IOException {
        dataOutStream.flush();
        this.marshaledBytes = byteOutStream.toByteArray();
    }
}
