package com.example.mitch.vibrationdetection;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class RecordAccelerometer extends AppCompatActivity implements SensorEventListener {

    private float SCALE = 3.5f;

    //Sensor
    private SensorManager _sensorManager;
    private long _lastUpdate;

    private ArrayList<Float> xList = new ArrayList<Float>();
    private ArrayList<Float> yList = new ArrayList<Float>();
    private ArrayList<Float> zList = new ArrayList<Float>();
    private ArrayList<Float> thresholdList = new ArrayList<Float>();

    private int _pollCount = 0;
    private int _ticksSinceLastEvent = 0;


    //Activity variables
    private boolean _isRecording;
    private ArrayList<String> _recordedValues =  new ArrayList<String>();

    //ButterKnife UI elements injection
    @InjectView(R.id.TV_XAxis) TextView _xAxisTV;
    @InjectView(R.id.TV_YAxis) TextView _yAxisTV;
    @InjectView(R.id.TV_ZAxis) TextView _zAxisTV;
    @InjectView(R.id.TV_ThreshScale) TextView _threshScaleTV;

    @InjectView(R.id.ET_RecordingName) EditText _recordingNameET;
    @InjectView(R.id.ET_Distance) EditText _distanceET;
    @InjectView(R.id.ET_Height) EditText _heightET;
    @InjectView(R.id.ET_ThreshScale) EditText _threshScaleET;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_accelerometer);

        _isRecording = false;

        ButterKnife.inject(this);

        //Init sensor variables
        _sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        _lastUpdate = System.currentTimeMillis();

        _threshScaleTV.setText("ThreshScale : " + SCALE);




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record_accelerometer, menu);
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

    //Parent method used for all sensors. Call getAccelerometer if appropriate
    @Override
    public void onSensorChanged(SensorEvent event) {
        if ( _isRecording) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                getAccelerometer(event);
            }
        }

    }


    private void getAccelerometer(SensorEvent event) {
        _pollCount++;
        //Skip the first few events, finger on screen will cause wobble
        if (_pollCount < 3)
            return;

        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];
        //Round values to 5 decimal places
        x = (float)Math.round(x * 100000) / 100000;
        y = (float)Math.round(y * 100000) / 100000;
        z = (float)Math.round(z * 100000) / 100000;

        //Store values for avg computation
        xList.add(new Float(x));
        yList.add(new Float(y));
        zList.add(new Float(z));


        float accelationSquareRoot = calcAccelWithGravity(x,y,z);
        //Log.d("AccelerationSquareRoot", "Value: " + accelationSquareRoot);

        long actualTime = event.timestamp;
        _lastUpdate = actualTime;

        _xAxisTV.setText("X Axis: " + x );
        _yAxisTV.setText("Y Axis: " + y );
        _zAxisTV.setText("Z Axis: " + z );


        //Check threshold
        String thresholdResult = "";
        float avgX, avgY, avgZ;
        avgX = calcAverage(xList);
        avgY = calcAverage(yList);
        avgZ = calcAverage(zList);

        //This seems like a great idea.
        float avgAccel = calcAccelWithGravity(avgX, avgY, avgZ);
        //Log.d("avgAccel: ", "" + avgAccel);


        //Should allow the user to have dynamic (average based) or static threshold. Dynamic should work by a scale factor to allow greater customisaation
        float actualThreshold = Math.abs(DiffAcellBaseline(accelationSquareRoot, avgAccel));
        thresholdList.add(actualThreshold);

        float avgThresh = calcAverage(thresholdList);

        //Log.d("avgThresh", "" + avgThresh);


        //TODO: Work out an appropriate if statement to only capture and record the values I actually am interested in. Ignore all non events!

        if (accelationSquareRoot > avgAccel + (avgThresh * SCALE))
        {
            Log.d("SignificantEvent", "Occurred!");
            if ( _ticksSinceLastEvent > 100)
            {

                Log.d("SignificantEvent", "New Event, adding 5 previous entries!");
                for(int i = xList.size() -6; i < xList.size(); i++)
                {
                    Log.d("Looping:", "i : " + i);
                    _recordedValues.add(_lastUpdate + " , " + xList.get(i) + " , " + yList.get(i) + " , " + zList.get(i) + " , " + SCALE +  ";");
                }
            }
            _recordedValues.add(_lastUpdate + " , " + x + " , " + y + " , " + z + " , " + SCALE + ";");
            _ticksSinceLastEvent = 0;

        }
        //No significant event detected
        else
        {
            _ticksSinceLastEvent++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @OnClick(R.id.BUT_Record)
    public void RecordOnClick(View view) {
        Log.d("But Record", "OnClick");

        //Stop recording
        if ( _isRecording )
        {
            //Save data to file
            ArrayList<String> data = getData();
            //Right to the CSV.
            writeToFile(data);

            //Having recorded, stored data in file, return home
            Intent intent = new Intent(this, Home.class);
            startActivity(intent);
        }

        //Start recording
        else
        {
            //If EditTexts are null, don't let the recording start
            if (isEmpty(_distanceET) || isEmpty(_heightET) || isEmpty(_recordingNameET) || isEmpty(_threshScaleET))
            {
                Toast.makeText(getApplicationContext(), "One of the EditText's is null, unable to submit data.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            SCALE = Float.parseFloat(_threshScaleET.getText().toString());
            _threshScaleTV.setText("ThreshScale : " + SCALE);

            Log.d("# Recorded entries: ", " " + _recordedValues.size());

        }
        //Change value of recording
        _isRecording = !_isRecording;

    }

    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors
        _sensorManager.registerListener(this,
                _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        _sensorManager.unregisterListener(this);
    }

    private ArrayList<String> getData()
    {
        ArrayList<String> data = new ArrayList<>();


        //Ensure EditTexts are not empty
        if (!isEmpty(_distanceET) && !isEmpty(_heightET) && !isEmpty(_recordingNameET))
        {
            //Get the edit text values
            String dist = _distanceET.getText().toString().trim();
            String height = _heightET.getText().toString().trim();
            String recordingName = _recordingNameET.getText().toString().trim();

            data.add("Recording: " + recordingName + ";");

            //Loop over _recordedValues list and add the recordingName, distance, height, x , y , z into the data list
            for ( String values : _recordedValues)
            {
                data.add(recordingName + " , " + dist + " , " + height + " , " + values);
            }

            //Should have complete data List constructed now ready for the CSV.
            return data;
        }
        else
        {
            Toast.makeText(getApplicationContext(), (String) "One of the EditText's is null, unable to submit data.",
                    Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void writeToFile(ArrayList<String> data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("data.txt", Context.MODE_APPEND));
            for (String s : data)
            {
                outputStreamWriter.write(s);
            }
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkThreshold(double threshold, double accelValue, double baseline)
    {
        if ( (accelValue > baseline + threshold) || (accelValue < baseline - threshold))
            return true;
        return false;
    }

    private float DiffAcellBaseline(double accellValue, double baseline)
    {

        return (float)(Math.abs(baseline) - Math.abs(accellValue));
    }

    private float calcAccelWithGravity(float x, float y, float z)
    {
        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        return accelationSquareRoot;
    }

    private float calcAverage(ArrayList<Float> values)
    {
        float result = 0;
        for(Float val : values)
        {
            result += val;
        }
        return result / values.size();
    }


}



