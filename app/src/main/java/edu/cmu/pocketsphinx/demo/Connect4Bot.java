package edu.cmu.pocketsphinx.demo;

import android.util.Log;

/**
 * This is the bot class for connect 4. It finds the next best move given a board.
 *
 * Please excuse any bad style or code in this class. I wrote this class in C++ a while ago and
 * didn't want to play around with the code too much as it might affect the logic. I tried to make it
 * more presentable however.
 */
public class Connect4Bot {

    private static final int BOARD_X = Connect4Model.BOARD_WIDTH;
    private static final int BOARD_Y = Connect4Model.BOARD_HEIGHT;

    //These are swapped as the bot assumes it is player 1, but it is player 2 in the game
    private static final Connect4Model.Color RED = Connect4Model.Color.YELLOW;
    private static final Connect4Model.Color YELLOW = Connect4Model.Color.RED;
    private static final Connect4Model.Color EMPTY = Connect4Model.Color.EMPTY;

    private static final boolean BEST_MOVE_RED_ONLY = true;
    private static final boolean BEST_MOVE_YELLOW_ONLY = true;
    private static final boolean YELLOW_WEIGHTING = true;

    //Various tuning factors
    private static final int RATING_FOUR_IN_A_ROW = 1;
    private static final int RATING_FOUR_FACTOR = 1000;
    public static final float RATING_THREE_IN_A_ROW = 0.1f;
    public static final float RATING_TWO_IN_A_ROW = 0.015f;
    private static final int VERY_NEGATIVE = -1000000;

    //These are constants affecting how far ahead to look when calculating a move.
    private static final int MIN_DEPTH = 3;
    private static final int DEPTH_FACTOR = 6;
    private static final int MAX_TIME = 1000;

    public static final String NAME = "Bot";

    private int mDepth;

    /**
     * Gets the next move as judged by the bot.
     * @param board board being played on
     * @return the column number of the move from 0 - BOARD_X
     */
    public int getNextMove(Connect4Model.Color board[][]) {

        long t = System.currentTimeMillis();
        mDepth = MIN_DEPTH;
        int move = 0;


        float ratings[] = new float[7];

        while ((System.currentTimeMillis() - t) < (MAX_TIME/DEPTH_FACTOR)) {
            move = getNextMove(board, ratings);
            mDepth++;
            if (mDepth > countBoardSpacesLeft(board)) {
                break;
            }
        }

        Log.d("Connect4Bot", "Found move with depth " + (mDepth -1)
                + " and time taken: " + (System.currentTimeMillis() - t));

        Log.d("Connect4Bot", "Ratings:");
        for (int k = 0; k < BOARD_X; k++) {
            Log.d("Connect4Bot", Float.toString(ratings[k]));
        }

        return move;
    }

    /**
     * Counts the number of spaces in the board
     * @param board board being played on
     * @return number of spaces
     */
    private int countBoardSpacesLeft(Connect4Model.Color board[][])
    {
        int count = 0;
        for (int i = 0; i < BOARD_X; i++)
            for (int j = 0; j < BOARD_Y; j++)
                if (board[i][j] == EMPTY)
                    count++;
        return count;
    }

