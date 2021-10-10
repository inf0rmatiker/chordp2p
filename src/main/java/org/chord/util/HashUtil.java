package org.chord.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashUtil {
    private static final Logger log = LoggerFactory.getLogger(HashUtil.class);

    /**
     * Converts a set of bytes into a Hexadecimal representation.
     *
     * @param buf
     * @return
     */
    public static String bytesToHex(byte[] buf) {
        StringBuffer strBuf = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            int byteValue = (int) buf[i] & 0xff;
            if (byteValue <= 15) {
                strBuf.append("0");
            }
            strBuf.append(Integer.toString(byteValue, 16));
        }
        return strBuf.toString();
    }

    /**
     * Converts a specified hexadecimal String into a set of bytes.
     *
     * @param hexString
     * @return
     */
    public static byte[] hexToBytes(String hexString) {
        int size = hexString.length();
        byte[] buf = new byte[size / 2];
        int j = 0;
        for (int i = 0; i < size; i++) {
            String a = hexString.substring(i, i + 2);
            int valA = Integer.parseInt(a, 16);
            i++;
            buf[j] = (byte) valA;
            j++;
        }
        return buf;
    }

    /**
     * Converts a hex string to an int for comparison.
     * @param hex 4-character String hex value, i.e. "3dcf"
     * @return Integer representation of that value
     */
    public static Integer hexToInt(String hex) {
        return Integer.parseInt(hex, 16);
    }
}
