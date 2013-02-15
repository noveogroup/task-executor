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
// этот объект совпадает с глобальным объектом синхронизции
////////////////////////////////////////////////////////////////////////////////
// todo lock заимствуется извне и поэтому создание этого объекта усложняется
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
            // todo implement remove & may be some other methods

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

    /**
     * Clears the pack. Removes all arguments and leaves pack empty.
     *
     * @return this pack object.
     * @see #isEmpty()
     * @see #size()
     */
    public Pack clear() {
        synchronized (lock()) {
            map.clear();
            return this;
        }
    }

    /**
     * Removes an argument corresponding the specified key.
     *
     * @param key the key.
     * @return this pack object.
     */
    public Pack remove(String key) {
        synchronized (lock()) {
            map.remove(key);
            return this;
        }
    }

    /**
     * Updates an argument corresponding to the specified key and sets its value to the specified one.
     *
     * @param key   the key.
     * @param value the value.
     * @param <T>   the type of the value.
     * @return this pack object.
     * @see #put(String, Object)
     * @see #put(String, Object, Object)
     * @see #putIf(boolean, String, Object)
     * @see #putAll(Pack)
     */
    public <T> Pack put(String key, T value) {
        synchronized (lock()) {
            map.put(key, value);
            return this;
        }
    }

    /**
     * Updates an argument corresponding to the specified key and sets its value to the specified one
     * or, if the specified value is {@code null} - to the specified default one.
     *
     * @param key          the key.
     * @param value        the value.
     * @param defaultValue the default value to use when the usual one is {@code null}.
     * @param <T>          the type of the value.
     * @return this pack object.
     * @see #put(String, Object)
     * @see #putIf(boolean, String, Object)
     * @see #putAll(Pack)
     */
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

    /**
     * Updates an argument just like {@link #put(String, Object)} but do it if only the specified
     * condition is {@code true}.
     *
     * @param condition the condition.
     * @param key       the key.
     * @param value     the value.
     * @param <T>       the type of the value.
     * @return this pack object.
     * @see #put(String, Object)
     * @see #put(String, Object, Object)
     * @see #putAll(Pack)
     */
    public <T> Pack putIf(boolean condition, String key, T value) {
        synchronized (lock()) {
            if (condition) {
                map.put(key, value);
            }
            return this;
        }
    }

    /**
     * Copies all the arguments in the specified pack to this pack.
     *
     * @param pack the pack to copy arguments from.
     * @return this pack object.
     * @see #put(String, Object)
     * @see #put(String, Object, Object)
     * @see #putIf(boolean, String, Object)
     */
    public Pack putAll(Pack pack) {
        synchronized (lock()) {
            synchronized (pack.lock()) {
                map.putAll(pack.map);
            }
            return this;
        }
    }

}
