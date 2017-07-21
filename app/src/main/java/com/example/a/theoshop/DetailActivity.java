package com.example.a.theoshop;

import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.a.theoshop.data.ItemContract.ItemEntry;
import com.example.a.theoshop.data.ItemDbHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static android.provider.MediaStore.Images.Media.getBitmap;

/**
 * Created by a on 19-Jul-17.
 */

public class DetailActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_EDITOR_ACTIVITY = 0;
    private static final int PICK_IMAGE = 200;
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private Uri imageUri;
    private Bitmap itemBitmap;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mEmailEditText;
    private ImageView mItemImageView;
    private Button mOrderButton;
    private Button increaseButton;
    private Button decreaseButton;

    private ItemDbHelper mDbHelper;
    private Uri currentItemUri;
    private boolean mItemHasChanged = false;
    private int quantity = 0;
    //OnToucheListener listens if user has made an attempt to edit
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        currentItemUri = intent.getData();

        if (currentItemUri == null) {
            setTitle(getString(R.string.editor_activity_new_item));
            Button orderMore = (Button) findViewById(R.id.button_order_more);
            orderMore.setVisibility(View.GONE);
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_item));
            getLoaderManager().initLoader(LOADER_EDITOR_ACTIVITY, null, this);
        }
        mNameEditText = (EditText) findViewById(R.id.name_edit_text);
        mPriceEditText = (EditText) findViewById(R.id.price_edit_text);
        mQuantityEditText = (EditText) findViewById(R.id.quantity_change);
        mItemImageView = (ImageView) findViewById(R.id.product_image);
        mEmailEditText = (EditText) findViewById(R.id.supplier_email);
        increaseButton = (Button) findViewById(R.id.button_quantity_plus);
        decreaseButton = (Button) findViewById(R.id.button_quantity_minus);
        mOrderButton = (Button) findViewById(R.id.button_order_more);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mItemImageView.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);

        mItemImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    openGallery();
            }
        });
    }

    private void saveItem() {
        String productName = mNameEditText.getText().toString().trim();
        String priceValue = mPriceEditText.getText().toString().trim();
        String quantityValue = mQuantityEditText.getText().toString().trim();
        String supplierEmail = mEmailEditText.getText().toString().trim();

        if (currentItemUri == null &&
                TextUtils.isEmpty(productName) && TextUtils.isEmpty(priceValue) &&
                TextUtils.isEmpty(quantityValue) && TextUtils.isEmpty(supplierEmail)) {
            // Since no fields were modified, we can return early without creating a new item.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }
        ContentValues values = new ContentValues();
        if (TextUtils.isEmpty(productName)) {
            Toast.makeText(this, getString(R.string.lack_product_name), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(priceValue)) {
            Toast.makeText(this, getString(R.string.lack_product_price), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(quantityValue)) {
            Toast.makeText(this, getString(R.string.lack_product_quantity), Toast.LENGTH_SHORT).show();
        } else if (itemBitmap == null) {
            Toast.makeText(this, getString(R.string.lack_product_image), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(supplierEmail)) {
            Toast.makeText(this, getString(R.string.lack_supplier_email), Toast.LENGTH_SHORT).show();
        }else {
            values.put(ItemEntry.COLUMN_ITEM_NAME, productName);
            float price = Float.parseFloat(priceValue);
            values.put(ItemEntry.COLUMN_ITEM_PRICE, price);
            quantity = Integer.parseInt(quantityValue);
            values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
            byte[] image = getBytes(itemBitmap);
            values.put(ItemEntry.COLUMN_ITEM_IMAGE, image);
            Log.v(LOG_TAG, "Image inserted " + image.toString() );
            values.put(ItemEntry.COLUMN_ITEM_EMAIL, supplierEmail);


            // Determine if this is a new or existing item by checking if currentItemUri is null or not
            if (currentItemUri == null) {
                // This is a NEW item, so insert a new item into the provider,
                // returning the content URI for the new item.
                Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                int rowsAffected = getContentResolver().update(currentItemUri, values, null, null);
                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_product_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
            finish();
        }

    }

    public void buttonIncrease(View view) {
        quantity += 1;
        mQuantityEditText.setText(String.valueOf(quantity));
    }

    public void buttonDecrease(View view) {
        if (quantity > 0) {
            quantity -= 1;
            mQuantityEditText.setText(String.valueOf(quantity));
        }
    }

    public void openEmail(String email, String product) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        Log.v(LOG_TAG, "Hi , i go ur email " + email);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_SUBJECT, product);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item to database
                saveItem();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_IMAGE,
                ItemEntry.COLUMN_ITEM_EMAIL};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentItemUri,         // Query the content URI for the current item
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);
            int emailColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_EMAIL);
            Log.v(LOG_TAG,"Image column number is " + imageColumnIndex);
            // Extract out the value from the Cursor for the given column index
            final String name = cursor.getString(nameColumnIndex);
            final String email = cursor.getString(emailColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);

            byte[] image = cursor.getBlob(imageColumnIndex);
            Log.v(LOG_TAG,"Image byte is " + image.toString());
            // Display image attached to the product
            itemBitmap = getImage(image);
            Log.v(LOG_TAG,"itemBitmap is " + itemBitmap.toString());
            if (image != null){
                mItemImageView.setImageBitmap(itemBitmap);
            }
            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(Float.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mEmailEditText.setText(email);
            Log.v(LOG_TAG,"email is " + email);
            cursor.close();
            mOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openEmail(email, name);
                }
            });

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mPriceEditText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (currentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(currentItemUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.delete_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.delete_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    private void openGallery() {
        Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            imageUri = data.getData();

            try {
                itemBitmap = getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mItemImageView.setImageURI(imageUri);
        }
    }

    /**
     * Convert from bitmap to byte array
     * @param bitmap Revived data from the user gallery is converted
     * @return byte[] to store in database BLOB
     */
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    /**
     * byte array -> Bitmap
     * @param image BLOB from database
     * @return Bitmap to display in UI
     */
    private static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
