package utfpr.biagini.integrationapp.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import utfpr.biagini.integrationapp.Constants;
import utfpr.biagini.integrationapp.MqttClientService;
import utfpr.biagini.integrationapp.OnServiceListener;
import utfpr.biagini.integrationapp.R;
import utfpr.biagini.integrationapp.TimerHandler;

public class MainActivity extends AppCompatActivity {

    //Shared para preferencia do tema
    private static final String FILE = "Shared_Persistance";
    private static final String THEME = "THEME";
    private static final String TAG = "MainActivity";
    private static final int ON = 1;
    private static final int OFF = 0;

    //Service Bind
    MqttClientService mService;
    boolean mBound;

    //Timeout
    TimerHandler timerHandler;
    boolean onWait = false;

    private int count = 0;
    private int connectionId;
    private boolean isConnected = false;

    private Switch switch1, switch2, switch3, switch4;
    private Switch connectionStatus;
    private TextView connectionText;
    private Button windowButton, simulateButton;

    public static String room1;
    public static String room2;
    public static String room3;
    public static String room4;


    @Override
    protected void onDestroy() {
       // Intent intent = new Intent(MainActivity.this, MqttClientService.class);

       // startService(intent);

        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Recupera o shared e aplica o tema desejado
        SharedPreferences shared = getSharedPreferences(FILE, Context.MODE_PRIVATE);
        String theme = "";
        theme = shared.getString(THEME, theme);
        if (theme.equals("DARK")) {
            setTheme(R.style.Theme_AppCompat_DialogWhenLarge);
        } else if (theme.equals("LIGHT")) {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.Theme_AppCompat_Light);
        }
        setTitle("");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeActivity();
/*
        timerHandler = new TimerHandler();
        timerHandler.setOnTimeOutListener(new OnTimeoutListener() {
            @Override
            public void timeOut() {
                if(onWait) {
                  //  setConnectionStatus(false);
                    Toast toast = Toast.makeText(getApplicationContext(), "Timeout! Device do not respond", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        */
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mService == null){
            connectionStatus.setEnabled(false);
            enableActivity(false);
            connectionText.setText("Connecting...");
        }

        //Starts the Service
        Intent intent = new Intent(MainActivity.this, MqttClientService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent set = new Intent(this, Settings0.class);
        Intent help = new Intent(this, InfoHelp.class);
        if (item.getItemId() == R.id.settings) {
            startActivity(set);
            return true;
        } else if (item.getItemId() == R.id.help) {
            startActivity(help);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // Service Communication
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MqttClientService.LocalBinder binder = (MqttClientService.LocalBinder) service;
            mService = binder.getService();
            mService.setOnServiceListener(new OnServiceListener() {
                @Override
                public void isConnected(boolean status) {
                    onWait = true;
                    if(status) {
                        connectionId = getRandomInteger(0, Integer.MAX_VALUE);
                        mService.publishMessage(getPublishLoad());
                    }
                   // startTimer();
                    //isConnected = true;


                }

                @Override
                public void getMessage(String field, int message) {
                    // Select field
                    if (field.matches(Constants.COMMUNICATION)) {
                        if(isConnected) {
                         //   enableActivity(connectionId == message);
                        //    setConnectionStatus(connectionId == message);
                        }
                        enableActivity(true);
                        setConnectionStatus(true);


                    } else if (field.matches(Constants.LAMP1)) {
                        switch1.setChecked(message == ON);

                        //mService.publishMessage(field, message);

                    } else if (field.matches(Constants.LAMP2)) {
                        switch2.setChecked(message == ON);

                    } else if (field.matches(Constants.LAMP3)) {
                        switch3.setChecked(message == ON);

                    } else if (field.matches(Constants.LAMP4)) {
                        switch4.setChecked(message == ON);

                    }

                    count++;
                    if(count == 6){
                        if(onWait)
                            onWait = false;

                        else
                            mService.publishMessage(getPublishLoad());

                        count = 0;
                    }
                }


            });
            mBound = true;

            //startTimer(); //TimeOut connection;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    //Methods
    private void initializeActivity() {
        windowButton = findViewById(R.id.windowButton);
        simulateButton = findViewById(R.id.simulateButton);

        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        switch3 = findViewById(R.id.switch3);
        switch4 = findViewById(R.id.switch4);

        connectionStatus = findViewById(R.id.connectionStatus);
        connectionText = findViewById(R.id.connectionText);

        room1 = getString(R.string.BathroomLamp);
        room2 = getString(R.string.entranceLamp);
        room3 = getString(R.string.kitchen);
        room4 = getString(R.string.hall);

        switch1.setOnClickListener(lampClick);
        switch2.setOnClickListener(lampClick);
        switch3.setOnClickListener(lampClick);
        switch4.setOnClickListener(lampClick);

        connectionStatus.setOnClickListener(statusSwitchListener);
        connectionStatus.setClickable(false);

    }

    private void setConnectionStatus(boolean enable){
        connectionStatus.setEnabled(true);
        connectionStatus.setChecked(enable);

        isConnected = enable;

        if (enable)
            connectionText.setText("Online");
        else {
            connectionText.setText("Offline");
            mService.disconnectService();
            mService.onDestroy();

        }
    }

    private void startTimer() {
        timerHandler.start();
        onWait = true;
    }

    private void enableActivity(boolean enable) {
        switch1.setEnabled(enable);
        switch2.setEnabled(enable);
        switch3.setEnabled(enable);
        switch4.setEnabled(enable);

        windowButton.setEnabled(enable);
        simulateButton.setEnabled(enable);
    }

    private int switchClick(Switch s) {
        if (s.isChecked()) {
            return ON;
        } else
            return OFF;

    }

    private String getPublishLoad() {
        String load = Constants.COMMUNICATION + "=" + connectionId;

        load += "&" + Constants.LAMP1 + "=" + switchClick(switch1);
        load += "&" + Constants.LAMP2 + "=" + switchClick(switch2);
        load += "&" + Constants.LAMP3 + "=" + switchClick(switch3);
        load += "&" + Constants.LAMP4 + "=" + switchClick(switch4);
        load += "&status=MQTTPUBLISH";

        return load;
    }

    private View.OnClickListener lampClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            mService.publishMessage(getPublishLoad());
            onWait = true;


        }
    };

    private View.OnClickListener statusSwitchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(connectionStatus.isChecked()) {
                connectionStatus.setEnabled(false);
                //enableActivity(false);
                connectionText.setText("Connecting...");
                mService.startConnection();
                onWait  =true;
//                startTimer();

            } else {
                connectionText.setText(getText(R.string.offline));
                enableActivity(false);
                mService.disconnectService();
               // mService.onDestroy();
            }
        }
    };

    public static int getRandomInteger(int min, int max){
        int x = (int) (Math.random()*((max-min)+1))+min;
        return x < 0 ? x * -1: x; // unsigned

    }

}

