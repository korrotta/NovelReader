package com.softwaredesign.novelreader.Global;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.softwaredesign.novelreader.Activities.SearchActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class ReusableFunction {
    public static void LogVariable(Object var){
        Class varClass = var.getClass();
        Field[] fields = varClass.getFields();
        for (Field field: fields) {
            Log.d(field.getName(), var.toString());
        }
    }
    public static void ChangeActivityWithString(Context context, Class newActivity, String packageName, String data){
        Intent intent = new Intent(context, newActivity);
        intent.putExtra(packageName, data);
        context.startActivity(intent);
    }

    public static void ReplaceList(List destinationList, List dataList){
        if (destinationList == null){
            destinationList = new ArrayList<>();
        }
        destinationList.clear();
        destinationList.addAll(dataList);
    }

}
