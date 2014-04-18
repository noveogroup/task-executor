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

package com.noveogroup.android.task.example;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import com.noveogroup.android.task.SimpleTaskEnvironment;
import com.noveogroup.android.task.Task;
import com.noveogroup.android.task.TaskExecutor;
import com.noveogroup.android.task.ui.AndroidTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TaskPerformanceExampleActivity extends ExampleActivity {

    private AndroidTaskExecutor executor = new AndroidTaskExecutor(this);

    @Override
    protected void onResume() {
        super.onResume();
        executor.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        executor.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        loadWebViewExample();

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long startTime = SystemClock.uptimeMillis();
                synchronized (executor.lock()) {
                    for (int i = 0; i < 1000; i++) {
                        final int index = i;
                        executor.execute(new Task<SimpleTaskEnvironment>() {
                            @Override
                            public void run(SimpleTaskEnvironment env) throws Throwable {
                                Log.i(TaskExecutor.TAG, "task #" + index);
                                Utils.calculate(100);
                            }
                        });
                    }
                }
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            executor.queue().join();
                            Log.i(TaskExecutor.TAG, "time = " + (SystemClock.uptimeMillis() - startTime));

                            final long startTime = SystemClock.uptimeMillis();
                            ExecutorService executorService = Executors.newFixedThreadPool(7);
                            for (int i = 0; i < 1000; i++) {
                                final int index = i;
                                executorService.submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TaskExecutor.TAG, "task #" + index);
                                        Utils.calculate(100);
                                    }
                                });
                            }
                            executorService.shutdown();
                            while (!executorService.isTerminated()) {
                                executorService.awaitTermination(1, TimeUnit.SECONDS);
                            }
                            Log.i(TaskExecutor.TAG, "ExecutorService time = " + (SystemClock.uptimeMillis() - startTime));
                        } catch (InterruptedException e) {
                            Log.e(TaskExecutor.TAG, "error", e);
                        }
                    }
                }.start();
            }
        });
    }

}
