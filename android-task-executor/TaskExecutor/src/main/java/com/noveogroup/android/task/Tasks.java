package com.noveogroup.android.task;

import java.util.ArrayList;
import java.util.List;

public class Tasks {

    private Tasks() {
        throw new UnsupportedOperationException();
    }

    public static <T> List<T> asList() {
        return new ArrayList<T>();
    }

    public static <T> List<T> asList(T v1) {
        ArrayList<T> list = new ArrayList<T>();
        list.add(v1);
        return list;
    }

    public static <T> List<T> asList(T v1, T v2) {
        ArrayList<T> list = new ArrayList<T>();
        list.add(v1);
        list.add(v2);
        return list;
    }

    public static <T> List<T> asList(T v1, T v2, T v3) {
        ArrayList<T> list = new ArrayList<T>();
        list.add(v1);
        list.add(v2);
        list.add(v3);
        return list;
    }

    public static <T> List<T> asList(T v1, T v2, T v3, T v4) {
        ArrayList<T> list = new ArrayList<T>();
        list.add(v1);
        list.add(v2);
        list.add(v3);
        list.add(v4);
        return list;
    }

    public static <I1, O1, I2, O2> Task<C2<I1, I2>, C2<O1, O2>> sequence(final Task<I1, O1> task1, final Task<I2, O2> task2) {
        return new Task<C2<I1, I2>, C2<O1, O2>>() {
            @Override
            public C2<O1, O2> run(C2<I1, I2> value, TaskEnvironment<C2<I1, I2>, C2<O1, O2>> env) throws Throwable {
                TaskHandler<I1, O1> handler1 = env.owner().execute(task1, env.args().input().getV1());
                handler1.join();
                TaskHandler<I2, O2> handler2 = env.owner().execute(task2, env.args().input().getV2());
                handler2.join();
                return new C2<O1, O2>(handler1.args().output(), handler2.args().output());
            }
        };
    }

    public static <I1, O1, I2, O2> Task<C2<I1, I2>, C2<O1, O2>> parallel(final Task<I1, O1> task1, final Task<I2, O2> task2) {
        return new Task<C2<I1, I2>, C2<O1, O2>>() {
            @Override
            public C2<O1, O2> run(C2<I1, I2> value, TaskEnvironment<C2<I1, I2>, C2<O1, O2>> env) throws Throwable {
                TaskHandler<I1, O1> handler1 = env.owner().execute(task1, env.args().input().getV1());
                TaskHandler<I2, O2> handler2 = env.owner().execute(task2, env.args().input().getV2());
                handler1.join();
                handler2.join();
                return new C2<O1, O2>(handler1.args().output(), handler2.args().output());
            }
        };
    }

}
