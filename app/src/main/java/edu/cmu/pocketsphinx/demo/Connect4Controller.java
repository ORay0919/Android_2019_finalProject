package edu.cmu.pocketsphinx.demo;

import android.os.AsyncTask;
import android.util.Log;

/**
 * This class is the controller of the view and the model. It is the brain of the game.
 * It talks over the network through the MultiplayerHandler class and updates the view whenever the model changes
 */

public class Connect4Controller {

    private static final String PLAYER_1_NAME_AGAINST_BOT = "You";

    private Connect4Model mModel;
    private Connect4View mView;

    private boolean mPlayerNumber;

    private Connect4Bot mBot;


    public Connect4Controller(Connect4Model model, Connect4View view ) {

        mModel = model;
        mView = view;
        mPlayerNumber = Connect4Model.PLAYER_1;

        mBot = new Connect4Bot();
        mModel.setPlayer1Name(PLAYER_1_NAME_AGAINST_BOT);
        mModel.setPlayer2Name(Connect4Bot.NAME);

    }


    /**
     * This function is called whenever the view has detected a touch event on the screen.
     * The controller decides what to do with the event, the view only reports it to the controller
     * @param column column number of the touch event
     */
    public void userTouchedScreen(int column) {

        if (mModel.hasWinner()) {
            return;
        }

        if (mModel.getPlayerTurn() == mPlayerNumber) {

            int row = mModel.addBall(column, Connect4Model.Color.RED);

            //Column is full
            if (row == -1) {
                return;
            }
            else{

                mModel.ballRecord[mModel.turn] = column;
                mModel.turn++;

                mModel.checkForWinner();

                if (!mModel.hasWinner()) {
                    new BotAsyncTask().execute(mModel.getBoard());
                    nextPlayerTurn();
                } else {
                    nextPlayerTurn();
                }

                if(mModel.mFirstBall)
                {
                    mModel.mFirstBall = false;
                }
            }
        }
        else  {
            mModel.checkForWinner();
        }

        mView.invalidate();
    }

    /**
     * This is called when the user clicks the Play Again button after a game is complete.
     * Starts a new game or tells the opponent if its online
     */
    public void userClickedPlayAgain() {

        mModel.reset();
        //Must start the bot if its his turn next game
        if (mModel.getPlayerTurn() != mPlayerNumber) {
            new BotAsyncTask().execute(mModel.getBoard());
            mModel.mFirstBall = false;
        }

        mView.invalidate();
    }

    /**
     * Ends the game. Communicates this to opponent if playing online
     */
    public void endGame() {

    }

    /**
     * Helper function which goes to next player turn and updates the view
     */
    private void nextPlayerTurn() {

        mModel.nextPlayerTurn();
        mView.invalidate();
    }


    /**
     * This class runs the bot on a separate thread so the main thread is not blocked
     * After the bot is run it updates the board
     */
    private class BotAsyncTask extends AsyncTask<Connect4Model.Color[][], Void, Integer> {

        @Override
        protected Integer doInBackground(Connect4Model.Color[][] ... params) {

            return mBot.getNextMove(params[0]);
        }

        @Override
        protected void onPostExecute(Integer move) {

            mModel.addBall(move, Connect4Model.Color.YELLOW);

            //record
            mModel.ballRecord[mModel.turn] = move *10;
            mModel.turn++;

            nextPlayerTurn();
            mModel.checkForWinner();
            mView.invalidate();
        }
    }

}
