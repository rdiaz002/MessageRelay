package com.example.messagerelay;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.PrintWriter;

public class RelayProtocol implements Runnable {
    final PrintWriter out;
    private String msgBuffer = "";
    private Context context;

    public RelayProtocol(PrintWriter out, Context context) {
        this.out = out;
        this.context = context;
    }

    public void write(SmsMessage[] content) {

        //data structure for communication with host software.
        for (SmsMessage msg : content) {
            String sender_name = msg.getOriginatingAddress();
            msgBuffer += getContactName(sender_name, context) + (char) 0x01;
            msgBuffer += sender_name + (char) 0x02;
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
