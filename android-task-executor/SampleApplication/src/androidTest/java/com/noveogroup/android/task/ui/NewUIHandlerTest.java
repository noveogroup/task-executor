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

    private static final Runnable EMPTY_CALLBACK = new Runnable() {
        @Override
        public void run() {
        }
    };

    public void testConstructor() {
        NewUIHandler uiHandler1 = new NewUIHandler();
        uiHandler1.post(EMPTY_CALLBACK);
        NewUIHandler uiHandler2 = new NewUIHandler(getContext());
        uiHandler2.post(EMPTY_CALLBACK);
        NewUIHandler uiHandler3 = new NewUIHandler(Looper.myLooper());
        uiHandler3.post(EMPTY_CALLBACK);
        NewUIHandler uiHandler4 = new NewUIHandler(new Handler());
        uiHandler4.post(EMPTY_CALLBACK);
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
