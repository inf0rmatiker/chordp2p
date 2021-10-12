package org.chord.peer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FingerTableTest {

    @Test
    public void testFingerTableConstructor() {
        Identifier id = new Identifier("shark", "0000");
        FingerTable ft = new FingerTable(16,id);

        assertEquals(16, ft.getPeerIds().size());
        for (Identifier peerId: ft.getPeerIds()) {
            assertEquals(id, peerId);
        }
    }

    @Test
    public void testFingerTableHopsAway() {
        Identifier id = new Identifier("shark", "0000");
        FingerTable ft = new FingerTable(16,id);

        int[] expectedValues = {
                1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768
        };

        for (int i = 0; i < expectedValues.length; i++) {
            int expected = expectedValues[i];
            int actual = ft.hopsAwayToDistance(i+1);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testFingerTableRingPosition() {
        int idValue = 43690;
        Identifier id = new Identifier("shark", "aaaa"); // aaaa = 43690
        FingerTable ft = new FingerTable(16,id);

        int[] expectedValues = {
                43691, 43692, 43694, 43698, 43706, 43722, 43754, 43818, 43946, 44202, 44714, 45738, 47786, 51882, 60074,
                10922 // this last one wraps around
        };

        for (int i = 0; i < expectedValues.length; i++) {
            int expected = expectedValues[i];
            int actual = ft.ringPositionOfIndex(i);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testFingerTableGetSuccessorWraps() {
        Identifier id = new Identifier("shark", "6");
        List<Identifier> peerIds = new ArrayList<>() {{
           add(new Identifier("a", "1"));
           add(new Identifier("b", "2"));
           add(new Identifier("c", "3"));
        }};
        FingerTable ft = new FingerTable(id, peerIds); // id space is 2^3, or 0 - 7
        Identifier expected = peerIds.get(1); // 2 hops away
        Identifier actual = ft.successor("0");
        assertEquals(expected, actual);

        actual = ft.successor("1"); // still 2 hops away
        assertEquals(expected, actual);

        actual = ft.successor("3");
        expected = peerIds.get(2); // 2 hops away
        assertEquals(expected, actual);
    }

    @Test
    public void testFingerTableGetSuccessorDoesntWrap() {
        Identifier id = new Identifier("shark", "2");
        List<Identifier> peerIds = new ArrayList<>() {{
            add(new Identifier("a", "1"));
            add(new Identifier("b", "2"));
            add(new Identifier("c", "3"));
        }};
        FingerTable ft = new FingerTable(id, peerIds); // id space is 2^3, or 0 - 7
        Identifier expected = peerIds.get(1); // 2 hops away
        Identifier actual = ft.successor("1");
        assertEquals(expected, actual);
    }


}
