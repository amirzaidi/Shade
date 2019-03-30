package amirz.shade.icons.clock;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.SystemClock;

import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.ItemInfoWithIcon;

import java.util.TimeZone;

class AutoUpdateClock extends FastBitmapDrawable implements Runnable {
    private ClockLayers mLayers;

    AutoUpdateClock(ItemInfoWithIcon info, ClockLayers layers) {
        super(info);
        mLayers = layers;
    }

    private void rescheduleUpdate() {
        long millisInSecond = 1000L;
        unscheduleSelf(this);
        long uptimeMillis = SystemClock.uptimeMillis();
        scheduleSelf(this, uptimeMillis - uptimeMillis % millisInSecond + millisInSecond);
    }

    // Used only by Google Clock
    void updateLayers(ClockLayers layers) {
        mLayers = layers;
        if (mLayers != null) {
            mLayers.mDrawable.setBounds(getBounds());
        }
        invalidateSelf();
    }

    void setTimeZone(TimeZone timeZone) {
        if (mLayers != null) {
            mLayers.setTimeZone(timeZone);
            invalidateSelf();
        }
    }

    @Override
    public void drawInternal(Canvas canvas, Rect rect) {
        if (mLayers == null) {
            super.drawInternal(canvas, rect);
            return;
        }
        mLayers.updateAngles();
        Rect bounds = getBounds();
        canvas.scale(mLayers.scale, mLayers.scale, bounds.exactCenterX(), bounds.exactCenterY());
        mLayers.mDrawable.draw(canvas);
        rescheduleUpdate();
    }

    @Override
    protected void onBoundsChange(final Rect bounds) {
        super.onBoundsChange(bounds);
        if (mLayers != null) {
            mLayers.mDrawable.setBounds(bounds);
        }
    }

    @Override
    public void run() {
        if (mLayers.updateAngles()) {
            invalidateSelf();
        } else {
            rescheduleUpdate();
        }
    }
}
