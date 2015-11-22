package com.example.mitch.vibrationdetection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;




public class LiveDetection extends AppCompatActivity implements SensorEventListener {


    @InjectView(R.id.TV_NoiseLevel) TextView _noiseLevelTV;
    @InjectView(R.id.TV_FallThreshold) TextView _fallThreshTV;

    @InjectView(R.id.ET_NoiseLevel) EditText _noiseLevelET;
    @InjectView(R.id.ET_FallThresh) EditText _fallThreshET;


    @InjectView(R.id.BUT_Sensing) Button _sensingBUT;
    @InjectView(R.id.BUT_SaveData) Button _saveBUT;

    @InjectView(R.id.XYPlot_VectorMagnitude) XYPlot _vectorMagnitudeXYPlot;

    private SimpleXYSeries _vectorMagnitudeSeries = null;
    private SimpleXYSeries _noiseRemovedSeries = null;
    private SimpleXYSeries _potentialFallsSeries = null;

    private ArrayList<Float> _xList = new ArrayList<Float>();
    private ArrayList<Float> _yList = new ArrayList<Float>();
    private ArrayList<Float> _zList = new ArrayList<Float>();
    private ArrayList<Float> _sumVectorMagnitudeList = new ArrayList<Float>();
    private ArrayList<Float> _thresholdList = new ArrayList<Float>();
    private ArrayList<Float> _noiseRemovedList = new ArrayList<Float>();
    private ArrayList<Float> _potentialFallsList = new ArrayList<Float>();
    private ArrayList<Float> _fallSumVecsToSave = new ArrayList<Float>();

    private ArrayList<String> _recordedValues =  new ArrayList<String>();

    private String _recordingName;

    private float _noiseScale;
    private float _potentialFallScale;

    private SensorManager _sensorManager;

    private boolean _active = false;
    private boolean _displaySave = false;

    private static final int _HISTORY_SIZE = 500;

    private ArrayList<Integer> _ticksList = new ArrayList<Integer>();
    private int _ticks = 0;
    private int _ticksSinceLastEvent = 0;

