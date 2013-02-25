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

package com.noveogroup.android.task.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

// Что должна будет делать эта штуковина:
// - отслеживать исполнение задач:
//  - показать progress dialog пока задачи есть в очереди
//  - показать activity progress пока задачи есть в очереди
//  - покрутить анимацию или что-то такое пока задачи есть в очереди
//  - предоставить вызов для отмены задач
//  - показать error dialog
//  - показать error toast
//  - показать завершенность задачи в процентах, в полоске, в цифрах
// todo implement it
// todo make it customizable
public class ProgressManager {

    private final Context context;
    private final AdvancedHandler handler;
    private final Object lock = new Object();
    private boolean visible = false;
    private ProgressDialog progressDialog = null;
    private boolean destroyed = false;

    private final Runnable syncCallback = new Runnable() {
        @Override
        public void run() {
            synchronized (lock) {
                if (visible) {
                    if (progressDialog == null) {
                        progressDialog = ProgressDialog.show(context, "Please wait", "Downloading ...", true, true,
                                new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialogInterface) {
                                        ProgressManager.this.onCancel();
                                    }
                                });
                    }
                } else {
                    if (progressDialog != null) {
                        progressDialog.hide();
                        progressDialog = null;
                    }
                }
            }
        }
    };

    public ProgressManager(Context context) {
        this.context = context;
        this.handler = new AdvancedHandler(context);
    }

    public void show() {
        synchronized (lock) {
            if (destroyed) return;
            if (!visible) {
                visible = true;
                handler.postSingleton(syncCallback);
            }
        }
    }

    public void hide() {
        synchronized (lock) {
            if (destroyed) return;
            if (visible) {
                visible = false;
                handler.postSingleton(syncCallback);
            }
        }
    }

    public void destroy() {
        synchronized (lock) {
            hide();
            destroyed = true;
            handler.removeCallbacks();
        }
    }

    public void error(Throwable throwable) {
        synchronized (lock) {
            if (destroyed) return;
            final String message = throwable.getMessage();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    protected void onCancel() {
    }

}
