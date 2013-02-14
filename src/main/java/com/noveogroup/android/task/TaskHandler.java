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

import java.util.Set;

////////////////////////////////////////////////////////////////////////////////
// Состояния задачи: CREATED, STARTED, CANCELED, SUCCEED, FAILED
// STARTED  - задача ожидает исполнения в очереди
// STARTED  - задача запущена и в данный момент исполняется
// CANCELED - задача была отменена когда ожидала исполнения
// FAILED   - исполнение задачи завершилось ошибкой (+ InterruptedException)
// SUCCEED  - задача корректно завершилась
////////////////////////////////////////////////////////////////////////////////
// Этот интерфейс позволяет пользователю управлять задачей извне, а именно:
// - получить окружение задачи                 env
// - получить исполняемую задачу               task
// - получить список тэгов задачи              tags
// - получить статус задачи                    getStatus
// - получить ошибку                           getThrowable
// - получить статус отмены                    isInterrupted
// - проверить статус отмены                   checkInterrupted
// - отменить задачу                           interrupt
// - ожидать завершения задачи                 join
////////////////////////////////////////////////////////////////////////////////
// Список тэгов нельзя изменить. В качестве тэга может быть использован
// любой объект, поэтому ссылки на тэг уничтожаются из менеджера сразу
// по завершении задачи и ни в коем случае не хранятся.
////////////////////////////////////////////////////////////////////////////////
public interface TaskHandler<T extends Task, E extends TaskEnvironment> {

    public enum Status {

        CREATED,

        CANCELED,

        STARTED,

        FAILED,

        SUCCEED;

        public boolean isDestroyed() {
            return this == CANCELED || this == FAILED || this == SUCCEED;
        }

        public boolean isFinished() {
            return this == FAILED || this == SUCCEED;
        }

    }

    public TaskSet<E> owner();

    public T task();

    public E env();

    // todo дублирование с owner().tags()
    public Set<Object> tags();

    public Status getStatus();

    public Throwable getThrowable();

    public boolean isInterrupted();

    public void interrupt();

    public void join() throws InterruptedException;

    public void join(long timeout) throws InterruptedException;

}
