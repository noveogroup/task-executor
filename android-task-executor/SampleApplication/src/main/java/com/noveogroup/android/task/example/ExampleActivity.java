package com.noveogroup.android.task.example;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.webkit.WebView;

import com.noveogroup.android.task.SimpleTaskExecutor;
import com.noveogroup.android.task.TaskExecutor;
import com.noveogroup.android.task.TaskHandler;
import com.noveogroup.android.task.TaskListener;
import com.noveogroup.android.task.ui.UITaskListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class ExampleActivity extends Activity {

    public static final String TAG = "AndroidTaskExecutor";

    public static class LogTaskListener implements TaskListener<Object, Object> {

        @Override
        public void onCreate(TaskHandler<Object, Object> handler) {
            Log.i(TAG, "TaskListener::onCreate      " + handler.hashCode());
        }

        @Override
        public void onQueueInsert(TaskHandler<Object, Object> handler) {
            Log.i(TAG, "TaskListener::onQueueInsert " + handler.hashCode());
        }

        @Override
        public void onStart(TaskHandler<Object, Object> handler) {
            Log.i(TAG, "TaskListener::onStart       " + handler.hashCode());
        }

        @Override
        public void onFinish(TaskHandler<Object, Object> handler) {
            Log.i(TAG, "TaskListener::onFinish      " + handler.hashCode());
        }

        @Override
        public void onQueueRemove(TaskHandler<Object, Object> handler) {
            Log.i(TAG, "TaskListener::onQueueRemove " + handler.hashCode());
        }

        @Override
        public void onDestroy(TaskHandler<Object, Object> handler) {
            Log.i(TAG, "TaskListener::onDestroy     " + handler.hashCode());
        }

        @Override
        public void onCanceled(TaskHandler<Object, Object> handler) {
            Log.i(TAG, "TaskListener::onCanceled    " + handler.hashCode());
        }

        @Override
        public void onFailed(TaskHandler<Object, Object> handler) {
            Log.i(TAG, "TaskListener::onFailed      " + handler.hashCode());
        }

        @Override
        public void onSucceed(TaskHandler<Object, Object> handler) {
            Log.i(TAG, "TaskListener::onSucceed     " + handler.hashCode());
        }

    }

    protected final TaskExecutor executor = new SimpleTaskExecutor();
    private final UITaskListener<Object, Object> taskListener = new UITaskListener<Object, Object>() {
        @Override
        @UI
        public void uiCreate(TaskHandler<Object, Object> handler) {
            if (!executor.queue().isEmpty()) {
                progressManager.show();
            }
        }

        @Override
        @UI
        public void uiDestroy(TaskHandler<Object, Object> handler) {
            if (executor.queue().isEmpty()) {
                progressManager.hide();
            }

            if (handler.getState() == TaskHandler.State.FAILED) {
                progressManager.error(handler.getThrowable());
            }
        }
    };
    private ProgressManager progressManager = new ProgressManager(this) {
        @Override
        protected void onCancel() {
            executor.queue().interrupt();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        progressManager.onResume();
        executor.addTaskListener(taskListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressManager.onPause();
        executor.queue().interrupt();
        executor.removeTaskListener(taskListener);
    }

    private static String loadHtmlFor(Context context, Class<? extends ExampleActivity> activityClass) {
        Resources resources = context.getResources();
        String resourceName = activityClass.getSimpleName().replaceAll("(.)([A-Z]+)", "$1_$2").toLowerCase();
        int resourceId = resources.getIdentifier(resourceName, "raw", context.getPackageName());
        if (resourceId == 0) {
            return "cannot find resource " + resourceName;
        }
        InputStream inputStream = resources.openRawResource(resourceId);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        try {
            for (int length = 0; length != -1; length = inputStream.read(buffer)) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            return new String(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            return Log.getStackTraceString(e);
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    protected void loadWebViewExample() {
        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.loadData(loadHtmlFor(this, this.getClass()), "text/html", "UTF-8");
    }

}
