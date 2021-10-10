package org.chord.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HashUtilTest {

    @Test
    public void testHexToIntSingleChar() {
        String testHex = "a";
        Integer expected = 10;
        Integer actual = HashUtil.hexToInt(testHex);
        assertEquals(expected, actual);
    }

    @Test
    public void testHexToIntTwoChars() {
        String testHex = "a3";
        Integer expected = 163;
        Integer actual = HashUtil.hexToInt(testHex);
        assertEquals(expected, actual);
    }

    @Test
    public void testHexToIntThreeChars() {
        String testHex = "3bd";
        Integer expected = 957;
        Integer actual = HashUtil.hexToInt(testHex);
        assertEquals(expected, actual);
    }

    @Test
    public void testHexToIntFourChars() {
        String testHex = "d92f";
        Integer expected = 55599;
        Integer actual = HashUtil.hexToInt(testHex);
        assertEquals(expected, actual);

        testHex = "ffff";
        expected = 65535;
        actual = HashUtil.hexToInt(testHex);
        assertEquals(expected, actual);

        testHex = "0000";
        expected = 0;
        actual = HashUtil.hexToInt(testHex);
        assertEquals(expected, actual);
    }
}
