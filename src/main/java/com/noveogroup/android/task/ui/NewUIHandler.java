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

import java.util.*;

public class NewUIHandler {

    private static class AssociationSet<V> {

        private final Map<V, Set<String>> values = new HashMap<V, Set<String>>();

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

    private static Set<String> union(Collection<String> set1, Collection<String> set2) {
        HashSet<String> set = new HashSet<String>(set1.size() + set2.size());
        set.addAll(set1);
        set.addAll(set2);
        return Collections.unmodifiableSet(set);
    }

    private final Object lock;
    private final Handler handler;
    private final AssociationSet<WaitCallback> associationSet;
    private final Map<Runnable, Set<WaitCallback>> callbacks;

    private final NewUIHandler root;
    private final Set<String> tags;

    private NewUIHandler(Object lock, Handler handler,
                         AssociationSet<WaitCallback> associationSet, Map<Runnable, Set<WaitCallback>> callbacks,
                         NewUIHandler root, Set<String> tags) {
        this.lock = lock;
        this.handler = handler;
        this.associationSet = associationSet;
        this.callbacks = callbacks;
        this.root = root;
        this.tags = tags;
    }

    public NewUIHandler(Handler handler) {
        this(new Object(), handler,
                new AssociationSet<WaitCallback>(), new HashMap<Runnable, Set<WaitCallback>>(),
                null, Collections.<String>emptySet());
    }

    public NewUIHandler() {
        this(new Handler());
    }

    public NewUIHandler(Looper looper) {
        this(new Handler(looper));
    }

    public NewUIHandler(Context context) {
        this(new Handler(context.getMainLooper()));
    }

    public NewUIHandler sub(String... tags) {
        return new NewUIHandler(lock, handler, null, null, this, union(this.tags, Arrays.asList(tags)));
    }

    private WaitCallback createWaitCallback(final Runnable callback) {
        return new WaitCallback() {
            @Override
            protected void runCallback() {
                try {
                    callback.run();
                } finally {
                    synchronized (lock) {
                        associationSet.remove(this);
                        callbacks.remove(callback);
                    }
                }
            }
        };
    }

    public void post(Runnable callback, String... tags) {
        post(callback, Arrays.asList(tags));
    }

    public void post(Runnable callback, Collection<String> tags) {
        if (root != null) {
            root.post(callback, union(this.tags, tags));
        } else {
            synchronized (lock) {
                WaitCallback waitCallback = createWaitCallback(callback);
                if (handler.post(waitCallback)) {
                    associationSet.add(waitCallback, union(this.tags, tags));
                    Set<WaitCallback> waitCallbacks = callbacks.get(callback);
                    if (waitCallbacks == null) {
                        waitCallbacks = new HashSet<WaitCallback>();
                    }
                    waitCallbacks.add(waitCallback);
                } else {
                    throw new RuntimeException("cannot post callback to the handler");
                }
            }
        }
    }

    public void post(long delay, Runnable callback, String... tags) {
        post(delay, callback, Arrays.asList(tags));
    }

    public void post(long delay, Runnable callback, Collection<String> tags) {
        if (root != null) {
            root.post(delay, callback, union(this.tags, tags));
        } else {
            synchronized (lock) {
                WaitCallback waitCallback = createWaitCallback(callback);
                if (handler.postDelayed(waitCallback, delay)) {
                    associationSet.add(waitCallback, union(this.tags, tags));
                    Set<WaitCallback> waitCallbacks = callbacks.get(callback);
                    if (waitCallbacks == null) {
                        waitCallbacks = new HashSet<WaitCallback>();
                    }
                    waitCallbacks.add(waitCallback);
                } else {
                    throw new RuntimeException("cannot post callback to the handler");
                }
            }
        }
    }

    public void sync(Runnable callback, String... tags) throws InterruptedException {
        sync(callback, Arrays.asList(tags));
    }

    public void sync(Runnable callback, Collection<String> tags) throws InterruptedException {
        if (root != null) {
            root.sync(callback, union(this.tags, tags));
        } else {
            // todo
        }
    }

    public void join(Runnable callback, String... tags) throws InterruptedException {
        join(callback, Arrays.asList(tags));
    }

    public void join(Runnable callback, Collection<String> tags) throws InterruptedException {
        if (root != null) {
            root.join(callback, union(this.tags, tags));
        } else {
            // todo
        }
    }

    public void join(String... tags) throws InterruptedException {
        join(Arrays.asList(tags));
    }

    public void join(Collection<String> tags) throws InterruptedException {
        if (root != null) {
            root.join(union(this.tags, tags));
        } else {
            // todo
        }
    }

    public void remove(Runnable callback, String... tags) {
        remove(callback, Arrays.asList(tags));
    }

    public void remove(Runnable callback, Collection<String> tags) {
        if (root != null) {
            root.remove(callback, union(this.tags, tags));
        } else {
            // todo
        }
    }

    public void remove(String... tags) {
        remove(Arrays.asList(tags));
    }

    public void remove(Collection<String> tags) {
        if (root != null) {
            root.remove(union(this.tags, tags));
        } else {
            // todo
        }
    }

}
