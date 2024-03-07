package com.aleksandar_damnjanovic.hiddenline;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.state.State;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Request;

public class ChatRoom extends AppCompatActivity {

    String userName;
    String password;
    String address;
    String chatWith;
    LinearLayout container;
    List<String> messages;
    ScrollView scrollView;

    boolean scroll=false;
    boolean running=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        container=findViewById(R.id.message_container);
        TextView sendButton=findViewById(R.id.send_message);
        EditText messageContainer=findViewById(R.id.message);
        scrollView=findViewById(R.id.message_scroll);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(messageContainer.getText().toString());
                messageContainer.setText("");
            }
        });

        userName=getIntent().getStringExtra("userName");
        password=getIntent().getStringExtra("password");
        address=getIntent().getStringExtra("address");
        chatWith=getIntent().getStringExtra("chatWith");

        messages=new ArrayList<>();

        populateFullContainer(chatWith);
    }

    private void populateFullContainer(String ss){

        if(!running){
            running=true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (running){
                        ChatRoom.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                List<String>list=MessageHandler.getContactMessages(ss);

                                for(String s:list)
                                    if(!messages.contains(s)){
                                        messages.add(s);
                                        addElementToContainer(s);
                                        scroll=true;
                                    }

                                container.invalidate();
                            }
                        });

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if(scroll)
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);

                        scroll=false;
                    }
                }
            }).start();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        MainActivity.visible=true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainActivity.visible=false;
    }

    private void addElementToContainer(String text){
        TextView t=new TextView(this);
        t.setText(text);

        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(10,10,10,10);
        t.setPadding(10,10,10,10);

        if(text.split("\\n")[0].contains(userName)) {
            t.setBackground(getResources().getDrawable(R.drawable.chat_host_background));
            t.setTextColor(Color.argb(255,0,0,0));
            params.gravity=Gravity.LEFT|Gravity.START;
        }else{
            t.setBackground(getResources().getDrawable(R.drawable.chat_visitor_background));
            t.setTextColor(Color.argb(255,255,255,255));
            params.gravity=Gravity.RIGHT|Gravity.END;
        }

        t.setLayoutParams(params);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);

        container.addView(t);
    }

    private void sendMessage(String message){
        hlcMessage mess= new hlcMessage(message, chatWith);
        MessageHandler.addMessage(mess);
    }

}