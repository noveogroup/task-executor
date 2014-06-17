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
                TaskHandler<I1, O1> handler1 = env.owner().execute(task1, env.vars().input().getV1());
                handler1.join();
                TaskHandler<I2, O2> handler2 = env.owner().execute(task2, env.vars().input().getV2());
                handler2.join();
                return new C2<O1, O2>(handler1.vars().output(), handler2.vars().output());
            }
        };
    }

    public static <I1, O1, I2, O2> Task<C2<I1, I2>, C2<O1, O2>> parallel(final Task<I1, O1> task1, final Task<I2, O2> task2) {
        return new Task<C2<I1, I2>, C2<O1, O2>>() {
            @Override
            public C2<O1, O2> run(C2<I1, I2> value, TaskEnvironment<C2<I1, I2>, C2<O1, O2>> env) throws Throwable {
                TaskHandler<I1, O1> handler1 = env.owner().execute(task1, env.vars().input().getV1());
                TaskHandler<I2, O2> handler2 = env.owner().execute(task2, env.vars().input().getV2());
                handler1.join();
                handler2.join();
                return new C2<O1, O2>(handler1.vars().output(), handler2.vars().output());
            }
        };
    }

    public static <T> Task<T, T> simple() {
        return new Task<T, T>() {
            @Override
            public T run(T value, TaskEnvironment<T, T> env) throws Throwable {
                return value;
            }
        };
    }

    public static <Input1, Output1, Input2, Output2, Output>
    Task<C2<Input1, Input2>, Output> merge(final Task<Input1, Output1> task1,
                                           final Task<Input2, Output2> task2,
                                           final Task<C2<Output1, Output2>, Output> task) {
        return new Task<C2<Input1, Input2>, Output>() {
            @Override
            public Output run(C2<Input1, Input2> value, TaskEnvironment<C2<Input1, Input2>, Output> env) throws Throwable {
                Output1 output1 = env.executor().execute(task1, value.getV1()).get();
                Output2 output2 = env.executor().execute(task2, value.getV2()).get();
                return env.executor().execute(task, new C2<Output1, Output2>(output1, output2)).get();
            }
        };
    }

    public static <Input1, Output1, Input2, Output2, Input3, Output3, Output>
    Task<C3<Input1, Input2, Input3>, Output> merge(final Task<Input1, Output1> task1,
                                                   final Task<Input2, Output2> task2,
                                                   final Task<Input3, Output3> task3,
                                                   final Task<C3<Output1, Output2, Output3>, Output> task) {
        return new Task<C3<Input1, Input2, Input3>, Output>() {
            @Override
            public Output run(C3<Input1, Input2, Input3> value, TaskEnvironment<C3<Input1, Input2, Input3>, Output> env) throws Throwable {
                Output1 output1 = env.executor().execute(task1, value.getV1()).get();
                Output2 output2 = env.executor().execute(task2, value.getV2()).get();
                Output3 output3 = env.executor().execute(task3, value.getV3()).get();
                return env.executor().execute(task, new C3<Output1, Output2, Output3>(output1, output2, output3)).get();
            }
        };
    }

    public static <Input1, Output1, Input2, Output2, Input3, Output3, Input4, Output4, Output>
    Task<C4<Input1, Input2, Input3, Input4>, Output> merge(final Task<Input1, Output1> task1,
                                                           final Task<Input2, Output2> task2,
                                                           final Task<Input3, Output3> task3,
                                                           final Task<Input4, Output4> task4,
                                                           final Task<C4<Output1, Output2, Output3, Output4>, Output> task) {
        return new Task<C4<Input1, Input2, Input3, Input4>, Output>() {
            @Override
            public Output run(C4<Input1, Input2, Input3, Input4> value, TaskEnvironment<C4<Input1, Input2, Input3, Input4>, Output> env) throws Throwable {
                Output1 output1 = env.executor().execute(task1, value.getV1()).get();
                Output2 output2 = env.executor().execute(task2, value.getV2()).get();
                Output3 output3 = env.executor().execute(task3, value.getV3()).get();
                Output4 output4 = env.executor().execute(task4, value.getV4()).get();
                return env.executor().execute(task, new C4<Output1, Output2, Output3, Output4>(output1, output2, output3, output4)).get();
            }
        };
    }

}
