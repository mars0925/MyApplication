package com.example.user.myapplication;

import android.Manifest;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_CONTACTS;

public class MainActivity extends AppCompatActivity {

    private static final int request_contacts = 1;
    ListView list;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //檢查是否已經向使用者取得權限
        int permission = ActivityCompat.checkSelfPermission(this, READ_CONTACTS);
        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            //沒有權限的時候 向使用者請求權限
            //request_contacts是設計者定義的Int常數,代表權限的識別碼
            //private static final int request_contacts = 1;
            ActivityCompat.requestPermissions(this,new String[]{READ_CONTACTS,WRITE_CONTACTS},request_contacts);

        }
        else
        {
            //已經有權限
            Toast.makeText(this,"有權限" ,Toast.LENGTH_SHORT).show();
            readContacts();
        }

        list = (ListView)findViewById(R.id.list);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursor,
                new String[]{ContactsContract.Contacts.DISPLAY_NAME},new int[]{android.R.id.text1},0);
        list.setAdapter(adapter);

    }
    //不論使用者同意會拒絕都會自動執行 onRequestPermissionsResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case request_contacts:
                if (grantResults.length > 0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED)
                {   //同意取得權限要做的事
                    readContacts();
                }
                else//沒有同意取得權限要做的事
                {
                     new AlertDialog.Builder(this)
                             .setMessage("必須取得權限能")
                             .setPositiveButton("OK",null)
                             .show();
                }

        }
    }



    private void readContacts() {
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
        while (cursor.moveToNext())
        {
            int id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            Log.d("RECORD",id + "/" +name);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu,menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.setting)
        {
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }
}
