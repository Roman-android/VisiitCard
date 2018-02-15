package ru.roman.visiitcard;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.Scanner;

/**
 * Created by Roman on 01.03.2017.
 */

public class SmsWork {

    private TextSmsCallback textSmsCallback;
    private Context getContext;

    public SmsWork(Context context){
        final String myPackageName = context.getPackageName();
        Toast.makeText(context, myPackageName, Toast.LENGTH_SHORT).show();
        this.getContext = context;
    }

    public interface TextSmsCallback{
        void callbackTextSms(String name,String number,String email,String sait);
    }

    public void registredTextSmsCallback(TextSmsCallback textSmsCallback){
        this.textSmsCallback = textSmsCallback;
    }




    public void isDefaultApp(Context context){
        if(android.os.Build.VERSION.SDK_INT >= 19) {
            final String myPackageName = context.getPackageName();
            Intent defaultIntent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            defaultIntent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,myPackageName);
            context.startActivity(defaultIntent);
            Toast.makeText(context, "Версия устройства 4.4 или выше", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Версия устройства ниже 4.4", Toast.LENGTH_SHORT).show();
        }
    }

    public void readSMS(Intent intent,String startSms){
        final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
        //Toast.makeText(getContext, "intent "+intent, Toast.LENGTH_SHORT).show();
        if (intent.getAction().equals(SMS_RECEIVED)) {

            Bundle bundleSMS = intent.getExtras();
            String msg = "";
            Toast.makeText(getContext, "Принял СМС", Toast.LENGTH_SHORT).show();


            if (bundleSMS != null) {
                Object[] pdus = (Object[]) bundleSMS.get("pdus");

                if (pdus != null) {
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    for (SmsMessage message : messages) {
                        msg = message.getMessageBody();
                        //Toast.makeText(getContext, "Принял СМС "+msg, Toast.LENGTH_SHORT).show();
                    }



                    if (msg.startsWith(startSms)) {

                        // TODO: 05.04.2017 Работа со сканером
                        Scanner scanner = new Scanner(msg).useDelimiter(",");
                        int i = 0;
                        String [] data = new String[5];

                        while (scanner.hasNext()){
                            data[i]=scanner.next();
                            i++;
                        }

                        Toast.makeText(getContext, "Принял СМС "+data[0], Toast.LENGTH_SHORT).show();
                        textSmsCallback.callbackTextSms(data[1],data[2],data[3],data[4]);
                    }

                }
            }

        }
    }



}
