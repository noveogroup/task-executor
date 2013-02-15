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

public class TEST {

    public static class TaskEnvironment1 implements TaskEnvironment {
        @Override
        public Pack args() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public TaskSet owner() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void interruptSelf() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isInterrupted() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void checkInterrupted() throws InterruptedException {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static class TaskEnvironment2 extends TaskEnvironment1 {
    }

    public static class Task1<E extends TaskEnvironment1> implements Task<E> {
        @Override
        public void run(E env) throws Throwable {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static class Task2<E extends TaskEnvironment2> implements Task<E> {
        @Override
        public void run(E env) throws Throwable {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static class TaskExecutor1 implements TaskExecutor<TaskEnvironment1> {
        @Override
        public void addTaskListener(TaskListener... taskListeners) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void removeTaskListener(TaskListener... taskListeners) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public <T extends Task<TaskEnvironment1>> TaskHandler<T, TaskEnvironment1> execute(T task, Collection<Object> tags, Pack args, TaskListener... taskListeners) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public <T extends Task<TaskEnvironment1>> TaskHandler<T, TaskEnvironment1> execute(T task, Collection<Object> tags, TaskListener... taskListeners) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public <T extends Task<TaskEnvironment1>> TaskHandler<T, TaskEnvironment1> execute(T task, Pack args, TaskListener... taskListeners) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public <T extends Task<TaskEnvironment1>> TaskHandler<T, TaskEnvironment1> execute(T task, TaskListener... taskListeners) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object lock() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public TaskSet<TaskEnvironment1> queue() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void shutdown() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isShutdown() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isTerminated() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean awaitTermination(long timeout) throws InterruptedException {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static class TaskExecutor2 implements TaskExecutor<TaskEnvironment2> {
        @Override
        public void addTaskListener(TaskListener... taskListeners) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void removeTaskListener(TaskListener... taskListeners) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public <T extends Task<TaskEnvironment2>> TaskHandler<T, TaskEnvironment2> execute(T task, Collection<Object> tags, Pack args, TaskListener... taskListeners) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public <T extends Task<TaskEnvironment2>> TaskHandler<T, TaskEnvironment2> execute(T task, Collection<Object> tags, TaskListener... taskListeners) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public <T extends Task<TaskEnvironment2>> TaskHandler<T, TaskEnvironment2> execute(T task, Pack args, TaskListener... taskListeners) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public <T extends Task<TaskEnvironment2>> TaskHandler<T, TaskEnvironment2> execute(T task, TaskListener... taskListeners) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object lock() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public TaskSet<TaskEnvironment2> queue() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void shutdown() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isShutdown() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isTerminated() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean awaitTermination(long timeout) throws InterruptedException {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    static {
        Task1 task1 = new Task1();
        Task2 task2 = new Task2();
        TaskExecutor1 taskExecutor1 = new TaskExecutor1();
        TaskExecutor2 taskExecutor2 = new TaskExecutor2();

        taskExecutor1.execute(task1);
        taskExecutor1.execute(task2);
        taskExecutor2.execute(task1);
        taskExecutor2.execute(task2);
    }

}
