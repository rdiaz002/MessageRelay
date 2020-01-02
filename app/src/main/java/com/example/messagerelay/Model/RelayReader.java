package com.example.messagerelay.Model;

import android.content.Context;
import android.telephony.SmsManager;

import java.io.BufferedReader;
import java.io.IOException;

public class RelayReader extends Thread {
    private Context cont;
    private BufferedReader input;

    public RelayReader(Context cont, BufferedReader input) {
        this.cont = cont;
        this.input = input;
        this.start();
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                if (!input.ready()) {
                    continue;
                }
                StringBuilder data = new StringBuilder();
                data.append(input.readLine());
                while (input.ready()) {
                    data.append('\n');
                    data.append(input.readLine());
                }
                String phone = data.substring(0, data.indexOf("" + (char) 0x02));
                String msg = data.substring(data.indexOf("" + (char) 0x02) + 1);
                SendMessage(phone, msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void SendMessage(String phone, String msg) {
        SmsManager.getDefault().sendTextMessage(phone, null, msg, null, null);
    }


}
