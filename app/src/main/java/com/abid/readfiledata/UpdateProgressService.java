package com.abid.readfiledata;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

/**
 * Created by abid on 8/12/17.
 */

public class UpdateProgressService extends AppCompatActivity implements BackgroundService.Callbacks {
    SeekBar seekBar;
    Button stopService, btnPickFile, btnStartSaving;
    TextView tvupdation;
    private static final int PICKFILE_RESULT_CODE = 1;
    String exactPath;
    private AResultReceiver mResultReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seekBar = findViewById(R.id.seekBar);
        stopService = findViewById(R.id.stopService);
        btnPickFile = findViewById(R.id.buttonpick);
        btnStartSaving = findViewById(R.id.startSaving);
        tvupdation = findViewById(R.id.textfile);
        mResultReceiver = new AResultReceiver(new Handler());
        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btnStartSaving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnStartSaving.setVisibility(View.GONE);
                stopService.setVisibility(View.VISIBLE);
                if (exactPath != null) {
                    Intent intent = new Intent(UpdateProgressService.this, UpdateProgressIntentService.class);
                    intent.putExtra(AppConstant.RECEIVER, mResultReceiver);
                    intent.putExtra("filePath", exactPath);
                    startService(intent);
                }

            }
        });
        btnPickFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] mimeTypes =
                        {"image/*", "application/*|text/*"};
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
                    if (mimeTypes.length > 0) {
                        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                    }
                } else {
                    String mimeTypesStr = "";
                    for (String mimeType : mimeTypes) {
                        mimeTypesStr += mimeType + "|";
                    }
                    intent.setType(mimeTypesStr.substring(0, mimeTypesStr.length() - 1));
                }
                startActivityForResult(Intent.createChooser(intent, "ChooseFile"), PICKFILE_RESULT_CODE);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    exactPath = FilePath.getPath(UpdateProgressService.this, data.getData());
                    /*file = new File(FilePath.getPath(UpdateProgressService.this, data.getData()));
                    Log.e("file ","dhdh "+file);*/
                    btnStartSaving.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void updateClient(String data) {
        tvupdation.setText("Current Progress : " + data);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    class AResultReceiver extends ResultReceiver {
        public AResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            String mAddressOutput = resultData.getString(AppConstant.RESULT_DATA_KEY);

            if (resultCode == AppConstant.SUCCESS_RESULT) {
                Log.e("Address data", mAddressOutput);
                tvupdation.setText(mAddressOutput);
            }
        }
    }
}
