package com.noveogroup.android.task;

public class Tasks {

    private Tasks() {
        throw new UnsupportedOperationException();
    }

    public static Task sequence(final Task task1, final Task task2) {
        return new Task() {
            @Override
            public void run(TaskEnvironment env) throws Throwable {
                env.owner().execute(task1).join();
                env.owner().execute(task2).join();
            }
        };
    }

    public static Task parallel(final Task task1, final Task task2) {
        return new Task() {
            @Override
            public void run(TaskEnvironment env) throws Throwable {
                TaskHandler handler1 = env.owner().execute(task1);
                TaskHandler handler2 = env.owner().execute(task2);
                handler1.join();
                handler2.join();
            }
        };
    }

}
