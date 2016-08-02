package com.senmonsimulator.senmonsimulator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;


public class MainActivity2 extends AppCompatActivity {

    TextView textViewTimer;

    String Vx, Vy, Vz, Vtotal, TC, TS, Hud;

    String timerDisplay;
    private int mInterval = 10000;
    private Handler mHandler;

    Button btnTimer;

    ListView listView;
    CustomListAdapter listAdapter;

    ArrayList<String> machineIDarray = new ArrayList();
    ArrayList<String> machineDatesArray = new ArrayList();
    ArrayList<String> machineOffCheckArray = new ArrayList();
    ArrayList<Boolean> machineOffStatesArray = new ArrayList();

    CountDownTimer countdownTimer;

    private static final String TAG_RESULTS = "result";
    ProgressDialog progressDialog;
    Activity currentActivity;

    private final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("MM/dd/yyyy,HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        currentActivity = this;

        textViewTimer = (TextView) findViewById(R.id.textViewTimer);
        btnTimer = (Button) findViewById(R.id.btnTimer);
        listView = (ListView) findViewById(R.id.list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,  int position, long id) {
                listAdapter.notifyDataSetChanged();
            }});

        btnTimer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnTimer.getText().equals("START"))
                {
                    mHandler = new Handler();
                    startRepeatingTask();
                    btnTimer.setText("STOP");
                }
                else
                {
                    stopRepeatingTask();
                    btnTimer.setText("START");
                }
            }
        });

        progressDialog = new ProgressDialog(this);

        // to get list of machine id and their dates
        getSQLData();
    }

    Runnable addRecordChecker = new Runnable() {
        @Override
        public void run() {
            progressDialog.dismiss();
            try{
                countdownTimer = new CountDownTimer(mInterval,1000){
                    @Override
                    public void onTick(long millisUntilFinished) {
                        timerDisplay = "Add records in: " + millisUntilFinished / 1000 +" sec";
                        textViewTimer.setText(timerDisplay);
                    }
                    @Override
                    public void onFinish() {
                        textViewTimer.setText("Adding records...");
                        addNewRecord();
                        timerDisplay = "";
                        mHandler.postDelayed(addRecordChecker, mInterval);          // start next timer after records have been added
                    }
                }.start();
            }
            finally {
//                mHandler.postDelayed(addRecordChecker, mInterval);
            }
        }
    };

    void startRepeatingTask(){
        addRecordChecker.run();
    }

    void stopRepeatingTask(){
        mHandler.removeCallbacks(addRecordChecker);
        countdownTimer.cancel();                    // cancel current countdown timer
    }

    public void simulateAddRecord(){

        ArrayList<Boolean> addingEnabled = listAdapter.getAddingEnabled();
        ArrayList<Integer> statesSelected = listAdapter.getStatesSelected();

        int machineState;

        for(int i = 0; i < machineIDarray.size(); i++) {
            //random machine inputs
            double lower = 0;
            double upper = 0;
            double tempLower = 0, tempUpper = 0;

            if(addingEnabled.get(i)) {          // adding of records enabled
                machineState = statesSelected.get(i);
                switch(machineState)
                {
                    case 0:             // off
                        break;
                    case 1:             // normal
                        lower = 0.28;
                        upper = 2.70;
                        tempLower = 0;
                        tempUpper = 50;
                        break;
                    case 2:             // warning
                        lower = 2.80;
                        upper = 7.00;
                        tempLower = 0;
                        tempUpper = 70;
                        break;
                    case 3:             // critical
                        lower = 7.10;
                        upper = 45.90;
                        tempLower = 0;
                        tempUpper = 100;
                        break;
                }

                Date machineDate, timeAfterAnHour;
                String newTime;
                try {
                    machineDate = dateTimeFormatter.parse(machineDatesArray.get(i));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(machineDate);

                    if(machineOffStatesArray.get(i))            // machine is previously in off state
                    {
                        // add 1 day + (0-300) min + (0-300) seconds
                        cal.add(Calendar.DATE, 1);
                        cal.add(Calendar.MINUTE, new Random().nextInt(300));
                        cal.add(Calendar.SECOND, new Random().nextInt(300));
                    }
                    else                                        // machine is previously in on state
                        cal.add(Calendar.HOUR, 1);
                    timeAfterAnHour = cal.getTime();

                    newTime = dateTimeFormatter.format(timeAfterAnHour);

                    if(machineState == 0)           // machine is off, values all set to 0
                    {
                        Vx = "0";
                        Vy = "0";
                        Vz = "0";
                        Vtotal = "0";
                        TC = "0";
                        TS = "0";
                        Hud = "0";
                        machineOffStatesArray.set(i, true);         // save machine as off state
                    }
                    else                            // machine is not off, give real values
                    {
                        Vx = String.format("%.2f", Math.random() * (upper - lower) + lower);
                        Vy = String.format("%.2f", Math.random() * (upper - lower) + lower);
                        Vz = String.format("%.2f", Math.random() * (upper - lower) + lower);
                        Vtotal = String.format("%.2f", Math.random() * (upper - lower) + lower);
                        TC = String.format("%.2f", Math.random() * (tempUpper - tempLower) + tempLower);
                        TS = String.format("%.2f", Math.random() * (100.0 - 0) + 0);
                        Hud = String.format("%.2f", Math.random() * (53.00 - 48.00) + 48.00);
                        machineOffStatesArray.set(i, false);       // save machine as on state
                    }

                    //Check to see if latest record is off state, and if machine is still in off state
                    //do not add in anymore records
                    Log.d("machineoffcheckarray: ", machineOffCheckArray.get(i));
                    Log.e("JSON initial date/time", dateTimeFormatter.format(machineDate));
                    if(!(machineOffCheckArray.get(i).equals(Vy))){

                        machineDatesArray.set(i, newTime);

                        //Overwrite the array with new value
                        machineOffCheckArray.set(i, Vy);

                        String[]getNewDateTime = newTime.split(",");

                        try{
                            JSONObject json = new JSONObject();
                            json.put("machineID", machineIDarray.get(i));
                            json.put("date", getNewDateTime[0]);
                            json.put("time", getNewDateTime[1]);
                            json.put("vx", Vx);
                            json.put("vy", Vy);
                            json.put("vz", Vz);
                            json.put("vtotal", Vtotal);
                            json.put("tc", TC);
                            json.put("ts", TS);
                            json.put("hud", Hud);

                            HttpParams httpParams = new BasicHttpParams();
                            HttpConnectionParams.setConnectionTimeout(httpParams, 1000 * 30);
                            HttpConnectionParams.setSoTimeout(httpParams, 1000 * 30);
                            HttpClient client = new DefaultHttpClient(httpParams);

                            String url = "http://itpsenmon.net23.net/" + "addToSQL.php";
                            HttpPost request = new HttpPost(url);
                            request.setEntity(new ByteArrayEntity(json.toString().getBytes("UTF8")));
                            request.setHeader("json", json.toString());
                            client.execute(request);
                        }catch (Throwable t){
                            Toast.makeText(this, "Request failed: " + t.toString(),
                                    Toast.LENGTH_LONG).show();
                        }

                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // async task for add new record
    public void addNewRecord() {
        class AddNewRecord extends AsyncTask<Void, Void, Void> {
            @Override
            protected void onPreExecute() {
                progressDialog.setMessage("Adding Records...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setIndeterminate(false);
                progressDialog.show();
                progressDialog.setCancelable(false);
            }

            @Override
            protected Void doInBackground(Void... params) {
                simulateAddRecord();
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
//                progressDialog.dismiss();         // dismissing of dialog done in start of timer instead
            }
        }
        AddNewRecord a = new AddNewRecord();
        a.execute();
    }

    public void getSQLData() {
        class GetSQLDataJSON extends AsyncTask<Void, Void, JSONObject> {

            URL encodedUrl;
            HttpURLConnection urlConnection = null;

            String url = "http://itpsenmon.net23.net/readFromSQL.php";

            JSONObject responseObj;

            @Override
            protected void onPreExecute() {
                progressDialog.setMessage("Loading Records...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setIndeterminate(false);
                progressDialog.show();

                machineIDarray.clear();
                machineDatesArray.clear();
                machineOffCheckArray.clear();
                machineOffStatesArray.clear();
            }

            @Override
            protected JSONObject doInBackground(Void... params) {
                try {
                    encodedUrl = new URL(url);
                    urlConnection = (HttpURLConnection) encodedUrl.openConnection();
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    urlConnection.setUseCaches(false);
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.connect();

                    InputStream input = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    Log.d("doInBackground(Resp)", result.toString());
                    responseObj = new JSONObject(result.toString());

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
                return responseObj;
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                super.onPostExecute(result);
                getSQLRecords(result);
                listAdapter = new CustomListAdapter(currentActivity, R.layout.activity_main2, machineIDarray);
                listView.setAdapter(listAdapter);
                progressDialog.dismiss();
            }
        }
        GetSQLDataJSON g = new GetSQLDataJSON();
        g.execute();
    }

    //Get the server SQL records
    public void getSQLRecords(JSONObject jsonObj) {
        try {
            JSONArray serverSQLrecords = jsonObj.getJSONArray(TAG_RESULTS);

            String cleanupLatestRecords;

            //remove all unwanted symbols and text
            cleanupLatestRecords = serverSQLrecords.toString().replaceAll(",false]]", "").replace("[[", "").replace("[", "").replace("]]", "").replace("\"", "").replace("]", "");
            //split different csv records, the ending of each csv record list is machineID.csv
            String[] allSQLRecords = cleanupLatestRecords.split("split,");
            String[] latestRecords;
            Log.d("cleanuplatestrecords: ", cleanupLatestRecords);

            //loop through and get the latest records. afterwhich, split each field
            for (String record : allSQLRecords) {
                latestRecords = record.split(",");
                if(latestRecords.length < 10)
                {
                    newRecords();
                    break;
                }
                machineIDarray.add(latestRecords[0]);
                machineOffCheckArray.add(latestRecords[3]);
                Date machineDateTime;
                try {
                    machineDateTime = dateTimeFormatter.parse(latestRecords[1].replace("\\", "") + "," + latestRecords[2]);
                    machineDatesArray.add(dateTimeFormatter.format(machineDateTime));
                    if(latestRecords[6] == "0")
                        machineOffStatesArray.add(true);
                    else
                        machineOffStatesArray.add(false);
                } catch(ParseException e) {
                    machineDatesArray.add(dateTimeFormatter.format(new Date()));
                    e.printStackTrace();
                }
            }
            Log.e("OFF STATE ARRAY", String.valueOf(machineOffStatesArray.get(0)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void newRecords() {
        int numOfMachines = 8;
        for(int i = 1; i <= numOfMachines; i++) {
            machineDatesArray.add(dateTimeFormatter.format(new Date("01/01/2016,08:00:00")));
            machineOffStatesArray.add(false);
            machineIDarray.add(String.valueOf(i));
            machineOffCheckArray.add("0");
        }
    }
}
