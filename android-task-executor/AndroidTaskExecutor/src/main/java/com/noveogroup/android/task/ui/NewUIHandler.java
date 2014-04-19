/*
 * Copyright (c) 2014 Noveo Group
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Except as contained in this notice, the name(s) of the above copyright holders
 * shall not be used in advertising or otherwise to promote the sale, use or
 * other dealings in this Software without prior written authorization.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.noveogroup.android.task.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class NewUIHandler {

    private static class AssociationSet<V> {

        private final Map<V, Set<String>> values = new HashMap<V, Set<String>>();

        public void clear() {
            values.clear();
        }

        public void add(V value, Collection<String> tags) {
            values.put(value, new HashSet<String>(tags));
        }

        public void remove(V value) {
            values.remove(value);
        }

        public Set<V> getAssociated(Collection<String> tags) {
            Set<V> set = new HashSet<V>();
            for (Map.Entry<V, Set<String>> entry : values.entrySet()) {
                if (entry.getValue().containsAll(tags)) {
                    set.add(entry.getKey());
                }
            }
            return Collections.unmodifiableSet(set);
        }

    }

    private static class WaitCallback implements Runnable {

        private final Object lock = new Object();
        private boolean finished = false;

        protected void runCallback() {
        }

        @Override
        public final void run() {
            try {
                runCallback();
            } finally {
                release();
            }
        }

        public final void join() throws InterruptedException {
            synchronized (lock) {
                while (!finished) {
                    lock.wait();
                }
            }
        }

        public final void release() {
            synchronized (lock) {
                finished = true;
                lock.notifyAll();
            }
        }

    }

    private final Object lock;
    private final Handler handler;
    private final AssociationSet<WaitCallback> associationSet;
    private final Map<Runnable, Set<WaitCallback>> callbacks;

    private final NewUIHandler root;
    private final Set<String> tags;
    private final WeakHashMap<Set<String>, NewUIHandler> subCache;

    public NewUIHandler() {
        this(new Handler());
    }

    public NewUIHandler(Looper looper) {
        this(new Handler(looper));
    }

    public NewUIHandler(Context context) {
        this(new Handler(context.getMainLooper()));
    }

    public NewUIHandler(Handler handler) {
        this.lock = new Object();
        this.handler = handler;
        this.associationSet = new AssociationSet<WaitCallback>();
        this.callbacks = new HashMap<Runnable, Set<WaitCallback>>();

        this.root = this;
        this.tags = Collections.emptySet();
        this.subCache = new WeakHashMap<Set<String>, NewUIHandler>();
    }

    private NewUIHandler(NewUIHandler root, Set<String> tags) {
        this.lock = root.lock;
        this.handler = root.handler;
        this.associationSet = root.associationSet;
        this.callbacks = root.callbacks;

        this.tags = tags;
        this.root = root;
        this.subCache = root.subCache;
    }

    public NewUIHandler sub(String... tags) {
        return sub(Arrays.asList(tags));
    }

    public NewUIHandler sub(Collection<String> tags) {
        Set<String> tagSet = Collections.unmodifiableSet(new HashSet<String>(tags));

        synchronized (lock) {
            NewUIHandler uiHandler = subCache.get(tagSet);
            if (uiHandler == null) {
                uiHandler = new NewUIHandler(this, tagSet);
                subCache.put(tagSet, uiHandler);
            }
            return uiHandler;
        }
    }

    public Set<String> tags() {
        return tags;
    }

    private void checkJoinAbility() {
        if (Thread.currentThread() == handler.getLooper().getThread()) {
            throw new RuntimeException("current thread is a looper thread and cannot wait itself");
        }
    }

    private WaitCallback createWaitCallback(final Runnable callback) {
        if (callback == null) {
            throw new NullPointerException("callback is null");
        }
        return new WaitCallback() {
            @Override
            protected void runCallback() {
                try {
                    callback.run();
                } finally {
                    removeWaitCallback(callback, this);
                }
            }
        };
    }

    private void addWaitCallback(Runnable callback, WaitCallback waitCallback) {
        synchronized (lock) {
            associationSet.add(waitCallback, tags);

            Set<WaitCallback> waitCallbacks = callbacks.get(callback);
            if (waitCallbacks == null) {
                waitCallbacks = new HashSet<WaitCallback>();
            }
            waitCallbacks.add(waitCallback);
        }
    }

    private void removeWaitCallback(Runnable callback, WaitCallback waitCallback) {
        synchronized (lock) {
            associationSet.remove(waitCallback);

            Set<WaitCallback> waitCallbacks = callbacks.get(callback);
            if (waitCallbacks != null) {
                waitCallbacks.remove(waitCallback);
                if (waitCallbacks.isEmpty()) {
                    callbacks.remove(callback);
                }
            }
        }
    }

    private WaitCallback postCallback(Runnable callback) {
        WaitCallback waitCallback = createWaitCallback(callback);

        synchronized (lock) {
            if (handler.post(waitCallback)) {
                addWaitCallback(callback, waitCallback);
                return waitCallback;
            } else {
                throw new RuntimeException("cannot post callback to the handler");
            }
        }
    }

    private WaitCallback postCallback(long delay, Runnable callback) {
        WaitCallback waitCallback = createWaitCallback(callback);

        synchronized (lock) {
            if (handler.postDelayed(waitCallback, delay)) {
                addWaitCallback(callback, waitCallback);
                return waitCallback;
            } else {
                throw new RuntimeException("cannot post callback to the handler");
            }
        }
    }

    public void post(Runnable callback) {
        postCallback(callback);
    }

    public void post(long delay, Runnable callback) {
        postCallback(delay, callback);
    }

    public void single(Runnable callback) {
        synchronized (lock) {
            remove(callback);
            post(callback);
        }
    }

    public void single(long delay, Runnable callback) {
        synchronized (lock) {
            remove(callback);
            post(delay, callback);
        }
    }

    public void sync(Runnable callback) throws InterruptedException {
        checkJoinAbility();
        postCallback(callback).join();
    }

    public void sync(long delay, Runnable callback) throws InterruptedException {
        checkJoinAbility();
        postCallback(delay, callback).join();
    }

    private Set<WaitCallback> getWaitCallbacks(Runnable callback) {
        synchronized (lock) {
            Set<WaitCallback> waitCallbacks = callbacks.get(callback);
            if (waitCallbacks == null) {
                return Collections.emptySet();
            } else {
                return new HashSet<WaitCallback>(waitCallbacks);
            }
        }
    }

    private Set<WaitCallback> getWaitCallbacks() {
        synchronized (lock) {
            Set<WaitCallback> waitCallbacks = new HashSet<WaitCallback>();
            for (Runnable callback : callbacks.keySet()) {
                waitCallbacks.addAll(getWaitCallbacks(callback));
            }
            return waitCallbacks;
        }
    }

    public void join(Runnable callback) throws InterruptedException {
        checkJoinAbility();
        for (WaitCallback waitCallback : getWaitCallbacks(callback)) {
            waitCallback.join();
        }
    }

    public void join() throws InterruptedException {
        checkJoinAbility();
        for (WaitCallback waitCallback : getWaitCallbacks()) {
            waitCallback.join();
        }
    }

    public void remove(Runnable callback) {
        synchronized (lock) {
            Set<WaitCallback> waitCallbacks = callbacks.remove(callback);
            if (waitCallbacks != null) {
                for (WaitCallback waitCallback : waitCallbacks) {
                    associationSet.remove(waitCallback);
                    waitCallback.release();
                }
            }
        }
    }

    public void remove() {
        synchronized (lock) {
            for (Runnable callback : callbacks.keySet()) {
                remove(callback);
            }
        }
    }

}
