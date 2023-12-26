package com.example.testris.shapes;

import android.graphics.Color;

import com.example.testris.BoardCell;

public class S1shape extends Shape {
    int color = Color.RED;
    public S1shape(final int behavior){
        a[2][1] = a[2][2] = a[3][2] = a[3][3] = 1;
        for (int i = 0; i < 5; ++i) {
            for (int j = 0; j < 5; ++j) {
                if (a[i][j] == 1)
                    mat[i][j] = new BoardCell(a[i][j], color, behavior);
                else
                    mat[i][j] = new BoardCell();

            }
        }
        canRotate = true;
    }
}
