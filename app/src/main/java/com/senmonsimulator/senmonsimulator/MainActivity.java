package com.senmonsimulator.senmonsimulator;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogRecord;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner spinnerMachineID;
    Spinner spinnerStatus;
    TextView textViewTimer;
    Switch onOffSwitch;

    Button buttonAdd;

    String machineID;
    String time;
    String date;
    String Vx;
    String Vy;
    String Vz;
    String Vtotal;
    String TC;
    String TS;
    String Hud;

    String statusFlag;
    Boolean triggerFlag = false;
    String timerDisplay;
    private int mInterval = 5000;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        spinnerMachineID = (Spinner) findViewById(R.id.spinnerMachineID);
        spinnerStatus = (Spinner) findViewById(R.id.spinnerStatus);
        textViewTimer = (TextView) findViewById(R.id.textViewTimer);
        onOffSwitch = (Switch) findViewById(R.id.switchOnOff);
        buttonAdd = (Button) findViewById(R.id.buttonAdd);

        //Dropdownlist for machineID selection
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.machineIDArray, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerMachineID.setAdapter(adapter);

        spinnerMachineID.setOnItemSelectedListener(this);

        //Dropdownlist for record selection
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.statusArray, android.R.layout.simple_spinner_item);

        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerStatus.setAdapter(adapter2);

        spinnerStatus.setOnItemSelectedListener(this);



        //button click event
        buttonAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerFlag = true;
                Toast.makeText(MainActivity.this, "Next record status added will be: " + statusFlag, Toast.LENGTH_SHORT).show();

            }
        });

        //switch toggle event
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //call simulate adding of record method
                    if(!machineID.equals("null")){

                        mHandler = new Handler();
                        startRepeatingTask();

                    }
                    else{
                        Toast.makeText(MainActivity.this, "Select a machine to On!", Toast.LENGTH_SHORT).show();
                        onOffSwitch.setChecked(false);
                    }

                }
                else{
                    Toast.makeText(MainActivity.this, machineID + " Off!", Toast.LENGTH_SHORT).show();
                    stopRepeatingTask();
                }


            }
        });

    }

    Runnable addRecordChecker = new Runnable() {
        @Override
        public void run() {
            try{
                simulateAddRecord();
                new CountDownTimer(5000,1000){
                    @Override
                    public void onTick(long millisUntilFinished) {
                        timerDisplay = "Add record in: " + millisUntilFinished / 1000;
                        textViewTimer.setText(timerDisplay);
                    }
                    @Override
                    public void onFinish() {
                        timerDisplay = "";
                    }
                }.start();
            }finally {
                mHandler.postDelayed(addRecordChecker, mInterval);
            }
        }
    };

    void startRepeatingTask(){
        addRecordChecker.run();
    }

    void stopRepeatingTask(){
        mHandler.removeCallbacks(addRecordChecker);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinnerMachineID:
                switch (position){
                    case 0:
                        //No machine selected
                        machineID = "null";
                        break;
                    case 1:
                        //Machine1
                        machineID = "SDK001-M001-01-0001a";
                        break;
                    case 2:
                        //Machine2
                        machineID = "SDK002-M002-02-0002a";
                        break;
                    case 3:
                        //Machine3
                        machineID = "SDK003-M003-03-0003a";
                        break;
                    case 4:
                        //Machine4
                        machineID = "SDK004-M004-04-0004a";
                        break;
                    case 5:
                        //Machine5
                        machineID = "SDK005-M005-05-0005a";
                        break;
                    case 6:
                        //Machine6
                        machineID = "SDK006-M006-06-0006a";
                        break;
                    case 7:
                        //Machine7
                        machineID = "SDK007-M007-07-0007a";
                        break;
                    case 8:
                        //Machine8
                        machineID = "SDK008-M008-08-0008a";
                        break;
                    case 9:
                        //Machine9
                        machineID = "SDK009-M009-09-0009a";
                        break;
                    case 10:
                        //Machine10
                        machineID = "SDK010-M010-10-0010a";
                        break;

                }
            case R.id.spinnerStatus:
                switch (position){
                    case 1:
                        //Warning
                        statusFlag = "warning";
                        break;
                    case 2:
                        //Critical
                        statusFlag = "critical";
                        break;

                }

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    public void simulateAddRecord(){

        //random machine inputs
        double lower = 0;
        double upper = 0;

        if(triggerFlag){
            //Add in warning records
            if(statusFlag.equals("warning")){
                lower = 2.80;
                upper = 4.50;

            }
            //Add in critical records
            else if(statusFlag.equals("critical")){
                lower = 7.10;
                upper = 45.90;

            }

        }
        //Add in normal records
        else{
            lower = 0.28;
            upper = 1.80;
        }
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        Date getTime = new Date();
        time = timeFormat.format(getTime);

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date getDate = new Date();
        date = dateFormat.format(getDate);

        Vx = String.format("%.2f", Math.random() * (upper - lower) + lower);
        Vy = String.format("%.2f", Math.random() * (upper - lower) + lower);
        Vz = String.format("%.2f", Math.random() * (upper - lower) + lower);
        Vtotal = String.format("%.2f", Math.random() * (upper - lower) + lower);
        TC = String.format("%.2f", Math.random() * (100.0 - 0) + 0);
        TS = String.format("%.2f", Math.random() * (100.0 - 0) + 0);
        Hud = String.format("%.2f", Math.random() * (53.00 -48.00) + 48.00);

        String record = date + "," + time + "," + Vx + "," + Vy + "," + Vz + ","
                + Vtotal + "," + TC + "," + TS + "," + Hud;

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 1000 * 30);
        HttpConnectionParams.setSoTimeout(httpParams, 1000 * 30);
        HttpClient client = new DefaultHttpClient(httpParams);

        ArrayList<NameValuePair> dataToSend = new ArrayList<>();
        dataToSend.add(new BasicNameValuePair("record", record));
        dataToSend.add(new BasicNameValuePair("machine", machineID));

        HttpPost post = new HttpPost("http://itpsenmon.net23.net/" + "addToCSV.php");

        try {
            post.setEntity(new UrlEncodedFormEntity(dataToSend));
            client.execute(post);


        } catch (Exception e) {
            e.printStackTrace();
        }
        triggerFlag = false;
    }


}
