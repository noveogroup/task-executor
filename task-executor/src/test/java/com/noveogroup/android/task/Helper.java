package com.noveogroup.android.task;

import org.junit.Assert;

public class Helper {

    private final Object lock = new Object();
    private final StringBuilder builder = new StringBuilder();

    public Helper append(String format, Object... args) {
        synchronized (lock) {
            builder.append(String.format(format, args));
            return this;
        }
    }

    public void check(String expected) {
        Assert.assertEquals(expected, builder.toString());
    }

}
