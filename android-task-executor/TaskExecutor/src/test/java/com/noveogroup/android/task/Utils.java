package com.noveogroup.android.task;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class Utils {

    private Utils() {
        throw new UnsupportedOperationException();
    }

    public static final long DT = 10;

    public static Set<String> set(String... values) {
        return new HashSet<String>(Arrays.asList(values));
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ignored) {
        }
    }

}
