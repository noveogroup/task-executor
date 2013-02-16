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
// Состояния задачи: CREATED, STARTED, CANCELED, SUCCEED, FAILED
// CREATED  - задача ожидает исполнения в очереди
// STARTED  - задача запущена и в данный момент исполняется
// CANCELED - задача была отменена когда ожидала исполнения
// FAILED   - исполнение задачи завершилось ошибкой (+ InterruptedException)
// SUCCEED  - задача корректно завершилась
////////////////////////////////////////////////////////////////////////////////
// Этот интерфейс позволяет пользователю управлять задачей извне, а именно:
// - получить множество-хозяин                 owner
// - получить исполняемую задачу               task
// - получить аргументы задачи                 args
// - получить статус задачи                    getStatus
// - получить ошибку                           getThrowable
// - получить статус отмены                    isInterrupted
// - отменить задачу                           interrupt
// - ожидать завершения задачи                 join
////////////////////////////////////////////////////////////////////////////////
public interface TaskHandler<T extends Task, E extends TaskEnvironment> {

    public enum Status {

        CREATED,

        STARTED,

        CANCELED,

        FAILED,

        SUCCEED;

        public boolean isAlive() {
            return this == CREATED || this == STARTED;
        }

        public boolean isDestroyed() {
            return this == CANCELED || this == FAILED || this == SUCCEED;
        }

        public boolean isFinished() {
            return this == FAILED || this == SUCCEED;
        }

    }

    public TaskSet<E> owner();

    public T task();

    public Pack args();

    public Status getStatus();

    public Throwable getThrowable();

    public boolean isInterrupted();

    public void interrupt();

    public void join() throws InterruptedException;

    public boolean join(long timeout) throws InterruptedException;

}
