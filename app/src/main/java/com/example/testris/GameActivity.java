package com.example.testris;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.example.testris.shapes.Boxshape;
import com.example.testris.shapes.Ishape;
import com.example.testris.shapes.L1shape;
import com.example.testris.shapes.L2shape;
import com.example.testris.shapes.S1shape;
import com.example.testris.shapes.S2shape;
import com.example.testris.shapes.Shape;
import com.example.testris.shapes.Tshape;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{
    private FirebaseAuth mAuth;
    int ROWS = 22;
    int COLUMNS = 12;
    final int BOARD_HEIGHT = 800;
    final int BOARD_WIDTH = 400;
    final Handler handler = new Handler();
    final Shape[] shapes = new Shape[7];
    final int RIGHT_DIRECTION = 1;
    final int DOWN_DIRECTION = 2;
    final int LEFT_DIRECTION = 3;
    int SPEED_NORMAL = 500;
    int SPEED_FAST = 50;
    int score;
    int currentShapeIndex;
    boolean gameInProgress, gamePaused, fastSpeedState, currentShapeAlive, dialogRunning;

    final int[] dx = {-1, 0, 1, 0};
    final int[] dy = {0, 1, 0, -1};

    private GestureDetectorCompat gestureDetector;

    Random random = new Random();
    HashMap<String, Object> scoreMap;
    BoardCell[][] gameMatrix;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    LinearLayout linearLayout;
    Shape currentShape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        dialogRunning = false;
        TextView leave = findViewById(R.id.leaveBtn);
        //creates the board
        bitmap = Bitmap.createBitmap(BOARD_WIDTH, BOARD_HEIGHT, Bitmap.Config.ARGB_8888);
        //creates the canvas for the representation of the board
        canvas = new Canvas(bitmap);
        paint = new Paint();
        linearLayout = findViewById(R.id.game_board);
        score = 0;
        currentShapeAlive = false;
        //sets up the gesture detector that is used for movements like moving a piece left/right by
        // swiping
        gestureDetector = new GestureDetectorCompat(this, this);

        ShapesInit();

        GameInit();
        leave.setOnClickListener(v -> {
            gamePaused = true;
            dialogRunning = true;
            createPauseDialog();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gameInProgress) {
            gamePaused = true;
            PaintMatrix();
        }
    }

    private void ShapesInit() {
        shapes[0] = new Boxshape(BoardCell.BEHAVIOR_IS_FALLING);
        shapes[1] = new Ishape(BoardCell.BEHAVIOR_IS_FALLING);
        shapes[2] = new L1shape(BoardCell.BEHAVIOR_IS_FALLING);
        shapes[3] = new L2shape(BoardCell.BEHAVIOR_IS_FALLING);
        shapes[4] = new S1shape(BoardCell.BEHAVIOR_IS_FALLING);
        shapes[5] = new S2shape(BoardCell.BEHAVIOR_IS_FALLING);
        shapes[6] = new Tshape(BoardCell.BEHAVIOR_IS_FALLING);
    }

    private void CopyMatrix(BoardCell[][] A, BoardCell[][] B) {
        for (int i = 0; i < ROWS; ++i) {
            for (int j = 0; j < COLUMNS; ++j) {
                B[i][j] = new BoardCell(A[i][j].getState(), A[i][j].getColor(), A[i][j].getBehavior());
            }
        }
    }

    private void FixGameMatrix() {
        for (int i = 1; i < ROWS - 1; ++i) {
            for (int j = 1; j < COLUMNS - 1; ++j) {
                if (gameMatrix[i][j].getState() == 0) {
                    gameMatrix[i][j].setColor(Color.BLACK);
                    gameMatrix[i][j].setBehavior(BoardCell.BEHAVIOR_NOTHING);
                    continue;
                }
                if (gameMatrix[i][j].getBehavior() == BoardCell.BEHAVIOR_IS_FIXED)
                    continue;
                if (gameMatrix[i][j].getBehavior() == BoardCell.BEHAVIOR_IS_FALLING) {
                    int x, y, startX, startY;
                    for (x = 1, startX = currentShape.x; x <= 4; ++x, ++startX) {
                        for (y = 1, startY = currentShape.y; y <= 4; ++y, ++startY) {
                            if (startX == x && startY == y) {
                                if (currentShape.mat[x][y].getState() == 0) {
                                    gameMatrix[x][y] = new BoardCell();
                                }
                            }
                        }
                    }
                    continue;
                }
                if (gameMatrix[i][j].getBehavior() == BoardCell.BEHAVIOR_NOTHING) {
                    int x, y, startX, startY;
                    for (x = 1, startX = currentShape.x; x <= 4; ++x, ++startX) {
                        for (y = 1, startY = currentShape.y; y <= 4; ++y, ++startY) {
                            if (startX == x && startY == y) {
                                if (currentShape.mat[x][y].getState() == 1) {
                                    gameMatrix[x][y] = currentShape.mat[x][y];
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean MoveShape(final int direction, Shape nowShape) {
        // copy the gameMatrix in aux
        BoardCell[][] aux = new BoardCell[ROWS][];
        for (int i = 0; i < ROWS; ++i)
            aux[i] = new BoardCell[COLUMNS];
        CopyMatrix(gameMatrix, aux);
        int i, startX, j, startY;
        // eliminate the shape from the table
        for (startX = nowShape.x, i = 1; i <= 4; ++i, ++startX) {
            for (startY = nowShape.y, j = 1; j <= 4; ++j, ++startY) {
                if (nowShape.mat[i][j].getState() == 1) {
                    gameMatrix[startX][startY] = new BoardCell();
                }
            }
        }

        // try to move the shape to the specified direction
        for (startX = nowShape.x + dx[direction], i = 1; i <= 4; ++i, ++startX) {
            for (startY = nowShape.y + dy[direction], j = 1; j <= 4; ++j, ++startY) {
                gameMatrix[startX][startY].setState(gameMatrix[startX][startY].getState() + nowShape.mat[i][j].getState());
                if (nowShape.mat[i][j].getState() == 1) {
                    gameMatrix[startX][startY].setColor(nowShape.mat[i][j].getColor());
                    gameMatrix[startX][startY].setBehavior(nowShape.mat[i][j].getBehavior());
                }
                if (gameMatrix[startX][startY].getState() > 1) {
                    CopyMatrix(aux, gameMatrix);
                    FixGameMatrix();
                    return false;
                }
            }
        }
        nowShape.x += dx[direction];
        nowShape.y += dy[direction];
        FixGameMatrix();
        return true;
    }

    private void RotateLeft(Shape nowShape) {
        // copy the gameMatrix in aux
        BoardCell[][] aux = new BoardCell[ROWS][];
        for (int i = 0; i < ROWS; ++i)
            aux[i] = new BoardCell[COLUMNS];
        CopyMatrix(gameMatrix, aux);
        int i, startX, j, startY;
        // eliminate the shape from the gameMatrix
        for (startX = nowShape.x, i = 1; i <= 4; ++i, ++startX) {
            for (startY = nowShape.y, j = 1; j <= 4; ++j, ++startY) {
                if (nowShape.mat[i][j].getState() == 1) {
                    gameMatrix[startX][startY] = new BoardCell();
                }
            }
        }
        // rotate the shape to left
        nowShape.RotateLeft();
        // ... and try to put it again on the table
        for (startX = nowShape.x, i = 1; i <= 4; ++i, ++startX) {
            for (startY = nowShape.y, j = 1; j <= 4; ++j, ++startY) {
                gameMatrix[startX][startY].setState(gameMatrix[startX][startY].getState() + nowShape.mat[i][j].getState());
                if (nowShape.mat[i][j].getState() == 1) {
                    gameMatrix[startX][startY].setColor(nowShape.mat[i][j].getColor());
                    gameMatrix[startX][startY].setBehavior(nowShape.mat[i][j].getBehavior());
                }
                // if we can't put the rotated shape on the table
                if (gameMatrix[startX][startY].getState() > 1) {
                    // then recreate the initial state of the table
                    CopyMatrix(aux, gameMatrix);
                    // ... and rotate the shape to right, to obtain its initial state
                    nowShape.RotateRight();
                    FixGameMatrix();
                    return;
                }
            }
        }
        FixGameMatrix();
    }

    private void RotateRight(Shape nowShape) {
        // copy the gameMatrix in aux
        BoardCell[][] aux = new BoardCell[ROWS][];
        for (int i = 0; i < ROWS; ++i)
            aux[i] = new BoardCell[COLUMNS];
        CopyMatrix(gameMatrix, aux);
        int i, startX, j, startY;
        // eliminate the shape from the gameMatrix
        for (startX = nowShape.x, i = 1; i <= 4; ++i, ++startX) {
            for (startY = nowShape.y, j = 1; j <= 4; ++j, ++startY) {
                if (nowShape.mat[i][j].getState() == 1) {
                    gameMatrix[startX][startY] = new BoardCell();
                }
            }
        }

        // rotate the shape to right
        nowShape.RotateRight();
        // ... and try to put it again on the table
        for (startX = nowShape.x, i = 1; i <= 4; ++i, ++startX) {
            for (startY = nowShape.y, j = 1; j <= 4; ++j, ++startY) {
                gameMatrix[startX][startY].setState(gameMatrix[startX][startY].getState() + nowShape.mat[i][j].getState());
                if (nowShape.mat[i][j].getState() == 1) {
                    gameMatrix[startX][startY].setColor(nowShape.mat[i][j].getColor());
                    gameMatrix[startX][startY].setBehavior(nowShape.mat[i][j].getBehavior());
                }
                // if we can't put the rotated shape on the table
                if (gameMatrix[startX][startY].getState() > 1) {
                    // then recreate the initial state of the table
                    CopyMatrix(aux, gameMatrix);
                    // ... and rotate the shape to left, to obtain its initial state
                    nowShape.RotateLeft();
                    FixGameMatrix();
                    return;
                }
            }
        }
        FixGameMatrix();
    }

    public boolean CreateShape() {
        // generate a random shape to put on the gameMatrix
        currentShapeIndex = random.nextInt(shapes.length);
        currentShape = shapes[currentShapeIndex];

        // generate random number of rotations
        int number_of_rotations = random.nextInt(4);
        for (int i = 1; i <= number_of_rotations; ++i) {
            currentShape.RotateRight();
        }
        //starting positions should be equal to the middle top of the screen
        currentShape.x = 0;
        currentShape.y = -1 + (COLUMNS - 2) / 2;
        for (int offset = 0; offset <= 3; ++offset) {
            int i, startX, j, startY;
            boolean hasPlace = true;
            //checks if there is place for the shape to be placed or keep falling
            for (startX = currentShape.x + offset, i = 1; i <= 4; ++i, ++startX) {
                for (startY = currentShape.y, j = 1; j <= 4; ++j, ++startY) {
                    gameMatrix[startX][startY].setState(gameMatrix[startX][startY].getState() + currentShape.mat[i][j].getState());
                    //if getState() outputs more than 1 that means that an occupied cell has met an occupied cell and therefore there is no space
                    if (gameMatrix[startX][startY].getState() > 1) {
                        hasPlace = false;
                    }
                }
            }
            if (hasPlace) {
                //paints the shape
                for (i = 1, startX = currentShape.x + offset; i <= 4; ++i, ++startX) {
                    for (j = 1, startY = currentShape.y; j <= 4; ++j, ++startY) {
                        if (currentShape.mat[i][j].getState() == 1) {
                            gameMatrix[startX][startY].setColor(currentShape.mat[i][j].getColor());
                            gameMatrix[startX][startY].setBehavior(currentShape.mat[i][j].getBehavior());
                        }
                    }
                }
                currentShape.x += offset;
                FixGameMatrix();
                return true;
            } else {
                for (startX = currentShape.x + offset, i = 1; i <= 4; ++i, ++startX) {
                    for (startY = currentShape.y, j = 1; j <= 4; ++j, ++startY) {
                        gameMatrix[startX][startY].setState(gameMatrix[startX][startY].getState() - currentShape.mat[i][j].getState());
                    }
                }
                FixGameMatrix();
            }
        }
        return false;
    }

    private void Check() {
        int k = 0;
        for (int i = ROWS - 2; i >= 3; --i) {
            boolean lineFull = true;
            for (int j = 1; j < COLUMNS - 1; ++j) {
                if (gameMatrix[i][j].getState() == 0) {
                    lineFull = false;
                    break;
                }
            }
            if (lineFull) {
                ++k;
                SPEED_NORMAL -= score;
            } else {
                if (k == 0)
                    continue;
                for (int j = 1; j < COLUMNS - 1; ++j) {
                    int state = gameMatrix[i][j].getState();
                    int color = gameMatrix[i][j].getColor();
                    int behavior = gameMatrix[i][j].getBehavior();
                    gameMatrix[i + k][j] = new BoardCell(state, color, behavior);
                }
            }
        }
        for (int pas = 0; pas < k; ++pas) {
            for (int j = 1; j < COLUMNS - 1; ++j) {
                gameMatrix[3 + pas][j] = new BoardCell();
            }
        }
        // Update the score
        score += k * (k + 1) / 2;
        FixGameMatrix();
    }

    void PaintMatrix() {

        // Paint the game board background
        paint.setColor(Color.BLACK);
        canvas.drawRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT, paint);
        // Paint the grid on the game board
        paint.setColor(Color.WHITE);
        int actualRows = ROWS - 2;
        int actualCol = COLUMNS - 2;

        int heightPerRow = BOARD_HEIGHT / actualRows;
        for (int i = 0; i <= actualRows; ++i) {
            canvas.drawLine(0, i * heightPerRow, BOARD_WIDTH,
                    i * heightPerRow, paint);
        }
        int widthPerCol = BOARD_WIDTH / actualCol;
        for (int i = 0; i <= actualCol; ++i) {
            canvas.drawLine(i * widthPerCol, 0,
                    i * widthPerCol, BOARD_HEIGHT, paint);
        }

        // Paint the tetris blocks
        for (int i = 1; i < ROWS- 1 ; ++i) {
            for (int j = 1; j < COLUMNS - 1; ++j) {
                if (gameMatrix[i][j].getState() == 1) {
                    paint.setColor(gameMatrix[i][j].getColor());
                    canvas.drawRect((j - 1) * widthPerCol,
                            (i - 1) * heightPerRow,
                            (j) * widthPerCol,
                            (i) * heightPerRow,
                            paint);
                }
            }
        }

        // Paint borders to the tetris blocks
        for (int i = 1; i < ROWS - 1; ++i) {
            for (int j = 1; j < COLUMNS - 1; ++j) {
                if (gameMatrix[i][j].getState() == 1) {
                    paint.setColor(Color.BLACK);
                    canvas.drawLine((j - 1) * widthPerCol,
                            (i - 1) * heightPerRow,
                            (j - 1) * widthPerCol,
                            (i) * heightPerRow,
                            paint);
                    canvas.drawLine((j - 1) * widthPerCol,
                            (i -1) * heightPerRow,
                            (j ) * widthPerCol,
                            (i - 1) * heightPerRow,
                            paint);
                    canvas.drawLine((j ) * widthPerCol,
                            (i - 1) * heightPerRow,
                            (j ) * widthPerCol,
                            (i ) * heightPerRow,
                            paint);
                    canvas.drawLine((j - 1) * widthPerCol,
                            (i ) * heightPerRow,
                            (j ) * widthPerCol,
                            (i) * heightPerRow,
                            paint);
                }
            }
        }
        if (!gameInProgress ) {
            dialogRunning = true;
            createLoseDialog();
        }
        // Display the current painting
        linearLayout.setBackground(new BitmapDrawable(this.getResources(), bitmap));

        // Update the score textview
        TextView textView =  findViewById(R.id.game_score_textview);
        textView.setText("Score: " + score);
    }


    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if (!gameInProgress) {
                return;
            }
            if (gamePaused) {
                PaintMatrix();
                if (fastSpeedState)
                    handler.postDelayed(this, SPEED_FAST);
                else
                    handler.postDelayed(this, SPEED_NORMAL);
                return;
            }

            boolean moved = MoveShape(DOWN_DIRECTION, currentShape);
            Log.d("Currentshape.x", String.valueOf(currentShape.x));
            Log.d("Currentshape.y", String.valueOf(currentShape.y));

            if (!moved) { // current shape is down
                // mark the new cells as fixed so they won't be affected by eventual bugs
                int i, j, startX, startY;
                for (i = 1, startX = currentShape.x; i <= 4; ++i, ++startX) {
                    for (j = 1, startY = currentShape.y; j <= 4; ++j, ++startY) {
                        if (currentShape.mat[i][j].getState() == 1) {
                            gameMatrix[startX][startY].setBehavior(BoardCell.BEHAVIOR_IS_FIXED);
                            currentShape.mat[i][j].setBehavior(BoardCell.BEHAVIOR_IS_FIXED);
                        }
                    }
                }
                    currentShapeAlive = false;
                    Check(); // check for complete rows
                    currentShapeAlive = CreateShape(); // create another shape
                    if (!currentShapeAlive ) // if not possible, then game over
                    {
                        gameInProgress = false;
                        PaintMatrix();
                        return;
                    }
                    PaintMatrix();
                    if (fastSpeedState) {
                        ChangeFastSpeedState(false);
                        return;
                    }

            } else
                PaintMatrix();

            if (fastSpeedState)
                handler.postDelayed(this, SPEED_FAST);
            else
                handler.postDelayed(this, SPEED_NORMAL);
        }
    };

    void ChangeFastSpeedState(boolean mFastSpeedState) {
        // fastSpeedState = false for normal speed
        // fastSpeedState = true for fast speed
        handler.removeCallbacks(runnable);
        fastSpeedState = mFastSpeedState;
            if (fastSpeedState)
                handler.postDelayed(runnable, SPEED_FAST);
            else
                handler.postDelayed(runnable, SPEED_NORMAL);
        }

    private void InsertScore() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getUid() != null) {
            FirebaseFirestore.getInstance().collection("user")
                    .document(mAuth.getUid())
                    .get().addOnSuccessListener(
                    task -> {
                        int fbScore = (int) (long) task.getData().get("Score");

                        if (score > fbScore) {
                            scoreMap = (HashMap<String, Object>) task.getData();
                            scoreMap.put("Score", score);
                            FirebaseFirestore.getInstance().collection("user").
                                    document(mAuth.getUid()).
                                    set(scoreMap);
                        }
                    }
            );
        }
    }


    void GameInit() {

        // Create the game board (backend)
        gameMatrix = new BoardCell[ROWS][COLUMNS];
        for (int i = 0; i < ROWS; ++i) {
            for (int j = 0; j < COLUMNS; ++j) {
                gameMatrix[i][j] = new BoardCell();
            }
        }

        // Marking the first and the last lines and columns as occupied.
        for (int j = 0; j < COLUMNS; ++j) {
                gameMatrix[0][j] = new BoardCell(1, Color.BLACK);
                gameMatrix[ROWS-1][j] = new BoardCell(1, Color.BLACK);
        }
        for (int i = 0; i < ROWS; ++i) {
                gameMatrix[i][0] = new BoardCell(1, Color.BLACK);
                gameMatrix[i][COLUMNS-1] = new BoardCell(1, Color.BLACK);
        }


        // Create an initial tetris block
        currentShapeAlive = CreateShape();

        // Start the game
        gameInProgress = true;
        gamePaused = false;

        // Paint the initial matrix (frontend)
        PaintMatrix();

        ChangeFastSpeedState(false);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (!gameInProgress) {
            finish();
            return true;
        }
        if (gamePaused || !currentShapeAlive)
            return false;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float width = size.x;
        float x = e.getX();
        if (x <= width / 2.0) {
            // rotate left
            RotateLeft(currentShape);
            PaintMatrix();
        } else {
            // rotate right
            RotateRight(currentShape);
            PaintMatrix();
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!gameInProgress)
            return false;
        try {
            float x1 = e1.getX();
            float y1 = e1.getY();

            float x2 = e2.getX();
            float y2 = e2.getY();

            double angle = getAngle(x1, y1, x2, y2);

            if (inRange(angle, 45, 135)) {
                // UP
                // pause
                gamePaused = true;
                createPauseDialog();
            } else if (inRange(angle, 0, 45) || inRange(angle, 315, 360)) {
                // RIGHT
                // move right
                if (gamePaused || !currentShapeAlive)
                    return false;
                ChangeFastSpeedState(false);
                MoveShape(RIGHT_DIRECTION, currentShape);
                PaintMatrix();
            } else if (inRange(angle, 225, 315)) {
                // DOWN
                // move fast down
                if (gamePaused || !currentShapeAlive)
                    return false;
                ChangeFastSpeedState(true);
            } else {
                // LEFT
                // move left
                if (gamePaused || !currentShapeAlive)
                    return false;
                ChangeFastSpeedState(false);
                MoveShape(LEFT_DIRECTION, currentShape);
                PaintMatrix();
            }

        } catch (Exception e) {
            // nothing
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public double getAngle(float x1, float y1, float x2, float y2) {

        double rad = Math.atan2(y1 - y2, x2 - x1) + Math.PI;
        return (rad * 180 / Math.PI + 180) % 360;
    }

    private boolean inRange(double angle, float init, float end) {
        return (angle >= init) && (angle < end);
    }


    public void createPauseDialog() {
        Dialog dialog;
        Button leaveBtn, cancelBtn;
        dialog = new Dialog(GameActivity.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.pausedialog);
        leaveBtn = dialog.findViewById(R.id.leaveGame);
        cancelBtn = dialog.findViewById(R.id.cancel);
        dialog.show();

        leaveBtn.setOnClickListener(v -> {
            InsertScore();
            dialog.dismiss();
            Toast.makeText(GameActivity.this, "Left game", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        cancelBtn.setOnClickListener(v -> {
            dialog.cancel();
            gamePaused = false;
            dialogRunning = false;
        });
    }
    public void createLoseDialog() {
        Dialog dialog;
        Button mainMenu,restart;
        dialog = new Dialog(GameActivity.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.losedialog);
        mainMenu = dialog.findViewById(R.id.mainmenu);
        restart = dialog.findViewById(R.id.restart);
        dialog.show();
        InsertScore();
        mainMenu.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(GameActivity.this, "Left to Main Menu", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        restart.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(GameActivity.this, "Game Restarted", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GameActivity.this, GameActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
