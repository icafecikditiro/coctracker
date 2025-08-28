package com.example.coctracker.database;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migrations {

    /**
     * Migration from version 1 to 2.
     * Adds the 'isBuilderBase' column to the Timer table.
     */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Timer ADD COLUMN isBuilderBase INTEGER NOT NULL DEFAULT 0");
        }
    };

    /**
     * Migration from version 2 to 3.
     * Adds 'playerTag' and 'townHallLevel' columns to the Account table.
     */
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Account ADD COLUMN playerTag TEXT");
            database.execSQL("ALTER TABLE Account ADD COLUMN townHallLevel INTEGER NOT NULL DEFAULT 0");
        }
    };

    /**
     * Migration from version 3 to 4.
     * Creates the new 'History' table.
     */
    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `History` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `accountId` INTEGER NOT NULL, `upgradeName` TEXT, `completionDate` INTEGER NOT NULL)");
        }
    };

    /**
     * Migration from version 4 to 5.
     * Adds the 'category' column to the Timer table.
     */
    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Timer ADD COLUMN category TEXT");
        }
    };
}
