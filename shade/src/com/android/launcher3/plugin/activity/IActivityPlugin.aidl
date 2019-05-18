package com.android.launcher3.plugin.activity;

interface IActivityPlugin {
    oneway void clearState();

    oneway void setState(in int state);
}
