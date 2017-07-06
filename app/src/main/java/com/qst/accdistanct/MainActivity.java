package com.qst.accdistanct;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.util.Log;
import java.lang.Math;
import java.lang.System;

public class MainActivity extends AppCompatActivity {
    private static final String YZQ_TAG = "yzqtest";
    private int TIME_DELAY = 10;
    private int STATIC_COUNT = 80;
    private float alpha = 0.0f;

    private SensorManager sensorManager;
    private Sensor accSensor;
    private TextView mTextAcc_x;
    private TextView mTextAcc_y;
    private TextView mTextAcc_z;

    private TextView mTextvelocity_x;
    private TextView mTextvelocity_y;
    private TextView mTextvelocity_z;

    private TextView mTextdistance_x;
    private TextView mTextdistance_y;
    private TextView mTextdistance_z;
    private TextView mTextdis;
    private Button mButtonCalibration;
    private Button mButtonStart;
    private Button mButtonExit;
    private float acc_x;
    private float acc_y;
    private float acc_z;
    private int mCount = 0;
    private int mDebounce1 = 0;
    private int mDebounce2 = 0;
    private boolean mIsMove = false;

    private long currentUpdateTime = System.nanoTime();
    private long currentUpdateTime_old = System.nanoTime();
    private long lastUpdateTime = 0;

    private double timeInterval = 0;
    private boolean bIsStart = false;
    private boolean bIsCali = false;

    private double[] gravity={0, 0, 0};
    private  float[] output = {0,0,0};

    private double[] linear_acceleration={0, 0, 0};
    //ivate double[] linear_acceleration_old={0, 0, 0};
    private double[] velocity={0, 0, 0};
    //ivate double[] velocity_old={0, 0, 0};
    private double[] distance={0.0, 0.0, 0.0};
    //ivate double[] distance_old={0.0, 0.0, 0.0};

    private int mIndex = 0;
    private double[] linear_x = {0.0, 0.0, 0.0};
    private double[] linear_y = {0.0, 0.0, 0.0};
    private double[] linear_z = {0.0, 0.0, 0.0};

    static final float timeConstant = 0.279f;
    private  int count = 0;

    //Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mTextAcc_x = (TextView)findViewById(R.id.linear_x);
        mTextAcc_y = (TextView)findViewById(R.id.linear_y);
        mTextAcc_z = (TextView)findViewById(R.id.linear_z);

        mTextvelocity_x = (TextView)findViewById(R.id.velocity_x);
        mTextvelocity_y = (TextView)findViewById(R.id.velocity_y);
        mTextvelocity_z = (TextView)findViewById(R.id.velocity_z);
        mTextvelocity_x.setVisibility(View.INVISIBLE);
        mTextvelocity_y.setVisibility(View.INVISIBLE);
        mTextvelocity_z.setVisibility(View.INVISIBLE);

        mTextdistance_x = (TextView)findViewById(R.id.distance_x);
        mTextdistance_y = (TextView)findViewById(R.id.distance_y);
        mTextdistance_z = (TextView)findViewById(R.id.distance_z);

        mTextdis = (TextView)findViewById(R.id.text_dis);

