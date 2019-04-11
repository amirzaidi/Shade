package com.google.android.libraries.launcherclient;

import android.view.WindowManager.LayoutParams;
import com.google.android.libraries.launcherclient.ILauncherOverlayCallback;

interface ILauncherOverlay {
    oneway void startScroll();

    oneway void onScroll(in float progress);

    oneway void endScroll();

    oneway void windowAttached(in LayoutParams attrs, in ILauncherOverlayCallback callbacks, in int options);

    oneway void windowDetached(in boolean isChangingConfigurations);

    oneway void closeOverlay(in int options);

    oneway void onPause();

    oneway void onResume();

    oneway void openOverlay(in int options);

    oneway void requestVoiceDetection(in boolean start);

    String getVoiceSearchLanguage();

    boolean isVoiceDetectionRunning();

    boolean hasOverlayContent();

    oneway void windowAttached2(in Bundle params, in ILauncherOverlayCallback callbacks);

    oneway void unusedMethod();

    oneway void setActivityState(in int stateFlags);

    boolean startSearch(in byte[] config, in Bundle extras);
}
