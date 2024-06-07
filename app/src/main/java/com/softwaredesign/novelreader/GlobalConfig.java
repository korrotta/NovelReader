package com.softwaredesign.novelreader;

import android.util.Log;

import com.softwaredesign.novelreader.Models.NovelSourceModel;

import java.lang.reflect.Field;
import java.util.List;

public class GlobalConfig {
    public static List<NovelSourceModel> g_SourceList;
    public static void logVariable(Object var){
        Class varClass = var.getClass();
        Field[] fields = varClass.getFields();
        for (Field field: fields){
            Log.d(field.getName() + " value check", var.toString());
        }


    }
}
