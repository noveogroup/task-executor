package com.noveogroup.android.task;

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
        final Helper helper = new Helper();

        TaskExecutor executor = createTaskExecutor();
        executor.execute(new Task() {
            @Override
            public Object run(Object input, TaskEnvironment env) throws Throwable {
                helper.append("Task::run");
                return null;
            }
        });
        Thread.sleep(Utils.DT);

        helper.check("Task::run");
    }

}