    private GregorianCalendar _calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_detection);

        ButterKnife.inject(this);

        _sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        _vectorMagnitudeSeries = new SimpleXYSeries(_ticksList, _sumVectorMagnitudeList,"VectorMagnitude");

        _noiseRemovedSeries = new SimpleXYSeries(_ticksList,_noiseRemovedList,"NoiseRemoved");

        _potentialFallsSeries = new SimpleXYSeries(_ticksList,_potentialFallsList,"PotentialFalls");

        _vectorMagnitudeXYPlot.addSeries(_vectorMagnitudeSeries, new LineAndPointFormatter(Color.BLACK, Color.argb(250,20,250,20), null, null));
        _vectorMagnitudeXYPlot.addSeries(_noiseRemovedSeries, new LineAndPointFormatter(Color.BLACK, Color.argb(250, 250, 250, 20), null, null));
        _vectorMagnitudeXYPlot.addSeries(_potentialFallsSeries, new LineAndPointFormatter(Color.BLACK, Color.argb(250, 250, 20, 20), null, null));

        _vectorMagnitudeXYPlot.setRangeBoundaries(0.9, 1.2, BoundaryMode.GROW);

        //Ensures keyboard is hidden when user taps outside of edittext
        hideKeyboardFromEditText();

         _calendar = new GregorianCalendar();
        Date currentTime = new Date();
        _calendar.setTime(currentTime);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_live_detection, menu);
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
        //Fall detection
        if ( _active ) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                _ticks++;
                _ticksList.add(_ticks);
                getAccelerometer(event);
            }
        }
        //Don't want to do this stuff repeatedly, although it will not have any effect it may be slower than this if statement?
        if (_fallSumVecsToSave.size() > 5 )
        {
            _displaySave = true;
            _saveBUT.setVisibility(View.VISIBLE);
            //Gradually increase the greeness as the size increase
            if ((_fallSumVecsToSave.size() * 5) < 250 )
            {
                _saveBUT.setBackgroundColor(Color.rgb(0,_fallSumVecsToSave.size() * 5,0));
            }
            else
            {
                _saveBUT.setBackgroundColor(Color.rgb(0,250,0));
            }
        }






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


    private void getAccelerometer(SensorEvent event) {

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
        _xList.add(new Float(x));
        _yList.add(new Float(y));
        _zList.add(new Float(z));

        float sumVec = calcAccelWithGravity(x, y, z);
        _sumVectorMagnitudeList.add(sumVec);

        float avgX = calcAverage(_xList);
        float avgY = calcAverage(_yList);
        float avgZ = calcAverage(_zList);

        //This seems like a great idea.
        float avgAccel = calcAccelWithGravity(avgX, avgY, avgZ);

        float actualThreshold = Math.abs(DiffAcellBaseline(sumVec, avgAccel));
        _thresholdList.add(actualThreshold);

        float avgThresh = calcAverage(_thresholdList);

        //Something other than noise

        if ( (sumVec > avgAccel + (avgThresh * _noiseScale)) || (sumVec < avgAccel - (avgThresh * _noiseScale)) )
        {
            _noiseRemovedList.add(sumVec);

            _noiseRemovedSeries.addLast(_ticks, sumVec);
        }

        //Potential Fall
        if ( (sumVec > avgAccel + (avgThresh * _potentialFallScale)) || (sumVec < avgAccel - (avgThresh * _potentialFallScale)) )
        {
            //Get the current time of this event
            _calendar.setTime(new Date());
            String formattedTime = _calendar.getTime().toString();
            formattedTime = formattedTime.replace(' ', '-');


            //Display points on graph
            _potentialFallsList.add(sumVec);
            _potentialFallsSeries.addLast(_ticks,sumVec);

            //Calculate how many times greater than the threshold the event is, the higher the more significant
            //Calculate how many intervals of avgAcel the sumVec is greater than avgThresh
            float result = (Math.abs(sumVec) - Math.abs(avgAccel)) / avgThresh;
            result = Math.abs(result);
            Log.d("Result", "Result: " + result);

            //Add some previous data points prior to this tick
            if ( _ticksSinceLastEvent > 50)
            {
                for(int i = _xList.size() -3; i < _xList.size(); i++)
                {

                    float sumVector = calcAccelWithGravity(_xList.get(i), _yList.get(i), _zList.get(i));
                    _fallSumVecsToSave.add(sumVector);




                    _recordedValues.add(formattedTime + " , " + _xList.get(i) + " , " + _yList.get(i) + " , " + _zList.get(i) + " , " + sumVector + " , " + result + " , " +  _noiseScale + " , " + _potentialFallScale + ";");
                }
            }
            _fallSumVecsToSave.add(sumVec);
            _recordedValues.add(formattedTime + " , " + x + " , " + y + " , " + z + " , " + sumVec +  " , " + result + " , " +  _noiseScale + " , " + _potentialFallScale + ";");
            _ticksSinceLastEvent = 0;

        }
        else
        {
            _ticksSinceLastEvent++;
        }


        //TODO: Plot all data on a dynamic graph
        //Handle graphing
        _vectorMagnitudeSeries.addLast(_ticks, sumVec);
        //If we've reached HISTORY_SIZE data points then remove the oldest one
        if ( _vectorMagnitudeSeries.size() > _HISTORY_SIZE )
        {
            _vectorMagnitudeSeries.removeFirst();
        }
        if ((_vectorMagnitudeXYPlot.getCalculatedMaxX().intValue() - _vectorMagnitudeXYPlot.getCalculatedMinX().intValue()) > _HISTORY_SIZE)
        {
            try {
                _noiseRemovedSeries.removeFirst();
            }
            catch(Exception e)
            {

            }
        }
        //If still bigger, must be a potential fall point
        if ((_vectorMagnitudeXYPlot.getCalculatedMaxX().intValue() - _vectorMagnitudeXYPlot.getCalculatedMinX().intValue()) > _HISTORY_SIZE + 20)
        {
            try {
                _potentialFallsSeries.removeFirst();
            }
            catch(Exception e)
            {

            }
        }

        _vectorMagnitudeXYPlot.redraw();




    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    @OnClick(R.id.BUT_Sensing)
    public void SensingOnClick(View view) {

        //Check that editTexts are filled in
        if (!isEmpty(_fallThreshET) && !isEmpty(_noiseLevelET))
        {
            //flip the text on the button
            if (_sensingBUT.getText().toString().equals("Start Sensing"))
            {
                _sensingBUT.setText("Stop Sensing");
                _active = true;

                _noiseScale = Float.parseFloat(_noiseLevelET.getText().toString());
                _potentialFallScale = Float.parseFloat(_fallThreshET.getText().toString());

            }
            else
            {
                _sensingBUT.setText("Start Sensing");
                _active = false;
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), (String) "One of the EditText's is null, unable to begin sensing.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.BUT_SaveData)
    public void SaveOnClick(View view) {
        //Assuming when they save they want to stop recording/sensing new events
        _active = false;
        _sensingBUT.setVisibility(View.INVISIBLE);

        final LiveDetection thisclass = this;

        //TODO: Somehow prompt the user to enter a name for the recording
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Recording Name");
        alert.setMessage("Give a meaningful name to this recording");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                // Do something with value!
                if (!isEmpty(input))
                {
                    _recordingName = input.getText().toString();

                    //Get the data to save
                    ArrayList<String> data = getData();
                    //Right to the CSV.
                    writeToFile(data);

                    //Go back home or reset everything and stay on this activity
                    Intent intent = new Intent(thisclass, Home.class);
                    startActivity(intent);

                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Please enter a name for the recording.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    //Not really used to do much in this version, was previously adding extra data such as the name, dist and height to the recording.
    private ArrayList<String> getData()
    {
        ArrayList<String> data = new ArrayList<>();

            data.add("Recording: " + _recordingName + ";");

            for ( String values : _recordedValues)
            {
                data.add(values);
            }

            //Should have complete data List constructed now ready for the CSV.
            return data;

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

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private float calcAccelWithGravity(float x, float y, float z)
    {
        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        return accelationSquareRoot;
    }

    private void hideKeyboardFromEditText()
    {
        //Hide keyboard when tapped away from ET
        _noiseLevelET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        _fallThreshET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
    }

    private float DiffAcellBaseline(double accellValue, double baseline)
    {

        return (float)(Math.abs(baseline) - Math.abs(accellValue));
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

    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0) {
            return false;
        } else {
            return true;
        }
    }

}
