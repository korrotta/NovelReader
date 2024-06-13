package com.softwaredesign.novelreader.Fragments;

import static androidx.core.app.ActivityCompat.recreate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.softwaredesign.novelreader.Activities.ReadActivity;
import com.softwaredesign.novelreader.R;

public class SettingsDialogFragment extends DialogFragment {

    private SharedPreferences sharedPreferences;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        sharedPreferences = context.getSharedPreferences("ReadSetting", context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Remove the title bar
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        // Inflate the settings layout
        View view = inflater.inflate(R.layout.settings_read, container, false);

        RadioGroup modeRadioGroup = view.findViewById(R.id.modeRadioGroup);
        RadioButton lightTheme = view.findViewById(R.id.settingsLightTheme);
        RadioButton darkTheme = view.findViewById(R.id.settingsDarkTheme);

        RadioGroup fontRadioGroup = view.findViewById(R.id.fontRadioGroup);
        RadioButton fontPalatino = view.findViewById(R.id.fontPalatino);
        RadioButton fontTimes = view.findViewById(R.id.fontTimes);
        RadioButton fontArial = view.findViewById(R.id.fontArial);
        RadioButton fontGeorgia = view.findViewById(R.id.fontGeorgia);

        TextView textSizeTextView = view.findViewById(R.id.text_size);
        AppCompatButton buttonIncrease = view.findViewById(R.id.button_increase);
        AppCompatButton buttonDecrease = view.findViewById(R.id.button_decrease);

        String theme = sharedPreferences.getString("theme", "dark");
        if (theme.equals("light")) {
            lightTheme.setChecked(true);
        } else {
            darkTheme.setChecked(true);
        }


        modeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (checkedId == R.id.settingsLightTheme) {
                editor.putString("theme", "light").apply();
                //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                //restartActivity();

            } else if (checkedId == R.id.settingsDarkTheme) {
                editor.putString("theme", "dark").apply();
                //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                //restartActivity();
            }
            //applyThemeChange();
        });

        String font = sharedPreferences.getString("font", "Palatino");
        switch (font) {
            case "Palatino":
                fontPalatino.setChecked(true);
                break;
            case "Times":
                fontTimes.setChecked(true);
                break;
            case "Arial":
                fontArial.setChecked(true);
                break;
            case "Georgia":
                fontGeorgia.setChecked(true);
                break;
        }

        fontRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();


            if (checkedId == R.id.fontPalatino){
                editor.putString("font", "Palatino");
            } else if (checkedId == R.id.fontTimes) {
                editor.putString("font", "Times");
            } else if (checkedId == R.id.fontArial) {
                editor.putString("font", "Arial");
            } else if (checkedId == R.id.fontGeorgia) {
                editor.putString("font", "Georgia");
            }
            editor.apply();
            applyFontChange();
        });


        final int[] textSize = {sharedPreferences.getInt("textSize", 22)}; // Đặt giá trị mặc định là 22
        textSizeTextView.setText(String.valueOf(textSize[0]));
        applyTextSizeChange(textSize[0]);

        buttonIncrease.setOnClickListener(v -> {

            if (textSize[0] < 40) {
                textSize[0]++;
                textSizeTextView.setText(String.valueOf(textSize[0]));
                sharedPreferences.edit().putInt("textSize", textSize[0]).apply();
                applyTextSizeChange(textSize[0]);
            }
            buttonIncrease.setAlpha(textSize[0] >= 40 ? 0.2f : 1.0f);
            buttonDecrease.setAlpha(textSize[0] <= 16 ? 0.2f : 1.0f);
            buttonIncrease.setClickable(textSize[0] < 40);
            buttonDecrease.setClickable(textSize[0] > 16);
        });

        buttonDecrease.setOnClickListener(v -> {

            if (textSize[0] > 16) {
                textSize[0]--;
                textSizeTextView.setText(String.valueOf(textSize[0]));
                sharedPreferences.edit().putInt("textSize", textSize[0]).apply();
                applyTextSizeChange(textSize[0]);
            }
            buttonIncrease.setAlpha(textSize[0] >= 40 ? 0.2f : 1.0f);
            buttonDecrease.setAlpha(textSize[0] <= 16 ? 0.2f : 1.0f);
            buttonIncrease.setClickable(textSize[0] < 40);
            buttonDecrease.setClickable(textSize[0] > 16);
        });

        buttonIncrease.setAlpha(textSize[0] >= 40 ? 0.2f : 1.0f);
        buttonDecrease.setAlpha(textSize[0] <= 16 ? 0.2f : 1.0f);
        buttonIncrease.setClickable(textSize[0] < 40);
        buttonDecrease.setClickable(textSize[0] > 16);

        return view;
    }

    private void applyFontChange() {
        String font = sharedPreferences.getString("font", "Palatino");
        TextView chapterContent = getActivity().findViewById(R.id.chapterContentRead);
        switch (font) {
            case "Palatino":
                chapterContent.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.palatino));
                break;
            case "Times":
                chapterContent.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.times));
                break;
            case "Arial":
                chapterContent.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.arial));
                break;
            case "Georgia":
                chapterContent.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.georgia));
                break;
        }
    }

    private void applyTextSizeChange(int textSize) {
        TextView chapterContent = getActivity().findViewById(R.id.chapterContentRead);
        chapterContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    }

    private void restartActivity() {
        Intent intent = new Intent(getActivity(), ReadActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void applyThemeChange(){
        String theme = sharedPreferences.getString("theme", "dark");
        if (theme.equals("light")){
            getActivity().setTheme(R.style.LightTheme);
            Log.d("MyActivity", "Light");
        }
        else {
            getActivity().setTheme(R.style.DarkTheme);
            Log.d("MyActivity", "Dark");
        }

        /*getActivity().recreate();
        dismiss();*/

        TextView chapterNameTV = getActivity().findViewById(R.id.chapterNameRead);
        TextView chapterContentTV = getActivity().findViewById(R.id.chapterContentRead);
        ImageView prevChapterIV = getActivity().findViewById(R.id.previousChapterRead);
        ImageView nextChapterIV = getActivity().findViewById(R.id.nextChapterRead);
        ScrollView contentScrollView = getActivity().findViewById(R.id.contentScrollView);
        int backgroundColor = ((ColorDrawable) contentScrollView.getBackground()).getColor();

        if (theme.equals("light")) {
            chapterContentTV.setTextColor(getResources().getColor(R.color.black));
            chapterNameTV.setTextColor(getResources().getColor(R.color.black));
            prevChapterIV.setColorFilter(getResources().getColor(R.color.black));
            nextChapterIV.setColorFilter(getResources().getColor(R.color.black));
            contentScrollView.setBackgroundColor(getResources().getColor(R.color.white));
        } else {
            chapterContentTV.setTextColor(getResources().getColor(R.color.white));
            chapterNameTV.setTextColor(getResources().getColor(R.color.white));
            prevChapterIV.setColorFilter(getResources().getColor(R.color.white));
            nextChapterIV.setColorFilter(getResources().getColor(R.color.white));
            contentScrollView.setBackgroundColor(backgroundColor);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Set the dialog width and height
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
