package com.example.a.theoshop.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

/**
 * Created by a on 18-Jul-17.
 */

public final class ItemContract {
    public ItemContract() {
    }
    //Content provider. package name for the app
    public static final String CONTENT_AUTHORITY = "com.example.a.theoshop.items";
    //Base for all URI which app will use to contact the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //possible path base content URI for possible URI's
    public static final String PATH_ITEMS = "items";
    //Inner class to define constant values in items database
    public static final class ItemEntry implements BaseColumns{
        //content URI to access items data in the table
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);
        //MIME typr of the content uri for list of items
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;
        //MIME type for single pet
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;
        public static final String TABLE_NAME = "items";
        public final static String _ID= BaseColumns._ID;
        public final static String COLUMN_ITEM_NAME= "name";
        public final static String COLUMN_ITEM_PRICE= "price";
        public final static String COLUMN_ITEM_QUANTITY= "quantity";
        public final static String COLUMN_ITEM_IMAGE= "image";
        public final static String COLUMN_ITEM_EMAIL= "email";

        public static boolean isValidPrice(int price){
            if(price > 0){
                return true;
            }
            return false;
        }
        public static boolean isValidQuantity(int quantity){
            if(quantity < 0){
                return false;
            }
            return true;
        }
        public static boolean inValidEmail(String email){
            if (TextUtils.isEmpty(email)) {
                return false;
            }
            return true;
        }

    }



}
