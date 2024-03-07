package com.aleksandar_damnjanovic.hiddenline;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

public class MessageHandler {

    private static List<Contact>contacts=new ArrayList<>();
    private static List<hlcMessage>messages=new ArrayList<>();
    private static String userName;
    private static String password;
    private static String address;

    public static void addMessage(hlcMessage message){
        messages.add(message);
    }

    public static List<String>getContactMessages(String contactName){
        loop:for(Contact cc: contacts)
            if(cc.getName().equals(contactName))
                return cc.getMessages();

        return new ArrayList<String>();
    }

    public static void clear(){
        contacts=new ArrayList<>();
        userName="";
        password="";
    }

    public static void provideList(List<String> contactsString, String un,String pass, String addr){
        userName=un;
        password=pass;
        address=addr;

        for(String s:contactsString){
            boolean match=false;
            loop:for(Contact c:contacts)
                if(c.getName().equals(s)){
                    match=true;
                    break loop;
                }

            if(!match){
                contacts.add(new Contact(s));
            }
        }

        List<Contact> toRemove=new ArrayList<>();
        for(Contact c:contacts){
            String n=c.getName();

            if(!contactsString.contains(n))
                for(Contact cc:contacts)
                    if(cc.getName().equals(n))
                        toRemove.add(cc);
        }

        for(Contact c:toRemove)
            contacts.remove(c);

    }

    public static void retrieveAllMessages(){
        for(Contact c:contacts)
            retrieveMessages(c);
    }

    public static Contact contactForName(String name){
        for(Contact c:contacts)
            if(c.getName().equals(name))
                return c;

        return null;
    }

    public static void retrieveMessages(Contact c){
        if(c==null)
            return;

        HttpUrl url=null;
        if(messages.size()==0){
            url=new HttpUrl.Builder()
                    .scheme("http")
                    .host(address)
                    .addPathSegment("chatRoom")
                    .addQueryParameter("userName",userName)
                    .addQueryParameter("password",password)
                    .addQueryParameter("chatWith",c.getName())
                    .addQueryParameter("reference", String.valueOf(c.getReference()))
                    .addQueryParameter("mobile","true")
                    .addQueryParameter("android","true")
                    .build();
        }else{

            if(!c.getName().equals(messages.get(0).getContactName()))
                return;

            url=new HttpUrl.Builder()
                    .scheme("http")
                    .host(address)
                    .addPathSegment("chatRoom")
                    .addQueryParameter("userName",userName)
                    .addQueryParameter("password",password)
                    .addQueryParameter("chatWith",c.getName())
                    .addQueryParameter("reference", String.valueOf(c.getReference()))
                    .addQueryParameter("submit","sentFromMobile")
                    .addQueryParameter("mobile","true")
                    .addQueryParameter("android","true")
                    .addQueryParameter("message",messages.get(0).getText())
                    .build();

            messages.remove(0);
        }

        Request request= new Request.Builder()
                .url(url)
                .build();

        App.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String value=response.body().string();

                if(value.equals(""))
                    return;
                else{
                    if(!MainActivity.visible)
                        notifyUser(c.getName());
                }

                String all[]=value.split("\n");

                for(int i=0;i<all.length;i++)
                    if(all[i].contains("|||||")){
                        String message[]=all[i].split("[|]{5}");
                        c.addMessage(message[0]+":\n"+message[1]);
                        c.setReference(c.getReference()+1);
                        c.setFlag(true);
                    }

            }
        });
    }

    private static void notifyUser(String chatWith){

        Intent notificationIntent = new Intent(MainActivity.activity, MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(MainActivity.activity,
                1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification=new NotificationCompat.Builder(
                MainActivity.activity,App.CHANNEL_ID)
                .setContentTitle("Hidden Line Communicator")
                .setContentText("New message from: "+chatWith)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notify)
                .build();

        synchronized (notification){
            NotificationManager manager=(NotificationManager)
                    MainActivity.activity.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(2,notification);
        }

    }

}