package com.example.coctracker;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coctracker.adapter.HistoryAdapter;
import com.example.coctracker.database.AppDatabase;
import com.example.coctracker.database.DatabaseClient;
import com.example.coctracker.models.History;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private AppDatabase db;
    private long accountId;
    private String accountName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        accountId = getIntent().getLongExtra("ACCOUNT_ID", -1);
        accountName = getIntent().getStringExtra("ACCOUNT_NAME");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(accountName + " - History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back arrow
        }

        RecyclerView recyclerView = findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<History> historyList = db.historyDao().getHistoryForAccount(accountId);
        HistoryAdapter adapter = new HistoryAdapter(this, historyList);
        recyclerView.setAdapter(adapter);
    }

    // Add this method to handle the back arrow click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
