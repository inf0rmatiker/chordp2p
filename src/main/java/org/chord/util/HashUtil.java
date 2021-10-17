package org.chord.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    private static final Logger log = LoggerFactory.getLogger(HashUtil.class);

    /**
     * Calculates the 16-bit digest for a given array of bytes
     * Returns the hex representation of the digest
     * @param fileBytes
     * @return
     */
    public static String hashFile(byte[] fileBytes) {
        byte[] hash = null;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update(fileBytes);
            hash = md.digest();
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getLocalizedMessage());
        }

        // TODO: find a better way to calculate a 16-bit hash
        return bytesToHex(hash).substring(0, 4);
    }

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
        return Integer.parseUnsignedInt(hex, 16);
    }

    /**
     * Converts an int to a length-4 hex value
     * @param val integer to convert
     * @return converted String
     */
    public static String intToHex(Integer val) {
        StringBuilder sb = new StringBuilder(Integer.toHexString(val));
        while (sb.length() < 4) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }
}
