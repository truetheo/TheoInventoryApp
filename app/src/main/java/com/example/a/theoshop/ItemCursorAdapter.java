package com.example.a.theoshop;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a.theoshop.data.ItemContract.ItemEntry;

/**
 * Created by a on 19-Jul-17.
 */

public class ItemCursorAdapter extends CursorAdapter {
    public ItemCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name_text_view);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_value);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_value);
        //getting column index number
        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);
        // getting the content
        String itemName = cursor.getString(nameColumnIndex);
        float priceValue = cursor.getFloat(priceColumnIndex);
        final int quantityValue = cursor.getInt(quantityColumnIndex);
        final Uri uri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, cursor.getInt(cursor.getColumnIndex(ItemEntry._ID)));

        nameTextView.setText(itemName);
        priceTextView.setText("" + priceValue);
        quantityTextView.setText("" + quantityValue);
        Button saleBtn = (Button) view.findViewById(R.id.sale_button);
        saleBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(quantityValue > 0){
                    int newQuantity = quantityValue - 1;
                    ContentValues values = new ContentValues();
                    values.put(ItemEntry.COLUMN_ITEM_QUANTITY, newQuantity);
                    context.getContentResolver().update(uri, values, null, null);
                } else {
                    Toast.makeText(context, context.getString(R.string.item_out_of_stock),Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
