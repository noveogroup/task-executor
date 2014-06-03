package com.noveogroup.android.task;

public class C2<T1, T2> {

    private T1 v1;
    private T2 v2;

    public C2() {
    }

    public T1 getV1() {
        return v1;
    }

    public void setV1(T1 v1) {
        this.v1 = v1;
    }

    public T2 getV2() {
        return v2;
    }

    public void setV2(T2 v2) {
        this.v2 = v2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        C2 c = (C2) o;
        return (v1 == null ? c.v1 == null : v1.equals(c.v1)) &&
                (v2 == null ? c.v2 == null : v2.equals(c.v2));
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (v1 != null ? v1.hashCode() : 0);
        result = 31 * result + (v2 != null ? v2.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("C4{v1=%s,v2=%s}", v1, v2);
    }

}
