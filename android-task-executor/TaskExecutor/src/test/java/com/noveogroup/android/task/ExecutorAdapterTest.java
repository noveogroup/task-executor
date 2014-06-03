package com.noveogroup.android.task;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class ExecutorAdapterTest {

    private static ExecutorService createAdapter() {
        return new ExecutorAdapter(new SimpleTaskExecutor(createExecutor()));
    }

    private static ExecutorService createExecutor() {
        return Executors.newFixedThreadPool(3);
    }

    private void testExecute(ExecutorService executorService) {
        final Helper helper = new Helper();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                helper.append("[runnable]");
            }
        });

        Utils.doSleep(5);

        helper.check("[runnable]");
    }

    @Test
    public void adapterTestExecute() {
        testExecute(createAdapter());
    }

    @Test
    public void executorTestExecute() {
        testExecute(createExecutor());
    }

    private void testShutdown(ExecutorService executorService) {
        final Helper helper = new Helper();

        for (int i = 0; i < 5; i++) {
            final char index = "ABCDE".charAt(i);
            final int time = 10 + 5 * i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    helper.append(String.format("[%s-1]", index));
                    Utils.doSleep(time);
                    helper.append(String.format("[%s-2]", index));
                }
            });
            Utils.doSleep(1);
        }

        Utils.doSleep(1);

        Assert.assertEquals(false, executorService.isShutdown());
        Assert.assertEquals(false, executorService.isTerminated());

        executorService.shutdown();
        helper.append("[shutdown]");

        Assert.assertEquals(true, executorService.isShutdown());
        Assert.assertEquals(false, executorService.isTerminated());

        try {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                }
            });
            Assert.fail("should throw RejectedExecutionException");
        } catch (RejectedExecutionException ignored) {
        }

        Utils.doSleep(50);
        Assert.assertEquals(true, executorService.isTerminated());

        helper.check("[A-1][B-1][C-1][shutdown][A-2][D-1][B-2][E-1][C-2][D-2][E-2]");
    }

    @Test
    public void adapterTestShutdown() {
        testShutdown(createAdapter());
    }

    @Test
    public void executorTestShutdown() {
        testShutdown(createExecutor());
    }

    private void testShutdownNow(ExecutorService executorService) {
        final Helper helper = new Helper();

        for (int i = 0; i < 5; i++) {
            final char index = "ABCDE".charAt(i);
            final int time = 10 + 5 * i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    helper.append(String.format("[%s-1]", index));
                    Utils.doSleep(time);
                    helper.append(String.format("[%s-2]", index));
                }

                @Override
                public String toString() {
                    return "[" + index + "]";
                }
            });
            Utils.doSleep(1);
        }

        Utils.doSleep(1);

        Assert.assertEquals(false, executorService.isShutdown());
        Assert.assertEquals(false, executorService.isTerminated());

        List<Runnable> list = executorService.shutdownNow();
        Assert.assertEquals(2, list.size());
        helper.append("[shutdown]");

        Assert.assertEquals(true, executorService.isShutdown());
        Assert.assertEquals(false, executorService.isTerminated());

        try {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                }
            });
            Assert.fail("should throw RejectedExecutionException");
        } catch (RejectedExecutionException ignored) {
        }

        Utils.doSleep(20);
        Assert.assertEquals(true, executorService.isTerminated());

        helper.check("[A-1][B-1][C-1][shutdown][A-2][B-2][C-2]");
    }

    @Test
    public void adapterTestShutdownNow() {
        testShutdownNow(createAdapter());
    }

    @Test
    public void executorTestShutdownNow() {
        testShutdownNow(createExecutor());
    }

    private void testAwaitTermination(ExecutorService executorService) throws InterruptedException {
        final Helper helper = new Helper();

        for (int i = 0; i < 5; i++) {
            final char index = "ABCDE".charAt(i);
            final int time = 10 + 5 * i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    helper.append(String.format("[%s-1]", index));
                    Utils.doSleep(time);
                    helper.append(String.format("[%s-2]", index));
                }
            });
            Utils.doSleep(1);
        }

        Utils.doSleep(1);
        executorService.shutdown();
        helper.append("[shutdown]");

        Assert.assertEquals(true, executorService.isShutdown());
        Assert.assertEquals(false, executorService.isTerminated());
        helper.check("[A-1][B-1][C-1][shutdown]");
        Assert.assertEquals(false, executorService.awaitTermination(2 * Utils.DT, TimeUnit.MILLISECONDS));

        Assert.assertEquals(true, executorService.isShutdown());
        Assert.assertEquals(false, executorService.isTerminated());
        helper.check("[A-1][B-1][C-1][shutdown]");
        Assert.assertEquals(false, executorService.awaitTermination(10 * Utils.DT, TimeUnit.MILLISECONDS));

        Assert.assertEquals(true, executorService.isShutdown());
        Assert.assertEquals(false, executorService.isTerminated());
        helper.check("[A-1][B-1][C-1][shutdown][A-2][D-1][B-2][E-1]");
        Assert.assertEquals(false, executorService.awaitTermination(20 * Utils.DT, TimeUnit.MILLISECONDS));

        Assert.assertEquals(true, executorService.isShutdown());
        Assert.assertEquals(false, executorService.isTerminated());
        helper.check("[A-1][B-1][C-1][shutdown][A-2][D-1][B-2][E-1][C-2][D-2]");
        Assert.assertEquals(true, executorService.awaitTermination(20 * Utils.DT, TimeUnit.MILLISECONDS));

        Assert.assertEquals(true, executorService.isShutdown());
        Assert.assertEquals(true, executorService.isTerminated());
        helper.check("[A-1][B-1][C-1][shutdown][A-2][D-1][B-2][E-1][C-2][D-2][E-2]");
        Assert.assertEquals(true, executorService.awaitTermination(20 * Utils.DT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void adapterTestAwaitTermination() throws InterruptedException {
        testAwaitTermination(createAdapter());
    }

    @Test
    public void executorTestAwaitTermination() throws InterruptedException {
        testAwaitTermination(createExecutor());
    }

}
