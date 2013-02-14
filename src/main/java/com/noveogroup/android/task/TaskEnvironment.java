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

////////////////////////////////////////////////////////////////////////////////
// Синхронизационный объект используется из pack, то есть:
// lock() == args().lock()
////////////////////////////////////////////////////////////////////////////////
// Этот интерфейс - заготовка для всяческих помощников, которые упрощают
// вывод диалогов и прочую такую вот вспомогательную ерунду.
////////////////////////////////////////////////////////////////////////////////
// Синхронизация при работе с этим объектом происходит по другому объекту,
// так как эта синхронизхация - частная, она - личное дело задачи.
// Но вложенность синхронизаций все же может быть, да и скорее есть:
// НО: только в порядке частная - общая, что должно быть проверено прямо в
// реализации метода lock().
////////////////////////////////////////////////////////////////////////////////
// todo strange cyclic dependency between TaskEnvironment and TaskHandler (may be to delete TaskHandler.env ???)
public class TaskEnvironment<T extends Task, E extends TaskEnvironment> {

    private final Pack args;
    private final TaskHandler<T, E> taskHandler;

    public TaskEnvironment(TaskHandler<T, E> taskHandler) {
        this.taskHandler = taskHandler;
        this.args = new Pack();
    }

    public TaskEnvironment(TaskHandler<T, E> taskHandler, Pack args) {
        this(taskHandler);
        this.args.putAll(args);
    }

    public final Object lock() {
        return args().lock();
    }

    public final Pack args() {
        return args;
    }

    public final TaskHandler<T, E> taskHandler() {
        return taskHandler;
    }

    public final void checkInterrupted() throws InterruptedException {
        if (taskHandler().isInterrupted()) {
            throw new InterruptedException();
        }
    }

}
