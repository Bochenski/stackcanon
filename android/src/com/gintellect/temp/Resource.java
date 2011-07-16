package com.gintellect.temp;

import android.app.Activity;
import android.os.Bundle;


import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import android.os.Handler;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import android.util.Log;


public class Resource extends Activity  implements OnClickListener
{
    private Handler guiThread;
    private ExecutorService restThread;
    private Runnable updateTask;
    private Runnable postTask;
    private Future callPending;
    private View getButton;
    private View postButton;
    private TextView getLabel;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //set up click listener
        getButton = findViewById(R.id.get_button);
        getButton.setOnClickListener(this);
        postButton = findViewById(R.id.post_button);
        postButton.setOnClickListener(this);

        //init text view
        getLabel = (TextView)findViewById(R.id.get_label);

        initThreading();
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.get_button:
                //call our webservice async function
                guiThread.removeCallbacks(updateTask);
                guiThread.post(updateTask);
                break;
            case R.id.post_button:
                //call our webservice async function
                guiThread.removeCallbacks(postTask);
                guiThread.post(postTask);
                break;
        }
    }

    private void initThreading() {
        guiThread = new Handler();
        restThread = Executors.newSingleThreadExecutor();

        updateTask = new Runnable() {
            public void run() {

                //Cancel previous call if there was one
                if (callPending != null)
                    callPending.cancel(true);
                try
                {
                   ResourceTask resourceTask = new ResourceTask(Resource.this, ResourceTask.TaskType.index);
                   callPending = restThread.submit(resourceTask);
                }
                catch (RejectedExecutionException e) {
                   setLabel("Exception calling service");
                }
            }

        };
        postTask = new Runnable() {
            public void run() {

                //Cancel previous call if there was one
                if (callPending != null)
                    callPending.cancel(true);
                try
                {
                   ResourceTask resourceTask = new ResourceTask(Resource.this, ResourceTask.TaskType.create);
                   callPending = restThread.submit(resourceTask);
                }
                catch (RejectedExecutionException e) {
                   setLabel("Exception calling service");
                }
            }

        };
    }

    public void setLabel(String text) {
        guiSetText(getLabel, text);
    }

    private void guiSetText(final TextView view, final String text) {
        guiThread.post(new Runnable() {
            public void run() {
                view.setText(text);
            }
        });
    }
}
