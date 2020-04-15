/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package amirz.shade.hidden;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.android.launcher3.AppInfo;
import com.android.launcher3.ButtonDropTarget;
import com.android.launcher3.DropTarget;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.R;
import com.android.launcher3.dragndrop.DragOptions;
import com.android.launcher3.logging.LoggerUtils;
import com.android.launcher3.userevent.nano.LauncherLogProto.ControlType;
import com.android.launcher3.userevent.nano.LauncherLogProto.Target;
import com.android.launcher3.views.Snackbar;

import amirz.shade.util.AppReloader;
import amirz.unread.UnreadSession;

public class HideDropTarget extends ButtonDropTarget {

    public HideDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HideDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // Get the hover color
        mHoverColor = getResources().getColor(R.color.delete_target_hover_tint);

        setDrawable(R.drawable.ic_eye_hide_shadow);
    }

    @Override
    public void onDragStart(DropTarget.DragObject dragObject, DragOptions options) {
        super.onDragStart(dragObject, options);
        setTextBasedOnDragSource(dragObject.dragInfo);
    }

    @Override
    public boolean supportsAccessibilityDrop(ItemInfo info, View view) {
        return false;
    }

    @Override
    public int getAccessibilityAction() {
        return 0;
    }

    @Override
    protected boolean supportsDrop(ItemInfo info) {
        return canHide(info);
    }

    /**
     * Set the drop target's text to either "Hide" or "Show" depending on the drag item.
     */
    private void setTextBasedOnDragSource(ItemInfo item) {
        if (!TextUtils.isEmpty(mText) && canHide(item)) {
            boolean isHidden = HiddenAppsDatabase.isHidden(mLauncher, item);

            setDrawable(isHidden
                    ? R.drawable.ic_eye_unhide_shadow
                    : R.drawable.ic_eye_hide_shadow);

            mText = getResources().getString(isHidden
                    ? R.string.show_drop_target_label
                    : R.string.hide_drop_target_label);

            setContentDescription(mText);
            requestLayout();
        }
    }

    private boolean canHide(ItemInfo item) {
        return item instanceof AppInfo && item.id == ItemInfo.NO_ID;
    }

    @Override
    public void completeDrop(DragObject d) {
        ItemInfo item = d.dragInfo;
        if (canHide(item)) {
            boolean isHidden = HiddenAppsDatabase.isHidden(mLauncher, item);
            HiddenAppsDatabase.setHidden(mLauncher, item, !isHidden);
            UnreadSession.getInstance(mLauncher).forceUpdate(); // Show or hide notifications.
            AppReloader.get(mLauncher).reload(item);

            if (!isHidden) {
                Runnable onUndoClicked = () -> {
                    HiddenAppsDatabase.setHidden(mLauncher, item, false);
                    UnreadSession.getInstance(mLauncher).forceUpdate();
                    AppReloader.get(mLauncher).reload(item);
                };
                Snackbar.show(mLauncher, R.string.item_hidden, R.string.undo, null, onUndoClicked);
            }
        }
    }

    @Override
    public void onAccessibilityDrop(View view, ItemInfo item) {
    }

    @Override
    public Target getDropTargetForLogging() {
        Target t = LoggerUtils.newTarget(Target.Type.CONTROL);
        t.controlType = ControlType.DEFAULT_CONTROLTYPE;
        return t;
    }
}
