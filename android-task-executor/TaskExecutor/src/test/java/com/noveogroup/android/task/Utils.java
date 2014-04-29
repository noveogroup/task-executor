package com.noveogroup.android.task;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class Utils {

    private Utils() {
        throw new UnsupportedOperationException();
    }

    private static final long DT = 10;

    private static final Object lock = new Object();
    private static volatile long counter = 0;

    static {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        synchronized (lock) {
                            counter++;
                            lock.notifyAll();
                        }
                        Thread.yield();
                        Thread.sleep(DT);
                    }
                } catch (InterruptedException ignored) {
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    public static void doSleep(int time) {
        synchronized (lock) {
            long startCounter = counter;
            while (startCounter + time > counter) {
                try {
                    lock.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public static Set<String> set(String... values) {
        return new HashSet<String>(Arrays.asList(values));
    }

}
