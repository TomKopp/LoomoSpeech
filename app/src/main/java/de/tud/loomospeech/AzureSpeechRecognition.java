package de.tud.loomospeech;

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
        this.mHandler = myActivity.mHandler;
    }

    @Override
    public void onPartialResponseReceived(String s) {
        String msg = "Partial response:" + s;
        Log.d(TAG, msg);
        mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, msg));
    }

    @Override
    public void onFinalResponseReceived(final RecognitionResult recognitionResult) {
        this.recognitionClientWithIntent.endMicAndRecognition();

        String msg = "Final response:" + recognitionResult.toString();
        for (RecognizedPhrase el: recognitionResult.Results) {
            msg = msg.concat("\nConfidence: " + el.Confidence + " Text: \"" + el.DisplayText + "\"");
        }

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
    public void onAudioEvent(boolean isRecording) {
        String msg = "Microphone status: " + isRecording;
        if (isRecording) { msg += "\nPlease start speaking..."; }
        mHandler.sendMessage(mHandler.obtainMessage(MessageHandler.INFO, MessageHandler.APPEND, 0, msg));

        if (!isRecording) {
            this.recognitionClientWithIntent.endMicAndRecognition();
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
        return recognitionClientWithIntent;
    }

}
