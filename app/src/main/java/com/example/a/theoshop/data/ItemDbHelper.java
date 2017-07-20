package com.example.a.theoshop.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.a.theoshop.data.ItemContract.ItemEntry;

/**
 * Created by a on 18-Jul-17.
 */

public class ItemDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = ItemDbHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "inventory.db";
    private static int DATABASE_VERSION = 1;
    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String TYPE_TEXT = " TEXT ";
        final String TYPE_FLOAT = " REAL ";
        final String TYPE_INTEGER = " INTEGER ";
        final String TYPE_COMMA = ", ";
        String CREATE_ITEMS_TABLE_SQL = "CREATE TABLE " +
                ItemEntry.TABLE_NAME +
                " (" +
                ItemEntry._ID + TYPE_INTEGER + "PRIMARY KEY AUTOINCREMENT"+ TYPE_COMMA +
                ItemEntry.COLUMN_ITEM_NAME + TYPE_TEXT+"NOT NULL"+ TYPE_COMMA+
                ItemEntry.COLUMN_ITEM_PRICE + TYPE_FLOAT +"NOT NULL"+ TYPE_COMMA +
                ItemEntry.COLUMN_ITEM_QUANTITY + TYPE_INTEGER + "NOT NULL"+ TYPE_COMMA +
                ItemEntry.COLUMN_ITEM_IMAGE + " BLOB NOT NULL" +
                 ");";
        Log.v(LOG_TAG, "SQL Command is: " + CREATE_ITEMS_TABLE_SQL);
        db.execSQL(CREATE_ITEMS_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
