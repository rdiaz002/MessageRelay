package com.example.messagerelay;


import android.telephony.SmsMessage;
import android.util.Log;

import java.io.PrintWriter;

public class RelayProtocol implements Runnable {
    final PrintWriter out;
    private String msgBuffer = "";

    public RelayProtocol(PrintWriter out) {
        this.out = out;
    }

    public void write(SmsMessage[] content) {

        for (SmsMessage msg : content) {
            msgBuffer += msg.getOriginatingAddress() + (char) 0x02;
            msgBuffer += msg.getMessageBody() + (char) 0x03;
        }
        Log.d("proto", msgBuffer);
    }

    @Override
    public void run() {
        if (null != out) {
            out.println(msgBuffer);
        }
        msgBuffer = "";
    }
}
