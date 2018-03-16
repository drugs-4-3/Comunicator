package com.example.michal.comunicator;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private Handler handler = null;
    private Button button = null;
    private EditText editText = null;
    private ListView listView = null;
    private MessagesAdapter adapter = null;

    private int message_count = 0;

    private Runnable refreshList;

    private static final int RETRIEVED_MESSAGE_COUNT = 0;
    private static final int RETRIEVED_MESSAGES = 1;
    private static final int PUT_MESSAGE_EXECUTED = 2;
    private static final int RETURNED_MESSAGES = 3;
    private static final int MESSAGES_UPDATED = 4;
    private static final int MESSAGE_COUNT_CHANGED = 5;

    private Retrofit retrofit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = getHandler();
        button = (Button) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.editText);
        listView = (ListView)findViewById(R.id.listView);
        adapter = new MessagesAdapter(getApplicationContext(), handler);

        listView.setAdapter(adapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = editText.getText().toString();
                editText.setText("");
                putNewMessage(txt);
            }
        });

        refreshList = new Runnable() {
            @Override
            public void run() {
                adapter.fetchData();
                listView.invalidateViews();
                listView.refreshDrawableState();
            }
        };

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    getMessageCount();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 5, TimeUnit.SECONDS);

    }

    private void getMessageCount() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL service = new URL("http://e-biuro.net/android10/messages/");
                    HttpURLConnection connection = (HttpURLConnection) service.openConnection();

                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    String json = reader.readLine();

                    JSONObject jObject = new JSONObject(json);
                    String t = jObject.getString("AvailableItems");

                    Message msg = handler.obtainMessage(RETRIEVED_MESSAGE_COUNT, t);
                    msg.sendToTarget();

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void putNewMessage(final String msgTxt) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL service = new URL("http://e-biuro.net/android10/messages/" + msgTxt);
                    HttpURLConnection connection = (HttpURLConnection) service.openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestMethod("PUT");
                    connection.connect();
                    OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                    out.write(msgTxt);
                    out.close();

                    connection.getInputStream();
                    int response = connection.getResponseCode();
                    Message msg = handler.obtainMessage(PUT_MESSAGE_EXECUTED, response);
                    msg.sendToTarget();
                    adapter.fetchData();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Handler getHandler() {

        if (handler == null) {

            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message inputMessage) {

                    switch (inputMessage.what) {
                        case RETRIEVED_MESSAGE_COUNT:
                            int new_message_count = Integer.valueOf((String) inputMessage.obj);
                            if (new_message_count != message_count) {
                                Message m = this.obtainMessage(MESSAGE_COUNT_CHANGED, new_message_count);
                                m.sendToTarget();
                                Log.i("comunicator", "nowa wiadomosc");
                            }
                            else {
                                Log.i("comunicator", "brak nowych wiadomosci");
                            }
//                            Toast.makeText(getApplicationContext(), "msg count: " + (String)inputMessage.obj, Toast.LENGTH_SHORT).show();
                            break;
                        case RETRIEVED_MESSAGES:
                            break;
                        case PUT_MESSAGE_EXECUTED:
                            Toast.makeText(getApplicationContext(), "HTTP response: " + Integer.toString((Integer)inputMessage.obj), Toast.LENGTH_SHORT).show();
                            break;
                        case RETURNED_MESSAGES:
                            adapter.notifyDataSetChanged();
                            listView.invalidateViews();
                            listView.refreshDrawableState();
                            listView.setSelection(adapter.getCount() - 1);
                            break;
                        case MESSAGES_UPDATED:
                            adapter.fetchData();
                            Toast.makeText(getApplicationContext(), "MESSAGES UPDATED", Toast.LENGTH_SHORT).show();
                            break;
                        case MESSAGE_COUNT_CHANGED:
                            message_count = (Integer)inputMessage.obj;
                            Message msg = this.obtainMessage(MESSAGES_UPDATED, null);
                            msg.sendToTarget();
                        default:
                            break;
                    }
                }
            };
        }

        return handler;
    }
}
