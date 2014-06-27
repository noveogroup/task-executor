package com.noveogroup.android.task.example;

import android.os.Bundle;
import android.view.View;

import com.noveogroup.android.task.Task;
import com.noveogroup.android.task.TaskEnvironment;

import java.io.IOException;

public class DownloadExampleActivity extends ExampleActivity {

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
                        try {
                            Utils.download(3000, 0.5);
                        } catch (IOException e) {
                            synchronized (env.lock()) {
                                int tryNumber = env.vars().get("tryNumber", 0);
                                if (tryNumber < 3) {
                                    env.vars().put("tryNumber", tryNumber + 1);
                                    env.owner().execute(this);
                                } else {
                                    throw e;
                                }
                            }
                        }
                        return null;
                    }
                });
            }
        });
    }

}
