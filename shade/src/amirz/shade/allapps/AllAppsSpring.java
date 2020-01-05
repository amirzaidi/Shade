package amirz.shade.allapps;

import android.graphics.Canvas;
import android.widget.EdgeEffect;

import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import static androidx.dynamicanimation.animation.SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY;
import static androidx.dynamicanimation.animation.SpringForce.STIFFNESS_LOW;
import static androidx.dynamicanimation.animation.SpringForce.STIFFNESS_MEDIUM;

class AllAppsSpring {
    private static final float STIFFNESS = (STIFFNESS_MEDIUM + STIFFNESS_LOW) / 2;
    private static final float DAMPING_RATIO = DAMPING_RATIO_MEDIUM_BOUNCY;

    private static final float ABSORB_SCALE = 0.4f;
    private static final float DRAG_SCALE = 0.12f;

    private static final FloatPropertyCompat<AllAppsSpring> DAMPED_SCROLL =
            new FloatPropertyCompat<AllAppsSpring>("value") {

                @Override
                public float getValue(AllAppsSpring object) {
                    return object.mDisplacement;
                }

                @Override
                public void setValue(AllAppsSpring object, float value) {
                    object.mDisplacement = value;
                    object.updateView();
                }
            };

    private final AllAppsView mView;
    private final SpringAnimation mSpring;
    private float mDisplacement;

    AllAppsSpring(AllAppsView view) {
        mView = view;

        mSpring = new SpringAnimation(this, DAMPED_SCROLL, 0);
        mSpring.setSpring(new SpringForce(0)
                .setStiffness(STIFFNESS)
                .setDampingRatio(DAMPING_RATIO));
    }

    private void updateView() {
        mView.setDampedScrollShift(mDisplacement);
    }

    EdgeEffect createSide(float multiplier) {
        return new EdgeEffect(mView.getContext()) {
            private boolean mFinished;

            @Override
            public boolean isFinished() {
                return mFinished;
            }

            @Override
            public boolean draw(Canvas canvas) {
                return false;
            }

            @Override
            public void onPull(float deltaDistance) {
                onPull(deltaDistance, 0.5f);
            }

            @Override
            public void onPull(float deltaDistance, float displacement) {
                mDisplacement += multiplier * deltaDistance * mView.getHeight() * DRAG_SCALE;
                updateView();
                mFinished = false;
            }

            @Override
            public void onAbsorb(int velocity) {
                mSpring.setStartVelocity(multiplier * velocity * ABSORB_SCALE);
                mSpring.start();
                mFinished = false;
            }

            @Override
            public void onRelease() {
                mSpring.start();
                mFinished = true;
            }
        };
    }
}
