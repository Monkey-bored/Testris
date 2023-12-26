package com.example.testris;

import android.graphics.Color;

public class BoardCell {
    public final static int BEHAVIOR_IS_FIXED = 2, BEHAVIOR_IS_FALLING = 1, BEHAVIOR_NOTHING = 0;
    private int state, color, behavior;
    // state = 0 means free cell, state = 1 means occupied cell

    public BoardCell() {
        state = 0;
        color = Color.BLACK;
        behavior = BEHAVIOR_NOTHING;
    }

    public BoardCell(int state, int color) {
        this.state = state;
        this.color = color;
        this.behavior = BEHAVIOR_NOTHING;
    }

    public BoardCell(int state, int color, int behavior) {
        this.state = state;
        this.color = color;
        this.behavior = behavior;
    }

    public int getState() {
        //returns 0 if the cell is free, 1 if the cell is occupied
        return state;
    }

    public int getColor() {
        return color;
    }

    public int getBehavior() {
        return behavior;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setBehavior(int behavior) {
        this.behavior = behavior;
    }
}
