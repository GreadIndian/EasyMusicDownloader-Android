package com.kaiv.easymusicdownloaderplus.Model;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;

import com.kaiv.easymusicdownloaderplus.MainActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FindMusicStrategy {

    public static LinkedList<Track> namesTopHitsList = new LinkedList<>();
    public static CopyOnWriteArraySet<Track> trackListFinal = new CopyOnWriteArraySet<>();
    public static CountDownLatch latch;


    public static void getTracksFromShazam(String url) throws IOException, JSONException {

        JSONObject allShazamTracksObjects = readJsonFromUrl(url);
        JSONArray chartList = allShazamTracksObjects.getJSONArray("chart");

        int id = 0;
        for (int i = 0; i < chartList.length(); i++) {
            String singer = "";
            String title = "";

            JSONObject oneElement = chartList.getJSONObject(i);
            JSONObject headingElement = oneElement.getJSONObject("heading");
            title = headingElement.get("title").toString();
            singer = headingElement.get("subtitle").toString();

            Track track = new Track();
            track.id = id++;
            track.title = title;
            track.artist = singer;
            namesTopHitsList.add(track);
        }
    }


    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }


    public static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static void createListForTopHits(LinkedList<Track> namesTopHitsList, final UpdateScreen updateScreen, final MainActivity mainActivity) {

        int findLinksProgress = 0;
        int listSize = namesTopHitsList.size();
        int grade = 100 / listSize;

        latch = new CountDownLatch(listSize);

        for (Track oneTrack : namesTopHitsList) {

            final int trackId = oneTrack.id;

            final String searchedTrack = oneTrack.artist + " - " + oneTrack.title;

            Thread thread = new Thread(String.valueOf(latch)) {
                @Override
                public void run() {
                    LinksFinder.getTracksFromSoundCloud(searchedTrack, trackId, mainActivity, updateScreen);
                }
            };
            thread.start();

            findLinksProgress = findLinksProgress + grade;
            MainActivity.mProgress.setProgress(findLinksProgress);
        }

        try {
            latch.await();
        } catch (Exception e) {
        }
    }


}
