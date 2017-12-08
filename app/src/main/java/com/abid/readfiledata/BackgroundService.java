package com.abid.readfiledata;

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

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

public class BackgroundService extends Service {
    private final IBinder mBinder = new LocalBinder();
    Callbacks activity;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        //Do what you need in onStartCommand when service has been started
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //((StartedServiceActivity)context).updateui();
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "service stoped", Toast.LENGTH_SHORT).show();
    }
    //returns the instance of the service
    public class LocalBinder extends Binder {
        public BackgroundService getServiceInstance(){
            return BackgroundService.this;
        }
    }
    //Here Activity register to the service as Callbacks client
    public void registerClient(Activity activity){
        this.activity = (Callbacks)activity;
    }
    //callbacks interface for communication with service clients!
    public interface Callbacks{
        void updateClient(String data);
    }

   /* public void startCounter(){
        startTime = System.currentTimeMillis();
        handler.postDelayed(serviceRunnable, 0);
        Toast.makeText(getApplicationContext(), "Counter started", Toast.LENGTH_SHORT).show();
    }

    public void stopCounter(){
        handler.removeCallbacks(serviceRunnable);
    }*/
    public void startSavingContact(File file){

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
                activity.updateClient((r+1)+"/"+rowsCount);
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

}
