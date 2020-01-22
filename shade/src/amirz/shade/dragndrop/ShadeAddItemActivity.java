package amirz.shade.dragndrop;

import android.os.Bundle;

import com.android.launcher3.dragndrop.AddItemActivity;

import amirz.shade.ShadeFont;
import amirz.shade.customization.ShadeStyle;

public class ShadeAddItemActivity extends AddItemActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ShadeFont.override(this);
        ShadeStyle.override(this);
        super.onCreate(savedInstanceState);
    }
}
