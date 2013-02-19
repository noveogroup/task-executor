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

////////////////////////////////////////////////////////////////////////////////
// Синхронизационный объект отвечает за всю синхронизацию в менеджере задач.
// Он должен быть передан в Pack. Это проверяется и Pack даже пересоздается
// если в качестве аргументов будет передан неправильный.
////////////////////////////////////////////////////////////////////////////////
// Можно добавлять листенеры с помощью addTaskListener и удалять с помощью
// removeTaskListener. Порядок добавления учитывается при исполнении, смотри
// описание методов листенера.
// todo как насчет листенеров жизненного цикла менеджера ?
////////////////////////////////////////////////////////////////////////////////
// Методы execute позволяют добавить задачу в очередь, при этом создается новый
// TaskSet или используется существующий. Существующий используется даже если
// он уже прерван. Но когда все задачи в TaskSet завершаеются - он исчезает тоже
////////////////////////////////////////////////////////////////////////////////
// С остановкой менеджера связаны методы shutdown, isShutdown, isTerminated
// и awaitTermination.
// Если менеджер isShutdown, то все задачи будут автоматически отменяться
// проходя при этом корректный жизненный цикл отмены.
////////////////////////////////////////////////////////////////////////////////
// join на самого себя заканчивается ошибкой IllegalThreadException
////////////////////////////////////////////////////////////////////////////////
public interface TaskExecutor<E extends TaskEnvironment> {

    /**
     * A standard log tag for {@link TaskExecutor}.
     * <p/>
     * The following log messages are printed into the log:
     * <ul>
     * <li><b>with ERROR log level:</b> Exceptions occurred in callback
     * methods of {@link TaskListener}.</li>
     * <li><b>with VERBOSE log level:</b> Task queue modifications and
     * {@link TaskHandler.State} updates with details.</li>
     * <li><b>with DEBUG log level:</b> Important operations with {@link TaskSet}</li>
     * </ul>
     */
    public static final String TAG = "com.noveogroup.android.task";

    public void addTaskListener(TaskListener... taskListeners);

    public void removeTaskListener(TaskListener... taskListeners);

    public <T extends Task<E>> TaskHandler<T, E> execute(T task, Pack args, List<TaskListener> taskListeners, String... tags);

    public <T extends Task<E>> TaskHandler<T, E> execute(T task, Pack args, TaskListener taskListener, String... tags);

    public <T extends Task<E>> TaskHandler<T, E> execute(T task, Pack args, TaskListener... taskListeners);

    public <T extends Task<E>> TaskHandler<T, E> execute(T task, Pack args, String... tags);

    public <T extends Task<E>> TaskHandler<T, E> execute(T task, List<TaskListener> taskListeners, String... tags);

    public <T extends Task<E>> TaskHandler<T, E> execute(T task, TaskListener taskListener, String... tags);

    public <T extends Task<E>> TaskHandler<T, E> execute(T task, TaskListener... taskListeners);

    public <T extends Task<E>> TaskHandler<T, E> execute(T task, String... tags);

    public Object lock();

    public Pack newPack();

    public Pack newPack(Pack pack);

    public TaskSet<E> queue();

    public TaskSet<E> queue(String... tags);

    public TaskSet<E> queue(Collection<String> tags);

    public void shutdown();

    public boolean isShutdown();

    public boolean isTerminated();

    public void awaitTermination() throws InterruptedException;

    public boolean awaitTermination(long timeout) throws InterruptedException;

}
