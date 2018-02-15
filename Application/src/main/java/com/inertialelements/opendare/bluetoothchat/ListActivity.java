/*
 * Copyright (C) 2015 GT Silicon Pvt Ltd
 *
 * Licensed under the Creative Commons Attribution 4.0
 * International Public License (the "CCBY4.0 License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://creativecommons.org/licenses/by/4.0/legalcode
 *
 * Note that the CCBY4.0 license is applicable only for the modifications made
 * by GT Silicon Pvt Ltd
 */
package com.inertialelements.opendare.bluetoothchat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.inertialelements.opendare.R;

import java.util.List;

import static com.inertialelements.opendare.bluetoothchat.Utilities.REQUEST_PERMISSIONS_LOG_STORAGE;

public class ListActivity extends Activity {
    TextView aboutmimu;
    TextView dare;
    TextView run;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.listarray);

        aboutmimu = (TextView) findViewById(R.id.label1);
        dare = (TextView) findViewById(R.id.label2);
        run = (TextView) findViewById(R.id.label3);
        aboutmimu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mimu = new Intent(ListActivity.this, MIMU22BTP.class);
                startActivity(mimu);

            }
        });
        dare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent web = new Intent(ListActivity.this, Webview.class);
                startActivity(web);

            }
        });
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                checkAndRequestWriteLog();
                Intent Main = new Intent(ListActivity.this, MainActivity.class);
                startActivity(Main);

            }
        });
    }
    private Toast toast;
    private long lastBackPressTime = 0;

    @Override
    public void onBackPressed() {

        AlertDialog.Builder alertdialog = new AlertDialog.Builder(this);
        alertdialog.setTitle("Exit DaRe?");
      //  alertdialog.setMessage("Do you want to exit?");
        alertdialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //  return true;
                //  mConversationArrayAdapter.add("BT Connected");
                ListActivity.super.onBackPressed();
            }
        });
        alertdialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alertdialog.show();

    }

}

