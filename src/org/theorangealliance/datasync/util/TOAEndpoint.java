package org.theorangealliance.datasync.util;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Kyle Flynn on 11/28/2017.
 */
public class TOAEndpoint implements Runnable {

    private final String BASE_URL = "http://localhost:8080/apiv2/";
    private String endpoint;
    private String requestType;
    private String apiKey;
    private String eventKey;

    private Thread connection;
    private TOACompleteListener completeListener;

    public TOAEndpoint(String requestType, String endpoint) {
        this.requestType = requestType;
        this.endpoint = BASE_URL + endpoint;
        this.connection = new Thread(this, endpoint);
        this.apiKey = "";
        this.eventKey = "";
    }

    public TOAEndpoint(String endpoint) {
        this("GET", endpoint);
    }

    public void setCredentials(String apiKey, String eventKey) {
        this.apiKey = apiKey;
        this.eventKey = eventKey;
    }

    public void execute(TOACompleteListener completeListener) {
        this.connection.start();
        this.completeListener = completeListener;
    }

    @Override
    public void run() {
        Platform.runLater(() -> {
            try {
                URL url = new URL(this.endpoint);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod(this.requestType);
                con.setRequestProperty("X-Application-Origin", "TOA-DataSync");
                con.setRequestProperty("X-TOA-Key", this.apiKey);
                con.setRequestProperty("X-TOA-Event", this.eventKey);
                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    if (this.completeListener != null) {
                        this.completeListener.onComplete(response.toString(), true);
                    }
                } else {
                    if (this.completeListener != null) {
                        this.completeListener.onComplete(con.getResponseCode() + ": " + con.getResponseMessage(), false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.completeListener.onComplete(e.getLocalizedMessage(), false);
            }
        });
    }

    public interface TOACompleteListener {
        void onComplete(String response, boolean success);
    }

}
