package com.abid.readfiledata;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by abid on 11/12/17.
 */

public class ExportContact extends AppCompatActivity {
    private BResultReceiver mreciver;
    TextView tvupdation;
    Intent intent;
    SeekBar seekBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_contact);
        seekBar = findViewById(R.id.seekBar2);
        seekBar.setEnabled(false);
        tvupdation = findViewById(R.id.textfile2);
        mreciver=new BResultReceiver(new Handler());
        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("sh","dskjd");
                intent = new Intent(ExportContact.this, IntentServiceForExportContact.class);
                intent.putExtra(AppConstant.RECEIVER_EXTRACTCONTACT, mreciver);
               // intent.putExtra("filePath", exactPath);
                startService(intent);
            }
        });


    }

    public void onWriteClick() {
       /* try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName("mysheet"));
            for (int i=0;i<10;i++) {
                Log.e("creating xlsx row","---"+i);
                Row row = sheet.createRow(i);
                Cell cell = row.createCell(0);
                Cell cell2 = row.createCell(1);
                cell.setCellValue("abid "+i);
                cell2.setCellValue("705347550"+i);
            }
            String outFileName = "mysheet.xlsx";
            Log.e("writingFile"+outFileName,"data");
            File cacheDir =Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            Log.e("cache dir ",cacheDir+" --");
            File outFile = new File(cacheDir , outFileName);
            OutputStream outputStream = new FileOutputStream(FilePath.getPath(ExportContact.this,Uri.fromFile(outFile)));
            // Log.e("file path : ",FilePath.getPath(ExportContact.this,Uri.fromFile(outFileName)));
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
            Log.e("sharing file...","data");
            share(outFileName, getApplicationContext());
        } catch (Exception e) {
            *//* proper exception handling to be here *//*
            Log.e(e.toString(),"error");
        }*/

    }

    public void share(String fileName, Context context) {
        Uri fileUri = Uri.parse("content://"+getPackageName()+"/"+fileName);
        Log.e("sending "+fileUri.toString()+" ...","data");
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.setType("application/octet-stream");
        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
    }

     class BResultReceiver extends ResultReceiver{
         /**
          * Create a new ResultReceive to receive results.  Your
          * {@link #onReceiveResult} method will be called from the thread running
          * <var>handler</var> if given, or from an arbitrary thread if null.
          *
          * @param handler
          */
         public BResultReceiver(Handler handler) {
             super(handler);
         }

         @Override
         protected void onReceiveResult(int resultCode, Bundle resultData) {
             int currentProgress = resultData.getInt(AppConstant.CURRENT_PROGRESS);
             int totalCount = resultData.getInt(AppConstant.TOTAL_COUNT);
             if (resultCode == AppConstant.SUCCESS_RESULT) {
                 Log.e("Address data", currentProgress + " :- " + totalCount);
                 /*if (currentProgress == totalCount) {
                     tvSavedSuccessfully.setVisibility(View.VISIBLE);
                     stopService.setVisibility(View.GONE);
                 }*/
                 seekBar.setMax(totalCount);
                 seekBar.setProgress(currentProgress);
                 tvupdation.setText("Current progress : " + currentProgress + "/" + totalCount);

             }
         }
     }
}
