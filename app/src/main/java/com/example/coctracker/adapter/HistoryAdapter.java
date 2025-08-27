package com.example.coctracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coctracker.R;
import com.example.coctracker.models.History;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<History> historyList;

    public HistoryAdapter(Context context, List<History> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        History historyItem = historyList.get(position);
        holder.upgradeNameTextView.setText(historyItem.upgradeName);

        // Format the timestamp into a readable date string
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        String dateString = "Completed on: " + sdf.format(new Date(historyItem.completionDate));
        holder.completionDateTextView.setText(dateString);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView upgradeNameTextView;
        TextView completionDateTextView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            upgradeNameTextView = itemView.findViewById(R.id.upgradeNameTextView);
            completionDateTextView = itemView.findViewById(R.id.completionDateTextView);
        }
    }
}
