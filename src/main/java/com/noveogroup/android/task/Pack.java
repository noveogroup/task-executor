/*
 * Copyright (c) 2013 Noveo Group
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

package com.noveogroup.android.task;

import java.util.*;

////////////////////////////////////////////////////////////////////////////////
// Следует обратить внимание, что метод keySet() возвращает множество с
// удаляемыми элементами.
////////////////////////////////////////////////////////////////////////////////
// Для синхронизации есть специальный объект, возвращаемый методом lock().
////////////////////////////////////////////////////////////////////////////////
// Этот класс может создаваться с помощью конструктора, а затем использоваться
// для создания задачи, так как он и задает синхронизационный объект env.
////////////////////////////////////////////////////////////////////////////////
public final class Pack implements Cloneable, Iterable<String> {

    private final Object lock = new Object();
    private final HashMap<String, Object> map = new HashMap<String, Object>();

    public Pack() {
    }

    public Pack(Pack pack) {
        synchronized (lock()) {
            synchronized (pack.lock()) {
                putAll(pack);
            }
        }
    }

    public Object lock() {
        return lock;
    }

    @Override
    public Pack clone() {
        synchronized (lock()) {
            Pack pack = new Pack();
            synchronized (pack.lock()) {
                pack.putAll(this);
            }
            return pack;
        }
    }

    public int size() {
        synchronized (lock()) {
            return map.size();
        }
    }

    public boolean isEmpty() {
        synchronized (lock()) {
            return map.isEmpty();
        }
    }

    @Override
    public Iterator<String> iterator() {
        synchronized (lock()) {
            HashSet<String> copyKeySet = new HashSet<String>(map.keySet());
            final Iterator<String> delegate = copyKeySet.iterator();
            return new Iterator<String>() {
                private final Object iteratorLock = new Object();
                private String currentKey = null;

                @Override
                public boolean hasNext() {
                    synchronized (iteratorLock) {
                        return delegate.hasNext();
                    }
                }

                @Override
                public String next() {
                    synchronized (iteratorLock) {
                        return currentKey = delegate.next();
                    }
                }

                @Override
                public void remove() {
                    synchronized (iteratorLock) {
                        // implicitly check position of cursor
                        delegate.remove();
                        // so currentKey is valid
                        Pack.this.remove(currentKey);
                    }
                }
            };
        }
    }

    public Set<String> keySet() {
        return new AbstractSet<String>() {
            @Override
            public Iterator<String> iterator() {
                return Pack.this.iterator();
            }

            @Override
            public int size() {
                return Pack.this.size();
            }
        };
    }

    public boolean containsKey(String key) {
        synchronized (lock()) {
            return map.containsKey(key);
        }
    }

    public <T> T get(String key) {
        synchronized (lock()) {
            return get(key, null);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        synchronized (lock()) {
            T value = (T) map.get(key);
            return value != null ? value : defaultValue;
        }
    }

    public Pack clear() {
        synchronized (lock()) {
            map.clear();
            return this;
        }
    }

    public Pack remove(Object key) {
        synchronized (lock()) {
            map.remove(key);
            return this;
        }
    }

    public <T> Pack put(String key, T value) {
        synchronized (lock()) {
            map.put(key, value);
            return this;
        }
    }

    public <T> Pack put(String key, T value, T defaultValue) {
        synchronized (lock()) {
            if (value != null) {
                map.put(key, value);
            } else {
                map.put(key, defaultValue);
            }
            return this;
        }
    }

    public <T> Pack putNotNull(String key, T value) {
        synchronized (lock()) {
            if (value != null) {
                map.put(key, value);
            }
            return this;
        }
    }

    public <T> Pack putIf(boolean condition, String key, T value) {
        synchronized (lock()) {
            if (condition) {
                map.put(key, value);
            }
            return this;
        }
    }

    public Pack putAll(Pack pack) {
        synchronized (lock()) {
            synchronized (pack.lock()) {
                map.putAll(pack.map);
            }
            return this;
        }
    }

}
