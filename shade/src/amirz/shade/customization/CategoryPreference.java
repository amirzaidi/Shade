package amirz.shade.customization;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class CategoryPreference extends ListPreference {
    public CategoryPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CategoryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CategoryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CategoryPreference(Context context) {
        super(context);
    }
}
