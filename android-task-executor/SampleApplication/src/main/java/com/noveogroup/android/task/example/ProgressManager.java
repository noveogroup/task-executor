package com.noveogroup.android.task.example;/*
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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class ProgressManager {

    private final Context context;
    private boolean destroyed = false;
    private ProgressDialog progressDialog = null;

    public ProgressManager(Context context) {
        this.context = context;
    }

    public void show() {
        if (destroyed) return;

        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                return;
            } else {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }

        DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                ProgressManager.this.progressDialog.dismiss();
                ProgressManager.this.progressDialog = null;
                ProgressManager.this.onCancel();
            }
        };
        progressDialog = ProgressDialog.show(context, "Please wait", "Downloading ...", true, true, cancelListener);
    }

    public void hide() {
        if (progressDialog != null) {
            progressDialog.hide();
            progressDialog = null;
        }
    }

    public void onResume() {
        destroyed = false;
    }

    public void onPause() {
        hide();
        destroyed = true;
    }

    public void error(Throwable throwable) {
        if (destroyed) return;
        Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
    }

    protected void onCancel() {
    }

}
