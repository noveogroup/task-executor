package com.noveogroup.android.task;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class PackTest {

    @Test
    public void testConstructorSimple1() {
        Pack pack = new Pack();

        Assert.assertEquals(0, pack.size());
    }

    @Test
    public void testConstructorSimple2() {
        Object lock = new Object();
        Pack pack = new Pack(lock);

        Assert.assertEquals(lock, pack.lock());
        Assert.assertEquals(0, pack.size());
    }

    @Test
    public void testConstructorCopy1() {
        final Helper helper = new Helper();

        final Object lock = new Object();

        Pack pack = new Pack(lock);
        pack.put("key", "value");

        new Thread() {
            @Override
            public void run() {
                synchronized (lock) {
                    helper.append("[thread-A]");
                    Utils.doSleep(10);
                    helper.append("[thread-B]");
                }
            }
        }.start();

        Utils.doSleep(1);
        helper.append("[copy-A]");
        Pack copyPack = new Pack(pack);
        Utils.doSleep(1);
        helper.append("[copy-B]");

        Assert.assertEquals(lock, copyPack.lock());
        Assert.assertEquals(1, copyPack.size());
        Assert.assertEquals(Utils.set("key"), copyPack.keySet());
        Assert.assertEquals("value", copyPack.get("key"));
        helper.check("[thread-A][copy-A][thread-B][copy-B]");
    }

    @Test
    public void testConstructorCopy2() {
        final Helper helper = new Helper();

        final Object lock = new Object();
        final Object copyLock = new Object();

        Pack pack = new Pack(lock);
        pack.put("key", "value");

        new Thread() {
            @Override
            public void run() {
                synchronized (lock) {
                    helper.append("[thread-A]");
                    Utils.doSleep(10);
                    helper.append("[thread-B]");
                }
            }
        }.start();

        Utils.doSleep(1);
        helper.append("[copy-A]");
        Pack copyPack = new Pack(copyLock, pack);
        Utils.doSleep(1);
        helper.append("[copy-B]");

        Assert.assertEquals(copyLock, copyPack.lock());
        Assert.assertEquals(1, copyPack.size());
        Assert.assertEquals(Utils.set("key"), copyPack.keySet());
        Assert.assertEquals("value", copyPack.get("key"));
        helper.check("[thread-A][copy-A][thread-B][copy-B]");
    }

    @Test
    public void testEmpty() {
        Pack pack = new Pack();
        Assert.assertTrue(pack.isEmpty());
        pack.put("key", "value");
        Assert.assertFalse(pack.isEmpty());
    }

    @Test
    public void testIterator1() {
        final Helper helper = new Helper();

        final Pack pack = new Pack();
        pack.put("key1", "value1");
        pack.put("key2", "value2");
        pack.put("key3", "value3");

        Iterator<String> iterator = pack.iterator();

        new Thread() {
            @Override
            public void run() {
                synchronized (pack.lock()) {
                    helper.append("[thread-A]");
                    Utils.doSleep(10);
                    helper.append("[thread-B]");
                }
            }
        }.start();

        Utils.doSleep(1);

        while (iterator.hasNext()) {
            helper.append("[%s]", iterator.next());
        }

        Utils.doSleep(10);

        helper.check("[thread-A][key3][key2][key1][thread-B]");
    }

    @Test
    public void testIterator2() {
        final Helper helper = new Helper();

        final Pack pack = new Pack();
        pack.put("key1", "value1");
        pack.put("key2", "value2");
        pack.put("key3", "value3");

        Iterator<String> iterator = pack.iterator();
        pack.remove("key2");
        while (iterator.hasNext()) {
            helper.append("[%s]", iterator.next());
        }

        helper.check("[key3][key2][key1]");
    }

}
