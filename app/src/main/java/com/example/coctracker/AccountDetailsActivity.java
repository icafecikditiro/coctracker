package com.example.coctracker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.example.coctracker.adapter.UpgradeListAdapter;
import com.example.coctracker.database.AppDatabase;
import com.example.coctracker.database.DatabaseClient;
import com.example.coctracker.models.Account;
import com.example.coctracker.models.Timer;
import com.example.coctracker.models.display.HeaderItem;
import com.example.coctracker.models.display.ListItem;
import com.example.coctracker.models.display.TimerItem;
import com.example.coctracker.util.JsonParser;
import com.example.coctracker.util.ParsedDataWrapper;
import com.example.coctracker.workers.TimerWorker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AccountDetailsActivity extends AppCompatActivity {

    private AppDatabase db;
    private long accountId;
    private String accountName;

    private RecyclerView mainRecyclerView;
    private UpgradeListAdapter adapter;
    private List<ListItem> displayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        accountId = getIntent().getLongExtra("ACCOUNT_ID", -1);
        accountName = getIntent().getStringExtra("ACCOUNT_NAME");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(accountName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView accountNameTextView = findViewById(R.id.accountNameTextView);
        accountNameTextView.setText(accountName);

        mainRecyclerView = findViewById(R.id.mainRecyclerView);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UpgradeListAdapter(this, displayList);
        mainRecyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.addTimerFromJsonFab);
        fab.setOnClickListener(view -> pasteAndShowConfirmation());
    }

    private void pasteAndShowConfirmation() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String jsonString = "";

        if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            if (item.getText() != null) {
                jsonString = item.getText().toString();
            }
        }

        if (jsonString.isEmpty()) {
            Toast.makeText(this, "Clipboard is empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        ParsedDataWrapper parsedData = JsonParser.parse(jsonString, accountId);

        if (parsedData.timers.isEmpty()) {
            Toast.makeText(this, "No active upgrades found in clipboard data.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder previewMessage = new StringBuilder("The following upgrades will be added:\n\n");
        for (Timer timer : parsedData.timers) {
            previewMessage.append("• ").append(timer.name).append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Upgrades")
                .setMessage(previewMessage.toString())
                .setPositiveButton("Confirm", (dialog, which) -> processJsonData(parsedData))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processJsonData(ParsedDataWrapper parsedData) {
        WorkManager.getInstance(this).cancelAllWorkByTag("account_" + accountId);
        db.timerDao().deleteAllByAccountId(accountId);

        Account currentAccount = db.accountDao().getAccountById(accountId);
        if (currentAccount != null) {
            currentAccount.playerTag = parsedData.playerTag;
            currentAccount.townHallLevel = parsedData.townHallLevel;
            db.accountDao().update(currentAccount);
        }

        for (Timer timer : parsedData.timers) {
            long timerId = db.timerDao().insert(timer);
            long durationMillis = timer.endTime - System.currentTimeMillis();
            long durationHours = TimeUnit.MILLISECONDS.toHours(durationMillis);

            scheduleNotification(timerId, "Your upgrade is complete!", durationMillis, true);
            if (durationHours > 1) {
                scheduleNotification(timerId, "1 hour remaining on your upgrade.", durationMillis - TimeUnit.HOURS.toMillis(1), false);
            }
        }
        Toast.makeText(this, "Updated " + parsedData.timers.size() + " timers.", Toast.LENGTH_SHORT).show();
        refreshTimerList();
    }

    public void refreshTimerList() {
        displayList.clear();

        List<Timer> allTimers = db.timerDao().getTimersForAccount(accountId);

        // Temporary lists to hold sorted timers
        List<Timer> homeBuilding = new ArrayList<>(), homeHero = new ArrayList<>(), homeLab = new ArrayList<>();
        List<Timer> builderBuilding = new ArrayList<>(), builderHero = new ArrayList<>(), builderLab = new ArrayList<>();

        // Sort all timers into their respective categories
        for (Timer timer : allTimers) {
            String category = timer.category;
            if (category == null) category = "Buildings";

            if (timer.isBuilderBase) {
                if ("Heroes".equals(category)) builderHero.add(timer);
                else if ("Laboratory".equals(category)) builderLab.add(timer);
                else builderBuilding.add(timer);
            } else {
                if ("Heroes".equals(category)) homeHero.add(timer);
                else if ("Laboratory".equals(category)) homeLab.add(timer);
                else homeBuilding.add(timer);
            }
        }

        // Build the final display list
        addSectionToList("Town Hall - Buildings", homeBuilding);
        addSectionToList("Town Hall - Heroes", homeHero);
        addSectionToList("Town Hall - Laboratory", homeLab);
        addSectionToList("Builder Hall - Buildings", builderBuilding);
        addSectionToList("Builder Hall - Heroes", builderHero);
        addSectionToList("Builder Hall - Laboratory", builderLab);

        adapter.notifyDataSetChanged();
    }

    private void addSectionToList(String title, List<Timer> timers) {
        if (!timers.isEmpty()) {
            displayList.add(new HeaderItem(title));
            for (Timer timer : timers) {
                displayList.add(new TimerItem(timer));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_history) {
            Intent intent = new Intent(this, HistoryActivity.class);
            intent.putExtra("ACCOUNT_ID", accountId);
            intent.putExtra("ACCOUNT_NAME", accountName);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void scheduleNotification(long timerId, String message, long delay, boolean isFinal) {
        if (delay <= 0) return;
        Data data = new Data.Builder()
                .putLong(TimerWorker.TIMER_ID, timerId)
                .putString(TimerWorker.ACCOUNT_NAME, accountName)
                .putString(TimerWorker.NOTIFICATION_MESSAGE, message)
                .putBoolean(TimerWorker.IS_FINAL_NOTIFICATION, isFinal)
                .build();
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(TimerWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("account_" + accountId)
                .build();
        WorkManager.getInstance(this).enqueue(workRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTimerList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the handler to prevent memory leaks
        if (adapter != null) {
            adapter.stopUpdatingTimers();
        }
    }
}
