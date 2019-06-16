package edu.cmu.pocketsphinx.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * This is the view class which draws the board, score and player names.
 * It receives touch events and notifies the controller
 */

public class Connect4View extends View {

    private static final float BALL_PADDING_RATIO = 0.1f;
    private static final int TEXT_PADDING = 30;
    private static final int TURN_LINE_LENGTH = 100;
    private static final int TURN_LINE_HEIGHT = 23;

    private static final int PLAY_AGAIN_BUTTON_WIDTH  = 250; //212
    private static final int PLAY_AGAIN_BUTTON_HEIGHT = 80; //58

    private static final int BACKGROUND_COLOR = 0xFF3399FF;
    private static final int LAST_BALL_WIDTH = 2;

    private static final int NAME_TEXT_SIZE = 60;
    private static final int SCORE_TEXT_SIZE = 125;
    private static final int WIN_STROKE_WIDTH = 15;

    private Connect4Model mModel;
    private Connect4Controller mController;

    private Paint mBoardPaint;
    private Paint mNameTextPaint;
    private Paint mScoreTextPaint;
    private Paint mLineNumberPaint;
    private Paint mTurnLinePaint;
    private Paint mWinPaint;

    private float mGridWidth;
    private float mGridHeight;

    private float mPlayer1PosX;
    private float mPlayer2PosX;
    private float mCenterPos;

    private Rect mBoardRect;
    private Rect mPlayAgainRect;
    private Rect mUndoRect;

    public int colorArray[]={
            Color.WHITE,
            Color.RED,
            Color.YELLOW,
    };

    public int mCircleLineStrokeWidth = 15;

    public Connect4View(Context context, @Nullable AttributeSet attrs) {

        super(context, attrs);

        setBackgroundColor(BACKGROUND_COLOR);

        //Sets up the paint specs
        mBoardPaint = new Paint();
        mBoardPaint.setAntiAlias(true);
        mBoardPaint.setColor(Color.WHITE);
        mBoardPaint.setStyle(Paint.Style.FILL);
        mBoardPaint.setStrokeJoin(Paint.Join.ROUND);
        mBoardPaint.setStrokeWidth(LAST_BALL_WIDTH);

        mNameTextPaint = new Paint();
        mNameTextPaint.setColor(Color.WHITE);
        mNameTextPaint.setTextSize(NAME_TEXT_SIZE);
        mNameTextPaint.setTextAlign(Paint.Align.CENTER);

        mScoreTextPaint = new Paint();
        mScoreTextPaint.setColor(Color.WHITE);
        mScoreTextPaint.setTextSize(SCORE_TEXT_SIZE);
        mScoreTextPaint.setTextAlign(Paint.Align.CENTER);

        mLineNumberPaint = new Paint();
        mLineNumberPaint.setColor(Color.BLACK);
        mLineNumberPaint.setTextSize(SCORE_TEXT_SIZE);
        mLineNumberPaint.setTextAlign(Paint.Align.CENTER);

        mTurnLinePaint = new Paint();

        mWinPaint = new Paint();
        mWinPaint.setColor(Color.GREEN);
        mWinPaint.setStrokeJoin(Paint.Join.ROUND);
        mWinPaint.setStrokeWidth(WIN_STROKE_WIDTH);
    }

    public void setModel(Connect4Model model) {
        mModel = model;
    }

    public void setController(Connect4Controller controller) {
        mController = controller;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);

        mPlayer1PosX = w/4;
        mCenterPos = w/2;
        mPlayer2PosX = (3*w)/4;

        mGridWidth = w/ Connect4Model.BOARD_WIDTH;
        mGridHeight = mGridWidth;

        mBoardRect = new Rect(0, (int)(h - mGridHeight *Connect4Model.BOARD_HEIGHT), w, h);

        mPlayAgainRect = new Rect( (int) mCenterPos - PLAY_AGAIN_BUTTON_WIDTH/2,
                (int)( mBoardRect.bottom - mGridHeight *(Connect4Model.BOARD_HEIGHT+1) - PLAY_AGAIN_BUTTON_HEIGHT - TEXT_PADDING ),
                (int) mCenterPos + PLAY_AGAIN_BUTTON_WIDTH/2,
                (int)( mBoardRect.bottom - mGridHeight *(Connect4Model.BOARD_HEIGHT+1)) - TEXT_PADDING);

        mUndoRect = new Rect( (int) mCenterPos + PLAY_AGAIN_BUTTON_WIDTH/2,
                (int)( mBoardRect.bottom - mGridHeight *(Connect4Model.BOARD_HEIGHT+1) - PLAY_AGAIN_BUTTON_HEIGHT - TEXT_PADDING ),
                (int) mCenterPos + PLAY_AGAIN_BUTTON_WIDTH,
                (int)( mBoardRect.bottom - mGridHeight *(Connect4Model.BOARD_HEIGHT+1)) - TEXT_PADDING);

