package com.noveogroup.android.task;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OverrideTest {

    private static final String ARG_LOG = "log";

    private static synchronized void log(Pack pack, String place, Object... args) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(place);
        for (Object arg : args) {
            Class<?> aClass = arg.getClass();
            String name;
            if (aClass.isAnonymousClass()) {
                if (aClass.getSuperclass() == Object.class) {
                    name = aClass.getInterfaces()[0].getName() + "{...}";
                } else {
                    name = aClass.getSuperclass().getName() + "{...}";
                }
            } else {
                name = aClass.getName();
            }
            name = name.replaceAll("com.noveogroup.android.task.", "");
            name = name.replaceAll("OverrideTest\\$", "\\$");
            builder.append("|").append(name);
        }
        builder.append("]\n");
        pack.<StringBuffer>get(ARG_LOG).append(builder.toString());
    }

    public static class TaskEnvironment1 extends SimpleTaskEnvironment {
        public TaskEnvironment1(TaskHandler handler) {
            super(handler);
        }
    }

    public static class TaskEnvironment2 extends TaskEnvironment1 {
        public TaskEnvironment2(TaskHandler handler) {
            super(handler);
        }
    }

    public static class TaskExecutor1 extends SimpleTaskExecutor {
        public TaskExecutor1(ExecutorService executorService) {
            super(executorService);
        }

        @Override
        protected TaskEnvironment1 createTaskEnvironment(TaskHandler taskHandler) {
            return new TaskEnvironment1(taskHandler);
        }
    }

    public static class TaskExecutor2 extends TaskExecutor1 {
        public TaskExecutor2(ExecutorService executorService) {
            super(executorService);
        }

        @Override
        protected TaskEnvironment2 createTaskEnvironment(TaskHandler taskHandler) {
            return new TaskEnvironment2(taskHandler);
        }
    }

    public static interface Task1 extends Task {
    }

    public static interface Task2 extends Task1 {
    }

    public static class MyTask1 implements Task1 {
        @Override
        public void run(TaskEnvironment env) throws Throwable {
            TaskEnvironment1 taskEnvironment1 = (TaskEnvironment1) env;
            log(env.args(), "MyTask1::run", taskEnvironment1);
        }
    }

    public static class MyTask2 implements Task2 {
        @Override
        public void run(TaskEnvironment env) throws Throwable {
            TaskEnvironment2 taskEnvironment2 = (TaskEnvironment2) env;
            log(env.args(), "MyTask2::run", taskEnvironment2);
        }
    }

    public static class MyTaskListener1 extends TaskListener.Default {
        @Override
        public void onCreate(TaskHandler handler) {
            TaskExecutor1 taskExecutor1 = (TaskExecutor1) handler.executor();
            Task1 task1 = (Task1) handler.task();
            log(handler.args(), "MyTaskListener1::onCreate", task1, taskExecutor1);
        }

        @Override
        public void onDestroy(TaskHandler handler) {
            Task1 task1 = (Task1) handler.task();
            TaskExecutor1 taskExecutor1 = (TaskExecutor1) handler.executor();
            log(handler.args(), "MyTaskListener1::onDestroy", task1, taskExecutor1);
        }
    }

    public static class MyTaskListener2 extends TaskListener.Default {
        @Override
        public void onCreate(TaskHandler handler) {
            Task2 task2 = (Task2) handler.task();
            TaskExecutor2 taskExecutor2 = (TaskExecutor2) handler.executor();
            log(handler.args(), "MyTaskListener2::onCreate", task2, taskExecutor2);
        }

        @Override
        public void onDestroy(TaskHandler handler) {
            Task2 task2 = (Task2) handler.task();
            TaskExecutor2 taskExecutor2 = (TaskExecutor2) handler.executor();
            log(handler.args(), "MyTaskListener2::onDestroy", task2, taskExecutor2);
        }
    }

    /* ******************************************************************************** */

    private static void checkHandler(TaskHandler taskHandler) throws Throwable {
        Throwable throwable = taskHandler.getThrowable();
        if (throwable != null) {
            throw throwable;
        }
    }

    @Test
    public void taskExecutor1AnonymousTask() throws Throwable {
        TaskExecutor1 executor1 = new TaskExecutor1(Executors.newSingleThreadExecutor());

        StringBuffer log = new StringBuffer();
        Pack args = new Pack().put(ARG_LOG, log);

        TaskHandler handler = executor1.execute(new Task() {
            @Override
            public void run(TaskEnvironment env) throws Throwable {
                log(env.args(), "Task::run", env);
            }
        }, args, new TaskListener.Default() {
            @Override
            public void onCreate(TaskHandler handler) {
                Task task = handler.task();
                TaskExecutor taskExecutor = handler.executor();
                log(handler.args(), "TaskListener::onCreate", task, taskExecutor);
            }

            @Override
            public void onDestroy(TaskHandler handler) {
                Task task = handler.task();
                TaskExecutor taskExecutor = handler.executor();
                log(handler.args(), "TaskListener::onDestroy", task, taskExecutor);
            }
        });
        handler.join();

        Task task = handler.task();
        TaskExecutor executor = handler.executor();
        log(handler.args(), "1", task, executor);
        checkHandler(handler);


        Assert.assertEquals("" +
                        "[TaskListener::onCreate|Task{...}|$TaskExecutor1]\n" +
                        "[Task::run|$TaskEnvironment1]\n" +
                        "[TaskListener::onDestroy|Task{...}|$TaskExecutor1]\n" +
                        "[1|Task{...}|$TaskExecutor1]\n",
                log.toString()
        );
    }

    @Test
    public void taskExecutor1AnonymousTask1() throws Throwable {
        TaskExecutor1 executor1 = new TaskExecutor1(Executors.newSingleThreadExecutor());

        StringBuffer log = new StringBuffer();
        Pack args = new Pack().put(ARG_LOG, log);

        TaskHandler handler = executor1.execute(new Task1() {
            @Override
            public void run(TaskEnvironment env) throws Throwable {
                log(env.args(), "Task1::run", (TaskEnvironment1) env);
            }
        }, args, Arrays.<TaskListener>asList(new TaskListener.Default() {
            @Override
            public void onCreate(TaskHandler handler) {
                Task1 task1 = (Task1) handler.task();
                TaskExecutor1 taskExecutor1 = (TaskExecutor1) handler.executor();
                log(handler.args(), "TaskListener::onCreate", task1, taskExecutor1);
            }

            @Override
            public void onDestroy(TaskHandler handler) {
                Task1 task1 = (Task1) handler.task();
                TaskExecutor1 taskExecutor1 = (TaskExecutor1) handler.executor();
                log(handler.args(), "TaskListener::onDestroy", task1, taskExecutor1);
            }
        }, new MyTaskListener1()));
        handler.join();

        Task1 task1 = (Task1) handler.task();
        TaskExecutor1 taskExecutor1 = (TaskExecutor1) handler.executor();
        log(handler.args(), "2", task1, taskExecutor1);
        checkHandler(handler);

        Assert.assertEquals("" +
                        "[TaskListener::onCreate|$Task1{...}|$TaskExecutor1]\n" +
                        "[MyTaskListener1::onCreate|$Task1{...}|$TaskExecutor1]\n" +
                        "[Task1::run|$TaskEnvironment1]\n" +
                        "[MyTaskListener1::onDestroy|$Task1{...}|$TaskExecutor1]\n" +
                        "[TaskListener::onDestroy|$Task1{...}|$TaskExecutor1]\n" +
                        "[2|$Task1{...}|$TaskExecutor1]\n",
                log.toString()
        );
    }

    @Test
    public void taskExecutor1AnonymousTask2() throws Throwable {
        TaskExecutor1 executor1 = new TaskExecutor1(Executors.newSingleThreadExecutor());

        StringBuffer log = new StringBuffer();
        Pack args = new Pack().put(ARG_LOG, log);

        TaskHandler handler = executor1.execute(new Task2() {
            @Override
            public void run(TaskEnvironment env) throws Throwable {
                log(env.args(), "Task2::run", (TaskEnvironment2) env);
            }
        }, args, Arrays.<TaskListener>asList(new TaskListener.Default() {
            @Override
            public void onCreate(TaskHandler handler) {
                Task1 task1 = (Task1) handler.task();
                TaskExecutor1 taskExecutor1 = (TaskExecutor1) handler.executor();
                log(handler.args(), "TaskListener::onCreate", task1, taskExecutor1);
            }

            @Override
            public void onDestroy(TaskHandler handler) {
                Task1 task1 = (Task1) handler.task();
                TaskExecutor1 taskExecutor1 = (TaskExecutor1) handler.executor();
                log(handler.args(), "TaskListener::onDestroy", task1, taskExecutor1);
            }
        }, new TaskListener.Default() {
            @Override
            public void onCreate(TaskHandler handler) {
                Task2 task2 = (Task2) handler.task();
                TaskExecutor2 taskExecutor2 = (TaskExecutor2) handler.executor();
                log(handler.args(), "TaskListener::onCreate", task2, taskExecutor2);
            }

            @Override
            public void onDestroy(TaskHandler handler) {
                Task2 task2 = (Task2) handler.task();
                TaskExecutor2 taskExecutor2 = (TaskExecutor2) handler.executor();
                log(handler.args(), "TaskListener::onDestroy", task2, taskExecutor2);
            }
        }, new MyTaskListener1()));
        handler.join();

        Task1 task1 = (Task1) handler.task();
        TaskExecutor1 taskExecutor1 = (TaskExecutor1) handler.executor();
        log(handler.args(), "2", task1, taskExecutor1);
        checkHandler(handler);

        Assert.assertEquals("" +
                        "[TaskListener::onCreate|$Task1{...}|$TaskExecutor1]\n" +
                        "[MyTaskListener1::onCreate|$Task1{...}|$TaskExecutor1]\n" +
                        "[Task1::run|$TaskEnvironment1]\n" +
                        "[MyTaskListener1::onDestroy|$Task1{...}|$TaskExecutor1]\n" +
                        "[TaskListener::onDestroy|$Task1{...}|$TaskExecutor1]\n" +
                        "[2|$Task1{...}|$TaskExecutor1]\n",
                log.toString()
        );
    }

    @Test
    public void taskExecutor1MyTask1() throws Throwable {
        TaskExecutor1 executor1 = new TaskExecutor1(Executors.newSingleThreadExecutor());

        StringBuffer log = new StringBuffer();
        Pack args = new Pack().put(ARG_LOG, log);

        TaskHandler handler = executor1.execute(new MyTask1(), args, Arrays.<TaskListener>asList(new TaskListener.Default() {
            @Override
            public void onCreate(TaskHandler handler) {
                Task1 task1 = (Task1) handler.task();
                TaskExecutor1 taskExecutor1 = (TaskExecutor1) handler.executor();
                log(handler.args(), "TaskListener::onCreate", task1, taskExecutor1);
            }

            @Override
            public void onDestroy(TaskHandler handler) {
                Task1 task1 = (Task1) handler.task();
                TaskExecutor1 taskExecutor1 = (TaskExecutor1) handler.executor();
                log(handler.args(), "TaskListener::onDestroy", task1, taskExecutor1);
            }
        }, new MyTaskListener1()));
        handler.join();

        MyTask1 myTask1 = (MyTask1) handler.task();
        TaskExecutor1 taskExecutor1 = (TaskExecutor1) handler.executor();
        log(handler.args(), "3", myTask1, taskExecutor1);
        checkHandler(handler);

        Assert.assertEquals("" +
                        "[TaskListener::onCreate|$MyTask1|$TaskExecutor1]\n" +
                        "[MyTaskListener1::onCreate|$MyTask1|$TaskExecutor1]\n" +
                        "[MyTask1::run|$TaskEnvironment1]\n" +
                        "[MyTaskListener1::onDestroy|$MyTask1|$TaskExecutor1]\n" +
                        "[TaskListener::onDestroy|$MyTask1|$TaskExecutor1]\n" +
                        "[3|$MyTask1|$TaskExecutor1]\n",
                log.toString()
        );
    }

}
