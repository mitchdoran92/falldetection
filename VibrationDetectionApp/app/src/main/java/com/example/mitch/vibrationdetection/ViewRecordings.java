package com.example.mitch.vibrationdetection;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ViewRecordings extends AppCompatActivity {

    @InjectView(R.id.LV_Recordings)
    ListView _LV_Recordings;

    //The raw contents of the file
    private ArrayList<String> _fileContents = new ArrayList<String>();

    //The recordings extracted from the file in the format of:
    // "Recording name", {recordingdata ArrayList<String>}
        // timestamp, x , y , z , sumvec, magnitude, noisethresh, fallthresh
    private HashMap<String,ArrayList<String>> _recordings = new HashMap<String,ArrayList<String>>();

    private String _fileName = "data.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recordings);

        ButterKnife.inject(this);

        initListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_recordings, menu);
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


    private void initListView() {

        //If file doesn't exist, unable to progress
        if (!fileExistance(_fileName)) {
            Toast.makeText(getBaseContext(), "Recordings File not found", Toast.LENGTH_SHORT).show();
        }
        //Read the text file
        else {
            _recordings = readFromFile(_fileName);
            Log.d("readFromFile", "File read, " + _fileContents.size() + " lines found");
        }
        //Retrieve just the keys from our recordings
        ArrayList<String> recordingTitles = new ArrayList<String>(_recordings.keySet());


        //Create adpater and such
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, recordingTitles);

        _LV_Recordings.setAdapter(adapter);

        _LV_Recordings.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                Log.d("onItemClick", adapter.getItem(position).toString());

                ArrayList<String> dataPoints = _recordings.get(adapter.getItem(position));

                //Find the greatest magnitude (the highest spike in sumvec, scale colours based on this)
                float max = 0;
                for (String s : dataPoints)
                {
                    String[] splitData = s.split(",");
                    if (Float.parseFloat(splitData[5]) > max)
                        max = Float.parseFloat(splitData[5]);
                }

                Log.d("Max Magnitude","Magnitude: " + max);


                final AccelDataArrayAdapter rawDataAdapter = new AccelDataArrayAdapter(ViewRecordings.this.getBaseContext(), dataPoints, max);
                _LV_Recordings.setAdapter(rawDataAdapter);
            }
        });

    }



    private HashMap<String,ArrayList<String>> readFromFile(String fileName)
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
            //Split by semicolon and add to arrayList
            fileContents.addAll(Arrays.asList(contents.split(";")));
            _fileContents = fileContents;

            nestedList = nestedListProcess("Recording:", fileContents);
            Log.d("NestedList:", "Contains: " + nestedList.size() + " records");
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            Log.e("", "" + e.getMessage());
        }
        //If successful this should contain something
        return nestedList;
    }

    //Takes in a keyword delimiter to search the arrayList for.
    //When found it then seperates the arrayList into a list of hashmaps where the delimiter becomes the key for a list of strings.
    //Split this large array of strings into a nested array
    //Should look like
    //Recording 1
    //x,y,z
    //Recording 2
    //x,y,z
    private HashMap<String,ArrayList<String>> nestedListProcess(String delimiter, ArrayList<String> fileContents)
    {
        HashMap<String,ArrayList<String>> nestedList = new HashMap<String,ArrayList<String>>();
        //Iterate over the fileContents, we find a 'Recording: ' string
        //and then everything after is added to the sub-list until a new recording string is found
        for(int i = 0; i < fileContents.size(); i++)
        {
            String entryTitle;
            ArrayList<String> entryContents = new ArrayList<String>();

            if ( fileContents.get(i).contains(delimiter))
            {
                entryTitle = fileContents.get(i);
                i++;
                //Loop until another recording is found
                for (int j = i; j < fileContents.size(); j++)
                {
                    //Found "Recording: " time to start a new arrayList or on the last row
                    if (fileContents.get(j).contains(delimiter) || j == fileContents.size() -1)
                    {
                        nestedList.put(entryTitle,entryContents);
                        //Go back one spot, so that the next list starts at Recording not x,y,z
                        i = j - 2;
                        break;
                    }
                    //Add x,y,z until we find a recroding
                    else
                    {
                        entryContents.add(fileContents.get(j));
                    }

                }
            }
        }
        return nestedList;
    }






    private boolean fileExistance(String fname){
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }


}