    /**
     * Checks a strip oriented in the (dirx, diry) direction to return a rating where
     * x, y is the spot directly after the strip
     * @param board board being played on
     * @param precolor color of previous spot
     * @param count number of balls of color precolor on the strip
     * @param x X coordinate of spot
     * @param y Y coordinate of spot
     * @param dirx X coordinate of direction vector
     * @param diry Y coordinate of direction vector
     * @return
     */
    private float checkStrip(Connect4Model.Color board[][], Connect4Model.Color precolor,
                             int count, int x, int y, int dirx, int diry) {

        float rating = 0;

        if (count >= 3) {
            if (precolor == RED)
                return RATING_FOUR_FACTOR * RATING_FOUR_IN_A_ROW;
            else
                return -RATING_FOUR_FACTOR * RATING_FOUR_IN_A_ROW;
        }
        else if (count >= 2) {

            int n = 0;
            if (isWithinBounds(board, x - (4 * dirx), y - (4 * diry))) {
                if (board[x - 4 * dirx][y - 4 * diry] == EMPTY)
                    n++;
            }
            if (isWithinBounds(board, x, y) && board[x][y] == EMPTY) {
                n++;
            }
            if (precolor == RED) {
                rating += n * RATING_THREE_IN_A_ROW;
            }
            else{
                rating -= n * RATING_THREE_IN_A_ROW;
            }
        }
        else if (count >= 1) {

            int n = 0;
            if (isWithinBounds(board, x - (3 * dirx), y - (3 * diry))) {
                if (board[x - 3 * dirx][y - 3 * diry] == EMPTY) {
                    n++;
                    if (isWithinBounds(board, x - (4 * dirx), y - (4 * diry))) {
                        if (board[x - 4 * dirx][y - 4 * diry] == precolor) {
                            float r = RATING_THREE_IN_A_ROW;
                            if (isWithinBounds(board, x - (5 * dirx), y - (5 * diry)))
                                if (board[x - 5 * dirx][y - 5 * diry] == precolor) {
                                    r = r / 2;
                                    if (isWithinBounds(board, x - (6 * dirx), y - (6 * diry)))
                                        if (board[x - 6 * dirx][y - 6 * diry] == precolor)
                                            r = 0;
                                }
                            n = 0;
                            if (precolor == RED)
                                rating += r;
                            else
                                rating -= r;
                        }
                        else if (board[x - 4 * dirx][y - 4 * diry] == EMPTY)
                            n++;
                    }
                }
            }
            if (isWithinBounds(board, x, y) && board[x][y] == EMPTY) {
                n++;
                if (isWithinBounds(board, x + dirx, y + diry)) {
                    if (board[x + dirx][y + diry] == precolor) {
                        float r = RATING_THREE_IN_A_ROW;
                        if (isWithinBounds(board, x + 2 * dirx, y + 2 * diry))
                            if (board[x + 2 * dirx][y + 2 * diry] == precolor) {
                                r = r / 2;
                                if (isWithinBounds(board, x + 3 * dirx, y + 3 * diry))
                                    if (board[x + 3 * dirx][y + 3 * diry] == precolor)
                                        r = 0;
                            }
                        n = 0;
                        if (precolor == RED)
                            rating += r;
                        else
                            rating -= r;
                    }
                    else if (board[x + dirx][y + diry] == EMPTY)
                        n++;
                }
            }
            if (precolor == RED)
                rating += (n / 2) * RATING_TWO_IN_A_ROW;
            else
                rating -= (n / 2) * RATING_TWO_IN_A_ROW;
        }

        return rating;
    }

