package com.example.coctracker.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.coctracker.models.History;
import java.util.List;

@Dao
public interface HistoryDao {
    @Insert
    void insert(History history);

    @Query("SELECT * FROM history WHERE accountId = :accountId ORDER BY completionDate DESC")
    List<History> getHistoryForAccount(long accountId);
}
    