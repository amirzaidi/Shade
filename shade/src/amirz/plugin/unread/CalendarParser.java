package amirz.plugin.unread;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CalendarContract.Events;

import com.android.launcher3.Utilities;

import java.util.Calendar;

import static android.Manifest.permission.READ_CALENDAR;

class CalendarParser {
    static class Event {
        final String name;
        final Calendar start;
        final Calendar end;

        private Event(Cursor cursor) {
            name = cursor.getString(0);
            start = Calendar.getInstance();
            end = Calendar.getInstance();

            start.setTimeInMillis(Long.parseLong(cursor.getString(1)));
            end.setTimeInMillis(Long.parseLong(cursor.getString(2)));
        }
    }

    static Event getEvent(Context context) {
        if (Utilities.ATLEAST_MARSHMALLOW
                && context.checkSelfPermission(READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        Calendar latestStartTime = Calendar.getInstance();
        latestStartTime.add(Calendar.HOUR, 12);
        Calendar latestEndTime = Calendar.getInstance();
        latestEndTime.add(Calendar.HOUR, 24);
        Calendar currentTime = Calendar.getInstance();

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(Events.CONTENT_URI, new String[] {
                        Events.TITLE,
                        Events.DTSTART,
                        Events.DTEND
                },
                Events.DTSTART + " < ? AND " + Events.DTEND + " < ? AND " + Events.DTEND + " > ? AND deleted == 0",
                new String[] {
                        Long.toString(latestStartTime.getTimeInMillis()), // Close to starting.
                        Long.toString(latestEndTime.getTimeInMillis()), // Close to ending.
                        Long.toString(currentTime.getTimeInMillis()), // Did not end yet.
                },
                Events.DTSTART + " ASC");

        Event event = null;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Event newEvent = new Event(cursor);
                if (event == null || isBetterEvent(event, newEvent)) {
                    event = newEvent;
                }
            }
            cursor.close();
        }
        return event;
    }

    private static boolean isBetterEvent(Event oldEvent, Event newEvent) {
        // Note: Old means currently selected as the best candidate.
        Calendar currentTime = Calendar.getInstance();
        boolean oldAlreadyStarted = !oldEvent.start.after(currentTime);
        boolean newAlreadyStarted = !newEvent.start.after(currentTime);
        if (oldAlreadyStarted && newAlreadyStarted) {
            // Both have started, pick the one that ends first.
            return !newEvent.end.after(oldEvent.end);
        } else if (!oldAlreadyStarted && !newAlreadyStarted) {
            // Neither has started, pick the one that starts first.
            return newEvent.start.before(oldEvent.start);
        } else {
            // True if new event is upcoming, but old has already started.
            return oldAlreadyStarted;
        }
    }
}
