package amirz.shade.customization;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;

import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.widget.WidgetsBottomSheet;

public class InfoBottomSheet extends WidgetsBottomSheet {
    private final FragmentManager mFragmentManager;
    private View.OnClickListener mOnAppInfoClick;

    public InfoBottomSheet(Context context) {
        this(context, null);
    }

    public InfoBottomSheet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InfoBottomSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mFragmentManager = Launcher.getLauncher(context).getFragmentManager();
    }

    public void setOnAppInfoClick(View.OnClickListener onclick) {
        mOnAppInfoClick = onclick;
    }

    @Override
    public void populateAndShow(ItemInfo itemInfo) {
        super.populateAndShow(itemInfo);
        TextView title = findViewById(R.id.title);
        title.setText(itemInfo.title);

        View.OnClickListener l = v -> mOnAppInfoClick.onClick(v);
        title.setOnClickListener(l);
        findViewById(R.id.subtitle).setOnClickListener(l);

        PrefsFragment fragment =
                (PrefsFragment) mFragmentManager.findFragmentById(R.id.sheet_prefs);
        fragment.loadForApp(itemInfo, v -> handleClose(true));
    }

    @Override
    public void onDetachedFromWindow() {
        Fragment pf = mFragmentManager.findFragmentById(R.id.sheet_prefs);
        if (pf != null) {
            mFragmentManager.beginTransaction()
                    .remove(pf)
                    .commitAllowingStateLoss();
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void onWidgetsBound() {
    }

    public static class PrefsFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {
        private Context mContext;

        private ComponentName mComponent;
        private ComponentKey mKey;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mContext = getActivity();
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.app_info_preferences);
        }

        public void loadForApp(ItemInfo itemInfo, final View.OnClickListener onResetClick) {
            mComponent = itemInfo.getTargetComponent();
            mKey = new ComponentKey(mComponent, itemInfo.user);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
            }
            return false;
        }
    }
}
