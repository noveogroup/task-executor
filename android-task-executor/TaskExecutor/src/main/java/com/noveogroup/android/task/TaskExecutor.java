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

import java.util.Collection;
import java.util.List;

public interface TaskExecutor {

    /**
     * Returns synchronization object of this {@link TaskExecutor}.
     * <p/>
     * Any access to this {@link TaskExecutor} should be synchronized using
     * this object.
     * <p/>
     * The same object should be returned from method {@link Pack#lock()} by
     * all of collections of arguments associated with tasks belonging to
     * this {@link TaskExecutor}.
     *
     * @return the synchronization object.
     */
    public Object lock();

    public Pack newPack();

    public Pack newPack(Pack pack);

    public Pack args();

    public TaskSet queue(String... tags);

    public TaskSet queue(Collection<String> tags);

    public void addTaskListener(TaskListener... taskListeners);

    public void removeTaskListener(TaskListener... taskListeners);

    public TaskHandler execute(Task task, Pack args, List<TaskListener> taskListeners, Collection<String> tags);

    public TaskHandler execute(Task task, Pack args, List<TaskListener> taskListeners, String... tags);

    public TaskHandler execute(Task task, Pack args, TaskListener taskListener, String... tags);

    public TaskHandler execute(Task task, Pack args, String... tags);

    public TaskHandler execute(Task task, List<TaskListener> taskListeners, String... tags);

    public TaskHandler execute(Task task, TaskListener taskListener, String... tags);

    public TaskHandler execute(Task task, String... tags);

    public void shutdown();

    public boolean isShutdown();

}
