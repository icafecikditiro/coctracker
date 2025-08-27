package com.example.coctracker.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update; // Import Update
import com.example.coctracker.models.Account;
import java.util.List;

@Dao
public interface AccountDao {
    @Query("SELECT * FROM account")
    List<Account> getAll();

    @Insert
    void insert(Account account);

    @Update // Add this annotation
    void update(Account account); // Add this method

    @Delete
    void delete(Account account);

    @Query("SELECT * FROM account WHERE id = :accountId") // Add this method
    Account getAccountById(long accountId);
}
