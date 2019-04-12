package amirz.shade.dragndrop;

import android.content.res.Resources;
import android.os.Bundle;

import com.android.launcher3.R;
import com.android.launcher3.dragndrop.AddItemActivity;

import static amirz.shade.ShadeSettings.getThemeRes;

public class ShadeAddItemActivity extends AddItemActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load the Shade theme attributes
        Resources.Theme theme = getTheme();
        theme.applyStyle(getThemeRes(this, R.style.ShadeSettings_Default), false);
        theme.applyStyle(R.style.ShadeDialog_Override, true);

        super.onCreate(savedInstanceState);
    }
}
