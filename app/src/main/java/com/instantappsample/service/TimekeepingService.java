/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instantappsample.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Service} that keeps track of it's own running time.
 */

public class TimekeepingService extends Service implements HttpRequestAsync.RvulResult {
    class RvulBinder extends Binder {
        @SuppressLint("SimpleDateFormat")
        private SimpleDateFormat mFormatter = new SimpleDateFormat("mm:ss.SSS");

        RvulData getRvulData() {
            return mRvulData;
        }

        long getTimeRunning() {
            return System.currentTimeMillis() - mStartTimeMillis;
        }

        long getScrapeIntervalMillis() {
            return mScrapeIntervalMillis;
        }

        void setScrapeIntervalMillis(long value) {
            mScrapeIntervalMillis = value;
        }

        String getTimeRunningFormatted() {
            return mFormatter.format(new Date(getTimeRunning()));
        }
    }

    private long mScrapeIntervalMillis = 1000;//1000 * 60 * 60; // 1h
    private long mNextScrape = 0;
    private long mStartTimeMillis = 0;

    private RvulData mRvulData = null;

    private RvulBinder mBinder = new RvulBinder();

    @Override
    public void onResult(RvulData value) {
        mRvulData = value;
    }

    @NonNull
    public static Intent getIntent(Context context) {
        return new Intent(context, TimekeepingService.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        doSomething();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        doSomething();
        return mBinder;
    }

    private void doSomething() {
        if (mStartTimeMillis == 0) {
            mStartTimeMillis = System.currentTimeMillis();
            mNextScrape = mStartTimeMillis + mScrapeIntervalMillis;
        }
        scrapeRvul();
    }

    private void scrapeRvul() {
        while (true) {
            try {
                long delta = mNextScrape - System.currentTimeMillis();
                if (delta > 0) {
                    Thread.sleep(Math.min(delta / 2, 10));
                    continue;
                }
                mNextScrape = System.currentTimeMillis() + mScrapeIntervalMillis;

                new HttpRequestAsync(this).execute();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public HttpURLConnection getBaseConnection(String url, String method) throws IOException {
        URL obj = new URL( url );

        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
        conn.setDoOutput( true );
        conn.setInstanceFollowRedirects( false );
        conn.setRequestMethod( method );
        conn.setReadTimeout(5000);
        conn.setRequestProperty( "charset", getString(R.string.request_property_charset));
        conn.addRequestProperty("User-Agent", getString(R.string.request_property_user_agent));

        return conn;
    }

    public HttpURLConnection getBasePostConnection(String url) throws IOException {
        HttpURLConnection conn = getBaseConnection(url, "POST");
        return conn;
    }

    public String postJson(String url, String payload) throws IOException {

        URL obj = new URL(url);

        HttpURLConnection conn = getBasePostConnection(url);
        conn.addRequestProperty("Accept-Language", getString(R.string.request_property_accept_language));
        conn.addRequestProperty("Referer", "google.com");

        OutputStreamWriter w = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);

        w.write(payload);
        w.close();

        System.out.println("Request URL ... " + url);

        int status = conn.getResponseCode();

        System.out.println("Response Code ... " + status);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        conn.disconnect();

        return content.toString();
    }
}
