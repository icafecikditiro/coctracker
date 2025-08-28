package com.example.coctracker.util;

import com.example.coctracker.models.Timer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JsonParser {

    private static final Map<String, String> NAME_MAP = new HashMap<>();
    // New map to link JSON keys to our desired categories
    private static final Map<String, String> CATEGORY_MAP = new HashMap<>();

    static {
        // --- Category Mapping ---
        CATEGORY_MAP.put("buildings", "Buildings");
        CATEGORY_MAP.put("buildings2", "Buildings");
        CATEGORY_MAP.put("heroes", "Heroes");
        CATEGORY_MAP.put("heroes2", "Heroes");
        CATEGORY_MAP.put("units", "Laboratory");
        CATEGORY_MAP.put("units2", "Laboratory");
        CATEGORY_MAP.put("spells", "Laboratory");
        CATEGORY_MAP.put("pets", "Laboratory");
        CATEGORY_MAP.put("siege_machines", "Laboratory");

        // --- Name Mapping ---
        // Home Base (isBuilderBase = false)
        NAME_MAP.put("1000000_false", "Army Camp");
        NAME_MAP.put("1000001_false", "Town Hall");
        NAME_MAP.put("1000002_false", "Elixir Collector");
        NAME_MAP.put("1000003_false", "Elixir Storage");
        NAME_MAP.put("1000004_false", "Gold Mine");
        NAME_MAP.put("1000005_false", "Gold Storage");
        NAME_MAP.put("1000006_false", "Barracks");
        NAME_MAP.put("1000007_false", "Laboratory");
        NAME_MAP.put("1000008_false", "Cannon");
        NAME_MAP.put("1000009_false", "Archer Tower");
        NAME_MAP.put("1000010_false", "Wall");
        NAME_MAP.put("1000014_false", "Clan Castle");
        NAME_MAP.put("1000020_false", "Spell Factory");
        NAME_MAP.put("1000021_false", "X-Bow");
        NAME_MAP.put("1000024_false", "Dark Elixir Storage");
        NAME_MAP.put("1000028_false", "Air Sweeper");
        NAME_MAP.put("1000029_false", "Dark Spell Factory");
        NAME_MAP.put("1000031_false", "Eagle Artillery");
        NAME_MAP.put("1000068_false", "Pet House");
        NAME_MAP.put("1000070_false", "Blacksmith");
        NAME_MAP.put("1000071_false", "Hero Hall");
        NAME_MAP.put("1000072_false", "Spell Tower");
        NAME_MAP.put("4000000_false", "Barbarian");
        NAME_MAP.put("4000001_false", "Archer");
        NAME_MAP.put("4000002_false", "Goblin");
        NAME_MAP.put("4000003_false", "Giant");
        NAME_MAP.put("4000004_false", "Wall Breaker");
        NAME_MAP.put("4000005_false", "Balloon");
        NAME_MAP.put("4000006_false", "Wizard");
        NAME_MAP.put("4000007_false", "Healer");
        NAME_MAP.put("4000008_false", "Dragon");
        NAME_MAP.put("4000009_false", "P.E.K.K.A");
        NAME_MAP.put("4000010_false", "Minion");
        NAME_MAP.put("4000011_false", "Hog Rider");
        NAME_MAP.put("4000110_false", "Root Rider");
        NAME_MAP.put("4000150_false", "Furnace");
        NAME_MAP.put("28000000_false", "Barbarian King");
        NAME_MAP.put("28000001_false", "Archer Queen");
        NAME_MAP.put("28000002_false", "Grand Warden");
        NAME_MAP.put("28000004_false", "Royal Champion");
        NAME_MAP.put("28000006_false", "Minion Prince");
        NAME_MAP.put("73000000_false", "L.A.S.S.I");
        NAME_MAP.put("73000001_false", "Mighty Yak");
        NAME_MAP.put("73000002_false", "Electro Owl");
        NAME_MAP.put("73000003_false", "Unicorn");
        NAME_MAP.put("73000009_false", "Frosty");
        NAME_MAP.put("4000051_false", "Wall Wrecker");
        NAME_MAP.put("4000052_false", "Battle Blimp");
        NAME_MAP.put("4000062_false", "Stone Slammer");
        NAME_MAP.put("4000075_false", "Siege Barracks");
        NAME_MAP.put("4000087_false", "Log Launcher");
        NAME_MAP.put("4000091_false", "Flame Flinger");

        // Builder Base (isBuilderBase = true)
        NAME_MAP.put("1000034_true", "Builder Hall");
        NAME_MAP.put("1000041_true", "Double Cannon");
        NAME_MAP.put("28000003_true", "Battle Machine");
    }

    public static ParsedDataWrapper parse(String jsonString, long accountId) {
        List<Timer> timers = new ArrayList<>();
        String playerTag = "";
        int townHallLevel = 0;

        try {
            JSONObject root = new JSONObject(jsonString);
            playerTag = root.optString("tag", "#PLAYERTAG");

            String[] keys = {"buildings", "traps", "units", "siege_machines", "heroes", "spells", "pets",
                    "buildings2", "traps2", "units2", "heroes2"};

            for (String key : keys) {
                if (root.has(key)) {
                    JSONArray items = root.getJSONArray(key);
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        boolean isBuilderBase = key.endsWith("2");

                        if (item.has("data") && item.getInt("data") == 1000001 && !isBuilderBase) {
                            townHallLevel = item.getInt("lvl");
                        }

                        if (item.has("timer")) {
                            long timerSeconds = item.getLong("timer");
                            long durationMillis = TimeUnit.SECONDS.toMillis(timerSeconds);

                            Timer timer = new Timer();
                            timer.accountId = accountId;
                            timer.name = getUpgradeName(item, isBuilderBase);
                            timer.endTime = System.currentTimeMillis() + durationMillis;
                            timer.isBuilderBase = isBuilderBase;
                            // Assign the category from our new map
                            timer.category = CATEGORY_MAP.get(key.replaceAll("[0-9]", ""));
                            timers.add(timer);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return new ParsedDataWrapper(new ArrayList<>(), "", 0);
        }

        Collections.sort(timers, new Comparator<Timer>() {
            @Override
            public int compare(Timer t1, Timer t2) {
                return Long.compare(t1.endTime, t2.endTime);
            }
        });

        return new ParsedDataWrapper(timers, playerTag, townHallLevel);
    }

    private static String getUpgradeName(JSONObject item, boolean isBuilderBase) throws JSONException {
        int dataId = item.getInt("data");
        int level = item.has("lvl") ? item.getInt("lvl") + 1 : 1;
        String uniqueKey = dataId + "_" + isBuilderBase;

        if (NAME_MAP.containsKey(uniqueKey)) {
            String name = NAME_MAP.get(uniqueKey);
            return name + " to Lvl " + level;
        } else {
            String baseType = isBuilderBase ? "Builder Hall" : "Town Hall";
            return baseType + " Upgrade (ID: " + dataId + ")";
        }
    }
}
