package com.noveogroup.android.task.example;

import android.os.Bundle;
import android.view.View;

import com.noveogroup.android.task.Task;
import com.noveogroup.android.task.TaskEnvironment;

public class SimpleTaskExampleActivity extends ExampleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        loadWebViewExample();

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executor.execute(new Task<Object, Object>() {
                    @Override
                    public Object run(Object value, TaskEnvironment<Object, Object> env) throws Throwable {
                        Utils.download(3000);
                        return null;
                    }
                });
            }
        });
    }

}
