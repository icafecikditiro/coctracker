package com.example.coctracker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.example.coctracker.adapter.TimerAdapter;
import com.example.coctracker.database.AppDatabase;
import com.example.coctracker.database.DatabaseClient;
import com.example.coctracker.models.Account;
import com.example.coctracker.models.Timer;
import com.example.coctracker.util.JsonParser;
import com.example.coctracker.util.ParsedDataWrapper;
import com.example.coctracker.workers.TimerWorker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AccountDetailsActivity extends AppCompatActivity {

    private AppDatabase db;
    private long accountId;
    private String accountName;

    // UI Components
    private TextView homeBaseTitle, homeBuildingsTitle, homeHeroesTitle, homeLabTitle;
    private TextView builderBaseTitle, builderBuildingsTitle, builderHeroesTitle, builderLabTitle;
    private RecyclerView homeBuildingsRecyclerView, homeHeroesRecyclerView, homeLabRecyclerView;
    private RecyclerView builderBuildingsRecyclerView, builderHeroesRecyclerView, builderLabRecyclerView;

    // Data Lists and Adapters
    private List<Timer> homeBuildingTimers, homeHeroTimers, homeLabTimers;
    private List<Timer> builderBuildingTimers, builderHeroTimers, builderLabTimers;
    private TimerAdapter homeBuildingAdapter, homeHeroAdapter, homeLabAdapter;
    private TimerAdapter builderBuildingAdapter, builderHeroAdapter, builderLabAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        accountId = getIntent().getLongExtra("ACCOUNT_ID", -1);
        accountName = getIntent().getStringExtra("ACCOUNT_NAME");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(accountName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // This enables the back arrow
        }

        initializeViews();
        initializeListsAndAdapters();
        setupRecyclerViews();

        FloatingActionButton fab = findViewById(R.id.addTimerFromJsonFab);
        fab.setOnClickListener(view -> pasteAndShowConfirmation());
    }

    private void initializeViews() {
        TextView accountNameTextView = findViewById(R.id.accountNameTextView);
        accountNameTextView.setText(accountName);

        homeBaseTitle = findViewById(R.id.homeBaseTitle);
        homeBuildingsTitle = findViewById(R.id.homeBuildingsTitle);
        homeHeroesTitle = findViewById(R.id.homeHeroesTitle);
        homeLabTitle = findViewById(R.id.homeLabTitle);

        builderBaseTitle = findViewById(R.id.builderBaseTitle);
        builderBuildingsTitle = findViewById(R.id.builderBuildingsTitle);
        builderHeroesTitle = findViewById(R.id.builderHeroesTitle);
        builderLabTitle = findViewById(R.id.builderLabTitle);

        homeBuildingsRecyclerView = findViewById(R.id.homeBuildingsRecyclerView);
        homeHeroesRecyclerView = findViewById(R.id.homeHeroesRecyclerView);
        homeLabRecyclerView = findViewById(R.id.homeLabRecyclerView);

        builderBuildingsRecyclerView = findViewById(R.id.builderBuildingsRecyclerView);
        builderHeroesRecyclerView = findViewById(R.id.builderHeroesRecyclerView);
        builderLabRecyclerView = findViewById(R.id.builderLabRecyclerView);
    }

    private void initializeListsAndAdapters() {
        homeBuildingTimers = new ArrayList<>();
        homeHeroTimers = new ArrayList<>();
        homeLabTimers = new ArrayList<>();
        homeBuildingAdapter = new TimerAdapter(this, homeBuildingTimers);
        homeHeroAdapter = new TimerAdapter(this, homeHeroTimers);
        homeLabAdapter = new TimerAdapter(this, homeLabTimers);

        builderBuildingTimers = new ArrayList<>();
        builderHeroTimers = new ArrayList<>();
        builderLabTimers = new ArrayList<>();
        builderBuildingAdapter = new TimerAdapter(this, builderBuildingTimers);
        builderHeroAdapter = new TimerAdapter(this, builderHeroTimers);
        builderLabAdapter = new TimerAdapter(this, builderLabTimers);
    }

    private void setupRecyclerViews() {
        homeBuildingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        homeBuildingsRecyclerView.setAdapter(homeBuildingAdapter);
        homeHeroesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        homeHeroesRecyclerView.setAdapter(homeHeroAdapter);
        homeLabRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        homeLabRecyclerView.setAdapter(homeLabAdapter);

        builderBuildingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        builderBuildingsRecyclerView.setAdapter(builderBuildingAdapter);
        builderHeroesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        builderHeroesRecyclerView.setAdapter(builderHeroAdapter);
        builderLabRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        builderLabRecyclerView.setAdapter(builderLabAdapter);
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
        homeBuildingTimers.clear();
        homeHeroTimers.clear();
        homeLabTimers.clear();
        builderBuildingTimers.clear();
        builderHeroTimers.clear();
        builderLabTimers.clear();

        List<Timer> allTimers = db.timerDao().getTimersForAccount(accountId);
        for (Timer timer : allTimers) {
            if (timer.isBuilderBase) {
                switch (Objects.requireNonNull(timer.category)) {
                    case "Buildings": builderBuildingTimers.add(timer); break;
                    case "Heroes": builderHeroTimers.add(timer); break;
                    case "Laboratory": builderLabTimers.add(timer); break;
                }
            } else {
                switch (Objects.requireNonNull(timer.category)) {
                    case "Buildings": homeBuildingTimers.add(timer); break;
                    case "Heroes": homeHeroTimers.add(timer); break;
                    case "Laboratory": homeLabTimers.add(timer); break;
                }
            }
        }

        homeBuildingAdapter.notifyDataSetChanged();
        homeHeroAdapter.notifyDataSetChanged();
        homeLabAdapter.notifyDataSetChanged();
        builderBuildingAdapter.notifyDataSetChanged();
        builderHeroAdapter.notifyDataSetChanged();
        builderLabAdapter.notifyDataSetChanged();

        updateSectionVisibility();
    }

    private void updateSectionVisibility() {
        boolean isHomeBaseVisible = !homeBuildingTimers.isEmpty() || !homeHeroTimers.isEmpty() || !homeLabTimers.isEmpty();
        homeBaseTitle.setVisibility(isHomeBaseVisible ? View.VISIBLE : View.GONE);
        updateCategoryVisibility(homeBuildingTimers, homeBuildingsTitle, homeBuildingsRecyclerView);
        updateCategoryVisibility(homeHeroTimers, homeHeroesTitle, homeHeroesRecyclerView);
        updateCategoryVisibility(homeLabTimers, homeLabTitle, homeLabRecyclerView);

        boolean isBuilderBaseVisible = !builderBuildingTimers.isEmpty() || !builderHeroTimers.isEmpty() || !builderLabTimers.isEmpty();
        builderBaseTitle.setVisibility(isBuilderBaseVisible ? View.VISIBLE : View.GONE);
        updateCategoryVisibility(builderBuildingTimers, builderBuildingsTitle, builderBuildingsRecyclerView);
        updateCategoryVisibility(builderHeroTimers, builderHeroesTitle, builderHeroesRecyclerView);
        updateCategoryVisibility(builderLabTimers, builderLabTitle, builderLabRecyclerView);
    }

    private void updateCategoryVisibility(List<Timer> list, View title, View recyclerView) {
        if (list.isEmpty()) {
            title.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else {
            title.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle history button click
        if (item.getItemId() == R.id.action_history) {
            Intent intent = new Intent(this, HistoryActivity.class);
            intent.putExtra("ACCOUNT_ID", accountId);
            intent.putExtra("ACCOUNT_NAME", accountName);
            startActivity(intent);
            return true;
        }
        // Handle back arrow click
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
}
