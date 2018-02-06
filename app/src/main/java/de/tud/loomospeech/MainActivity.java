package de.tud.loomospeech;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.voice.Recognizer;
import com.segway.robot.sdk.voice.Speaker;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.audiodata.RawDataListener;
import com.segway.robot.sdk.voice.recognition.WakeupListener;
import com.segway.robot.sdk.voice.recognition.WakeupResult;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Recognizer mRecognizer;
    private Speaker mSpeaker;
    private WakeupListener mWakeupListener;
    private RawDataListener mRawDataListener;
    private AzureSpeechRecognition mSpeechRecognitionClient;

    MessageHandler mHandler;
    private Button btnAction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        btnAction = (Button) findViewById(R.id.btn_action);
        switchLanguage(Locale.getDefault());
        mHandler = new MessageHandler(this);
        mSpeechRecognitionClient = new AzureSpeechRecognition(this);

        initWakeUp();
        initRecognizer();
//        initSpeaker();

        initBtnAction();

    }

    @Override
    protected void onDestroy() {
        if (mRecognizer != null) mRecognizer = null;
        if (mSpeaker != null) mSpeaker = null;
        if (mSpeechRecognitionClient != null) mSpeechRecognitionClient = null;
        if (mHandler != null) mHandler = null;
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if (null != this.getCurrentFocus()) {
//            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
//        }
        return super.onTouchEvent(event);
    }

    protected void initRecognizer() {
        mRecognizer = Recognizer.getInstance();
        mRecognizer.bindService(MainActivity.this, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "Recognition service onBind");
                mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, getString(R.string.recognition_connected)));

//                showTip("start to wake up and recognize speech");

//                try {
//                    Slot moveSlot = new Slot("move");
//                    Slot toSlot = new Slot("to");
//                    Slot orientationSlot = new Slot("orientation");
//                    List<Slot> controlSlotList = new LinkedList<>();
//                    moveSlot.setOptional(false);
//                    moveSlot.addWord("turn");
//                    moveSlot.addWord("move");
//                    controlSlotList.add(moveSlot);
//
//                    toSlot.setOptional(true);
//                    toSlot.addWord("to the");
//                    controlSlotList.add(toSlot);
//
//                    orientationSlot.setOptional(false);
//                    orientationSlot.addWord("right");
//                    orientationSlot.addWord("left");
//                    controlSlotList.add(orientationSlot);
//
//                    GrammarConstraint mThreeSlotGrammar = new GrammarConstraint("three slots grammar", controlSlotList);
//
//                    String grammarJson = "{\n" +
//                            "         \"name\": \"play_media\",\n" +
//                            "         \"slotList\": [\n" +
//                            "             {\n" +
//                            "                 \"name\": \"play_cmd\",\n" +
//                            "                 \"isOptional\": false,\n" +
//                            "                 \"word\": [\n" +
//                            "                     \"play\",\n" +
//                            "                     \"close\",\n" +
//                            "                     \"pause\"\n" +
//                            "                 ]\n" +
//                            "             },\n" +
//                            "             {\n" +
//                            "                 \"name\": \"media\",\n" +
//                            "                 \"isOptional\": false,\n" +
//                            "                 \"word\": [\n" +
//                            "                     \"the music\",\n" +
//                            "                     \"the video\"\n" +
//                            "                 ]\n" +
//                            "             }\n" +
//                            "         ]\n" +
//                            "     }";
//                    GrammarConstraint mTwoSlotGrammar = mRecognizer.createGrammarConstraint(grammarJson);
//                    mRecognizer.addGrammarConstraint(mTwoSlotGrammar);
//                    mRecognizer.addGrammarConstraint(mThreeSlotGrammar);
//                } catch (VoiceException e) {
//                    Log.e(TAG, "Exception: ", e);
//                }

                try {
                    mRecognizer.startWakeupMode(mWakeupListener);
                } catch (VoiceException e) {
                    Log.e(TAG, "Exception: ", e);
                }
            }

            @Override
            public void onUnbind(String s) {
                Log.d(TAG, "Recognition service onUnbind");
                mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, getString(R.string.recognition_disconnected)));
                //speaker service or recognition service unbind, disable function buttons.
            }
        });
    }

