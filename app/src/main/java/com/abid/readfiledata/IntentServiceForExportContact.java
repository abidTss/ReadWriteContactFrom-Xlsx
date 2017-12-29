package com.abid.readfiledata;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by abid on 29/12/17.
 */

public class IntentServiceForExportContact extends IntentService {
    protected ResultReceiver mReceiver;

    public IntentServiceForExportContact() {
        super("ISExportService");

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mReceiver = intent.getParcelableExtra(AppConstant.RECEIVER_EXTRACTCONTACT);
        try {
            ContentResolver contentResolver = getContentResolver();
            Cursor cur = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            Log.e("contact count", cur.getCount() + " --");
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName("mysheet"));

            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    Log.e("contact pos", cur.getPosition() + " --");
                    deliverResultToReceiver(AppConstant.SUCCESS_RESULT, cur.getPosition(), cur.getCount());
                    Row row = sheet.createRow(cur.getPosition());
                    String id = cur.getString(cur.getColumnIndex(
                            ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(
                            ContactsContract.Contacts.DISPLAY_NAME));
                    Cell cell = row.createCell(0);
                    cell.setCellValue(name);
                    if (Integer.parseInt(cur.getString(cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            Cell cell2 = row.createCell(pCur.getPosition() + 1);

                            String phoneNumber = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                            cell2.setCellValue(phoneNumber);

                        }
                        pCur.close();
                    }
                }
            }
            /*for (int i=0;i<10;i++) {
                Log.e("creating xlsx row","---"+i);
                Row row = sheet.createRow(i);
                Cell cell = row.createCell(0);
                Cell cell2 = row.createCell(1);
                cell.setCellValue("abid "+i);
                cell2.setCellValue("705347550"+i);
            }*/
            String outFileName = "mysheet.xlsx";
            Log.e("writingFile" + outFileName, "data");
            File cacheDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            Log.e("cache dir ", cacheDir + " --");
            File outFile = new File(cacheDir, outFileName);
            OutputStream outputStream = new FileOutputStream(FilePath.getPath(IntentServiceForExportContact.this, Uri.fromFile(outFile)));
            // Log.e("file path : ",FilePath.getPath(ExportContact.this,Uri.fromFile(outFileName)));
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
            Log.e("sharing file...", "data");
            // share(outFileName, getApplicationContext());
        } catch (Exception e) {
            /* proper exception handling to be here */
            Log.e(e.toString(), "error");
        }
    }

    private void deliverResultToReceiver(int resultCode, int currentProgrees, int totalCount) {
        Log.e("hoja re ","hoja re");
        Bundle bundle = new Bundle();
        bundle.putInt(AppConstant.CURRENT_PROGRESS, currentProgrees);
        bundle.putInt(AppConstant.TOTAL_COUNT, totalCount);
        mReceiver.send(resultCode, bundle);
    }
}
