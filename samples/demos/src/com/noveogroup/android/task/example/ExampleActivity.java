package com.noveogroup.android.task.example;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.webkit.WebView;
import com.noveogroup.android.task.TaskExecutor;
import com.noveogroup.android.task.TaskHandler;
import com.noveogroup.android.task.TaskListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class ExampleActivity extends Activity {

    public static class LogTaskListener implements TaskListener {

        @Override
        public void onCreate(TaskHandler<?, ?> handler) {
            Log.i(TaskExecutor.TAG, "TaskListener::onCreate      " + handler.hashCode());
        }

        @Override
        public void onQueueInsert(TaskHandler<?, ?> handler) {
            Log.i(TaskExecutor.TAG, "TaskListener::onQueueInsert " + handler.hashCode());
        }

        @Override
        public void onStart(TaskHandler<?, ?> handler) {
            Log.i(TaskExecutor.TAG, "TaskListener::onStart       " + handler.hashCode());
        }

        @Override
        public void onFinish(TaskHandler<?, ?> handler) {
            Log.i(TaskExecutor.TAG, "TaskListener::onFinish      " + handler.hashCode());
        }

        @Override
        public void onQueueRemove(TaskHandler<?, ?> handler) {
            Log.i(TaskExecutor.TAG, "TaskListener::onQueueRemove " + handler.hashCode());
        }

        @Override
        public void onDestroy(TaskHandler<?, ?> handler) {
            Log.i(TaskExecutor.TAG, "TaskListener::onDestroy     " + handler.hashCode());
        }

        @Override
        public void onCanceled(TaskHandler<?, ?> handler) {
            Log.i(TaskExecutor.TAG, "TaskListener::onCanceled    " + handler.hashCode());
        }

        @Override
        public void onFailed(TaskHandler<?, ?> handler) {
            Log.i(TaskExecutor.TAG, "TaskListener::onFailed      " + handler.hashCode());
        }

        @Override
        public void onSucceed(TaskHandler<?, ?> handler) {
            Log.i(TaskExecutor.TAG, "TaskListener::onSucceed     " + handler.hashCode());
        }

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
