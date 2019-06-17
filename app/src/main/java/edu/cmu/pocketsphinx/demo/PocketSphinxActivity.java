/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package edu.cmu.pocketsphinx.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;


import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Random;


import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

// ASUS
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.RobotAPI;

import org.json.JSONObject;


public class PocketSphinxActivity extends Activity implements RecognitionListener {

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";

    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "eight"; //hello mighty computer
    private static final String KEYPHRASE2 = "阿飽 阿飽";
//    zero/1e-5/
//    one/1.0/
//    two/1.0/
//    three/1e-5/
//    four/1e-5/
//    five/1e-8/
//    six/1e-5/

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private SpeechRecognizer recognizer;

    // ASUS
    public RobotAPI robotAPI;

    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);
        }
    };

    public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
        @Override
        public void onFinishRegister() {

        }

        @Override
        public void onVoiceDetect(JSONObject jsonObject) {

        }

        @Override
        public void onSpeakComplete(String s, String s1) {

        }

        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {

        }

        @Override
        public void onResult(JSONObject jsonObject) {

        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    private Connect4Model mModel;
    private Connect4View mView;
    private Connect4Controller mController;

    @Override
    public void onCreate(Bundle savedInstanceState) { //state //savedInstanceState
        super.onCreate(savedInstanceState);
        // Prepare the data for UI
        setContentView(R.layout.main);

        // ASUSr Zenbo Setting
        setZenbo();

        // Check if user has given permission to record audio
        checkPermission();

        // draw game table
        mModel = new Connect4Model();

        mView = (Connect4View) findViewById( R.id.connectFourView);
        mView.setModel(mModel);

        mController = new Connect4Controller(mModel, mView);

        mView.setController(mController);

        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
//        new SetupTask(this).execute();
        new SetupTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // 異步執行
    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<PocketSphinxActivity> activityReference;
        SetupTask(PocketSphinxActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }
        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {

            } else {
                activityReference.get().switchSearch(KWS_SEARCH);
            }
        }
    }

    // Require permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                new SetupTask(this).execute();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
        // +
        robotAPI.release();
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        Log.d("Spoken onPartialResult", text);
        if ((text.length() == KEYPHRASE.length()) || (text.length() == KEYPHRASE2.length())) {
            if (text.equals(KEYPHRASE) || text.equals(KEYPHRASE2)) { //KEYPHRASE

                //
                // Here
                //
                //            switchSearch(MENU_SEARCH); //ori

                recognizer.stop();

                Random ran = new Random();
                int fxck = ran.nextInt(6+1);
                Log.d("Spoken", "text.equals(KEYPHRASE)"+ Integer.toString(fxck));
                mView.onVoiceEvent(fxck);

                Intent callapp = getPackageManager().getLaunchIntentForPackage("com.example.abaohelp");
                if (callapp != null) {
                    startActivity(callapp);
                }
                else {
                    Log.d("Spoken", "App not found");
                }

            // 語音輸入衝突點
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                recognizer.startListening(KWS_SEARCH);
            }
//            else {
//                // show錯誤辨識結果
//                ((TextView) findViewById(R.id.result_text)).setText(text);
//            }

        }
        else {
            Log.d("Spoken","text.length not match keyphrase.");
        }

    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KEYPHRASE))
            switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        recognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KEYPHRASE))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them
        // assetsDir: /storage/emulated/0/Android/data/edu.cmu.sphinx.pocketsphinx/files/sync
//        Log.d("Model path", String.valueOf(assetsDir));

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm")) //en-us-ptm // zh_cn.cd_cont_5000
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict")) //tai_mix.ci_cont //cmudict-en-us.dict //zh3000
//                .setRawLogDir(assetsDir) // 調用可紀錄錄音的結果
                .getRecognizer();
        recognizer.addListener(this);

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
//        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        File menuGrammar = new File(assetsDir, "digits.list");
        recognizer.addKeywordSearch(KWS_SEARCH, menuGrammar);


        // Create grammar-based search for selection between demos
//        File menuGrammar = new File(assetsDir, "en-keyphrase.list");
//        recognizer.addGrammarSearch('keyphrase'', menuGrammar);

        // Create grammar-based search for digit recognition
        //File digitsGrammar = new File(assetsDir, "digits.gram");
        //recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);

        // Create language model search
//        File languageModel = new File(assetsDir, "zh3000-AB-uni-cut.lm");
//        recognizer.addNgramSearch(KWS_SEARCH, languageModel);

        // Phonetic search
//        File phoneticModel = new File(assetsDir, "zh3000.phone");
//        recognizer.addAllphoneSearch(KWS_SEARCH, phoneticModel);
    }

    @Override
    public void onError(Exception error) {
    }

    @Override
    public void onTimeout() {

        switchSearch(KWS_SEARCH);
    }

    // +
    @Override
    protected void onPause() {
        super.onPause();
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(robotListenCallback!= null)
            robotAPI.robot.registerListenCallback(robotListenCallback);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    private void setZenbo() {
        this.robotAPI = new RobotAPI(getApplicationContext(), robotCallback);
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        robotAPI.robot.setVoiceTrigger(false);
        robotAPI.robot.setPressOnHeadAction(false);
    }

    private void checkPermission(){
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
    }
}
