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

/**
 * Created by michal on 15.03.18.
 */

public class MessagesAdapter extends BaseAdapter {

    private ArrayList<String>messageList;
    private ArrayList<String>indexList;
    private LayoutInflater li;
    Context context;
    private Handler handler;
    private int availableItems;

    final int RETURNED_MESSAGES = 3; // TRY TO SET THIS VALUE SAME AS IN MAINACTIVITY

    public MessagesAdapter(Context context) {
        super();
        this.context = context;
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        getHandler();
        messageList = new ArrayList<String>(10);
        indexList = new ArrayList<String>(10);
        fetchData();
    }

    @Override
    public int getCount() {
//        return messageList.size();
        return 10;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView != null) {
            return convertView;
        }
        View rowView = li.inflate(R.layout.message_view, parent, false);
        TextView messageText = (TextView) rowView.findViewById(R.id.messageText);
        TextView indexText = (TextView) rowView.findViewById(R.id.indexText);
//        messageText.setText(messageList.get(position));
        messageText.setText(messageList.get(position));
        indexText.setText(indexList.get(position));
        return rowView;
    }

    public void fetchData() {
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
                        int a = 5;
                        messageList.add(o.get("message").toString());
                        indexList.add(o.get("id").toString());
                    }

                    Message msg = handler.obtainMessage(RETURNED_MESSAGES, jObject);
                    msg.sendToTarget();

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        notifyDataSetChanged();
    }

    private Handler getHandler() {

        if (handler == null) {

            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message inputMessage) {

                    switch (inputMessage.what) {
                        case RETURNED_MESSAGES:
                            Toast.makeText(context, "returned messages", Toast.LENGTH_SHORT).show();
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
