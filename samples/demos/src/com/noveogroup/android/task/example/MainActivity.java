package com.noveogroup.android.task.example;

import android.R;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends ListActivity {

    @SuppressWarnings("unchecked")
    private static final List<Class<? extends ExampleActivity>> activities = Collections.unmodifiableList(Arrays.asList(
            SimpleTaskExampleActivity.class,
            TaskSequenceExampleActivity.class,
            TaskFailureExampleActivity.class,
            TaskPerformanceExampleActivity.class
    ));

    private static String getActivityName(Context context, Class<? extends Activity> activityClass) {
        ComponentName name = new ComponentName(context, activityClass);
        try {
            ActivityInfo activityInfo = context.getPackageManager().getActivityInfo(name, 0);
            return context.getString(activityInfo.labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            return name.flattenToShortString();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return activities.size();
            }

            @Override
            public Class<? extends ExampleActivity> getItem(int position) {
                return activities.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.simple_list_item_1, parent, false);
                }

                Class<? extends Activity> activityClass = getItem(position);
                String activityName = getActivityName(MainActivity.this, activityClass);

                TextView textView = (TextView) convertView.findViewById(R.id.text1);
                textView.setText(activityName);

                return convertView;
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Class<? extends Activity> activityClass = activities.get(position);
        startActivity(new Intent(this, activityClass));
    }

}
