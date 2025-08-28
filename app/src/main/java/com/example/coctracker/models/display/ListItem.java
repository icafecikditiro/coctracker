package com.example.coctracker.models.display;

// An interface to represent any item in our unified list.
public interface ListItem {
    int TYPE_HEADER = 0;
    int TYPE_TIMER = 1;

    int getItemType();
}
