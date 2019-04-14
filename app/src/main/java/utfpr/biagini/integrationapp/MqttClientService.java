package utfpr.biagini.integrationapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

import utfpr.biagini.integrationapp.Activities.MainActivity;


public class MqttClientService extends Service {

    private final IBinder mBinder = new LocalBinder();
    private static final String TAG = "MqttMessageService";
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private OnServiceListener onServiceListener;
    public boolean connected;

    public MqttClientService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        pahoMqttClient = new PahoMqttClient();
        connected = false;

        startConnection();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
       // stopSelf();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setMessageNotification(@NonNull String topic, @NonNull String msg) {
        NotificationManager notificationManager =  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Crate Notification Channel (API 26 or greater)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel("100", "Bell", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "100")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(topic)
                .setContentText(msg);

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);
        builder.setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        notificationManager.notify(100, builder.build());
    }

    public class LocalBinder extends Binder {
        public MqttClientService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MqttClientService.this;
        }
    }

    public void startConnection() {
        mqttAndroidClient = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, Constants.CLIENT_ID);

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {
                isConnected(false);
                connected = false;
                Log.d(TAG, "Connection Lost");
                //startConnection();
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                if(!connected){
                    connected = true;
                    isConnected(true);
                }

                Log.d(TAG+ this.toString(),s +"\n" + mqttMessage.toString());
                int value = Integer.valueOf(mqttMessage.toString());

                //BELL RINGING NOTIFICATION
                if(s.substring(33, 39).matches(Constants.BELL) && value == 1) {
                    Date currentTime = Calendar.getInstance().getTime();
                    setMessageNotification("Someone on your door", currentTime.toString().substring(11, 16));
                }

                //Send to MainActivity
                getMessage(s.substring(33, 39), value);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    // Client Methods
    public void publishMessage(String message) {
        String topic = "channels/" + Constants.ANDROID_NODE_CHANNEL_ID
                + "/publish/" + Constants.THINGSPEAK_WRITE_API_KEY;

        try {
            pahoMqttClient.publishMessage(mqttAndroidClient, message, 0, topic);
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void  disconnectService() {
        String topic = "channels/" + Constants.NODE_ANDROID_CHANNEL_ID + "/subscribe/fields/+/" + Constants.THINGSPEAK_READ_API_KEY;

        try {
            pahoMqttClient.unSubscribe(mqttAndroidClient, topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
       // stopSelf();
    }

    // Listener methods
    public void isConnected(boolean status){
        if(onServiceListener != null)
            onServiceListener.isConnected(status);
    }

    public void getMessage(String field, int message){
        if(onServiceListener != null)
            onServiceListener.getMessage(field, message);
    }

    public void setOnServiceListener(OnServiceListener onServiceListener) {
        this.onServiceListener = onServiceListener;
    }

}
