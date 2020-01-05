package amirz.shade.dragndrop;

import android.os.Bundle;

import com.android.launcher3.dragndrop.AddItemActivity;

import amirz.shade.ShadeFont;

public class ShadeAddItemActivity extends AddItemActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ShadeFont.override(this);
        super.onCreate(savedInstanceState);
    }
}
