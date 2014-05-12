package com.noveogroup.android.task;

import junit.framework.Assert;
import org.junit.Test;

import java.util.concurrent.Executors;

public class TaskTest {

    private TaskExecutor createTaskExecutor() {
        return new SimpleTaskExecutor(Executors.newFixedThreadPool(3)) {
            @Override
            protected TaskEnvironment createTaskEnvironment(TaskHandler taskHandler) {
                return new SimpleTaskEnvironment(taskHandler);
            }
        };
    }

    @Test
    public void runTest() throws InterruptedException {
        final StringBuffer buffer = new StringBuffer();

        TaskExecutor executor = createTaskExecutor();
        executor.execute(new Task() {
            @Override
            public void run(TaskEnvironment env) throws Throwable {
                buffer.append("Task::run");
            }
        });
        Thread.sleep(Utils.DT);

        Assert.assertEquals("Task::run", buffer.toString());
    }

}
