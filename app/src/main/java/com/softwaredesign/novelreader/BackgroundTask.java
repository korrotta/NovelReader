package com.softwaredesign.novelreader;

import android.app.Activity;

public abstract class BackgroundTask {
    private Activity activity;

    public BackgroundTask(Activity activity){
        this.activity = activity;
        onPreExecute();
    }

    private void startBackgroundTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doInBackground();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onPostExecute();
                    }
                });
            }
        }).start();
    }

    public void execute(){
        startBackgroundTask();
    }

    public abstract void onPreExecute();
    public abstract void doInBackground();
    public abstract void onPostExecute();
}