    /**
     * Gives a rating for a given board
     * @param board board being played on
     * @return
     */
    public float rateBoard(Connect4Model.Color board[][])
    {
        float rating = 0;
        int i, j, count;
        Connect4Model.Color precolor;

        //Check horizontally
        precolor = EMPTY; count = 0;
        for (i = 0; i < BOARD_Y; i++)    {
            for (j = 0; j < BOARD_X; j++)    {

                if (board[j][i] == precolor && board[j][i] != EMPTY) {
                    count++;
                }
                else    {
                    float temp = checkStrip(board, precolor, count, j, i, 1, 0);
                    if (Math.abs(temp) >= RATING_FOUR_FACTOR * RATING_FOUR_IN_A_ROW) {
                        return temp;
                    }
                    rating += temp;

                    count = 0;
                }
                precolor = board[j][i];
            }
            float temp = checkStrip(board, precolor, count, j, i, 1, 0);
            if (Math.abs(temp) >= RATING_FOUR_FACTOR * RATING_FOUR_IN_A_ROW) {
                return temp;
            }
            rating += temp;

            count = 0;
            precolor = EMPTY;
        }


        //Check vertically
        precolor = EMPTY; count = 0;
        for (j = 0; j < BOARD_X; j++)    {
            for (i = 0; i < BOARD_Y; i++)    {

                if (board[j][i] == precolor && board[j][i] != EMPTY)  {
                    count++;
                }
                else    {
                    float temp = checkStrip(board, precolor, count, j, i, 0, 1);
                    if (Math.abs(temp) >= RATING_FOUR_FACTOR * RATING_FOUR_IN_A_ROW) {
                        return temp;
                    }
                    rating += temp;
                    count = 0;
                }
                precolor = board[j][i];
            }
            float temp = checkStrip(board, precolor, count, j, i, 0, 1);
            if (Math.abs(temp) >= RATING_FOUR_FACTOR * RATING_FOUR_IN_A_ROW) {
                return temp;
            }
            rating += temp;
            count = 0;
            precolor = EMPTY;
        }


        //Check diagonally to the right
        precolor = EMPTY; count = 0;
        for (i = BOARD_Y-1; i >= 0; i--)    {
            for (j = 0; (j < BOARD_X && (i+j) < BOARD_Y); j++)    {
                if (board[j][i+j] == precolor && board[j][i+j] != EMPTY) {
                    count++;
                }
                else    {
                    float temp = checkStrip(board, precolor, count, j, i+j, 1, 1);
                    if (Math.abs(temp) >= RATING_FOUR_FACTOR * RATING_FOUR_IN_A_ROW) {
                        return temp;
                    }
                    rating += temp;
                    count = 0;
                }
                precolor = board[j][i+j];
            }
            float temp = checkStrip(board, precolor, count, j, i+j, 1, 1);
            if (Math.abs(temp) >= RATING_FOUR_FACTOR * RATING_FOUR_IN_A_ROW) {
                return temp;
            }
            rating += temp;
            count = 0;
            precolor = EMPTY;
        }

        precolor = EMPTY; count = 0;
        for (j = 1; j < BOARD_X; j++)    {
            for (i = 0; (i < BOARD_Y && (i+j) < BOARD_X); i++)    {
                if (board[j+i][i] == precolor && board[j+i][i] != EMPTY) {
                    count++;
                }
                else    {
                    float temp = checkStrip(board, precolor, count, j+i, i, 1, 1);
                    if (Math.abs(temp) >= RATING_FOUR_FACTOR * RATING_FOUR_IN_A_ROW) {
                        return temp;
                    }
                    rating += temp;
                    count = 0;
                }
                precolor = board[j+i][i];
            }
            float temp = checkStrip(board, precolor, count, j+i, i, 1, 1);
            if (Math.abs(temp) >= RATING_FOUR_FACTOR * RATING_FOUR_IN_A_ROW) {
                return temp;
            }
            rating += temp;
            count = 0;
            precolor = EMPTY;
        }


        //Check diagonally to the left
        precolor = EMPTY; count = 0;
        for (i = BOARD_Y-1; i >= 0; i--)    {
            for (j = 0; (j < BOARD_X && (i+j) < BOARD_Y); j++)    {
                if (board[BOARD_X-1-j][i+j] == precolor && board[BOARD_X-1-j][i+j] != EMPTY) {
                    count++;
                }
                else    {
                    float temp = checkStrip(board, precolor, count, BOARD_X-1-j, i+j, -1, 1);
                    if (Math.abs(temp) >= RATING_FOUR_FACTOR * RATING_FOUR_IN_A_ROW) {
                        return temp;
                    }
                    rating += temp;
                    count = 0;
                }
                precolor = board[BOARD_X-1-j][i+j];
            }
            float temp = checkStrip(board, precolor, count, BOARD_X-1-j, i+j, -1, 1);
            if (Math.abs(temp) >= RATING_FOUR_FACTOR * RATING_FOUR_IN_A_ROW) {
                return temp;
            }
            rating += temp;
            count = 0;
            precolor = EMPTY;
        }

        precolor = EMPTY; count = 0;
        for (j = BOARD_X-2; j >= 0; j--)    {
            for (i = 0; (i < BOARD_Y && (j-i) >= 0); i++)    {
                if (board[j-i][i] == precolor && board[j-i][i] != EMPTY) {
                    count++;
                }
                else    {
                    float temp = checkStrip(board, precolor, count, j-i, i, -1, 1);
                    if (Math.abs(temp) >= RATING_FOUR_FACTOR * RATING_FOUR_IN_A_ROW) {
                        return temp;
                    }
                    rating += temp;
                    count = 0;
                }
                precolor = board[j-i][i];
            }
            float temp = checkStrip(board, precolor, count, j-i, i, -1, 1);
            if (Math.abs(temp) >= RATING_FOUR_FACTOR * RATING_FOUR_IN_A_ROW) {
                return temp;
            }
            rating += temp;
            count = 0;
            precolor = EMPTY;
        }

        return rating;

    }

    private float rateWithDepth(Connect4Model.Color board[][], Connect4Model.Color player) {
        return rateWithDepth(board, player, 1);
    }

