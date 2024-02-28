package server.api;

import java.util.random.RandomGenerator;

public class TestRandom implements RandomGenerator {

    /**
     * Returns a pseudorandomly chosen {@code long} value.
     *
     * @return a pseudorandomly chosen {@code long} value
     */
    @Override
    public long nextLong() {
        return 0;
    }

    /**
     * @param bytes the byte array to fill with a sequence starting from 0
     */
    @Override
    public void nextBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte)i;
        }
    }
}
