package com.noveogroup.android.task.example;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.noveogroup.android.task.Task;
import com.noveogroup.android.task.TaskEnvironment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TaskPerformanceExampleActivity extends ExampleActivity {

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
                        executor.execute(new Task<Void, Void>() {
                            @Override
                            public Void run(Void value, TaskEnvironment<Void, Void> env) throws Throwable {
                                Log.i(TAG, "task #" + index);
                                Utils.calculate(100);
                                return null;
                            }
                        });
                    }
                }
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            executor.queue().join();
                            Log.i(TAG, "time = " + (SystemClock.uptimeMillis() - startTime));

                            final long startTime = SystemClock.uptimeMillis();
                            ExecutorService executorService = Executors.newFixedThreadPool(7);
                            for (int i = 0; i < 1000; i++) {
                                final int index = i;
                                executorService.submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, "task #" + index);
                                        Utils.calculate(100);
                                    }
                                });
                            }
                            executorService.shutdown();
                            while (!executorService.isTerminated()) {
                                executorService.awaitTermination(1, TimeUnit.SECONDS);
                            }
                            Log.i(TAG, "ExecutorService time = " + (SystemClock.uptimeMillis() - startTime));
                        } catch (InterruptedException e) {
                            Log.e(TAG, "error", e);
                        }
                    }
                }.start();
            }
        });
    }

}
