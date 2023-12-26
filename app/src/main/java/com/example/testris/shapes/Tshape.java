package com.example.testris.shapes;

import android.graphics.Color;

import com.example.testris.BoardCell;

public class Tshape extends Shape {
    int color = Color.rgb(139, 0, 139);
    public Tshape(final int behavior){
        a[1][2] = a[2][2] = a[2][3] = a[3][2] = 1;
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