    /**
     * Rates a board by looking into all possible moves at a given depth.
     * @param board board being played on
     * @param player current player's turn
     * @param depth how many moves to look ahead
     * @return
     */
    private float rateWithDepth(Connect4Model.Color board[][], Connect4Model.Color player, int depth)
    {
        float rating = 0;
        int y;
        float temp = rateBoard(board);

        //NOTE this is to check for a winner, if rate function changes then CHeckForFours function should replace this
        if (temp >= RATING_FOUR_IN_A_ROW*RATING_FOUR_FACTOR) {
            return RATING_FOUR_IN_A_ROW*RATING_FOUR_FACTOR;
        }
        else if (temp <= (-RATING_FOUR_IN_A_ROW*RATING_FOUR_FACTOR))    {

            return -RATING_FOUR_IN_A_ROW*RATING_FOUR_FACTOR;

        }

        if (depth >= mDepth)    {
            return temp;
        }

        int count = 0;
        boolean redCanWin = false; //This is if all yellow moves result in a win
        float max_rating = 0;
        float ratings8[] = new float[BOARD_X];
        if (player == RED)
            max_rating = VERY_NEGATIVE;
        else if (player == YELLOW)
            max_rating = -VERY_NEGATIVE;
        for (int i = 0; i < BOARD_X; i++)    {
            y = addBall(board, i, player);

            //column is full
            if (y == -1) {
                continue;
            }

            Connect4Model.Color otherPlayer = player == RED ? YELLOW : RED;
            temp = rateWithDepth(board, otherPlayer, depth+1);
            if (temp >= RATING_FOUR_IN_A_ROW*RATING_FOUR_FACTOR)    {
                if (player == RED)    {

                    board[i][y] = EMPTY;
                    return RATING_FOUR_IN_A_ROW*RATING_FOUR_FACTOR;
                }
                else if (player == YELLOW)    {

                    redCanWin = true;
                    board[i][y] = EMPTY;
                    continue;
                }
            }
            else if (temp <= (-RATING_FOUR_IN_A_ROW*RATING_FOUR_FACTOR))    {
                if (player == YELLOW)    {
                    board[i][y] = EMPTY;
                    return -RATING_FOUR_IN_A_ROW;
                }
            }

            if (player == RED && BEST_MOVE_RED_ONLY)    {
                if (temp > max_rating)    {
                    max_rating = temp;
                }
            }
            else if (player == YELLOW && BEST_MOVE_YELLOW_ONLY)    {
                if (temp < max_rating)    {
                    max_rating = temp;
                }
            }
            else    {
                if (YELLOW_WEIGHTING)
                    ratings8[count] = temp;
                else
                    rating += temp;
            }
            board[i][y] = EMPTY; //Remove Ball
            count++;
        }


        if (count > 0)    {
            if (player == RED && BEST_MOVE_RED_ONLY)    {
                return max_rating;
            }
            else if (player == YELLOW && BEST_MOVE_YELLOW_ONLY)    {
                return max_rating;
            }
            else    {
                if (YELLOW_WEIGHTING)    {
                    float total = 0;
                    float min = -VERY_NEGATIVE;
                    int min_i = 0, base = 0;
                    for (int n = count; n > 0; n--)    {

                        for (int i = 0; i < n; i++) {
                            if (ratings8[i] < min) {
                                min = ratings8[i];
                                min_i = i;
                            }
                        }
                        base += (n);
                        total += min*(n);
                        for (int i = min_i; i < n-1; i++)
                            ratings8[i] = ratings8[i+1];
                        min = -VERY_NEGATIVE;
                    }
                    return total/((float)base);
                }
                else
                    return rating/((float)count);
            }
        }
        else    {
            if (redCanWin)    {
                return RATING_FOUR_IN_A_ROW;
            }
            return 0;
        }
    }

    /**
     * Gets the next best move and returns the ratings for each move
     * @param arg_board board being played on
     * @param ratings array to store the ratings
     * @return
     */
    private int getNextMove(Connect4Model.Color arg_board[][], float ratings[])
    {
        Connect4Model.Color board[][] = new Connect4Model.Color[BOARD_X][BOARD_Y];
        copyBoard(arg_board, board);
        float rating = 0, max_rating = VERY_NEGATIVE;
        int max_rating_index = 0;
        int y;

        int count = 0;
        for (int i = 0; i < BOARD_X; i++)    {

            y = addBall(board, i, RED);
            if (y == -1)    {//Full
                if (ratings != null)
                    ratings[i] = VERY_NEGATIVE;
                continue;
            }

            rating = rateWithDepth(board, YELLOW);


            board[i][y] = EMPTY; //Remove Ball
            if (rating > max_rating)    {
                max_rating = rating;
                max_rating_index = i;
            }
            count++;
            if (ratings != null)
                ratings[i] = rating;

        }



        if (count > 0)
            return max_rating_index;
        else
            return -1; //No move
    }

    private boolean isWithinBounds(Connect4Model.Color board[][], int x, int y) {
        return (x >= 0 && y >= 0 && x < BOARD_X && y < BOARD_Y);
    }

    private void copyBoard(Connect4Model.Color source[][], Connect4Model.Color dest[][])
    {
        for (int i = 0; i < BOARD_X; i++)
            for (int j = 0; j < BOARD_Y; j++)
                dest[i][j] = source[i][j];
    }

    private int addBall(Connect4Model.Color board[][], int column, Connect4Model.Color color)//first column is 0
    {
        int i;
        for (i = 0; i < BOARD_Y; i++)
            if (board[column][i] == EMPTY)   {
                board[column][i] = color;
                return i;
            }
        return -1; //Error
    }



}
