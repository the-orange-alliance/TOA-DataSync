package org.theorangealliance.datasync.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import org.theorangealliance.datasync.json.toa.ErrorJSON;
import org.theorangealliance.datasync.logging.TOALogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

/**
 * Created by Kyle Flynn on 11/28/2017.
 */
public class FIRSTEndpointNonLambda {

    private static final String BASE_URL = "http://" + Config.FIRST_API_IP + "/apiv1/";
    //    private final String BASE_URL = "http://localhost:8080/apiv2/";


    public static String getResp(String endpoint) {
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("HI-MY-NAME-IS", "SOREN-AND-IM-COOL");
            con.setRequestProperty("Content-Type","application/json");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                TOALogger.log(Level.WARNING, con.getResponseMessage());
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                ErrorJSON error = getGson().fromJson(response.toString(), ErrorJSON.class);
                TOALogger.log(Level.WARNING, "URL " + error.getUrl() + " returned " + error.getStatus() + ": " + error.getCode());

                return response.toString() + " NOT_READY";
            }
        } catch (IOException e) {
            TOALogger.log(Level.WARNING, e.getMessage());
            return "NOT_READY";
        }
    }

    public static Gson getGson() {
        return new GsonBuilder().serializeNulls().create();
    }
}
