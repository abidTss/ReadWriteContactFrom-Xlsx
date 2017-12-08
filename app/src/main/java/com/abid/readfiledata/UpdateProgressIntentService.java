package com.abid.readfiledata;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by abid on 8/12/17.
 */

public class UpdateProgressIntentService extends IntentService {
    private static final String TAG ="Intent service";
    protected ResultReceiver mReceiver;
    private String exactPath;

    public UpdateProgressIntentService() {
        super("hello");
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mReceiver=intent.getParcelableExtra(AppConstant.RECEIVER);
        exactPath=intent.getStringExtra("filePath");
        File file=new File(exactPath);
        try {
            FileInputStream stream = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(stream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowsCount = sheet.getPhysicalNumberOfRows();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int r = 0; r<rowsCount; r++) {
                Row row = sheet.getRow(r);
                int cellsCount = row.getPhysicalNumberOfCells();
                String name="",phoneNo="";
                deliverResultToReceiver(AppConstant.SUCCESS_RESULT,"Current progress : "+(r+1)+"/"+rowsCount);
                for (int c = 0; c<cellsCount; c++) {
                    String cellInfo = "r:"+r+"; c:"+c+"; v:"+getCellAsString(row, c, formulaEvaluator);
                    String value = getCellAsString(row, c, formulaEvaluator);
                    if(c==0){
                        name=value;
                    }
                    if(c==1){
                        phoneNo=value;
                    }
                    Log.e("data value",cellInfo);
                }
                saveContact(name,phoneNo);
            }
        } catch (Exception e) {
            Log.e("error",e.toString());
        }

    }

    protected String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        String value = "";
        try {
            Cell cell = row.getCell(c);
            CellValue cellValue = formulaEvaluator.evaluate(cell);
            switch (cellValue.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN:
                    value = ""+cellValue.getBooleanValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    int numericValue = (int)cellValue.getNumberValue();
                    if(HSSFDateUtil.isCellDateFormatted(cell)) {
                        double date = cellValue.getNumberValue();
                        SimpleDateFormat formatter =
                                new SimpleDateFormat("dd/MM/yy");
                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
                    } else {
                        new DataFormatter().formatCellValue(cell);
                        value = ""+ (new DataFormatter().formatCellValue(cell));
                    }
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = ""+cellValue.getStringValue();
                    break;
                default:
            }
        } catch (NullPointerException e) {
            /* proper error handling should be here */
            Log.e("error",e.toString());
        }
        return value;
    }


    private void saveContact(String name, String phoneNo) {

        ArrayList<ContentProviderOperation> ops =
                new ArrayList<>();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        );

        //------------------------------------------------------ Names
        if(name != null)
        {
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            name).build()
            );
        }

        //------------------------------------------------------ Mobile Number
        if(phoneNo != null)
        {
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNo)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()
            );
        }

        // Asking the Contact provider to create a new contact
        try
        {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //  Toast.makeText(myContext, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstant.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }
}
