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

////////////////////////////////////////////////////////////////////////////////
// В лог записываются:
// - Операции с очередью задач, изменение статуса задач с подробностями
// - Ошибки листенеров
// - Операции с множествами задач - создание, interrupt и удаление
////////////////////////////////////////////////////////////////////////////////
// Есть способ доступа к очереди задач:
// - синхронизационный объект
//   смотри описание алгоритма синхронизации в TaskHandler-е
// - интерфейсы TaskSet которые позволяют осуществлять выборку задач по тэгам
// Интерфейс TaskSet - это просто обертка вокруг синхронизованных проходов
// по списку задач с отсевом задач не подходящих по тэгам.
// Можно делать следующее:
// - Добавлять задачу в task set (чтобы не указывать тэги)
// - Узнать тэги используемые для фильтрации, отдается unmodifiable что-то
//   потому что менять их можно только специальным методом
// - Гонять по иерархии куда хочется
// - получить количество задач (со статусами CREATED и STARTED)
//   просто задачи с другими статусами в очереди отсутствуют
// - отменять задачи скопом (исключая добавленные после вызова)
// - ожидать завершения задач (исключая добавленные после вызова)
// - перебирать задачи итератором, правда нельзя удалять
////////////////////////////////////////////////////////////////////////////////
// Можно добавлять листенеры с помощью addTaskListener и удалять с помощью
// removeTaskListener. Порядок добавления учитывается при исполнении, смотри
// описание методов листенера.
////////////////////////////////////////////////////////////////////////////////
// Методы execute позволяют добавить задачу в очередь.
////////////////////////////////////////////////////////////////////////////////
// С остановкой менеджера связаны методы shutdown, isShutdown, isTerminated
// и awaitTermination.
// Если менеджер isShutdown, то все задачи будут автоматически отменяться
// проходя при этом корректный жизненный цикл отмены.
////////////////////////////////////////////////////////////////////////////////
// join на самого себя заканчивается ошибкой IllegalThreadException
////////////////////////////////////////////////////////////////////////////////
// обычный execute добавляет фиктивный тэг, случайно сгенерированный
////////////////////////////////////////////////////////////////////////////////
public interface TaskExecutor<E extends TaskEnvironment> {

    public static final String TAG = "com.noveogroup.android.task";

    public void addTaskListener(TaskListener... taskListeners);

    public void removeTaskListener(TaskListener... taskListeners);

    // todo may be remove super ???
    public <T extends Task<? super E>> TaskHandler<T, E> execute(T task, Collection<Object> tags, Pack args, TaskListener... taskListeners);

    public <T extends Task<? super E>> TaskHandler<T, E> execute(T task, Collection<Object> tags, TaskListener... taskListeners);

    public <T extends Task<? super E>> TaskHandler<T, E> execute(T task, Pack args, TaskListener... taskListeners);

    public <T extends Task<? super E>> TaskHandler<T, E> execute(T task, TaskListener... taskListeners);

    public Object lock();

    public TaskSet<E> queue();

    // todo add queue(tags)

    public void shutdown();

    public boolean isShutdown();

    public boolean isTerminated();

    public boolean awaitTermination(long timeout) throws InterruptedException;

}
