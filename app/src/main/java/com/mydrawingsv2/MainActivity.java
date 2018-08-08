package com.mydrawingsv2;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import yuku.ambilwarna.AmbilWarnaDialog;


public class MainActivity extends AppCompatActivity {

    private MyPathDrawColor myPathDrawColor;
    private Context mContext;
    private PopupWindow mPopupWindow;
    private RelativeLayout mRelativeLayout;
    private int defaultColor;
    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.my_drawing2);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);

        setContentView(R.layout.activity_main);

        // Get the application context
        mContext = getApplicationContext();

        myPathDrawColor = findViewById(R.id.myPathDrawColor);
        mRelativeLayout = findViewById(R.id.myRL);

        defaultColor = ContextCompat.getColor(this, R.color.colorPrimary);

        // Use MyService to show toast message at application's launch and all every minute
        startService(new Intent(this,MyService.class)); // use to start the service

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.color:
                openColorPicker(false);
                break;
            case R.id.brush_size:
                brushSizePicker();
                break;
            case R.id.color_fill:
                fillFunction();
                break;
            case R.id.undo:
                myPathDrawColor.undoFunction();
                break;
            case R.id.redo:
                myPathDrawColor.redoFunction();
                break;
            case R.id.erase:
                myPathDrawColor.erase();
                break;
            case R.id.clear:
                myPathDrawColor.clearAll();
                Toast.makeText(this,R.string.screen_clear,Toast.LENGTH_SHORT).show();
                break;
            case R.id.save:
                save();
                break;
            case R.id.help:
                help();
                break;
            case R.id.close:
                finish();
                break;
        }
        return true;
    }


    private void openColorPicker(boolean AlphaSupport){
        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                Toast.makeText(MainActivity.this, "Color Picker Closed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                defaultColor = color;
                myPathDrawColor.setPathColor(color);
                myPathDrawColor.myColor = color;
            }
        });
        ambilWarnaDialog.show();
    }

    private void brushSizePicker() {
        BrushSizeChooseFragment brushSizeDialog = BrushSizeChooseFragment.NewInstance((int)myPathDrawColor.getPrevBrushSize());
        brushSizeDialog.setOnNewBrushSizeListener(new OnNewBrushSizeListener() {

            @Override
            public void OnNewBrushListener(float newBrushSize) {
                myPathDrawColor.setBrushSize(newBrushSize);
                myPathDrawColor.setPrevBrushSize(newBrushSize);
            }
        });
        brushSizeDialog.show(getSupportFragmentManager(), "Dialog");
    }

    private void fillFunction(){
        boolean smoothStatus = myPathDrawColor.smoothStrokes;
        myPathDrawColor.smoothStrokes = smoothStatus;
        myPathDrawColor.getMyCanvas().drawColor(0, PorterDuff.Mode.CLEAR);
        myPathDrawColor.getMyCanvas().drawColor(0, PorterDuff.Mode.SRC);
        myPathDrawColor.getMyCanvas().drawColor(defaultColor);
        myPathDrawColor.getMyCanvas().save();
        myPathDrawColor.invalidate();
    }

    private void save() {
        //Run code on the UI thread...
        runOnUiThread(new Runnable() {
            @ Override
            public void run() {
                //Create the alert dialog
                requestPermissions();
                createAlertDialog();
            }
        });
    }

    private void createAlertDialog() {

        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
            System.out.println("external available");
            System.out.println(getExternalFilesDir(null));
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
            System.out.println("external available but readonly");
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
        saveDialog.setTitle("Save drawing");
        saveDialog.setIcon(R.drawable.save);
        saveDialog.setMessage("Save drawing to device gallery?");
        saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Bitmap bitmap;
                View view = myPathDrawColor;
                view.setDrawingCacheEnabled(true);
                bitmap = Bitmap.createBitmap(view.getDrawingCache());

                String extStorageDirectory =  Environment.getExternalStorageDirectory().toString();
                File file = new File(extStorageDirectory+"/Pictures", getPictureName()+".png");
                try {
                    FileOutputStream outStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.flush();
                    outStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned "+path+" : ");
                        Log.i("ExternalStorage"," -> uri= "+uri);
                    }
                });
            }
        });
        saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Toast.makeText(mContext, "You canceled to save the drawing", Toast.LENGTH_SHORT).show();
            }
        });
        saveDialog.show();
    }

    private void requestPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            boolean hasPermission = (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
             if (! hasPermission){
                 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
             }
        }
    }

    private String getPictureName() {
        SimpleDateFormat timeStampFormat = new SimpleDateFormat("dd-MM-yyyy-HH.mm.ss");
        String date = timeStampFormat.format(new Date());
        return date;
    }


    private void help(){
        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

        // Inflate the custom layout/view
        View customView = inflater.inflate(R.layout.help_layout,null);

        // Initialize a new instance of popup window
        mPopupWindow = new PopupWindow(
                customView,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        // Set an elevation value for popup window
        // Call requires API level 21
        if(Build.VERSION.SDK_INT>=21)
            mPopupWindow.setElevation(5.0f);

        // Get a reference for the custom view close button
        ImageButton closeButton = (ImageButton) customView.findViewById(R.id.ib_close);

        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                mPopupWindow.dismiss();
            }
        });

        // Finally, show the popup window at the center location of root relative layout
        mPopupWindow.showAtLocation(mRelativeLayout, Gravity.CENTER,0,0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Kill the service
        stopService(new Intent(this,MyService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(new Intent(this,MyService.class));
    }
}
