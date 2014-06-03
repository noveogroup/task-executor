package com.noveogroup.android.task;

public class C4<T1, T2, T3, T4> {

    private T1 v1;
    private T2 v2;
    private T3 v3;
    private T4 v4;

    public C4() {
    }

    public C4(T1 v1, T2 v2, T3 v3, T4 v4) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.v4 = v4;
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

    public T4 getV4() {
        return v4;
    }

    public void setV4(T4 v4) {
        this.v4 = v4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        C4 c = (C4) o;
        return (v1 == null ? c.v1 == null : v1.equals(c.v1)) &&
                (v2 == null ? c.v2 == null : v2.equals(c.v2)) &&
                (v3 == null ? c.v3 == null : v3.equals(c.v3)) &&
                (v4 == null ? c.v4 == null : v4.equals(c.v4));
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (v1 != null ? v1.hashCode() : 0);
        result = 31 * result + (v2 != null ? v2.hashCode() : 0);
        result = 31 * result + (v3 != null ? v3.hashCode() : 0);
        result = 31 * result + (v4 != null ? v4.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("C4{v1=%s,v2=%s,v3=%s,v4=%s}", v1, v2, v3, v4);
    }

}
