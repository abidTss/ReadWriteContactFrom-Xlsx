package com.abid.readfiledata;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {
    TextView textFile;

    private static final int PICKFILE_RESULT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonPick = findViewById(R.id.buttonpick);
        textFile = findViewById(R.id.textfile);

        buttonPick.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
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
                    intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
                }
                startActivityForResult(Intent.createChooser(intent,"ChooseFile"), PICKFILE_RESULT_CODE);
              /*  Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/vnd.ms-excel");
                startActivityForResult(intent, PICKFILE_RESULT_CODE);*/

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    String line = null;
                    File file=new File(FilePath.getPath(MainActivity.this, data.getData()));
                    Log.e("file ","dhdh "+file);
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
                        printlnToUser(e.toString());
                    }
                }
                break;
        }
    }

    private void saveContact(String name, String phoneNo) {

        ArrayList<ContentProviderOperation> ops =
                new ArrayList<ContentProviderOperation>();

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
            printlnToUser(e.toString());
        }
        return value;
    }

    /**
     * print line to the output TextView
     * @param str
     */
    private void printlnToUser(String str) {
        final String string = str;
      Log.e("heloo data",str+" mila kuch");
    }

}
