package com.noveogroup.android.task.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.*;

// todo implement
// todo move JavaDoc
// todo test
public class UIHandler {

    private final Handler handler;
    private final Object lock;
    private final Set<String> tags;
    private final Map<Set<String>, List<Runnable>> callbacks;

    public UIHandler() {
        this(new Handler());
    }

    public UIHandler(Context context) {
        this(context.getMainLooper());
    }

    public UIHandler(Looper looper) {
        this(new Handler(looper));
    }

    public UIHandler(Handler handler) {
        this(handler, new Object(), new HashSet<String>(0), new HashMap<Set<String>, List<Runnable>>(8));
    }

    private UIHandler(UIHandler parent, Collection<String> tags) {
        this(parent.handler, parent.lock, tags, parent.callbacks);
    }

    private UIHandler(Handler handler, Object lock, Collection<String> tags, Map<Set<String>, List<Runnable>> callbacks) {
        this.handler = handler;
        this.lock = lock;
        this.tags = Collections.unmodifiableSet(new HashSet<String>(tags));
        this.callbacks = callbacks;
    }

    public UIHandler sub(String... tags) {
        return new UIHandler(this, Arrays.asList(tags));
    }

    public UIHandler sub(Collection<String> tags) {
        return new UIHandler(this, tags);
    }

    private class UICallback implements Runnable {

        private final Runnable callback;

        public UICallback(Runnable callback) {
            this.callback = callback;
        }

        public boolean post() {
            synchronized (lock) {
                List<Runnable> list = callbacks.get(tags);
                if (list == null) {
                    list = new ArrayList<Runnable>();
                    callbacks.put(tags, list);
                }
                list.add(this);
                return handler.post(this);
            }
        }

        @Override
        public final void run() {
        }

    }

    public boolean post(Runnable callback) {
        UICallback uiCallback = new UICallback(callback);
        return uiCallback.post();
    }

    public boolean postDelayed(Runnable callback, long delay) {
        return false;
    }

    public void joinCallbacks(Runnable callback) throws InterruptedException {
    }

    public void joinCallbacks() throws InterruptedException {
    }

    public void removeCallbacks(Runnable callback) {
    }

    public void removeCallbacks() {
    }

}
