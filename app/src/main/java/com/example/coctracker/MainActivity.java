package com.example.coctracker;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coctracker.adapter.AccountAdapter;
import com.example.coctracker.database.AppDatabase;
import com.example.coctracker.database.DatabaseClient;
import com.example.coctracker.models.Account;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private AccountAdapter adapter;
    private List<Account> accountList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        RecyclerView recyclerView = findViewById(R.id.accountsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        accountList = db.accountDao().getAll();
        adapter = new AccountAdapter(this, accountList);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.addAccountFab);
        fab.setOnClickListener(view -> addAccountDialog());
    }

    private void addAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_account);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.account_name);
        builder.setView(input);

        builder.setPositiveButton(R.string.add, (dialog, which) -> {
            String accountName = input.getText().toString().trim();
            if (!accountName.isEmpty()) {
                Account account = new Account();
                account.name = accountName;
                db.accountDao().insert(account);
                refreshAccountList();
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void refreshAccountList() {
        accountList.clear();
        accountList.addAll(db.accountDao().getAll());
        // This will sort the list and update the adapter
        adapter.sortAccounts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAccountList();
    }
}
