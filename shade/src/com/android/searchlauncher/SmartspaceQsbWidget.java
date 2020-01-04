package com.android.searchlauncher;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.launcher3.R;
import com.android.launcher3.qsb.QsbContainerView;

public class SmartspaceQsbWidget extends QsbContainerView {
    private static final String WIDGET_CLASS_NAME = "com.google.android.apps.gsa.staticplugins.smartspace.widget.SmartspaceWidgetProvider";
    public static final String WIDGET_PACKAGE_NAME = "com.google.android.googlequicksearchbox";

    public SmartspaceQsbWidget(Context context) {
        this(context,  null);
    }

    public SmartspaceQsbWidget(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SmartspaceQsbWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public static class SmartSpaceFragment extends QsbContainerView.QsbFragment {
        private static final int SMART_SPACE_WIDGET_HOST_ID = 1027;

        public SmartSpaceFragment() {
            this.mKeyWidgetId = "smart_space_widget_id";
        }

        @Override
        protected QsbContainerView.QsbWidgetHost createHost() {
            return new QsbContainerView.QsbWidgetHost(getContext(), SMART_SPACE_WIDGET_HOST_ID, SmartSpaceHostView::new);
        }

        @Override
        protected AppWidgetProviderInfo getSearchWidgetProvider() {
            for (AppWidgetProviderInfo next : AppWidgetManager.getInstance(getContext()).getInstalledProviders()) {
                // .getInstalledProvidersForPackage(SmartspaceQsbWidget.WIDGET_PACKAGE_NAME, Process.myUserHandle())
                if (next.getProfile().equals(Process.myUserHandle())
                        && SmartspaceQsbWidget.WIDGET_PACKAGE_NAME.equals(next.provider.getPackageName())
                        && SmartspaceQsbWidget.WIDGET_CLASS_NAME.equals(next.provider.getClassName())) {
                    return next;
                }
            }
            return null;
        }

        @Override
        protected View getDefaultView(ViewGroup container, boolean showSetupIcon) {
            return SmartspaceQsbWidget.getDateView(container);
        }

        @Override
        protected Bundle createBindOptions() {
            Bundle createBindOptions = super.createBindOptions();
            createBindOptions.putString("attached-launcher-identifier", getContext().getPackageName());
            createBindOptions.putBoolean("com.google.android.apps.gsa.widget.PREINSTALLED", true);
            return createBindOptions;
        }
    }

    public static View getDateView(ViewGroup root) {
        return LayoutInflater.from(root.getContext())
                .inflate(R.layout.smart_space_date_view, root, false);
    }
}