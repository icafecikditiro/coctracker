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
import com.example.coctracker.models.display.HeaderItem;
import com.example.coctracker.models.display.ListItem;
import com.example.coctracker.models.display.TimerItem;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UpgradeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ListItem> items;
    private AppDatabase db;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    public UpgradeListAdapter(Context context, List<ListItem> items) {
        this.context = context;
        this.items = items;
        this.db = DatabaseClient.getInstance(context).getAppDatabase();
        startUpdatingTimers();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getItemType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ListItem.TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_timer, parent, false);
            return new TimerViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == ListItem.TYPE_HEADER) {
            HeaderItem header = (HeaderItem) items.get(position);
            ((HeaderViewHolder) holder).bind(header);
        } else {
            TimerItem timerItem = (TimerItem) items.get(position);
            ((TimerViewHolder) holder).bind(timerItem.getTimer());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder for Headers
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.headerTitleTextView);
        }
        void bind(HeaderItem header) {
            titleTextView.setText(header.getTitle());
        }
    }

    // ViewHolder for Timers
    class TimerViewHolder extends RecyclerView.ViewHolder {
        TextView timerNameTextView;
        TextView timeLeftTextView;
        Button deleteTimerButton;

        public TimerViewHolder(@NonNull View itemView) {
            super(itemView);
            timerNameTextView = itemView.findViewById(R.id.timerNameTextView);
            timeLeftTextView = itemView.findViewById(R.id.timeLeftTextView);
            deleteTimerButton = itemView.findViewById(R.id.deleteTimerButton);
        }

        void bind(final Timer timer) {
            timerNameTextView.setText(timer.name);
            updateTimeLeft(timeLeftTextView, timer.endTime);

            deleteTimerButton.setOnClickListener(v -> {
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
    }

    // --- Timer Update Logic ---
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
        updateRunnable = () -> {
            notifyDataSetChanged();
            handler.postDelayed(updateRunnable, 1000);
        };
        handler.post(updateRunnable);
    }

    public void stopUpdatingTimers() {
        handler.removeCallbacks(updateRunnable);
    }
}
