package amirz.shade.search;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AllAppsSearchBackground extends FrameLayout {
    public AllAppsSearchBackground(@NonNull Context context) {
        this(context, null);
    }

    public AllAppsSearchBackground(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsSearchBackground(@NonNull Context context, @Nullable AttributeSet attrs,
                                   int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
