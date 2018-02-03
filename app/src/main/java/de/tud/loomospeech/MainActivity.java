package de.tud.loomospeech;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClientWithIntent;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.voice.Languages;
import com.segway.robot.sdk.voice.Recognizer;
import com.segway.robot.sdk.voice.Speaker;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.audiodata.RawDataListener;
import com.segway.robot.sdk.voice.recognition.WakeupListener;
import com.segway.robot.sdk.voice.recognition.WakeupResult;
import com.segway.robot.sdk.voice.tts.TtsListener;

import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ISpeechRecognitionServerEvents {
    private static final String TAG = "MainActivity";

    private Recognizer mRecognizer;
    private Speaker mSpeaker;
    private ServiceBinder.BindStateListener mRecognitionBindStateListener;
    private ServiceBinder.BindStateListener mSpeakerBindStateListener;
    private WakeupListener mWakeupListener;
    private MessageHandler mHandler;
    private RawDataListener mRawDataListener;
    private MicrophoneRecognitionClientWithIntent m_micClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        switchLanguage(Locale.getDefault());
        mHandler = new MessageHandler(this);

        initWakeUp();
        initRecognitionClient();
        initRecognizer();
        initSpeaker();

    }

    @Override
    protected void onDestroy() {
        if (mRecognizer != null) {
            mRecognizer = null;
        }
        if (mSpeaker != null) {
            mSpeaker = null;
        }
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

//                try {
//                    //get recognition language when service bind.
//                    mRecognitionLanguage = mRecognizer.getLanguage();
//                    initControlGrammar();
//                    switch (mRecognitionLanguage) {
//                        case Languages.EN_US:
//                            addEnglishGrammar();
//                            break;
//                        case Languages.ZH_CN:
//                            addChineseGrammar();
//                            break;
//                    }
//                } catch (VoiceException e) {
//                    Log.e(TAG, "Exception: ", e);
//                }
//                bindRecognitionService = true;
//                if (bindSpeakerService) {
//                    //both speaker service and recognition service bind, enable function buttons.
//                    enableSampleFunctionButtons();
//                    mUnbindButton.setEnabled(true);
//                }
//
//                showTip("start to wake up and recognize speech");
//                //start the wakeup and the recognition.
//                mStartRecognitionButton.setEnabled(false);
//                mStopRecognitionButton.setEnabled(true);
//                mBeamFormListenButton.setEnabled(false);
//                try {
//                    mRecognizer.startWakeupMode(mWakeupListener);
//                } catch (VoiceException e) {
//                    Log.e(TAG, "Exception: ", e);
//                }
            }

            @Override
            public void onUnbind(String s) {
                Log.d(TAG, "Recognition service onUnbind");
                mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, getString(R.string.recognition_disconnected)));
//                //speaker service or recognition service unbind, disable function buttons.
//                disableSampleFunctionButtons();
//                mUnbindButton.setEnabled(false);
            }
        });
    }

    protected void initSpeaker() {
        mSpeaker = Speaker.getInstance();
        mSpeaker.bindService(MainActivity.this, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "Speaker service onBind");
                mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, getString(R.string.speaker_connected)));
//                try {
//                    //get speaker service language.
//                    mSpeakerLanguage = mSpeaker.getLanguage();
//                } catch (VoiceException e) {
//                    Log.e(TAG, "Exception: ", e);
//                }
//                bindSpeakerService = true;
//
//                // set the volume of TTS
//                try {
//                    mSpeaker.setVolume(50);
//                } catch (VoiceException e) {
//                    e.printStackTrace();
//                }
//
//                if (bindRecognitionService) {
//                    //both speaker service and recognition service bind, enable function buttons.
//                }
            }

            @Override
            public void onUnbind(String s) {
                Log.d(TAG, "Speaker service onUnbind");
                //speaker service or recognition service unbind, disable function buttons.
                mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, getString(R.string.speaker_disconnected)));
            }
        });
    }

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
//                m_micClient.startMicAndRecognition();
            }

            @Override
            public void onWakeupError(String s) {
                //show the wakeup error reason.
                Log.d(TAG, "WakeUp onWakeupError");
                mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, getString(R.string.wakeup_error) + s));
            }
        };
    }

    protected void initRawData() {
        mRawDataListener = new RawDataListener() {
            @Override
            public void onRawData(byte[] bytes, int i) {
                createFile(bytes, "raw.pcm");
            }
        };
    }

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

    /* ----------------------------- Azure functions -------------------------------------- */

    @Override
    public void onPartialResponseReceived(String s) {
        String msg = "Partial response:" + s;
        Log.d(TAG, msg);
        mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, msg));
    }

    @Override
    public void onFinalResponseReceived(com.microsoft.cognitiveservices.speechrecognition.RecognitionResult recognitionResult) {
        String msg = "Final response:" + recognitionResult.toString();
        Log.d(TAG, msg);
        mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, msg));
    }

    @Override
    public void onIntentReceived(String s) {
        String msg = "Intent:" + s;
        Log.d(TAG, msg);
        mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, msg));
    }

    @Override
    public void onError(int i, String s) {
        String msg = "Error:" + i + " - " + s;
        Log.d(TAG, msg);
        mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, msg));
    }

    @Override
    public void onAudioEvent(boolean b) {

    }

    void initRecognitionClient()
    {
        if(m_micClient != null) {
            return;
        }
        String language = getResources().getConfiguration().locale.toString();

        String subscriptionKey = this.getString(R.string.subscriptionKey);
        String luisAppID = this.getString(R.string.luisAppID);
        String luisSubscriptionID = this.getString(R.string.luisSubscriptionID);

        m_micClient = SpeechRecognitionServiceFactory.createMicrophoneClientWithIntent(this, language, this, subscriptionKey, luisAppID, luisSubscriptionID);
    }


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
