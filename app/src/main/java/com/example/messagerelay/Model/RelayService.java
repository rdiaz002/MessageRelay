package com.example.messagerelay.Model;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.example.messagerelay.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RelayService extends Service {

    private IntentFilter filter;
    private static Socket mSoc;
    private PrintWriter out;
    private static Thread socketThread;
    private NotificationManager noti;
    private BufferedReader input;

    static boolean SERVICE_RUNNING = false;
    static boolean SOCKET_CONNECTED = false;
    private final String CHANNEL_ID = "MESSAGE_RELAY_CHANNEL";
    private int NOTIID = 111010;
    private Notification builder;
    private String IP_ADDRESS = "127.0.0.1";
    private int PORT = 8080;
    private RelayWriter socketWriter;
    private RelayReader socketReader;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            Log.d("reci", intent.getAction());
            //TODO: Differentiate between actions
            //TODO: disconnect socket depending on action

            if ("Connect/Disconnect".equals(intent.getAction())) {
                if (!SOCKET_CONNECTED) {
                    socketThread.start();
                }
            } else if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
                SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                socketWriter.write(msgs);
            } else if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                //TODO:SEND BATTERY MESSAGES.
            }
            }


    };

    private Runnable connectSocket = new Runnable() {

        @Override
        public void run() {
            try {
                mSoc = new Socket(IP_ADDRESS, PORT);
                out = new PrintWriter(mSoc.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(mSoc.getInputStream()));
                socketWriter = new RelayWriter(out, getApplicationContext());
                socketReader = new RelayReader(getApplicationContext(), input);
                SOCKET_CONNECTED = true;
            } catch (IOException e) {
                e.printStackTrace();
                SOCKET_CONNECTED = false;
            }
        }
    };

    public RelayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        filter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("Connect/Disconnect");
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        Intent intent = new Intent("Connect/Disconnect");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_noun_signal_1608606)
                .setContentTitle("Message Relay")
                .setContentText("Message Relay Service is Running")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .build();
        noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        noti.notify(CHANNEL_ID, NOTIID, builder);
        registerReceiver(mReceiver, filter);
    }

    public static boolean isServiceRunning() {
        return SERVICE_RUNNING;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        IP_ADDRESS = pref.getString("ip_address","127.0.0.1");
        PORT= Integer.parseInt(pref.getString("port","8080"));
        SERVICE_RUNNING = true;
        if (!SOCKET_CONNECTED) {
            if (socketThread != null) {
                socketThread.interrupt();
            }
            socketThread = new Thread(connectSocket);
            socketThread.start();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        socketThread.interrupt();
        if (SOCKET_CONNECTED) {
            try {
                mSoc.close();
                socketWriter.interrupt();
                socketReader.interrupt();
                out.close();
                input.close();
                SOCKET_CONNECTED = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        SERVICE_RUNNING = false;
        noti.cancel(CHANNEL_ID, NOTIID);
    }
}
