package com.noveogroup.android.task.ui;

import android.os.Handler;
import android.os.Looper;
import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NewUIHandlerTest extends AndroidTestCase {

    private static Set<String> set(String... values) {
        return new HashSet<String>(Arrays.asList(values));
    }

    public void testConstructor() {
        new NewUIHandler();
        new NewUIHandler(getContext());
        new NewUIHandler(Looper.myLooper());
        new NewUIHandler(new Handler());
    }

    public void testTags() {
        NewUIHandler uiHandler = new NewUIHandler();
        Assert.assertEquals(set(), uiHandler.tags());

        NewUIHandler subHandler = uiHandler.sub("A", "B");
        Assert.assertEquals(set("A", "B"), subHandler.tags());
        Assert.assertEquals(uiHandler, subHandler.root());

        NewUIHandler subSubHandler = subHandler.sub("C");
        Assert.assertEquals(set("A", "B", "C"), subSubHandler.tags());
        Assert.assertEquals(uiHandler, subSubHandler.root());
    }

}
