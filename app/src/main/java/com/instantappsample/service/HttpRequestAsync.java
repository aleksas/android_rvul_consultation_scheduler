package com.instantappsample.service;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

public class HttpRequestAsync extends AsyncTask<Void, Void, RvulData> {

    public interface RvulResult
    {
        void onResult(RvulData value);
    }

    private Exception mException;
    private Context mContext;

    public HttpRequestAsync(Context context)
    {
        mContext = context;
    }

    public HttpURLConnection getBaseConnection(String url, String method) throws IOException {
        URL obj = new URL( url );

        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
        conn.setDoOutput( true );
        conn.setInstanceFollowRedirects( false );
        conn.setRequestMethod( method );
        conn.setReadTimeout(5000);
        conn.setRequestProperty("charset", mContext.getString(R.string.request_property_charset));
        conn.addRequestProperty("User-Agent", mContext.getString(R.string.request_property_user_agent));
        conn.addRequestProperty("Content-Type", mContext.getString(R.string.request_property_content_type));

        return conn;
    }

    public HttpURLConnection getBasePostConnection(String url) throws IOException {
        HttpURLConnection conn = getBaseConnection(url, "POST");
        return conn;
    }

    public String postJson(String url, String payload) throws IOException {
        HttpURLConnection conn = getBasePostConnection(url);

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

    private RvulData scrapeRvul() {
        try {
            String loadMedicalServiceListPayload = "{\"None\": null}";
            String medicalServiceListJson = postJson("http://www.rvul.lt/ab/abapi/loadMedicalServiceList", loadMedicalServiceListPayload);
            JSONArray medicalServiceList = new JSONArray(medicalServiceListJson);

            List<RvulService> rvulServices = new ArrayList<>();
            HashMap<Integer, RvulDoctor> rvulDoctors = new HashMap<>();

            for (int i = 0; i < medicalServiceList.length(); i++) {
                JSONObject service = medicalServiceList.getJSONObject(i);

                boolean serviceActive = service.getBoolean("active");
                System.out.println("" + serviceActive);
                int serviceId = service.getInt("id");
                String serviceName = service.getString("name");

                List<Integer> serviceDoctorIds = new ArrayList<>();

                String loadDoctorListPayload = "{\"medicalServiceId\":\"" + serviceId + "\"}";
                String doctorListtJson = postJson("http://www.rvul.lt/ab/abapi/loadDoctorList?id=" + serviceId, loadDoctorListPayload);
                JSONArray doctorList = new JSONArray(doctorListtJson);

                for (int j = 0; j < doctorList.length(); j++) {
                    JSONObject doctor = doctorList.getJSONObject(j);

                    int doctorId = doctor.getInt("id");
                    serviceDoctorIds.add(doctorId);

                    if (!rvulDoctors.containsKey(doctorId)) {
                        boolean doctorActive = doctor.getBoolean("active");
                        String doctorFirstName = doctor.getString("firstName");
                        String doctorLastName = doctor.getString("lastName");

                        String getAvailableDaysPayload = "{\"doctorId\":" + doctorId + "}";
                        String availableDaysJson = postJson("http://www.rvul.lt/ab/abapi/getAvailableDays?id=" + doctorId, getAvailableDaysPayload);
                        JSONObject availableDays = new JSONObject(availableDaysJson).getJSONObject("data");

                        HashMap<Date, RvulDoctor.WorkStatus> schedule = new HashMap<>();

                        if (availableDays.names() != null) {
                            for (int k = 0; k < availableDays.names().length(); k++) {
                                String stringDate = availableDays.names().getString(k);
                                String available = availableDays.getString(stringDate);

                                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(stringDate);

                                schedule.put(date, RvulDoctor.WorkStatus.valueOf(available));
                            }
                        }

                        rvulDoctors.put(doctorId, new RvulDoctor(doctorId, doctorFirstName, doctorLastName, doctorActive, schedule));
                    }
                }

                rvulServices.add(new RvulService(serviceId, serviceName, serviceDoctorIds.toArray(new Integer[0]), serviceActive));
            }

            return new RvulData(rvulServices, rvulDoctors.values());

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected RvulData doInBackground(Void... conns) {
        return scrapeRvul();
    }

    protected void onPostExecute(RvulData value) {

    }
}
