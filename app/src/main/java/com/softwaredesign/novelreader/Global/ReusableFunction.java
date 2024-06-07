package com.softwaredesign.novelreader.Global;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Field;

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
}
