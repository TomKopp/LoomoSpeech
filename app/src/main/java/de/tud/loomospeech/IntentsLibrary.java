package de.tud.loomospeech;

import android.util.Log;

class IntentsLibrary {
    private static final String TAG = "IntentsLibrary";

    public void callByName(String functionName) {
        // Ignoring any possible result
        try {
            this.getClass().getDeclaredMethod(functionName).invoke(this);
        } catch (Exception e) {
//            Log.d(TAG, e.getMessage());
            Log.d(TAG, "Exception: ", e);
            this.None();
        }
    }

    private void None() {
        // No suitable action for command found.
        Log.d(TAG, "No suitable action for command found.");
    }

    private void HomeAutomationTurnOn() {}

    private void HomeAutomationTurnOff() {}

    private void OnDeviceOpenApplication() {}

    private void OnDeviceCloseApplication() {}

    private void UtilitiesStop() {}

}
