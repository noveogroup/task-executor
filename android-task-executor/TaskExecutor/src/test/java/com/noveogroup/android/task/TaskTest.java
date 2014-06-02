package com.noveogroup.android.task;

import org.junit.Test;

import java.util.concurrent.Executors;

public class TaskTest {

    private TaskExecutor createTaskExecutor() {
        return new SimpleTaskExecutor(Executors.newFixedThreadPool(3));
    }

    @Test
    public void runTest() throws InterruptedException {
        final Helper helper = new Helper();

        TaskExecutor executor = createTaskExecutor();

        executor.addTaskListener(new TaskListener.Default<Object, Object>() {
            @Override
            public void onStart(TaskHandler<Object, Object> handler) {
                helper.append("[TaskListener::onStart{%d,%s}]", handler.args().input(), handler.args().output());
            }

            @Override
            public void onFinish(TaskHandler<Object, Object> handler) {
                helper.append("[TaskListener::onFinish{%d,%s}]", handler.args().input(), handler.args().output());
            }
        });

        executor.execute(new Task<Integer, String>() {
            @Override
            public String run(Integer input, TaskEnvironment<Integer, String> env) throws Throwable {
                helper.append("[Task::run]");
                return String.valueOf(input);
            }
        }, 100, new TaskListener.Default<Integer, String>() {
            @Override
            public void onCreate(TaskHandler<Integer, String> handler) {
                helper.append("[TaskListener::onCreate{%d,%s}]", handler.args().input(), handler.args().output());
            }

            @Override
            public void onDestroy(TaskHandler<Integer, String> handler) {
                helper.append("[TaskListener::onDestroy{%d,%s}]", handler.args().input(), handler.args().output());
            }
        });
        Thread.sleep(Utils.DT);

        helper.check("[TaskListener::onCreate{100,null}][TaskListener::onStart{100,null}][Task::run][TaskListener::onFinish{100,100}][TaskListener::onDestroy{100,100}]");
    }

}
