package amirz.shade;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.os.SystemClock;

class ShadeRestarter {
    private static final int RESTART_REQUEST_CODE = 100;

    static void initiateRestart(Context context) {
        PendingIntent pi = getRestartIntent(context);
        getAlarmManager(context).setExact(
                AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 50, pi);

        Process.killProcess(Process.myPid());
    }

    static void cancelRestart(Context context) {
        getAlarmManager(context).cancel(getRestartIntent(context));
    }

    private static PendingIntent getRestartIntent(Context context) {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .setPackage(context.getPackageName())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, RESTART_REQUEST_CODE,
                homeIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
    }

    private static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
}
