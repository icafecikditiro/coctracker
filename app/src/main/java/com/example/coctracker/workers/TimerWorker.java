package com.example.coctracker.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.coctracker.R;
import com.example.coctracker.database.AppDatabase;
import com.example.coctracker.database.DatabaseClient;
import com.example.coctracker.models.History;
import com.example.coctracker.models.Timer;

public class TimerWorker extends Worker {

    public static final String TIMER_ID = "timer_id";
    public static final String ACCOUNT_NAME = "account_name";
    public static final String NOTIFICATION_MESSAGE = "notification_message";
    public static final String IS_FINAL_NOTIFICATION = "is_final_notification";

    public TimerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        long timerId = getInputData().getLong(TIMER_ID, -1);
        String accountName = getInputData().getString(ACCOUNT_NAME);
        String message = getInputData().getString(NOTIFICATION_MESSAGE);
        boolean isFinal = getInputData().getBoolean(IS_FINAL_NOTIFICATION, false);

        if (timerId != -1 && message != null && accountName != null) {
            AppDatabase db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
            Timer timer = db.timerDao().getTimerById(timerId);
            if (timer != null) {
                sendNotification(accountName, timer.name, message);
                if (isFinal) {
                    // Create a history record
                    History historyRecord = new History();
                    historyRecord.accountId = timer.accountId;
                    historyRecord.upgradeName = timer.name;
                    historyRecord.completionDate = System.currentTimeMillis();
                    db.historyDao().insert(historyRecord);

                    // Now delete the active timer
                    db.timerDao().delete(timer);
                }
            }
        }
        return Result.success();
    }

    private void sendNotification(String accountName, String upgradeName, String message) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("coc_tracker_channel", "CoC Tracker Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        String notificationTitle = accountName + ": " + upgradeName;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "coc_tracker_channel")
                .setContentTitle(notificationTitle)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
