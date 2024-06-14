package com.softwaredesign.novelreader.Global;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.softwaredesign.novelreader.Activities.SearchActivity;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class ReusableFunction {
    public static void LogVariable(Object var){
        // Get the class of the object
        Class varClass = var.getClass();
        // Get all public fields of the class
        Field[] fields = varClass.getFields();
        for (Field field: fields) {
            // Log the name of the field and its value converted to string
            Log.d(field.getName(), var.toString());
        }
    }
    public static void ChangeActivityWithString(Context context, Class newActivity, String packageName, String data){
        Intent intent = new Intent(context, newActivity);
        intent.putExtra(packageName, data);
        context.startActivity(intent);
    }

    public static void ReplaceList(List destinationList, List dataList){
        // If destinationList is null, initialize it as an empty ArrayList
        if (destinationList == null){
            destinationList = new ArrayList<>();
        }
        destinationList.clear();
        destinationList.addAll(dataList);
    }

    public static File MakeDirectory(String parentPath, String dirName){
        File file = new File(parentPath);
        File directory = new File(parentPath, dirName);
        if (!directory.exists()) {
            if (directory.mkdir()) {
                // Directory was created
                return directory;
            } else {
                // Directory creation failed
                return null;
            }
        } else {
            // Directory already exists
            return directory;
        }
    }

}
