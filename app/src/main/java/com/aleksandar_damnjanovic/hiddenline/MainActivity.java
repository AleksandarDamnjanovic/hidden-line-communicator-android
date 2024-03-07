package com.aleksandar_damnjanovic.hiddenline;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static boolean connected = false;
    public static boolean handler = false;
    public static Request request;
    public static String res = "";
    public static ListView contacts;
    public static List<String> contactList;
    static TextView connectToServer;

    public static Activity activity;

    Dialog loadConf;
    Dialog saveConf;
    Spinner loadSpinner;
    TextView loadOK;
    TextView loadCancel;
    TextView loadRemove;

    public static boolean visible=false;

    public static String ad = "";
    public static String un = "";
    public static String pass = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView menuButton = findViewById(R.id.menu_button);
        PopupMenu popup;

        Context wrapper = new ContextThemeWrapper(MainActivity.this, R.style.myPopupStyle);
        popup = new PopupMenu(wrapper, menuButton);
        popup.getMenuInflater().inflate(R.menu.top_menu, popup.getMenu());

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.show();
            }
        });

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                Intent web;

                switch (item.getItemId()) {

                    case R.id.save:

                        if (!ad.equals("") || !un.equals("") || !pass.equals("")) {
                            saveConf.show();
                        } else {
                            Toast.makeText(getBaseContext(), "You must be connected to server in order to save configuration", Toast.LENGTH_SHORT).show();
                        }

                        break;

                    case R.id.load:

                        List<String[]>list=getConfigurations();
                        for(String[] s:list)
                            if(s[0].contains("_"))
                                s[0]=s[0].replace("_"," ");

                        String confs[]=new String[list.size()];
                        for(int i=0;i<list.size();i++)
                            confs[i]=list.get(i)[0];

                        reloadConfSpinner(confs);

                        break;

                    case R.id.web:

                        web = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.web)));
                        web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getBaseContext().startActivity(web);

                        break;

                    case R.id.legal:

                        web = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.web) + "legal.html"));
                        web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getBaseContext().startActivity(web);

                        break;

                    case R.id.instructions:

                        web = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.web) + "tech.html"));
                        web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getBaseContext().startActivity(web);

                        break;

                }

                return true;
            }
        });

        contacts = findViewById(R.id.contact_list);
        contactList = new ArrayList();

        activity = MainActivity.this;

        connectToServer = findViewById(R.id.connect_to_server);
        Dialog connectToServerDialog = new Dialog(this);
        connectToServerDialog.setCancelable(false);
        connectToServerDialog.setContentView(R.layout.dialog_edit_server);
        TextView connectToServerCancel = connectToServerDialog.findViewById(R.id.connect_to_server_dialog_cancel_button);
        TextView connectToServerConnect = connectToServerDialog.findViewById(R.id.connect_to_server_dialog_connect_button);
        EditText onionAddressFD = connectToServerDialog.findViewById(R.id.onion_address_field_dialog);
        EditText usernameFD = connectToServerDialog.findViewById(R.id.username_field_dialog);
        EditText passwordFD = connectToServerDialog.findViewById(R.id.password_field_dialog);

        Dialog connectingMessageDialog = new Dialog(this);
        connectingMessageDialog.setCancelable(false);
        connectingMessageDialog.setContentView(R.layout.dialog_connecting);
        TextView connectionMessage = connectingMessageDialog.findViewById(R.id.connecting_dialog_message);
        String connectionOriginalMessage = "Connecting\nPlease wait\n";

        connectToServerConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                res = "";

                ad = onionAddressFD.getText().toString();
                un = usernameFD.getText().toString();
                pass = passwordFD.getText().toString();

                if (ad.equals("") || un.equals("") || pass.equals(""))
                    return;

                HttpUrl url = null;
                try {
                    url = new HttpUrl.Builder()
                            .scheme("http")
                            .host(ad)
                            .addPathSegment("mainHoll")
                            .addQueryParameter("id", un)
                            .addQueryParameter("code", pass)
                            .addQueryParameter("mobile", "true")
                            .build();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                request = new Request.Builder()
                        .url(url)
                        .build();

                connectToServerDialog.dismiss();

                connectingMessageDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        while (res.equals("")) {

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    connectionMessage.setText(connectionMessage.getText().toString() + ".");
                                    if (connectionMessage.getText().toString().endsWith("......"))
                                        connectionMessage.setText(connectionOriginalMessage);

                                }
                            });

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }

                        connectingMessageDialog.dismiss();

                    }
                }).start();

                App.client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        res = "CONNECTION ERROR";
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        res = response.body().string();
                    }
                });
            }
        });

        connectingMessageDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (res.equals("REQUEST ACCEPTED")) {
                    connected = true;
                    Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
                } else {
                    connected = false;
                    Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
                }

                if (connected && !handler)
                    communicationHandler();

                connectionMessage.setText(connectionOriginalMessage);
            }
        });

        connectToServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onionAddressFD.setText(ad);
                usernameFD.setText(un);
                passwordFD.setText(pass);

                if (connectToServer.getText().toString().equals("Connect")) {
                    connectToServerDialog.show();
                } else {
                    connectToServer.setText("Connect");
                    connected = false;
                    contactList = new ArrayList<>();

                    Intent service = new Intent(getApplicationContext(), ReceiverService.class);
                    getApplicationContext().stopService(service);
                }
            }
        });

        connectToServerCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToServerDialog.dismiss();
            }
        });

        contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), ChatRoom.class);
                intent.putExtra("userName", un);
                intent.putExtra("password", pass);
                intent.putExtra("address", ad);
                intent.putExtra("chatWith", contactList.get(position));
                startActivity(intent);

                Toast.makeText(getApplicationContext(), contactList.get(position), Toast.LENGTH_SHORT).show();
            }
        });

        saveConf = new Dialog(this);
        saveConf.setCancelable(false);
        saveConf.setContentView(R.layout.dialog_save);

        EditText saveConfName = saveConf.findViewById(R.id.configuration_name_save_field);
        TextView saveConfSave = saveConf.findViewById(R.id.save_configuration_name);
        TextView cancelConfSave = saveConf.findViewById(R.id.cancel_configuration_name);

        cancelConfSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConf.dismiss();
            }
        });

        saveConfSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = saveConfName.getText().toString();

                if(name.contains(" "))
                    name=name.replace(" ","_");

                saveConfiguration(name, ad, un, pass);
                saveConf.dismiss();
            }
        });

        loadConf=new Dialog(this);
        loadConf.setContentView(R.layout.dialog_load);
        loadConf.setCancelable(false);

        loadSpinner=loadConf.findViewById(R.id.configuration_load_spinner);
        loadOK=loadConf.findViewById(R.id.save_configuration_load);
        loadCancel=loadConf.findViewById(R.id.cancel_configuration_load);
        loadRemove=loadConf.findViewById(R.id.delete_configuration_load);

        loadCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadConf.dismiss();
            }
        });

        loadOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loadSpinner.getSelectedItem()!=null){
                    String name=loadSpinner.getSelectedItem().toString();
                    List<String[]>list=getConfigurations();

                    for(String s[]:list){
                        String check=s[0].contains("_")?s[0].replace("_"," "):s[0];
                        if(check.equals(name)){
                            ad=s[1];
                            un=s[2];
                            pass=s[3];
                        }
                    }

                    Toast.makeText(getBaseContext(),"Configuration "+name+" loaded",Toast.LENGTH_SHORT).show();
                        loadConf.dismiss();

                }
            }
        });

        loadRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loadSpinner.getSelectedItem()!=null){
                    String name=loadSpinner.getSelectedItem().toString();
                    removeConf(name);
                }
            }
        });

        if(connected)
            connectToServer.setText("Disconnect");

    }

    @Override
    protected void onStart() {
        super.onStart();

        visible=true;

    }

    @Override
    protected void onPause() {
        super.onPause();

        visible=false;

    }

    private void communicationHandler() {
        Intent service = new Intent(getApplicationContext(), ReceiverService.class);
        getApplicationContext().startService(service);
    }

    public static void setButtonText(String text) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectToServer.setText(text);
            }
        });
    }

    public static void clearContacts() {
        ArrayAdapter adapter = new ArrayAdapter(activity, R.layout.single_contact_view, new ArrayList());
        contacts.setAdapter(adapter);
        contacts.invalidate();
    }

    private boolean containsConfiguration(String address) {
        List<String[]> configurations = getConfigurations();

        if (configurations.size() == 0)
            return false;

        for (int i = 0; i < configurations.size(); i++) {
            if (configurations.get(i)[1].equals(address))
                return true;
        }

        return false;
    }

    private ArrayList<String[]> getConfigurations() {

        File file =  new File(getFilesDir()+"/configurations.conf");

        if (file.exists()) {

            if (file.length() == 0)
                return new ArrayList<String[]>();

            List<String[]> list = new ArrayList<>();
            try {
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data, 0, data.length);
                String full = new String(data, "UTF8");
                String[] lines = full.split("\\n");

                for (int i = 0; i < lines.length; i++) {
                    String[] line = lines[i].split("\\s");
                    list.add(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return (ArrayList<String[]>) list;
        } else {
            return new ArrayList<String[]>();
        }

    }

    private void saveConfiguration(String name, String address, String userName, String password) {

        List<String[]> configurations = getConfigurations();
        boolean overwritten=false;

        if (containsConfiguration(address))
            loop:for (int i = 0; i < configurations.size(); i++)
                if (configurations.get(i)[1].equals(address)) {
                    configurations.set(i, new String[]{name, address, userName, password});
                    overwritten=true;
                    break loop;
                }

        File file =  new File(getFilesDir()+"/configurations.conf");

        String full = "";
        for (String[] s : configurations) {
            for (String ss : s)
                full += ss + " ";

            full += "\n";
        }

        if(!overwritten)
            full += name + " " + address + " " + userName + " " + password;

        byte[] data = full.getBytes();

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data, 0, data.length);
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeConf(String name){
        List<String[]> configurations = getConfigurations();

        int index=-1;
            loop:for (int i = 0; i < configurations.size(); i++)
                if (configurations.get(i)[0].equals(name)) {
                    index=i;
                    break loop;
                }

        if(index==-1)
            return;

        configurations.remove(index);

        File file =  new File(getFilesDir()+"/configurations.conf");

        String full = "";
        for (String[] s : configurations) {
            for (String ss : s)
                full += ss + " ";

            full += "\n";
        }

        byte[] data = full.getBytes();

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data, 0, data.length);
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        String confs[]=new String[configurations.size()];

        for(int i=0;i<configurations.size();i++)
            confs[i]=configurations.get(i)[0];

        reloadConfSpinner(confs);
    }

    private void reloadConfSpinner(String confs[]){
        ArrayAdapter spinnerAdapter=new ArrayAdapter(getBaseContext(), android.R.layout.simple_spinner_dropdown_item,confs);
        loadSpinner.setAdapter(spinnerAdapter);

        loadConf.show();
    }

}