//    protected void initSpeaker() {
//        mSpeaker = Speaker.getInstance();
//        mSpeaker.bindService(MainActivity.this, new ServiceBinder.BindStateListener() {
//            @Override
//            public void onBind() {
//                Log.d(TAG, "Speaker service onBind");
//                mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, getString(R.string.speaker_connected)));
////                try {
////                    //get speaker service language.
////                    mSpeakerLanguage = mSpeaker.getLanguage();
////                } catch (VoiceException e) {
////                    Log.e(TAG, "Exception: ", e);
////                }
////                bindSpeakerService = true;
////
////                // set the volume of TTS
////                try {
////                    mSpeaker.setVolume(50);
////                } catch (VoiceException e) {
////                    e.printStackTrace();
////                }
////
////                if (bindRecognitionService) {
////                    //both speaker service and recognition service bind, enable function buttons.
////                }
//            }
//
//            @Override
//            public void onUnbind(String s) {
//                Log.d(TAG, "Speaker service onUnbind");
//                //speaker service or recognition service unbind, disable function buttons.
//                mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, getString(R.string.speaker_disconnected)));
//            }
//        });
//    }

    protected void initWakeUp() {
        mWakeupListener = new WakeupListener() {
            @Override
            public void onStandby() {
                Log.d(TAG, "WakeUp onStandby");
                mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, getString(R.string.wakeup_standby)));
            }

            @Override
            public void onWakeupResult(WakeupResult wakeupResult) {
                //show the wakeup result and wakeup angle.
                Log.d(TAG, "Wakeup result:" + wakeupResult.getResult() + ", angle " + wakeupResult.getAngle());
                mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, getString(R.string.wakeup_result) + wakeupResult.getResult() + getString(R.string.wakeup_angle) + wakeupResult.getAngle()));

                //start azure recognition
                mSpeechRecognitionClient.getRecognitionClientWithIntent().startMicAndRecognition();

                // disable action button
                btnAction.setEnabled(false);
            }

            @Override
            public void onWakeupError(String s) {
                //show the wakeup error reason.
                Log.d(TAG, "WakeUp onWakeupError");
                mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, getString(R.string.wakeup_error) + s));
            }
        };
    }

    protected void initBtnAction() {
        btnAction.setEnabled(true);
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnAction.setEnabled(false);
                mSpeechRecognitionClient.getRecognitionClientWithIntent().startMicAndRecognition();
            }
        });
    }

//    protected void initRawData() {
//        mRawDataListener = new RawDataListener() {
//            @Override
//            public void onRawData(byte[] bytes, int i) {
//                createFile(bytes, "raw.pcm");
//            }
//        };
//    }

//    protected void intTTS() {
//        mTtsListener = new TtsListener() {
//            @Override
//            public void onSpeechStarted(String s) {
//                //s is speech content, callback this method when speech is starting.
//                Log.d(TAG, "TTS onSpeechStarted() called with: s = [" + s + "]");
//                mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, getString(R.string.speech_start)));
//            }
//
//            @Override
//            public void onSpeechFinished(String s) {
//                //s is speech content, callback this method when speech is finish.
//                Log.d(TAG, "TTS onSpeechFinished() called with: s = [" + s + "]");
//                mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, getString(R.string.speech_end)));
//            }
//
//            @Override
//            public void onSpeechError(String s, String s1) {
//                //s is speech content, callback this method when speech occurs error.
//                Log.d(TAG, "TTS onSpeechError() called with: s = [" + s + "], s1 = [" + s1 + "]");
//                mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, getString(R.string.speech_error) + s1));
//            }
//        };
//    }


    /* ----------------------------- Helper functions -------------------------------------- */

    private void createFile(byte[] buffer, String fileName) {
        final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;

        RandomAccessFile randomFile = null;
        try {
            randomFile = new RandomAccessFile(FILE_PATH + fileName, "rw");
            long fileLength = randomFile.length();
            randomFile.seek(fileLength);
            randomFile.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomFile != null) {
                try {
                    randomFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void switchLanguage(Locale locale) {
        Configuration config = getResources().getConfiguration();
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        config.locale = locale;
        resources.updateConfiguration(config, dm);
    }

}
