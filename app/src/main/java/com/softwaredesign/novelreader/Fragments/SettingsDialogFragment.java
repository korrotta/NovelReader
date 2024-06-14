package com.softwaredesign.novelreader.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.softwaredesign.novelreader.R;

public class SettingsDialogFragment extends DialogFragment {

    private static final float MIN_LINE_SPACING = 1.0f;
    private static final float MAX_LINE_SPACING = 3.0f;
    private static final float INCREMENT = 0.5f;
    private SharedPreferences sharedPreferences;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        sharedPreferences = context.getSharedPreferences("ReadSetting", context.MODE_PRIVATE);
    }

    @SuppressLint("DefaultLocale")
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

        TextView lineSpacingTextView = view.findViewById(R.id.lineSpacingValue);
        SeekBar lineSpacingSeekBar = view.findViewById(R.id.lineSpacingSeekbar);

        // Theme
        String theme = sharedPreferences.getString("theme", "dark");
        if (theme.equals("light")) {
            lightTheme.setChecked(true);
        } else {
            darkTheme.setChecked(true);
        }

        modeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Get the SharedPreferences editor to modify preferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            // Check which radio button is checked
            if (checkedId == R.id.settingsLightTheme) {
                // Store "light" theme in SharedPreferences
                editor.putString("theme", "light").apply();
                // Recreate the activity to apply the theme change
                getActivity().recreate();
                // Dismiss the dialog after theme change
                dismiss();
            } else if (checkedId == R.id.settingsDarkTheme) {
                // Store "dark" theme in SharedPreferences
                editor.putString("theme", "dark").apply();
                // Recreate the activity to apply the theme change
                getActivity().recreate();
                // Dismiss the dialog after theme change
                dismiss();
            }
        });

        // Font
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


        // Text Size
        final int[] textSize = {sharedPreferences.getInt("textSize", 22)}; // Đặt giá trị mặc định là 22
        textSizeTextView.setText(String.valueOf(textSize[0]));
        applyTextSizeChange(textSize[0]);

        buttonIncrease.setOnClickListener(v -> {

            // Check if current text size is less than 40
            if (textSize[0] < 40) {
                // Increment text size by 1
                textSize[0]++;
                // Update TextView to display the new text size
                textSizeTextView.setText(String.valueOf(textSize[0]));
                // Save the updated text size to SharedPreferences
                sharedPreferences.edit().putInt("textSize", textSize[0]).apply();
                // Apply the new text size
                applyTextSizeChange(textSize[0]);
            }
            // Adjust alpha (opacity) of buttons based on text size limits
            buttonIncrease.setAlpha(textSize[0] >= 40 ? 0.2f : 1.0f);
            buttonDecrease.setAlpha(textSize[0] <= 16 ? 0.2f : 1.0f);
            // Adjust clickable state of buttons based on text size limits
            buttonIncrease.setClickable(textSize[0] < 40);
            buttonDecrease.setClickable(textSize[0] > 16);
        });

        buttonDecrease.setOnClickListener(v -> {

            // Check if current text size is greater than 16
            if (textSize[0] > 16) {
                // Decrease text size by 1
                textSize[0]--;
                // Update TextView to display the new text size
                textSizeTextView.setText(String.valueOf(textSize[0]));
                // Save the updated text size to SharedPreferences
                sharedPreferences.edit().putInt("textSize", textSize[0]).apply();
                // Apply the new text size
                applyTextSizeChange(textSize[0]);
            }
            // Adjust alpha (opacity) of buttons based on text size limits
            buttonIncrease.setAlpha(textSize[0] >= 40 ? 0.2f : 1.0f);
            buttonDecrease.setAlpha(textSize[0] <= 16 ? 0.2f : 1.0f);
            // Adjust clickable state of buttons based on text size limits
            buttonIncrease.setClickable(textSize[0] < 40);
            buttonDecrease.setClickable(textSize[0] > 16);
        });

        buttonIncrease.setAlpha(textSize[0] >= 40 ? 0.2f : 1.0f);
        buttonDecrease.setAlpha(textSize[0] <= 16 ? 0.2f : 1.0f);
        buttonIncrease.setClickable(textSize[0] < 40);
        buttonDecrease.setClickable(textSize[0] > 16);

        // Line Spacing
        float lineSpacing = sharedPreferences.getFloat("lineSpacing", 1.0f);
        lineSpacingTextView.setText(String.format("%.2f", lineSpacing));
        applyLineSpacingChange(lineSpacing);

        int maxProgress = (int) ((MAX_LINE_SPACING - MIN_LINE_SPACING) / INCREMENT); // Calculate max progress
        lineSpacingSeekBar.setMax(maxProgress);
        lineSpacingSeekBar.setProgress((int) ((lineSpacing - MIN_LINE_SPACING) / INCREMENT)); // Set progress based on saved line spacing value

        lineSpacingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float lineSpacing = MIN_LINE_SPACING + (progress * INCREMENT);
                lineSpacingTextView.setText(String.format("%.2f", lineSpacing));
                applyLineSpacingChange(lineSpacing);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do something when tracking starts, if needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Save the line spacing value when the user stops interacting with the SeekBar
                float lineSpacing = MIN_LINE_SPACING + (seekBar.getProgress() * INCREMENT);
                saveLineSpacing(lineSpacing);
            }
        });

        return view;
    }

    private void applyLineSpacingChange(float lineSpacing) {
        TextView chapterContentTV = getActivity().findViewById(R.id.chapterContentRead);
        chapterContentTV.setLineSpacing(1.0f, lineSpacing);
    }

    private void saveLineSpacing(float lineSpacing) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("lineSpacing", lineSpacing);
        editor.apply();
    }

    private void applyFontChange() {
        String font = sharedPreferences.getString("font", "Palatino");
        TextView chapterContentTV  = getActivity().findViewById(R.id.chapterContentRead);
        Typeface typeface = null;
        switch (font) {
            case "Palatino":
                typeface = ResourcesCompat.getFont(getActivity(), R.font.palatino);
                break;
            case "Times":
                typeface = ResourcesCompat.getFont(getActivity(), R.font.times);
                break;
            case "Arial":
                typeface = ResourcesCompat.getFont(getActivity(), R.font.arial);
                break;
            case "Georgia":
                typeface = ResourcesCompat.getFont(getActivity(), R.font.georgia);
                break;
        }
        if (typeface != null) {
            chapterContentTV.setTypeface(typeface);
        }
    }


    private void applyTextSizeChange(int textSize) {
        TextView chapterContent = getActivity().findViewById(R.id.chapterContentRead);
        chapterContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
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
