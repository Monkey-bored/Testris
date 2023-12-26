package com.example.testris.shapes;

import com.example.testris.BoardCell;

abstract public class Shape {
    public int x, y;
    public BoardCell[][] mat = new BoardCell[5][5];
    public boolean canRotate;
    int[][] a = new int[5][5];
    int color;
    public int getColor() {return color;}
    public void RotateLeft() {
        if (!this.canRotate) {
            return;
        }

        BoardCell[][] aux = new BoardCell[5][5];
        for (int i = 1; i < 5; ++i) {
            for (int j = 1; j < 5; ++j) {
                aux[5 - j][i] = mat[i][j];
            }
        }
        for (int i = 1; i < 5; ++i) {
            for (int j = 1; j < 5; ++j) {
                mat[i][j] = aux[i][j];
            }
        }
    }

    public void RotateRight() {
        if (!this.canRotate) {
            return;
        }

        BoardCell[][] aux = new BoardCell[5][5];
        for (int i = 1; i < 5; ++i) {
            for (int j = 1; j < 5; ++j) {
                aux[j][5-i] = mat[i][j];
            }
        }
        for (int i = 1; i < 5; ++i) {
            for (int j = 1; j < 5; ++j) {
                mat[i][j] = aux[i][j];
            }
        }
    }
}
