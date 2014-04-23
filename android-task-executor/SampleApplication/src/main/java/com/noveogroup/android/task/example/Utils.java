package com.noveogroup.android.task.example;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemClock;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public final class Utils {

    private Utils() {
        throw new UnsupportedOperationException();
    }

    public static void calculate(long time) {
        long startTime = SystemClock.uptimeMillis();
        while (startTime + time > SystemClock.uptimeMillis()) {
            long m = 1;
            for (long x = 0; x < 1000; x++) {
                m *= System.currentTimeMillis();
            }
            if (m * m < 0) break; // to prevent optimizations
        }
    }

    public static void download(long time) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        final int width = 160, height = 160; // = 4 Kb

        long startTime = SystemClock.uptimeMillis();
        while (startTime + time > SystemClock.uptimeMillis()) {
            Uri uri = Uri.parse("http://dummyimage.com/" + width + "x" + height + "/000/fff.png").buildUpon()
                    .appendQueryParameter("text", String.valueOf(System.currentTimeMillis())).build();

            HttpResponse response = httpClient.execute(new HttpGet(uri.toString()));
            byte[] bytes = EntityUtils.toByteArray(response.getEntity());
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length).recycle();
        }
    }

    public static void download(long time, double failureProbability) throws IOException {
        download(time);
        if (Math.random() < failureProbability) {
            throw new IOException("failure");
        }
    }

}
