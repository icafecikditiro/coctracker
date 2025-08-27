package com.example.coctracker.util;

import com.example.coctracker.models.Timer;
import java.util.List;

// This class holds all the data we extract from the JSON file.
public class ParsedDataWrapper {
    public final List<Timer> timers;
    public final String playerTag;
    public final int townHallLevel;

    public ParsedDataWrapper(List<Timer> timers, String playerTag, int townHallLevel) {
        this.timers = timers;
        this.playerTag = playerTag;
        this.townHallLevel = townHallLevel;
    }
}
