package com.noveogroup.android.task.ui;

import com.noveogroup.android.task.TaskHandler;
import com.noveogroup.android.task.TaskListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

public class UITaskListener<Input, Output> implements TaskListener<Input, Output> {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface UI {
    }

    private final UIHandler uiHandler;

    public UITaskListener() {
        this(new UIHandler());
    }

    public UITaskListener(UIHandler uiHandler) {
        this.uiHandler = uiHandler;
    }

    private void call(String methodName, Runnable uiCallback) {
        try {
            Method method = ((Object) this).getClass().getDeclaredMethod(methodName, TaskHandler.class);
            UI annotation = method.getAnnotation(UI.class);
            if (annotation != null) {
                try {
                    uiHandler.sync(uiCallback);
                } catch (InterruptedException ignored) {
                }
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreate(final TaskHandler<Input, Output> handler) {
        call("uiCreate", new Runnable() {
            @Override
            public void run() {
                uiCreate(handler);
            }
        });
    }

    @Override
    public void onQueueInsert(final TaskHandler<Input, Output> handler) {
        call("uiQueueInsert", new Runnable() {
            @Override
            public void run() {
                uiQueueInsert(handler);
            }
        });
    }

    @Override
    public void onStart(final TaskHandler<Input, Output> handler) {
        call("uiStart", new Runnable() {
            @Override
            public void run() {
                uiStart(handler);
            }
        });
    }

    @Override
    public void onFinish(final TaskHandler<Input, Output> handler) {
        call("uiFinish", new Runnable() {
            @Override
            public void run() {
                uiFinish(handler);
            }
        });
    }

    @Override
    public void onQueueRemove(final TaskHandler<Input, Output> handler) {
        call("uiQueueRemove", new Runnable() {
            @Override
            public void run() {
                uiQueueRemove(handler);
            }
        });
    }

    @Override
    public void onDestroy(final TaskHandler<Input, Output> handler) {
        call("uiDestroy", new Runnable() {
            @Override
            public void run() {
                uiDestroy(handler);
            }
        });
    }

    @Override
    public void onCanceled(final TaskHandler<Input, Output> handler) {
        call("uiCanceled", new Runnable() {
            @Override
            public void run() {
                uiCanceled(handler);
            }
        });
    }

    @Override
    public void onFailed(final TaskHandler<Input, Output> handler) {
        call("uiFailed", new Runnable() {
            @Override
            public void run() {
                uiFailed(handler);
            }
        });
    }

    @Override
    public void onSucceed(final TaskHandler<Input, Output> handler) {
        call("uiSucceed", new Runnable() {
            @Override
            public void run() {
                uiSucceed(handler);
            }
        });
    }

    public void uiCreate(TaskHandler<Input, Output> handler) {
    }

    public void uiQueueInsert(TaskHandler<Input, Output> handler) {
        // do nothing
    }

    public void uiStart(TaskHandler<Input, Output> handler) {
        // do nothing
    }

    public void uiFinish(TaskHandler<Input, Output> handler) {
        // do nothing
    }

    public void uiQueueRemove(TaskHandler<Input, Output> handler) {
        // do nothing
    }

    public void uiDestroy(TaskHandler<Input, Output> handler) {
        // do nothing
    }

    public void uiCanceled(TaskHandler<Input, Output> handler) {
        // do nothing
    }

    public void uiFailed(TaskHandler<Input, Output> handler) {
        // do nothing
    }

    public void uiSucceed(TaskHandler<Input, Output> handler) {
        // do nothing
    }

}
