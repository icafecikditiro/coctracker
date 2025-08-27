package com.example.coctracker.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coctracker.AccountDetailsActivity;
import com.example.coctracker.MainActivity;
import com.example.coctracker.R;
import com.example.coctracker.database.AppDatabase;
import com.example.coctracker.database.DatabaseClient;
import com.example.coctracker.models.Account;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private Context context;
    private List<Account> accountList;
    private AppDatabase db;

    public AccountAdapter(Context context, List<Account> accountList) {
        this.context = context;
        this.accountList = accountList;
        this.db = DatabaseClient.getInstance(context).getAppDatabase();
        sortAccounts();
    }

    public void sortAccounts() {
        // Sorts the list by Town Hall Level in descending order
        Collections.sort(accountList, (o1, o2) -> Integer.compare(o2.townHallLevel, o1.townHallLevel));
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accountList.get(position);
        holder.accountNameTextView.setText(account.name);

        // Set Town Hall Level text
        if (account.townHallLevel > 0) {
            holder.townHallTextView.setText("Townhall: " + account.townHallLevel);
            holder.townHallTextView.setVisibility(View.VISIBLE);
        } else {
            holder.townHallTextView.setVisibility(View.GONE);
        }

        // Set Player Tag text
        if (account.playerTag != null && !account.playerTag.isEmpty()) {
            holder.playerTagTextView.setText(account.playerTag);
            holder.playerTagTextView.setVisibility(View.VISIBLE);
        } else {
            holder.playerTagTextView.setVisibility(View.GONE);
        }

        // Click listener to view details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AccountDetailsActivity.class);
            intent.putExtra("ACCOUNT_ID", account.id);
            intent.putExtra("ACCOUNT_NAME", account.name);
            context.startActivity(intent);
        });

        // Click listener to copy tag
        holder.playerTagTextView.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Player Tag", account.playerTag);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Tag copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        // Click listener to delete account
        holder.deleteAccountButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete this account and all its timers?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        db.accountDao().delete(account);
                        ((MainActivity) context).refreshAccountList();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView accountNameTextView, townHallTextView, playerTagTextView;
        ImageView deleteAccountButton;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            accountNameTextView = itemView.findViewById(R.id.accountNameTextView);
            townHallTextView = itemView.findViewById(R.id.townHallTextView);
            playerTagTextView = itemView.findViewById(R.id.playerTagTextView);
            deleteAccountButton = itemView.findViewById(R.id.deleteAccountButton);
        }
    }
}
