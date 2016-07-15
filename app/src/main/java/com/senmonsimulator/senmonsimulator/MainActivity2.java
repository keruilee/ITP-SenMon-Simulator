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

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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

        //populateMachineID();

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
        getCSVData();
    }

//    private void populateMachineID() {
//        DecimalFormat twoDigits = new DecimalFormat("000");
//        DecimalFormat threeDigits = new DecimalFormat("000");
//        DecimalFormat fourDigits = new DecimalFormat("0000");
//        for(int i = 1; i <= 10; i++) {
//            String twoDigitsFormatted = twoDigits.format(i);
//            String threeDigitsFormatted = threeDigits.format(i);
//            String fourDigitsFormatted = fourDigits.format(i);
//            machineIDarray.add("SDK" +threeDigitsFormatted
//                                +"-M" +threeDigitsFormatted
//                                +"-" +twoDigitsFormatted
//                                +"-"+fourDigitsFormatted +"a");
//        }
//    }

    Runnable addRecordChecker = new Runnable() {
        @Override
        public void run() {
            progressDialog.dismiss();
            try{
                countdownTimer = new CountDownTimer(mInterval,1000){
                    @Override
                    public void onTick(long millisUntilFinished) {
                        timerDisplay = "Add record in: " + millisUntilFinished / 1000;
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

            if(addingEnabled.get(i)) {          // adding of records enabled
                machineState = statesSelected.get(i);
                switch(machineState)
                {
                    case 0:             // off
                        break;
                    case 1:             // normal
                        lower = 0.28;
                        upper = 1.80;
                        break;
                    case 2:             // warning
                        lower = 2.80;
                        upper = 4.50;
                        break;
                    case 3:             // critical
                        lower = 7.10;
                        upper = 45.90;
                        break;
                }

                Date machineDate, timeAfterAnHour;
                try {
                    machineDate = dateTimeFormatter.parse(machineDatesArray.get(i));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(machineDate);
                    cal.add(Calendar.HOUR, 1);
                    timeAfterAnHour = cal.getTime();

                    String newTime = dateTimeFormatter.format(timeAfterAnHour);
                    machineDatesArray.set(i, newTime);

                    if(machineState == 0)           // machine is off, values all set to 0
                    {
                        Vx = "0";
                        Vy = "0";
                        Vz = "0";
                        Vtotal = "0";
                        TC = "0";
                        TS = "0";
                        Hud = "0";
                    }
                    else                            // machine is not off, give real values
                    {
                        Vx = String.format("%.2f", Math.random() * (upper - lower) + lower);
                        Vy = String.format("%.2f", Math.random() * (upper - lower) + lower);
                        Vz = String.format("%.2f", Math.random() * (upper - lower) + lower);
                        Vtotal = String.format("%.2f", Math.random() * (upper - lower) + lower);
                        TC = String.format("%.2f", Math.random() * (100.0 - 0) + 0);
                        TS = String.format("%.2f", Math.random() * (100.0 - 0) + 0);
                        Hud = String.format("%.2f", Math.random() * (53.00 - 48.00) + 48.00);
                    }

                    String record = newTime + "," + Vx + "," + Vy + "," + Vz + ","
                            + Vtotal + "," + TC + "," + TS + "," + Hud;

                    HttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 1000 * 30);
                    HttpConnectionParams.setSoTimeout(httpParams, 1000 * 30);
                    HttpClient client = new DefaultHttpClient(httpParams);

                    ArrayList<NameValuePair> dataToSend = new ArrayList<>();
                    dataToSend.add(new BasicNameValuePair("record", record));
                    dataToSend.add(new BasicNameValuePair("machine", machineIDarray.get(i)));

                    HttpPost post = new HttpPost("http://itpsenmon.net23.net/" + "addToCSV.php");

                    try {
                        post.setEntity(new UrlEncodedFormEntity(dataToSend));
                        client.execute(post);
                    } catch (Exception e) {
                        e.printStackTrace();
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

    public void getCSVData() {
        class GetCSVDataJSON extends AsyncTask<Void, Void, JSONObject> {

            URL encodedUrl;
            HttpURLConnection urlConnection = null;

            String url = "http://itpsenmon.net23.net/readFromCSV.php";

            JSONObject responseObj;

            @Override
            protected void onPreExecute() {
                progressDialog.setMessage("Loading Records...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setIndeterminate(false);
                progressDialog.show();

                machineIDarray.clear();
                machineDatesArray.clear();
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
                getCSVRecords(result);
                listAdapter = new CustomListAdapter(currentActivity, R.layout.activity_main2, machineIDarray);
                listView.setAdapter(listAdapter);
                progressDialog.dismiss();
            }
        }
        GetCSVDataJSON g = new GetCSVDataJSON();
        g.execute();
    }

    //Get the server CSV records
    public void getCSVRecords(JSONObject jsonObj) {
        try {
            JSONArray serverCSVrecords = jsonObj.getJSONArray(TAG_RESULTS);

            String cleanupLatestRecords;
            //remove all unwanted symbols and text
            cleanupLatestRecords = serverCSVrecords.toString().replaceAll(",false]]", "").replace("[[", "").replace("[", "").replace("]]", "").replace("\"", "").replace("]", "");
            //split different csv records, the ending of each csv record list is machineID.csv
            String[] allCSVRecords = cleanupLatestRecords.split(".csv,");
            String[] latestRecords;

            //loop through each csv and get the latest records and split each field
            for (String record : allCSVRecords) {
                latestRecords = record.split(",");
                machineIDarray.add(latestRecords[10].replace(".csv",""));
                latestRecords[0] = latestRecords[0].replace("\\", "");
                Date machineDateTime;
                try {
                    machineDateTime = dateTimeFormatter.parse(latestRecords[0] + "," + latestRecords[1]);
                    machineDatesArray.add(dateTimeFormatter.format(machineDateTime));
                } catch(ParseException e) {
                    machineDatesArray.add(dateTimeFormatter.format(new Date()));
                    e.printStackTrace();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



}
