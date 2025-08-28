package com.example.coctracker.util;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * A custom LinearLayoutManager that disables vertical scrolling.
 * This is used to allow a RecyclerView to correctly measure its height
 * when placed inside a ScrollView.
 */
public class NonScrollingLinearLayoutManager extends LinearLayoutManager {

    public NonScrollingLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public boolean canScrollVertically() {
        // This is the key change: it disables the RecyclerView's own scrolling.
        return false;
    }
}
