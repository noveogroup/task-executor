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
// В лог записываются:
// - Операции с очередью задач, изменение статуса задач с подробностями
// - Ошибки листенеров
// - Операции с множествами задач - создание, interrupt и удаление
////////////////////////////////////////////////////////////////////////////////
// Синхронизационный объект отвечает за всю синхронизацию в менеджере задач.
// todo как он передается в Pack ?
// todo где проверятся что объект совпадает ? это напоминалка, что нужно проверять
/*
С pack следующая проблема:
- с одной стороны,
  хочется, создав его при старте задачи, продолжать пользоваться тем же
  объектом и в дальнейшем.
- с другой стороны,
  вынуждать пользователя указывать в конструкторе синхронизационный объект
  не самое удобное решение. кроме того, необходимо будет проверять объект
  дополнительно
 */
////////////////////////////////////////////////////////////////////////////////
// Можно добавлять листенеры с помощью addTaskListener и удалять с помощью
// removeTaskListener. Порядок добавления учитывается при исполнении, смотри
// описание методов листенера.
// todo как насчет листенеров жизненного цикла менеджера ?
////////////////////////////////////////////////////////////////////////////////
// Методы execute позволяют добавить задачу в очередь, при этом добавляется
// фиктивный тэг, случайно сгенерированный, если такой тэг не указан.
// todo тэги Object неудобны тем, что Collection тэгов и массив тэгов - тоже тэг
// todo то есть тэгов у меня уже целых два типа ?
// todo по-моему тэги расслоились из-за того , что не была проработана идея с указанием множества для задач
/*
Просто если вообще не изобретать фиктивный тэг то появляется
много сложностей с множествами задач:
 - Когда я создаю задачу с тэгами, в какое множество она попадает ?
 - Когда я достаю множество по тэгам, какое достается ?
 - Когда я прерываю множество по тэгам, какое прерывается ?
На самом деле эти дополнительные тэги проблем не решают тоже !
Самое лучшее решение, возможно - отдать это в руки пользователю.
Тогда тэги лучше всего сделать строчками и таким образом решить все проблемы с тэгами.
 */
////////////////////////////////////////////////////////////////////////////////
// С остановкой менеджера связаны методы shutdown, isShutdown, isTerminated
// и awaitTermination.
// Если менеджер isShutdown, то все задачи будут автоматически отменяться
// проходя при этом корректный жизненный цикл отмены.
////////////////////////////////////////////////////////////////////////////////
// join на самого себя заканчивается ошибкой IllegalThreadException
////////////////////////////////////////////////////////////////////////////////
public interface TaskExecutor<E extends TaskEnvironment> {

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
