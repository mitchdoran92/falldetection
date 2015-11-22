package com.example.mitch.vibrationdetection;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Mitch on 26/06/2015.
 */
public class AccelDataArrayAdapter extends ArrayAdapter<String> {

    private final Context context;
    private ArrayList<String> values = new ArrayList<String>();
    private float _maxMagnitude;

    public AccelDataArrayAdapter(Context context, ArrayList<String> values, float maxMagnitude) {
        super(context, R.layout.listview_accelvalues, values);
        this.context = context;
        this.values = values;
        this._maxMagnitude = maxMagnitude;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.listview_accelvalues, parent, false);


        String[] splitData = values.get(position).split(",");

        TextView TV_Timestamp = (TextView) rowView.findViewById(R.id.TV_Timestamp);
        TextView TV_X = (TextView) rowView.findViewById(R.id.TV_X);
        TextView TV_Y = (TextView) rowView.findViewById(R.id.TV_Y);
        TextView TV_Z = (TextView) rowView.findViewById(R.id.TV_Z);
        TextView TV_SumVec = (TextView) rowView.findViewById(R.id.TV_SumVec);
        TextView TV_Magnitude = (TextView) rowView.findViewById(R.id.TV_Magnitude);
        TextView TV_NoiseThresh = (TextView) rowView.findViewById(R.id.TV_NoiseThresh);
        TextView TV_FallThresh = (TextView) rowView.findViewById(R.id.TV_FallThresh);


        TV_Timestamp.setText(splitData[0]);
        TV_X.setText("x:" + splitData[1]);
        TV_Y.setText("y:" + splitData[2]);
        TV_Z.setText("z:" + splitData[3]);
        TV_SumVec.setText("SumVec: " + splitData[4]);
        //Set the colour of the cell based on this
        TV_Magnitude.setText("Magnitude: " + splitData[5]);

        //Percentage of max magnitude ( value between 0 and 100, times by 2.5 to get color
        float magnitude = Float.parseFloat(splitData[5]);
        double color = (((magnitude / _maxMagnitude) * 100) * 2.5);
        rowView.setBackgroundColor(Color.argb(160,0,(int)Math.round(color),0));

        TV_NoiseThresh.setText("Noise Threshold: " + splitData[6]);
        TV_FallThresh.setText("Fall Threshold: " + splitData[7]);

        return rowView;
    }

}