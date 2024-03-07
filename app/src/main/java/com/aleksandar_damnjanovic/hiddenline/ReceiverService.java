package com.aleksandar_damnjanovic.hiddenline;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import static com.aleksandar_damnjanovic.hiddenline.MainActivity.connected;
import static com.aleksandar_damnjanovic.hiddenline.MainActivity.handler;
import static com.aleksandar_damnjanovic.hiddenline.MainActivity.contacts;
import static com.aleksandar_damnjanovic.hiddenline.MainActivity.un;
import static com.aleksandar_damnjanovic.hiddenline.MainActivity.ad;
import static com.aleksandar_damnjanovic.hiddenline.MainActivity.pass;
import static com.aleksandar_damnjanovic.hiddenline.MainActivity.request;
import static com.aleksandar_damnjanovic.hiddenline.MainActivity.res;
import static com.aleksandar_damnjanovic.hiddenline.MainActivity.contactList;

public class ReceiverService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Context con=this;

        new Thread(new Runnable() {
            @Override
            public void run() {

                MainActivity.setButtonText("Disconnect");

                Intent notificationIntent=new Intent(MainActivity.activity,MainActivity.class);
                PendingIntent pendingIntent=PendingIntent.getActivity(con,0,
                        notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                Notification notification=new NotificationCompat.Builder(con,
                        App.CHANNEL_ID)
                        .setContentTitle("Hidden Line Communicator")
                        .setContentText("Connection established")
                        .setContentIntent(pendingIntent)
                        .build();

                startForeground(1, notification);

                while (connected){
                    handler=true;
                    List<String> list=returnListOfContacts();
                    MessageHandler.provideList(list,un,pass,ad);
                    MessageHandler.retrieveAllMessages();
                    populateContactList(list);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                MainActivity.setButtonText("Connect");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE);
                }

                MainActivity.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.clearContacts();
                    }
                });

                handler=false;
            }
        }).start();

        return START_NOT_STICKY;
    }

    private List<String> returnListOfContacts(){
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(ad)
                .addPathSegment("UserPage")
                .addQueryParameter("userName", un)
                .addQueryParameter("password", pass)
                .addQueryParameter("mobile", "true")
                .build();

        request= new Request.Builder()
                .url(url)
                .build();

        res="";
        App.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                res = response.body().string();
            }
        });

        while (res.equals("")){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(!res.equals("")){
            List<String>toGo=stringToList(res);
            res="";
            return toGo;
        }else
            return null;

    }

    private void populateContactList(List<String>listOfContacts) {
        ArrayAdapter adapter=new ArrayAdapter(getApplicationContext(),R.layout.single_contact_view,listOfContacts);
        MainActivity.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contacts.setAdapter(adapter);
            }
        });
    }

    private List<String> stringToList(String resource){
        String splited[]=resource.split("\n");
        contactList= Arrays.asList(splited);
        return contactList;
    }

}