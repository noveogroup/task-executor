package com.noveogroup.android.task;

public class C3<T1, T2, T3> {

    private T1 v1;
    private T2 v2;
    private T3 v3;

    public C3() {
    }

    public C3(T1 v1, T2 v2, T3 v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
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

    public T3 getV3() {
        return v3;
    }

    public void setV3(T3 v3) {
        this.v3 = v3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        C3 c = (C3) o;
        return (v1 == null ? c.v1 == null : v1.equals(c.v1)) &&
                (v2 == null ? c.v2 == null : v2.equals(c.v2)) &&
                (v3 == null ? c.v3 == null : v3.equals(c.v3));
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (v1 != null ? v1.hashCode() : 0);
        result = 31 * result + (v2 != null ? v2.hashCode() : 0);
        result = 31 * result + (v3 != null ? v3.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("C4{v1=%s,v2=%s,v3=%s}", v1, v2, v3);
    }

}
