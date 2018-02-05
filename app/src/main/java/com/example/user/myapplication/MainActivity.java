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
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
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
            readContacts2();
        }

        list = (ListView)findViewById(R.id.list);
        /*
        //SimpleCursorAdapter參數
        1.Context
        2.版面配置檔的資源,有內建的也可以自行設計
        3.查詢內容提供者所得到的Cursor物件
        4.想要顯示的欄位
        5.資料顯示的元件ID陣列
        6.int flag 0表示listview在顯示的過程中,如果資料被更動了,不會自動重新查詢
         */


        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_2,cursor,
                new String[]{Contacts.DISPLAY_NAME,Phone.NUMBER},new int[]{android.R.id.text1,android.R.id.text2},0);
        list.setAdapter(adapter);

        /*
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursor,
                new String[]{Contacts.DISPLAY_NAME},new int[]{android.R.id.text1},0);
        list.setAdapter(adapter);
        */



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
                    readContacts2();
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

    private void readContacts3()
    {
        ContentResolver resolver = getContentResolver();

        cursor = resolver.query(Contacts.CONTENT_URI,null,null,
                null,null,null);

    }


    private void readContacts2()
    {
        ContentResolver resolver = getContentResolver();
        String[] projection = {Contacts._ID,Contacts.DISPLAY_NAME,Phone.NUMBER};
        cursor = resolver.query(Phone.CONTENT_URI,projection,null,
                null,null,null);

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