        mUndoRect = new Rect( (int) w - PLAY_AGAIN_BUTTON_WIDTH *5 / 4,
                (int)( mBoardRect.bottom - mGridHeight *(Connect4Model.BOARD_HEIGHT+1) - PLAY_AGAIN_BUTTON_HEIGHT - TEXT_PADDING ),
                (int) w- PLAY_AGAIN_BUTTON_WIDTH *1 / 4,
                (int)( mBoardRect.bottom - mGridHeight *(Connect4Model.BOARD_HEIGHT+1)) - TEXT_PADDING);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBoard(canvas);
        drawScoreText(canvas);
        drawWinningMove(canvas);
        drawPlayAgainButton(canvas);
        drawLineNumbers(canvas);
        drawUndoButton(canvas);
    }

    /**
     * Draws the board with balls on the canvas
     * @param canvas
     */
    private void drawBoard(Canvas canvas) {
        Connect4Model.Color board[][] = mModel.getBoard();

        for (int i = 0; i < Connect4Model.BOARD_WIDTH; i++) {
            for (int j = 0; j < Connect4Model.BOARD_HEIGHT; j++) {

//                if (board[i][j] == Connect4Model.Color.EMPTY) {
//                    mBoardPaint.setColor(Color.WHITE);
//                }
//                else if (board[i][j] == Connect4Model.Color.RED) {
//                    mBoardPaint.setColor(Color.RED);
//                }
//                else if (board[i][j] == Connect4Model.Color.YELLOW) {
//                    mBoardPaint.setColor(Color.YELLOW);
//                }

                mBoardPaint.setColor( colorArray[ board[i][j].ordinal()]);

                canvas.drawOval(mBoardRect.left + (mGridWidth *(i+ BALL_PADDING_RATIO)), mBoardRect.bottom - (mGridHeight *(j+1- BALL_PADDING_RATIO)),
                        mBoardRect.left + (mGridWidth *(i+1- BALL_PADDING_RATIO)), mBoardRect.bottom - (mGridHeight *(j+ BALL_PADDING_RATIO)), mBoardPaint);

                //Draws a outline on the last placed ball
                if (mModel.getLastPlacedBall() != null && mModel.getLastPlacedBall().equals(i, j)) {

                    mBoardPaint.setStyle(Paint.Style.STROKE);
                    mBoardPaint.setColor(Color.GREEN);
                    mBoardPaint.setStrokeWidth( mCircleLineStrokeWidth);

                    canvas.drawOval(
                            mBoardRect.left + (mGridWidth *(i+ BALL_PADDING_RATIO)) + mCircleLineStrokeWidth/2,
                            mBoardRect.bottom - (mGridHeight *(j+1- BALL_PADDING_RATIO)) + mCircleLineStrokeWidth/2,
                            mBoardRect.left + (mGridWidth *(i+1- BALL_PADDING_RATIO)) - mCircleLineStrokeWidth/2,
                            mBoardRect.bottom - (mGridHeight *(j+ BALL_PADDING_RATIO)) - mCircleLineStrokeWidth/2,
                            mBoardPaint);
                    mBoardPaint.setStyle(Paint.Style.FILL);
                }
            }
        }

    }

    /**
     * Draws the winning move on the canvas if the game is over
     * @param canvas
     */
    private void drawWinningMove(Canvas canvas) {

        if (mModel.hasWinner() == true) {
            Connect4Model.WinningMove winningMove = mModel.getWinningMove();

            if (winningMove != null) {
                //Draws a line through the balls of the winning move
                canvas.drawLine(mBoardRect.left + mGridWidth * (winningMove.startPos.x + 0.5f),
                        mBoardRect.bottom - mGridWidth * (winningMove.startPos.y + 0.5f),
                        mBoardRect.left + mGridWidth * (winningMove.endPos.x + 0.5f),
                        mBoardRect.bottom - mGridWidth * (winningMove.endPos.y + 0.5f),
                        mWinPaint);

                //Highlights the connected circles
                int gap_X = ( winningMove.endPos.x - winningMove.startPos.x) / 3;
                int gap_Y = ( winningMove.endPos.y - winningMove.startPos.y) / 3;

                for (int i = 0; i < 4; i++) {
                    int x = winningMove.startPos.x + i*gap_X;
                    int y = winningMove.startPos.y + i*gap_Y;

                    mBoardPaint.setStyle(Paint.Style.STROKE);
                    mBoardPaint.setColor(Color.GREEN);
                    mBoardPaint.setStrokeWidth( mCircleLineStrokeWidth);

                    canvas.drawOval(
                            mBoardRect.left + (mGridWidth *(x+ BALL_PADDING_RATIO)) + mCircleLineStrokeWidth/2,
                            mBoardRect.bottom - (mGridHeight *(y+1- BALL_PADDING_RATIO)) + mCircleLineStrokeWidth/2,
                            mBoardRect.left + (mGridWidth *(x+1- BALL_PADDING_RATIO)) - mCircleLineStrokeWidth/2,
                            mBoardRect.bottom - (mGridHeight *(y+ BALL_PADDING_RATIO)) - mCircleLineStrokeWidth/2,
                            mBoardPaint);
                    mBoardPaint.setStyle(Paint.Style.FILL);
                }
            }
        }
    }

    /**
     * Draws the score on the canvas
     * @param canvas
     */
    private void drawScoreText(Canvas canvas) {

        //Calculates the height of the text being drawn so the items can be organized
        int nameHeight = getTextRect(mModel.getPlayer1Name(), mNameTextPaint).height() + TEXT_PADDING;
        canvas.drawText(mModel.getPlayer1Name(), mPlayer1PosX, nameHeight, mNameTextPaint);
        canvas.drawText(mModel.getPlayer2Name(), mPlayer2PosX, nameHeight, mNameTextPaint);

        int scoreHeight = nameHeight + getTextRect("0", mScoreTextPaint).height() + TEXT_PADDING;
        canvas.drawText(Integer.toString(mModel.getPlayer1Score()), mPlayer1PosX, scoreHeight, mScoreTextPaint);
        canvas.drawText(Integer.toString(mModel.getPlayer2Score()), mPlayer2PosX, scoreHeight, mScoreTextPaint);
        canvas.drawText("-", mCenterPos, scoreHeight, mScoreTextPaint);

        //Draws a line under the player whose turn it is
        if (mModel.getPlayerTurn() == Connect4Model.PLAYER_1) {
            mTurnLinePaint.setColor(Color.RED);
            canvas.drawRect(mPlayer1PosX - TURN_LINE_LENGTH/2, scoreHeight + TEXT_PADDING, mPlayer1PosX + TURN_LINE_LENGTH/2,
                    scoreHeight + TEXT_PADDING + TURN_LINE_HEIGHT, mTurnLinePaint);
        }
        else {
            mTurnLinePaint.setColor(Color.YELLOW);
            canvas.drawRect(mPlayer2PosX - TURN_LINE_LENGTH/2, scoreHeight + TEXT_PADDING, mPlayer2PosX + TURN_LINE_LENGTH/2,
                    scoreHeight + TEXT_PADDING + TURN_LINE_HEIGHT, mTurnLinePaint);
        }

    }

    /**
     * Draws the line numbers above every column
     * @param canvas
     */
    private void drawLineNumbers(Canvas canvas) {
        for (int i = 0; i < 7; i++) {
            canvas.drawText(Integer.toString(i+1),
                    (int) mBoardRect.left + (mGridWidth * ( i + 0.5f))
                    , (int) (mBoardRect.bottom - ( mGridHeight * Connect4Model.BOARD_HEIGHT)-TEXT_PADDING),
                    mLineNumberPaint);
        }
    }

    /**
     * Draws the Play Again button if the game is over
     * @param canvas
     */
    private void drawPlayAgainButton(Canvas canvas) {

        if (mModel.hasWinner()) {
            Drawable buttonImage = getResources().getDrawable(R.drawable.play_again_button, null);
            buttonImage.setBounds(mPlayAgainRect);
            buttonImage.draw(canvas);
        }
    }

    /**
     * Draws the Undo button
     * @param canvas
     */
    private void drawUndoButton(Canvas canvas) {

        Drawable buttonImage = getResources().getDrawable(R.drawable.undo_button, null);
        buttonImage.setBounds(mUndoRect);
        buttonImage.draw(canvas);
    }

    /**
     * Helper function which gets the bounding rect around a given text
     * @param text text that will be drawn
     * @param painter painter to draw the text
     * @return
     */
    private Rect getTextRect(String text, Paint painter) {

        Rect rect = new Rect();
        painter.getTextBounds(text, 0, text.length(), rect);
        return rect;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();
        Log.d("BLAH" , "FDSFSDF");

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE) {

        }
        else if (event.getAction() == MotionEvent.ACTION_UP) {

            //Checks if any of the presses were on the board or button

            //click board
            if (mBoardRect.contains((int)x, (int)y)) {

                int column = (int) (x / mGridWidth);
                if (column == Connect4Model.BOARD_WIDTH) {
                    column--;
                }
                //Notifies the controller of a click
                mController.userTouchedScreen(column);
            }

            //click undo button
            else if (mUndoRect.contains((int)x, (int)y)) {

                if(mModel.mFirstBall)
                {
                    Log.d("Damn","First");
                }
                else
                {
                    Log.d("Damn","NonFirst");
                }

                Log.d("Damn", "turn : "+mModel.turn);
                for(int i=0 ;i<mModel.turn ;i++)
                {
                    Log.d("Damn", mModel.ballRecord[i]+"");
                }
            }

            //click play again button
            else if (mModel.hasWinner() && mPlayAgainRect.contains((int)x, (int)y)) {

                mController.userClickedPlayAgain();
            }

            // redo : onDraw
            invalidate();
        }
        else {
            return false;
        }

        return true;
    }

    /**
     * This is called when the user says a number
     * And triigers the KWS to add a ball
     * @param column
     */
    public void onVoiceEvent(int column)
    {
        mController.userTouchedScreen(column);
    }

}
