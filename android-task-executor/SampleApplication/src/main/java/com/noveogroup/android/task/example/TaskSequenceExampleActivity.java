package com.noveogroup.android.task.example;

import android.os.Bundle;
import android.view.View;

import com.noveogroup.android.task.Task;
import com.noveogroup.android.task.TaskEnvironment;
import com.noveogroup.android.task.TaskHandler;
import com.noveogroup.android.task.TaskListener;

public class TaskSequenceExampleActivity extends ExampleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        loadWebViewExample();

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executor.execute(new Task<Void, Void>() {
                    @Override
                    public Void run(Void value, TaskEnvironment<Void, Void> env) throws Throwable {
                        Utils.download(1500);
                        executor.execute(new Task<Void, Void>() {
                            @Override
                            public Void run(Void value, TaskEnvironment<Void, Void> env) throws Throwable {
                                Utils.download(1500);
                                return null;
                            }
                        }, new TaskListener.Default<Void, Void>() {
                            @Override
                            public void onFinish(TaskHandler<Void, Void> handler) {
                                executor.execute(new Task<Void, Void>() {
                                    @Override
                                    public Void run(Void value, TaskEnvironment<Void, Void> env) throws Throwable {
                                        Utils.download(1500);
                                        return null;
                                    }
                                });
                            }
                        });
                        return null;
                    }
                });
            }
        });
    }

}
