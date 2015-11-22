package com.example.mitch.vibrationdetection;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.inject(this);

        for (String file : fileList())
        {
            Log.d("File found: ", file);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @OnClick(R.id.BUT_Record)
    public void RecordOnClick(View view) {
//        Log.d("But Record", "OnClick");
//
//        Intent intent = new Intent(this, RecordAccelerometer.class);
//        startActivity(intent);

    }

    @OnClick(R.id.BUT_ViewRecordings)
     public void ViewOnClick(View view) {
        Log.d("But View Recordings", "OnClick");

        Intent intent = new Intent(this, ViewRecordings.class);
        startActivity(intent);

    }

    @OnClick(R.id.BUT_LiveDetection)
    public void LiveOnClick(View view) {
        Log.d("But View Recordings", "OnClick");

        Intent intent = new Intent(this, LiveDetection.class);
        startActivity(intent);

    }

    @OnClick(R.id.BUT_EraseRecordings)
    public void EraseRecordingOnClick(View view) {
        //Open file and clear contents

        //File file = new File(getBaseContext().getFilesDir(), _fileName);
        //deleteFile(_fileName);

        File dir = getFilesDir();
        File file = new File(dir, "data.txt");
        boolean deleted = file.delete();
        Log.d("deleteFile:", String.valueOf(deleted));

    }

    @OnClick(R.id.BUT_EmailRecordings)
    public void EmailOnClick(View view) {

        ArrayList<String> fileContents = readFromFile("data.txt");

        String emailBody = "";
//        int i = 0;
//        for(String s : fileContents)
//        {
//            emailBody += s + "\n";
//            i++;
//            Log.d("fileRow", "Row: " + i);
//        }

        //Send the fileContents as an email
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"mitchdoran92@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "VibrationDetection data");
        //intent.putExtra(Intent.EXTRA_TEXT, emailBody);

        File dir = getFilesDir();
        File file = new File(dir, "data.txt");
        file.setReadable(true,false);

        Uri uri = Uri.parse("file://" + file.getAbsolutePath());
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Send email..."));




    }




    private ArrayList<String> readFromFile(String fileName)
    {
        ArrayList<String> fileContents = new ArrayList<String>();
        HashMap<String,ArrayList<String>> nestedList = new HashMap<String,ArrayList<String>>();
        //Try to open the file
        try
        {
            FileInputStream fis = getBaseContext().openFileInput(fileName);

            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            //Read from file, sb.toString is the file contents
            String contents = sb.toString();
            fileContents.addAll(Arrays.asList(contents.split(";")));
            return fileContents;
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            Log.e("", "" + e.getMessage());
        }
        return fileContents;
    }



}