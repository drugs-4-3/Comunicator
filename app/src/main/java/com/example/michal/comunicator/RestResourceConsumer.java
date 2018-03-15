package com.example.michal.comunicator;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by michal on 11.03.18.
 */

public class RestResourceConsumer {

    private URL service;
    private HttpURLConnection connection;

    public RestResourceConsumer() {
        try {
            service = new URL("http://e-biuro.net/android10/messages/");
            connection = (HttpURLConnection) service.openConnection();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getMessages() {

        try {
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String json = reader.readLine();
            JSONObject jObject = new JSONObject(json);
            return jObject.getString("AvailableItems");
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
