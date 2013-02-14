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
// Напомним, что состояния задачи: CREATED, STARTED, CANCELED, SUCCEED, FAILED
////////////////////////////////////////////////////////////////////////////////
// * Создали задачу и добавили в очередь
//   > CREATE onCreate                        { in direct order
// * Прервали задачу пока она была в очереди
//   > проставляется флаг interrupted
//   > CANCELED onCanceled onDestroy          } in reverse order
// * Задача поступила на исполнение
//   > STARTED onStart                        { in direct order
// * Задачу пытаются прервать во время исполнения
//   > проставляется флаг interrupted
//   > ничего не происходит
// * Задача корректно завершилась
//   > SUCCEED onFinish onSucceed onDestroy   } in reverse order
// * Задача выбросила throwable (в том числе и InterruptedException)
//   > проставляется throwable
//   > FAILED onFinish onFailed onDestroy     } in reverse order
// * Задачу создали в убитом executor-е:
//   > CANCELED onCanceled
////////////////////////////////////////////////////////////////////////////////
// Таким образом возможны следующие последовательности:
// CREATE [ADD] onCreate CANCELED onCanceled [DEL] onDestroy
// CREATE [ADD] onCreate STARTED onStart [RUN] SUCCEED onFinish onSucceed [DEL] onDestroy
// CREATE [ADD] onCreate STARTED onStart [RUN] FAILED  onFinish onFailed  [DEL] onDestroy
// CANCELED onCanceled
// то есть существуют парные методы: onCreate - onDestroy, onStart  - onFinish
// и непарные, уведомляющие: onCanceled, onFailed, onSucceed
////////////////////////////////////////////////////////////////////////////////
// Порядок исполнения листенеров подобран специально - сначала прямой, а в конце
// обратный порядку добаления. Это сделано для симметрии исполнения.
////////////////////////////////////////////////////////////////////////////////
// Листенер Default сделан просто для удобства
// Самодельный Wrapper делать не стал потому что он особенно не нужен снаружи
////////////////////////////////////////////////////////////////////////////////
// Внутри методов листенера можно виснуть сколько угодно
// Это ни на что не повлияет, разве что задержит жизненный цикл задачи
// То есть пока onCreate не исполнится задача не запустится
// Ломаться внутри листенеров лучше не надо - любой exception уходит на
// обработку соответствующему UncaughtExceptionHandler, после чего приложение
// скорее всего будет аварийно завершено.
////////////////////////////////////////////////////////////////////////////////
// Листенеры запускаются по очереди, то есть не параллельно.
////////////////////////////////////////////////////////////////////////////////
// todo ??? add onQueueInsert and onQueueRemove to TaskListener (onCreate onCancel onDestroy && onCreate onQueueInsert onCancel onQueueRemove onDestroy)
public interface TaskListener {

    public class Default implements TaskListener {

        @Override
        public void onCreate(TaskHandler<?, ?> handler) {
        }

        @Override
        public void onCancel(TaskHandler<?, ?> handler) {
        }

        @Override
        public void onStart(TaskHandler<?, ?> handler) {
        }

        @Override
        public void onFinish(TaskHandler<?, ?> handler) {
        }

        @Override
        public void onSucceed(TaskHandler<?, ?> handler) {
        }

        @Override
        public void onFailed(TaskHandler<?, ?> handler) {
        }

        @Override
        public void onDestroy(TaskHandler<?, ?> handler) {
        }

    }

    public void onCreate(TaskHandler<?, ?> handler);

    public void onCancel(TaskHandler<?, ?> handler);

    public void onStart(TaskHandler<?, ?> handler);

    public void onFinish(TaskHandler<?, ?> handler);

    public void onSucceed(TaskHandler<?, ?> handler);

    public void onFailed(TaskHandler<?, ?> handler);

    public void onDestroy(TaskHandler<?, ?> handler);

}
