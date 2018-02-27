package de.tud.loomospeech;

import android.media.ToneGenerator;
import android.util.Log;

import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClientWithIntent;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.RecognizedPhrase;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;


class AzureSpeechRecognition implements ISpeechRecognitionServerEvents {
    private static final String TAG = "AzureSpeechRecognition";

    private MainActivity activity;
    private MessageHandler mHandler;
    private MicrophoneRecognitionClientWithIntent recognitionClientWithIntent;

    AzureSpeechRecognition(MainActivity myActivity) {
        activity = myActivity;
        mHandler = myActivity.mHandler;
    }

    @Override
    public void onPartialResponseReceived(String s) {
        String msg = "Partial response: " + s;
        Log.d(TAG, msg);
        mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, msg));
    }

    @Override
    public void onFinalResponseReceived(final RecognitionResult recognitionResult) {
        recognitionClientWithIntent.endMicAndRecognition();

        String msg = "Final response: " + recognitionResult.RecognitionStatus;
        for (RecognizedPhrase el: recognitionResult.Results) {
            msg = msg.concat("\nConfidence: " + el.Confidence + " Text: \"" + el.DisplayText + "\"");
        }

        Log.d(TAG, msg);
        mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, msg));
    }

    @Override
    public void onIntentReceived(String s) {
        String msg = "Intent:\n" + s;
//        Log.d(TAG, msg);
        Log.d(TAG, "Intent received!!!");
        mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, msg));

//        recognitionClientWithIntent.endMicAndRecognition();
        activity.startWakeUpListener();
    }

    @Override
    public void onError(int i, String s) {
        String msg = "Error:" + i + " - " + s;
        Log.d(TAG, msg);
        mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, msg));

        activity.startWakeUpListener();
    }

    @Override
    public void onAudioEvent(boolean isRecording) {
        String msg = "Microphone status: " + isRecording;
        if (isRecording) { msg += "\nPlease start speaking..."; }
        mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, msg));
        activity.toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);

        if (!isRecording) {
            recognitionClientWithIntent.endMicAndRecognition();
            activity.startWakeUpListener();
//            this._startButton.setEnabled(true);
        }
    }

    MicrophoneRecognitionClientWithIntent getRecognitionClientWithIntent() {
        if (recognitionClientWithIntent != null) {
            return recognitionClientWithIntent;
        }

        String language = activity.getResources().getConfiguration().locale.toString();
        String subscriptionKey = activity.getString(R.string.subscriptionKey);
        String luisAppID = activity.getString(R.string.luisAppID);
        String luisSubscriptionID = activity.getString(R.string.luisSubscriptionID);

        recognitionClientWithIntent = SpeechRecognitionServiceFactory.createMicrophoneClientWithIntent(activity, language, this, subscriptionKey, luisAppID, luisSubscriptionID);
        recognitionClientWithIntent.setAuthenticationUri(activity.getString(R.string.authenticationUri));

        return recognitionClientWithIntent;
    }

}
