package com.example.coctracker.models.display;

import com.example.coctracker.models.Timer;

// Represents an actual upgrade timer in the list.
public class TimerItem implements ListItem {
    private Timer timer;

    public TimerItem(Timer timer) {
        this.timer = timer;
    }

    public Timer getTimer() {
        return timer;
    }

    @Override
    public int getItemType() {
        return TYPE_TIMER;
    }
}
