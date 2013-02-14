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
// Этот интерфейс - заготовка для всяческих помощников, которые упрощают
// вывод диалогов и прочую такую вот вспомогательную ерунду.
////////////////////////////////////////////////////////////////////////////////
// Синхронизационный объект принадлежит лично этому TaskEnvironment.
// Главное правило, которое, правда, все равно проверяется автоматически:
// блокировать только в порядке главный блок - частный блок.
// эта проверка реализуется внутри метода этого интерфейса. В случае нарушения
// контракта бросаем исключение IllegalStateException.
// Синхронизируется этим объектом следующее:
// - операции с аргументами
// - все другие действия, которые можно выполнять при осуществлении блокировки
//   очереди. Когда очередь блокируется - замораживаются почти все процессы.
//   но запущенные задачи могут продолжать работать, пока не обратятся к
//   заблокированному функционалу.
// - а все остальные действия синхронизируются по основному объекту:
//   (потому что они все напрямую влияют на очередь задач)
//   - isInterrupted
//   - checkInterrupted
//   - interruptSelf
//   - owner
////////////////////////////////////////////////////////////////////////////////
// todo описать
// todo часть методов нужны снаружи, а часть нет. как быть ?
// todo 1. сделать еще один интерфейс и выделить в него общую часть
// todo 2. удалить отсюда лишние методы и реализовать их в классе
/* ВАРИАНТ 1:
TaskEnvironment {
  public Object taskLock();
  public Pack args();
}
TaskHandler {
  public Object taskLock();
  public Pack args();
}
*/
/* ВАРИАНТ 2:
TaskEnvironment {
  public Pack args();
}
TaskHandler {
  public Pack args();
}
*/
/* ВАРИАНТ 3:
TaskEnvironment {
  public XXX xxx();
}
TaskHandler {
  public XXX xxx();
}
XXX {
  public Object lock();
  public Pack args();
}
*/

// а вообще все это растет из синхронизации.
// синхронизация очереди задач влючает в себя практически все.
// единственное что нуждается в похожей синхронизации и при этом
// отчуждаемо от блокировок, связанных с очередью - это аргументы

// но делать аргументы синхронизируемыми - может быть излишним усложнением
// поэтому возможно следует сделать синхронизацию аргументов проще

// с другой стороны, частный синхронизационный объект задачи с проверкой
// порядка блокировки общий-частный - не так уж плохо. даже вполне хорошо
// но такая архитектура все равно имеет врожденную склонность к dead-lock-ам

// есть левый вариант: сделать всю синхронизацию завязанной на один объект
// тогда отпадает столько проблем ... и с синхронизацией, и с интерфейсами,
// и с dead-lock-ами ...
// правда в этом случае проблема синхронизации внутри задачи остается
// непроработанной. может быть это и не такая проблема ...
public interface TaskEnvironment {

    public Object lock() throws IllegalStateException;

    public Pack args();

    public TaskSet owner();

    public void interruptSelf();

    public boolean isInterrupted();

    public void checkInterrupted() throws InterruptedException;

}
