package de.tud.loomospeech;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class MessageHandler extends Handler {
    public static final int ASSERT = 7;
    public static final int DEBUG = 3;
    public static final int ERROR = 6;
    public static final int INFO = 4;
    public static final int VERBOSE = 2;
    public static final int WARN = 5;

    public static final int APPEND = 10;
    public static final int SET = 11;

    private final WeakReference<Activity> output;

    MessageHandler(Activity instance) {
        output = new WeakReference<>(instance);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        // TODO: move to method getTextView and build checks
        TextView textView = (TextView) output.get().findViewById(R.id.output);

        if (msg.arg1 == APPEND) {
            textView.append(msg.obj.toString());
        }
        else {
            textView.setText(msg.obj.toString());
        }
    }
}
