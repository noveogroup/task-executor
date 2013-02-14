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
// интерфейс, а не класс, - не мешает отсутствие множественного наследования
////////////////////////////////////////////////////////////////////////////////
// объект-параметр позволяет задаче:
// - передавать input-output значения
// - управлять освобождаемыми ресурсами
// - проверять статус отмены через isInterrupted и checkInterrupted
////////////////////////////////////////////////////////////////////////////////
// метод кидает произвольную ошибку чтобы не приходилось оборачивать алгоритм
// лишними конструкциями try-catch
////////////////////////////////////////////////////////////////////////////////
public interface Task<T extends Task, CustomEnvironment extends TaskEnvironment> {

    public <E extends CustomEnvironment> void run(TaskHandler<T, E> handler, E env) throws Throwable;

}