        mButtonCalibration = (Button)findViewById(R.id.b_calibration);
        mButtonCalibration.setOnClickListener(mClick);
        mButtonStart = (Button)findViewById(R.id.b_start);
        mButtonStart.setOnClickListener(mClick);
        mButtonExit = (Button)findViewById(R.id.b_exit);
        mButtonExit.setOnClickListener(mClick);

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(mAccListener, accSensor);
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(mAccListener, accSensor, sensorManager.SENSOR_DELAY_FASTEST);
    }

    SensorEventListener mAccListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
           // currentUpdateTime = System.currentTimeMillis();
            if (event.sensor == accSensor) {
                acc_x = event.values[0];
                acc_y = event.values[1];
                acc_z = event.values[2];

                 currentUpdateTime = System.nanoTime();

                //Log.d(YZQ_TAG,"currentUpdateTime:"+currentUpdateTime+"  "+"acc_x:"+acc_x+"  "+"acc_y:"+acc_y+"  "+"acc_z:"+acc_z+" "+"delta_time:"+(currentUpdateTime-currentUpdateTime_old));



                /*
                // calc time diff
                if (lastUpdateTime == 0) {
                    lastUpdateTime = currentUpdateTime;
                }
                timeInterval = currentUpdateTime - lastUpdateTime;
                timeInterval = timeInterval/1000.00;
                lastUpdateTime = currentUpdateTime;
                // time diff
                if(bIsCali)
                {
                    gravity[0] += acc_x;
                    gravity[1] += acc_y;
                    gravity[2] += acc_z;
                    mCount++;
                    if(mCount >= STATIC_COUNT)
                    {
                        gravity[0] = gravity[0]/STATIC_COUNT;
                        gravity[1] = gravity[1]/STATIC_COUNT;
                        gravity[2] = gravity[2]/STATIC_COUNT;

                        mTextAcc_x.setText("gravity[0]:"+gravity[0]);
                        mTextAcc_y.setText("gravity[1]:"+gravity[1]);
                        mTextAcc_z.setText("gravity[2]:"+gravity[2]);
                        bIsCali = false;
                        mCount = 0;
                    }
                }
                else if(bIsStart == true) {
                    gravity[0] = gravity[0] * alpha + (1 - alpha) * acc_x;
                    gravity[1] = gravity[1] * alpha + (1 - alpha) * acc_y;
                    gravity[2] = gravity[2] * alpha + (1 - alpha) * acc_z;

                    linear_acceleration[0] = acc_x - gravity[0];
                    linear_acceleration[1] = acc_y - gravity[1];
                    linear_acceleration[2] = acc_z - gravity[2];

                    Log.d(YZQ_TAG, "," + linear_acceleration[0] + "," + linear_acceleration[1] + "," + linear_acceleration[2]);
                    mTextAcc_x.setText("linear x:"+linear_acceleration[0]);
                    mTextAcc_y.setText("linear y:"+linear_acceleration[1]);
                    mTextAcc_z.setText("linear z:"+linear_acceleration[2]);
// use average abs linear acc
                    if(mIndex < 3)
                    {
                        linear_x[mIndex] = Math.abs(linear_acceleration[0]);
                        linear_y[mIndex] = Math.abs(linear_acceleration[1]);
                        linear_z[mIndex] = Math.abs(linear_acceleration[2]);
                        mIndex++;
                    }
                    else
                    {
                        linear_x[0] = linear_x[1];
                        linear_x[1] = linear_x[2];
                        linear_y[0] = linear_y[1];
                        linear_y[1] = linear_y[2];
                        linear_z[0] = linear_z[1];
                        linear_z[1] = linear_z[2];

                        linear_x[2] = Math.abs(linear_acceleration[0]);
                        linear_y[2] = Math.abs(linear_acceleration[1]);
                        linear_z[2] = Math.abs(linear_acceleration[2]);
                    }

                    linear_acceleration[0] = (linear_x[0]+linear_x[1]+linear_x[2])/mIndex;
                    linear_acceleration[1] = (linear_y[0]+linear_y[1]+linear_y[2])/mIndex;
                    linear_acceleration[2] = (linear_z[0]+linear_z[1]+linear_z[2])/mIndex;
// use average abs linear acc
                    if((Math.abs(linear_acceleration[0]) >= 0.20)||(Math.abs(linear_acceleration[1]) >= 0.20)||(Math.abs(linear_acceleration[2]) >= 0.25))
                    {
                        mDebounce1++;
                        if(mDebounce1 >= 80)
                        {
                            mDebounce2 = 0;
                            mDebounce1 = 80;
                            mIsMove = true;
                        }
                    }
                    else
                    {
                        mDebounce2++;
                        if(mDebounce2 >= 40)
                        {
                            mDebounce1 = 0;
                            mDebounce2 = 40;
                            mIsMove = false;
                        }
                    }

                    if(mIsMove) {
                        if (Math.abs(linear_acceleration[0]) > 0.20) {
                            distance[0] += Math.abs(linear_acceleration[0]) * timeInterval;
                        }
                        if (Math.abs(linear_acceleration[1]) > 0.20) {
                            distance[1] += Math.abs(linear_acceleration[1]) * timeInterval;
                            //Log.d(YZQ_TAG, "  " + linear_acceleration[1] + "  "+timeInterval+" " + (linear_acceleration[1]*timeInterval) + "  " + distance[1]);
                        }
                        if (Math.abs(linear_acceleration[2]) > 0.25) {
                            distance[2] += Math.abs(linear_acceleration[2]) * timeInterval;
                        }
                    }
                    mTextdistance_x.setText("distance x:"+distance[0]);
                    mTextdistance_y.setText("distance y:"+distance[1]);
                    mTextdistance_z.setText("distance z:"+distance[2]);
                    if(mIsMove)
                        mTextdis.setText("Moving:" + Math.sqrt(distance[0]*distance[0]+distance[1]*distance[1]+distance[2]*distance[2]));
                    else
                        mTextdis.setText("Static:" + Math.sqrt(distance[0]*distance[0]+distance[1]*distance[1]+distance[2]*distance[2]));
                }
                */
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // ignore
        }
    };


    public View.OnClickListener mClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == mButtonCalibration.getId()) {
                bIsCali = true;
                mCount = 0;
                gravity[0] = 0;
                gravity[1] = 0;
                gravity[2] = 0;
                bIsStart = false;
                mButtonStart.setText("Start");
                Log.d(YZQ_TAG, "Calibration Button click!");
            }
            else if(v.getId() == mButtonStart.getId())
            {
                if(bIsStart == false)
                {
                    bIsStart = true;
                    mButtonStart.setText("Stop");
                }
                else
                {
                    bIsStart = false;
                    distance[0] = 0.0;
                    distance[1] = 0.0;
                    distance[2] = 0.0;
                    linear_acceleration[0] = 0;
                    linear_acceleration[1] = 0;
                    linear_acceleration[2] = 0;

                    mButtonStart.setText("Start");
                }
                Log.d(YZQ_TAG, "Start Button click!");
            }
            else if(v.getId() == mButtonExit.getId())
            {
                Log.d(YZQ_TAG, "Exit Button click!");
                finish();
            }
        }
    };
}
