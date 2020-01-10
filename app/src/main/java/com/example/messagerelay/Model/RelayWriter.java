package com.example.messagerelay.Model;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

public class RelayWriter extends Thread {
    final PrintWriter out;
    private String msgBuffer = "";
    private Queue<String> messageQueue;
    private Context context;

    public RelayWriter(PrintWriter out, Context context) {
        this.out = out;
        this.context = context;
        messageQueue = new LinkedList<>();
    }

    public void write(SmsMessage[] content) {

        //data structure for communication with host software.
        for (SmsMessage msg : content) {
            String sender_name = msg.getOriginatingAddress();
            //TODO:Move message buffer into this local scope. No longer neccessary to have it global.
            msgBuffer += getContactName(sender_name, context) + (char) 0x01;
            msgBuffer += sender_name + (char) 0x02;
            msgBuffer += msg.getMessageBody() + (char) 0x03;
            messageQueue.add(msgBuffer);
        }
        this.start();
    }

    @Override
    public void run() {
        if (null != out) {
            while (!messageQueue.isEmpty()) {
                out.println(messageQueue.poll());
            }
        }
        msgBuffer = "";
    }

    private String getContactName(final String phoneNumber, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName = "";
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }

        if (contactName.isEmpty()) {
            return phoneNumber;
        }
        return contactName;
    }

}
