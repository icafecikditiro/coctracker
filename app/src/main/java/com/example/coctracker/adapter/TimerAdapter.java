package com.example.coctracker.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coctracker.AccountDetailsActivity;
import com.example.coctracker.R;
import com.example.coctracker.database.AppDatabase;
import com.example.coctracker.database.DatabaseClient;
import com.example.coctracker.models.Timer;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimerAdapter extends RecyclerView.Adapter<TimerAdapter.TimerViewHolder> {

    private Context context;
    private List<Timer> timerList;
    private AppDatabase db;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    public TimerAdapter(Context context, List<Timer> timerList) {
        this.context = context;
        this.timerList = timerList;
        this.db = DatabaseClient.getInstance(context).getAppDatabase();
        startUpdatingTimers();
    }

    @NonNull
    @Override
    public TimerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timer, parent, false);
        return new TimerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimerViewHolder holder, int position) {
        Timer timer = timerList.get(position);
        holder.timerNameTextView.setText(timer.name);
        updateTimeLeft(holder.timeLeftTextView, timer.endTime);

        holder.deleteTimerButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Timer")
                    .setMessage("Are you sure you want to delete this timer?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        db.timerDao().delete(timer);
                        ((AccountDetailsActivity) context).refreshTimerList();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return timerList.size();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        stopUpdatingTimers();
    }

    private void updateTimeLeft(TextView textView, long endTime) {
        long millisLeft = endTime - System.currentTimeMillis();
        if (millisLeft > 0) {
            long days = TimeUnit.MILLISECONDS.toDays(millisLeft);
            long hours = TimeUnit.MILLISECONDS.toHours(millisLeft) % 24;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millisLeft) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millisLeft) % 60;

            if (days > 0) {
                textView.setText(String.format(Locale.getDefault(), "%d d %02d:%02d:%02d", days, hours, minutes, seconds));
            } else {
                textView.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
            }
        } else {
            textView.setText("Finished");
        }
    }

    private void startUpdatingTimers() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateRunnable);
    }

    private void stopUpdatingTimers() {
        handler.removeCallbacks(updateRunnable);
    }

    static class TimerViewHolder extends RecyclerView.ViewHolder {
        TextView timerNameTextView;
        TextView timeLeftTextView;
        Button deleteTimerButton;

        public TimerViewHolder(@NonNull View itemView) {
            super(itemView);
            timerNameTextView = itemView.findViewById(R.id.timerNameTextView);
            timeLeftTextView = itemView.findViewById(R.id.timeLeftTextView);
            deleteTimerButton = itemView.findViewById(R.id.deleteTimerButton);
        }
    }
}
