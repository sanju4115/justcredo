package com.credolabs.justcredo.internet;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.credolabs.justcredo.R;

/**
 * Created by Sanjay kumar on 10/18/2017.
 */

public class ConnectionUtil {

    private static void showSnack(boolean isConnected, final View mview) {

        String message;
        int color;
        if (!isConnected) {
            message = "Sorry! Not connected to internet";
            color = Color.RED;
            Snackbar snackbar = Snackbar
                    .make(mview, message, Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            checkConnection(mview);
                        }
                    });

            // Changing message text color
            snackbar.setActionTextColor(Color.GREEN);

            // Changing action button text color
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }


    }

    // Method to manually check connection status
    public static void checkConnection(View view) {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected, view);
    }
}
