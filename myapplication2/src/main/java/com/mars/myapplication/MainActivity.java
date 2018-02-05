package com.mars.myapplication;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_CONTACTS;
import static android.provider.ContactsContract.*;

public class MainActivity extends AppCompatActivity {

    private final int request_code = 123;
    ListView list;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list =findViewById(R.id.list);


        int permission = ActivityCompat.checkSelfPermission(this, READ_CONTACTS);
        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{READ_CONTACTS,WRITE_CONTACTS},request_code);
        }
        else
        {
            readContacts();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.insert:
                insertContact();
                break;
            case R.id.delete:
                deleteContact();
                break;
            case R.id.update:
                updateContact();
                break;
        }



        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case request_code:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    readContacts();
                }
                else
                {
                    new AlertDialog.Builder(this)
                            .setMessage("權限")
                            .setPositiveButton("OK",null)
                            .show();
                }

        }
    }

    private void readContacts() {
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(Contacts.CONTENT_URI,null,null,null,null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_2,
                cursor,new String[]{Contacts.DISPLAY_NAME,Contacts.HAS_PHONE_NUMBER},new int[]{android.R.id.text1,android.R.id.text2},1)

        {
            //複選bindView方法達到客製化的目的
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                super.bindView(view, context, cursor);
                TextView phone = (TextView)view.findViewById(android.R.id.text2);
                if (cursor.getInt(cursor.getColumnIndex(Contacts.HAS_PHONE_NUMBER)) == 0)
                {
                    phone.setText("");
                }
                else
                {
                    int id = cursor.getInt(cursor.getColumnIndex(Contacts._ID));
                    Cursor  pcursor  =getContentResolver().query(CommonDataKinds.Phone.CONTENT_URI,
                            null, CommonDataKinds.Phone.CONTACT_ID + "=?",
                            new String[]{String.valueOf(id)},null);
                    if (pcursor.moveToNext())
                    {
                        String number = pcursor.getString(pcursor.getColumnIndex(CommonDataKinds.Phone.DATA));
                        phone.setText(number);
                    }
                }
            }
        };
        list.setAdapter(adapter);
    }

    public void insertContact()
    {
        ArrayList ops = new ArrayList();
        int index = ops.size();

        //為了得到RAW_CONTACT_ID
        ops.add(ContentProviderOperation
        .newInsert(RawContacts.CONTENT_URI)
        .withValue(RawContacts.ACCOUNT_TYPE, null)
        .withValue(RawContacts.ACCOUNT_NAME,null)
        .build());
        //根據RAW_CONTACT_ID新增資料 這邊是新增名字
        ops.add(ContentProviderOperation
        .newInsert(Data.CONTENT_URI)
        .withValueBackReference(Data.RAW_CONTACT_ID,index)
        .withValue(Data.MIMETYPE, CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
        .withValue(CommonDataKinds.StructuredName.DISPLAY_NAME,"JANE")
        .build());
        //根據RAW_CONTACT_ID新增資料 這邊是新增電話號碼
        ops.add(ContentProviderOperation
        .newInsert(Data.CONTENT_URI)
        .withValueBackReference(Data.RAW_CONTACT_ID,index)
        .withValue(Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
        .withValue(CommonDataKinds.Phone.NUMBER,"095211229")
        .withValue(CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.TYPE_MOBILE)
        .build());
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY,ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }
    private void updateContact(){
        String where = CommonDataKinds.Phone.DISPLAY_NAME + " = ? AND "+Data.MIMETYPE+ " = ?";
        String[] params = new String[] {"JANE", CommonDataKinds.Phone.CONTENT_ITEM_TYPE};
        ArrayList ops = new ArrayList();
        ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                .withSelection(where, params)
                .withValue(CommonDataKinds.Phone.NUMBER, "0900333333")
                .build());
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public void deleteContact()
    {
        String where = ContactsContract.Data.DISPLAY_NAME + " = ? ";
        String[] params = new String[] {"JANE"};
        ArrayList ops = new ArrayList();
        ops.add(ContentProviderOperation.newDelete(RawContacts.CONTENT_URI)
                .withSelection(where, params)
                .build());
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }
}
