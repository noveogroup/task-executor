package com.noveogroup.android.task.example;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.webkit.WebView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class ExampleActivity extends Activity {

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
