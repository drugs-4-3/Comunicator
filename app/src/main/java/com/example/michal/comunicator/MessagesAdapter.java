package com.example.michal.comunicator;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by michal on 15.03.18.
 */

public class MessagesAdapter extends BaseAdapter {

    private ArrayList<String>messageList;
    private ArrayList<String>indexList;
    private LayoutInflater li;
    Context context;
    Handler parentHandler;
    private Handler handler;
    private int availableItems;

    final int RETURNED_MESSAGES = 3; // TRY TO SET THIS VALUE SAME AS IN MAINACTIVITY

    public MessagesAdapter(Context context, Handler handler) {
        super();
        this.context = context;
        this.parentHandler = handler;
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        getHandler();
        messageList = new ArrayList<String>(10);
        indexList = new ArrayList<String>(10);
        fetchData();
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        View rowView = li.inflate(R.layout.message_view, parent, false);
        TextView messageText = (TextView) rowView.findViewById(R.id.messageText);
        TextView indexText = (TextView) rowView.findViewById(R.id.indexText);

        messageText.setText(messageList.get(position));
        indexText.setText(indexList.get(position));
//        messageText.setText("asd");
//        indexText.setText("asd");
        return rowView;
    }

    public void fetchData() {
        indexList.clear();
        messageList.clear();
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
                    String avbIt = jObject.getString("AvailableItems");
                    availableItems = Integer.valueOf(avbIt);

                    JSONArray jsonMsgArr = jObject.getJSONArray("Messages");
                    for (int i = 0; i < jsonMsgArr.length(); i++) {
                        JSONObject o = (JSONObject) jsonMsgArr.get(i);
                        messageList.add(o.get("message").toString());
                        indexList.add(o.get("id").toString());
                    }

//                    Message msg = handler.obtainMessage(RETURNED_MESSAGES, jObject);
//                    msg.sendToTarget();
                    Message msg = parentHandler.obtainMessage(RETURNED_MESSAGES, jObject);
                    msg.sendToTarget();

                } catch (IOException | JSONException e) {
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
                        case RETURNED_MESSAGES:
                            notifyDataSetChanged();
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
