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
import java.util.Iterator;
import java.util.Set;

////////////////////////////////////////////////////////////////////////////////
// Интерфейс TaskSet - это просто обертка вокруг синхронизованных проходов
// по списку задач с отсевом задач не подходящих по тэгам.
////////////////////////////////////////////////////////////////////////////////
// Можно делать следующее:
// - Добавлять задачу в task set (чтобы не указывать тэги и привязывать задачу
//   сразу к данному множеству).
// - Узнать тэги используемые для фильтрации, отдается unmodifiable множество
// - Гонять по иерархии - получать подмножества
// - получить количество задач (со статусами CREATED и STARTED)
//   просто задачи с другими статусами в очереди отсутствуют
// - отменять задачи скопом (исключая добавленные после вызова)
// - ожидать завершения задач (исключая добавленные после вызова)
// - перебирать задачи итератором, правда нельзя удалять
////////////////////////////////////////////////////////////////////////////////
// Список тэгов нельзя изменить. В качестве тэга может быть использован
// любой объект, поэтому ссылки на тэг уничтожаются из менеджера сразу
// по завершении задачи и ни в коем случае не хранятся.
// todo тэги Object неудобны тем, что Collection тэгов и массив тэгов - тоже тэг
////////////////////////////////////////////////////////////////////////////////
public interface TaskSet<E extends TaskEnvironment> extends Iterable<TaskHandler<?, E>> {

    public TaskExecutor<E> executor();

    public Set<Object> tags();

    public TaskSet<E> sub(Object... tags);

    public TaskSet<E> sub(Collection<Object> tags);

    public <T extends Task<E>> TaskHandler<T, E> execute(T task, Pack args, TaskListener... taskListeners);

    public <T extends Task<E>> TaskHandler<T, E> execute(T task, TaskListener... taskListeners);

    public int size();

    public boolean isEmpty();

    @Override
    public Iterator<TaskHandler<? extends Task, E>> iterator();

    public void interrupt();

    public void join() throws InterruptedException;

    public void join(long timeout) throws InterruptedException;

}
