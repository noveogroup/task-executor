package com.noveogroup.android.task;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ExecutorAdapterTest {

    private static ExecutorService createAdapter() {
        SimpleTaskExecutor<SimpleTaskEnvironment> executor = new SimpleTaskExecutor<SimpleTaskEnvironment>(createExecutor()) {
            @Override
            protected <T extends Task> SimpleTaskEnvironment createTaskEnvironment(TaskHandler<T, SimpleTaskEnvironment> taskHandler) {
                return new SimpleTaskEnvironment<SimpleTaskEnvironment>(taskHandler);
            }
        };
        return new ExecutorAdapter<SimpleTaskEnvironment>(executor);
    }

    private static ExecutorService createExecutor() {
        return new ScheduledThreadPoolExecutor(3);
    }

    private void testExecute(ExecutorService executorService) {
        final StringBuffer buffer = new StringBuffer();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                buffer.append("[runnable]");
            }
        });

        Utils.sleep(Utils.DT);

        Assert.assertEquals("[runnable]", buffer.toString());
    }

    @Test
    public void adapterTestExecute() {
        testExecute(createAdapter());
    }

    @Test
    public void executorTestExecute() {
        testExecute(createExecutor());
    }

}
