package com.example.messagerelay;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class RelayService extends Service {

    private IntentFilter filter;
    private Socket mSoc;
    private PrintWriter out;
    private NotificationManager noti;
    static boolean SERVICE_RUNNING = false;
    static boolean SOCKET_CONNECTED = false;
    private final String CHANNEL_ID = "MESSAGE_RELAY_CHANNEL";
    private int NOTIID = 111010;
    private Notification builder;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            Log.d("reci", intent.getAction());
            //TODO: Differentiate between actions
            //TODO: disconnect socket depending on action
            String msgBuffer = "";
            RelayProtocol socketWriter = new RelayProtocol(out);

            if ("Connect/Disconnect".equals(intent.getAction())) {
                if (!SOCKET_CONNECTED) {
                    new Thread(connectSocket).start();
                }
            } else if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
                SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                socketWriter.write(msgs);
                new Thread(socketWriter).start();
            }

        }
    };

    private Runnable connectSocket = new Runnable() {
        @Override
        public void run() {
            try {
                SOCKET_CONNECTED = true;
                mSoc = new Socket("10.0.2.2", 8080);
                out = new PrintWriter(mSoc.getOutputStream(), true);
                Log.d("serv", "connected");

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("serv", "Not connected");
                SOCKET_CONNECTED = false;
            }
        }
    };

    public RelayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("reci", "created");

        filter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        filter.addAction("Connect/Disconnect");

        Intent intent = new Intent("Connect/Disconnect");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Message Relay")
                .setContentText("Connect/Disconnect the Message Relay Service")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .addAction(R.drawable.ic_launcher_background, "Start", pendingIntent)
                .build();
        noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        noti.notify(CHANNEL_ID, NOTIID, builder);

        registerReceiver(mReceiver, filter);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("reci", "start");

        SERVICE_RUNNING = true;
        if (!SOCKET_CONNECTED) {
            new Thread(connectSocket).start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("reci", "stopped");
        unregisterReceiver(mReceiver);
        if (SOCKET_CONNECTED) {
            try {
                mSoc.close();
                SOCKET_CONNECTED = false;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        SERVICE_RUNNING = false;
        noti.cancel(CHANNEL_ID, NOTIID);
    }
}
