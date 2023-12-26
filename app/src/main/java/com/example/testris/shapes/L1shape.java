package com.example.testris.shapes;

import android.graphics.Color;

import com.example.testris.BoardCell;

public class L1shape extends Shape {
    int color = Color.rgb(255, 165, 0);

    public L1shape(final int behavior) {
        a[1][2] = a[1][3] = a[2][3] = a[3][3] = 1;
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
