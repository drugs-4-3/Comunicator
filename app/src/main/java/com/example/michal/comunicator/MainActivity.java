package com.example.michal.comunicator;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private Handler handler = null;
    private Button button = null;
    private EditText editText = null;
    private static final int RETRIEVED_MESSAGE_COUNT = 0;
    private static final int RETRIEVED_MESSAGES = 1;
    private static final int PUT_MESSAGE_EXECUTED = 2;

    private Retrofit retrofit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = getHandler();
        button = (Button) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.editText);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = editText.getText().toString();
                editText.setText("");
                putNewMessage(txt);
            }
        });


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

    private void putMessage(String txt) {

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
                            Toast.makeText(getApplicationContext(), "msg count: " + (String)inputMessage.obj, Toast.LENGTH_SHORT).show();
                            break;
                        case RETRIEVED_MESSAGES:
                            break;
                        case PUT_MESSAGE_EXECUTED:
                            Toast.makeText(getApplicationContext(), "HTTP response: " + Integer.toString((Integer)inputMessage.obj), Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                }
            };
        }

        return handler;
    }
}
