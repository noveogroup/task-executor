package com.noveogroup.android.task.example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.noveogroup.android.task.Task;
import com.noveogroup.android.task.TaskEnvironment;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Query;

public class RetrofitActivity extends ExampleActivity {

    public interface NewsServerService {

        @GET("/news/getAll")
        public JsonArray getAllNews();

        @GET("/news/getById")
        public JsonObject getNewsById(@Query("id") String id);

    }

    public interface NewsServerTasks {

        public Task<Void, JsonArray> getAllNews();

        public Task<Void, JsonObject> getNewsById(String id);

    }

    @SuppressWarnings("unchecked")
    public static <T> T buildTasks(RestAdapter restAdapter, final Class<?> serviceClass, Class<T> tasksClass) {
        final Object service = restAdapter.create(serviceClass);
        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(service);
        return (T) Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class<?>[]{tasksClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
                        return new Task() {
                            @Override
                            public Object run(Object value, TaskEnvironment env) throws Throwable {
                                Method serviceMethod = serviceClass.getMethod(method.getName(), method.getParameterTypes());
                                return invocationHandler.invoke(service, serviceMethod, args);
                            }
                        };
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        RestAdapter restAdapter = new RestAdapter.Builder()
                                .setEndpoint("http://androidtraining.noveogroup.com")
                                .setConverter(new GsonConverter(new Gson()))
                                .build();

                        NewsServerService service = restAdapter.create(NewsServerService.class);
                        Log.i("XXX", "RESPONSE: " + service.getNewsById("1157bebe-1c18-11e4-9e46-0050568859de"));

                        NewsServerTasks tasks = buildTasks(restAdapter, NewsServerService.class, NewsServerTasks.class);
                        try {
                            Log.i("XXX", "TASK RESPONSE: " + executor.execute(tasks.getAllNews()).get());
                        } catch (Throwable throwable) {
                            Log.e("XXX", "error", throwable);
                        }
                    }
                }.start();
            }
        });
    }

}
