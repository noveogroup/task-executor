package com.noveogroup.android.task;

/**
 * Cortege of one value.
 * <p/>
 * Useful in cases of overloading {@link TaskExecutor#execute(Task, Object, String...)}
 * by {@link TaskExecutor#execute(Task, String...)}. For example:
 * <code><pre>
 *     // "input" will be a tag of started task
 *     executor.execute(task1, "input");
 *     // "input" will be an input value of task
 *     executor.execute(task2, new C1&lt;String&gt;("input"));
 * </pre></code>
 *
 * @param <T1> type of value.
 */
public class C1<T1> {

    private T1 v1;

    public C1() {
    }

    public C1(T1 v1) {
        this.v1 = v1;
    }

    public T1 getV1() {
        return v1;
    }

    public void setV1(T1 v1) {
        this.v1 = v1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        C1 c = (C1) o;
        return (v1 == null ? c.v1 == null : v1.equals(c.v1));
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (v1 != null ? v1.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("C1{v1=%s}", v1);
    }

}
