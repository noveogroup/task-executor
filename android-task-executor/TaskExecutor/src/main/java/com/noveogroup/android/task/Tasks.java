package com.noveogroup.android.task;

public class Tasks {

    private Tasks() {
        throw new UnsupportedOperationException();
    }

    public static <E extends TaskEnvironment> Task<E> sequence(final Task<E> task1, final Task<E> task2) {
        return new Task<E>() {
            @Override
            public void run(E env) throws Throwable {
                env.owner().execute(task1).join();
                env.owner().execute(task2).join();
            }
        };
    }

    public static <E extends TaskEnvironment> Task<E> parallel(final Task<E> task1, final Task<E> task2) {
        return new Task<E>() {
            @Override
            public void run(E env) throws Throwable {
                TaskHandler handler1 = env.owner().execute(task1);
                TaskHandler handler2 = env.owner().execute(task2);
                handler1.join();
                handler2.join();
            }
        };
    }

}
