package io.branch.referral.network;

import android.content.Context;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import io.branch.referral.BranchError;
import io.branch.referral.PrefHelper;

/**
 * Created by sojanpr on 5/31/17.
 * Class for implementing BranchRemoteInterface using the HttpUrlConnection.
 * This class provides implementation for Branch RESTful operations using HTTP URL Connection.
 */
public class BranchRemoteInterfaceUrlConnection extends BranchRemoteInterface {
    private static final int DEFAULT_TIMEOUT = 3000;
    PrefHelper prefHelper;

    BranchRemoteInterfaceUrlConnection(Context context) {
        prefHelper = PrefHelper.getInstance(context);
    }

    @Override
    public BranchResponse doRestfulGet(String url) throws BranchRemoteException {
        return doRestfulGet(url, 0);
    }

    @Override
    public BranchResponse doRestfulPost(String url, JSONObject payload) throws BranchRemoteException {
        return doRestfulPost(url, payload, 0);
    }

    ///-------------- private methods to implement RESTful GET / POST using HttpURLConnection ---------------//
    private BranchResponse doRestfulGet(String url, int retryNumber) throws BranchRemoteException {
        HttpsURLConnection connection = null;
        try {
            int timeout = prefHelper.getTimeout();
            if (timeout <= 0) {
                timeout = DEFAULT_TIMEOUT;
            }
            String appendKey = url.contains("?") ? "&" : "?";
            String modifiedUrl = url + appendKey + RETRY_NUMBER + "=" + retryNumber;
            URL urlObject = new URL(modifiedUrl);
            connection = (HttpsURLConnection) urlObject.openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);

            int responseCode = connection.getResponseCode();
            if (responseCode >= 500 &&
                    retryNumber < prefHelper.getRetryCount()) {
                try {
                    Thread.sleep(prefHelper.getRetryInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                retryNumber++;
                return doRestfulGet(url, retryNumber);
            } else {
                try {
                    if (responseCode != HttpsURLConnection.HTTP_OK && connection.getErrorStream() != null) {
                        return new BranchResponse(getResponseString(connection.getErrorStream()), responseCode);
                    } else {
                        return new BranchResponse(getResponseString(connection.getInputStream()), responseCode);
                    }
                } catch (FileNotFoundException ex) {
                    // In case of Resource conflict getInputStream will throw FileNotFoundException. Handle it here in order to send the right status code
                    PrefHelper.Debug("BranchSDK", "A resource conflict occurred with this request " + url);
                    return new BranchResponse(null, responseCode);
                }
            }
        } catch (SocketException ex) {
            PrefHelper.Debug(getClass().getSimpleName(), "Http connect exception: " + ex.getMessage());
            throw new BranchRemoteException(BranchError.ERR_BRANCH_NO_CONNECTIVITY);

        } catch (SocketTimeoutException ex) {
            // On socket  time out retry the request for retryNumber of times
            if (retryNumber < prefHelper.getRetryCount()) {
                try {
                    Thread.sleep(prefHelper.getRetryInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                retryNumber++;
                return doRestfulGet(url, retryNumber);
            } else {
                throw new BranchRemoteException(BranchError.ERR_BRANCH_REQ_TIMED_OUT);
            }
        } catch (IOException ex) {
            PrefHelper.Debug(getClass().getSimpleName(), "Branch connect exception: " + ex.getMessage());
            throw new BranchRemoteException(BranchError.ERR_BRANCH_NO_CONNECTIVITY);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    private BranchResponse doRestfulPost(String url, JSONObject payload, int retryNumber) throws BranchRemoteException {
        HttpURLConnection connection = null;
        int timeout = prefHelper.getTimeout();
        if (timeout <= 0) {
            timeout = DEFAULT_TIMEOUT;
        }
        try {
            payload.put(RETRY_NUMBER, retryNumber);
        } catch (JSONException ignore) {
        }
        try {
            URL urlObject = new URL(url);
            connection = (HttpURLConnection) urlObject.openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());

            outputStreamWriter.write(payload.toString());
            outputStreamWriter.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode >= HttpsURLConnection.HTTP_INTERNAL_ERROR
                    && retryNumber < prefHelper.getRetryCount()) {
                try {
                    Thread.sleep(prefHelper.getRetryInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                retryNumber++;
                return doRestfulPost(url, payload, retryNumber);
            } else {
                try {
                    if (responseCode != HttpsURLConnection.HTTP_OK && connection.getErrorStream() != null) {
                        return new BranchResponse(getResponseString(connection.getErrorStream()), responseCode);
                    } else {
                        return new BranchResponse(getResponseString(connection.getInputStream()), responseCode);
                    }
                } catch (FileNotFoundException ex) {
                    // In case of Resource conflict getInputStream will throw FileNotFoundException. Handle it here in order to send the right status code
                    PrefHelper.Debug("BranchSDK", "A resource conflict occurred with this request " + url);
                    return new BranchResponse(null, responseCode);
                }
            }


        } catch (SocketTimeoutException ex) {
            // On socket  time out retry the request for retryNumber of times
            if (retryNumber < prefHelper.getRetryCount()) {
                try {
                    Thread.sleep(prefHelper.getRetryInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                retryNumber++;
                return doRestfulPost(url, payload, retryNumber);
            } else {
                throw new BranchRemoteException(BranchError.ERR_BRANCH_REQ_TIMED_OUT);
            }
        } catch (IOException ex) {
            PrefHelper.Debug(getClass().getSimpleName(), "Http connect exception: " + ex.getMessage());
            throw new BranchRemoteException(BranchError.ERR_BRANCH_NO_CONNECTIVITY);
        } catch (Exception ex) {
            PrefHelper.Debug(getClass().getSimpleName(), "Exception: " + ex.getMessage());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                if (ex instanceof NetworkOnMainThreadException)
                    Log.i("BranchSDK", "Branch Error: Don't call our synchronous methods on the main thread!!!");
            }
            return new BranchResponse(null, 500);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    private String getResponseString(InputStream inputStream) {
        String responseString = null;
        if (inputStream != null) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            try {
                responseString = rd.readLine();
            } catch (IOException ignore) {
            }
        }
        return responseString;
    }

}
