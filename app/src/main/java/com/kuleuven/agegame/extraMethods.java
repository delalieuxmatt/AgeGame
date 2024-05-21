package com.kuleuven.agegame;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class extraMethods {
    Context context;

    extraMethods(Context context){
        this.context = context;
    }

    public void onBackPressed() {
        // Example: Showing a confirmation dialog before exiting the activity
        new AlertDialog.Builder(context)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // You can perform any action here when the user confirms
                        // For example, if you want to exit an activity, you may pass a reference to it.
                        if (context instanceof Activity) {
                            ((Activity) context).finish();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null) // Do nothing when the user cancels
                .show();

    }
}
