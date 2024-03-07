package com.aleksandar_damnjanovic.hiddenline;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class App  extends Application {

    public static OkHttpClient client=null;
    public static final String CHANNEL_ID="hidden_line_communicator_notification_service";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        InetSocketAddress proxyAddr = new InetSocketAddress("127.0.0.1", 9050);
        Proxy proxyTor = new Proxy(Proxy.Type.SOCKS, proxyAddr);

        OkHttpClient.Builder builder = new OkHttpClient.Builder().proxy(proxyTor).connectTimeout(30, TimeUnit.SECONDS);
        client = builder.build();
    }

    private void createNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel=new NotificationChannel(
                    CHANNEL_ID,
                    "Hidden Line Communicator service channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    }

}