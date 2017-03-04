package com.kaiv.easymusicdownloaderplus.Model;

import com.kaiv.easymusicdownloaderplus.MainActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import static com.kaiv.easymusicdownloaderplus.Model.FindMusicStrategy.*;

public class LinksFinder {

    public static String URL_FORMAT_soundCloud = "https://api-v2.soundcloud.com/search/tracks?q=%s&client_id=fDoItMDbsbZz8dY16ZzARCZmzgHBPotA&limit=1";
    private static String ULR_FORMAT_soundCloudPlayLink = "https://api.soundcloud.com/tracks/%s/stream?&client_id=fDoItMDbsbZz8dY16ZzARCZmzgHBPotA";
    private static final String URL_FORMAT_MP3NOTA = "http://mp3nota.com/poisk?q=%s";
    private static final String URL_FORMAT_muzofond = "https://muzofond.com/search/%s";
    private static int trackIdCommon = 0;


    public static void getSongLinkFromMp3nota(String searhString, String searhArtist, String searhTitle, int SearchId) throws IOException {

        try {
            String url = String.format(URL_FORMAT_MP3NOTA, searhString);
            Document document = Jsoup.connect(url).userAgent("Mozilla/5.0 jsoup").referrer("www.google.com.ua").get();
            Element artistElement = document.select("[class=song]").first();
            Element urlElement = artistElement.select("a").first();
            String songInternalPath = urlElement.attr("href");
            int pathLength = songInternalPath.split("/").length;
            String internalSongName = songInternalPath.split("/")[pathLength - 1];
            URL songLink = new URL("http://mp3nota.com/dwn/" + internalSongName);

            Track track = new Track();
            track.id = SearchId;
            track.title = searhTitle;
            track.artist = searhArtist;
            track.url = songLink;
            trackListFinal.add(track);

            latch.countDown();
        } catch (Exception e) {
            latch.countDown();
            e.printStackTrace();
        }
    }


    public static String getOneSongLinkFromMuzofond(String searhString) throws IOException {
        String url = String.format(URL_FORMAT_muzofond, searhString);
        Document document = Jsoup.connect(url).userAgent("Mozilla/5.0").referrer("www.google.com.ua").get();
        Element trackClassElement = document.select("[class=item]").first();
        String song = trackClassElement.select("[class=dl]").attr("href");
        return song;
    }


    public static void getTracksFromSoundCloud(String searhString, int trackIdTopList, MainActivity mainActivity, UpdateScreen updateScreen) {

        try {

            String url = String.format(URL_FORMAT_soundCloud, URLEncoder.encode(searhString, "UTF-8"));

            JSONObject allSoundCloudtracks = readJsonFromUrl(url);
            JSONArray arr = allSoundCloudtracks.getJSONArray("collection");
            int arrLength = arr.length();
            int grade = 100 / arrLength;
            if (arrLength >= 1) {
                for (int i = 0; i < arr.length(); i++) {
                    String oneStringElement = arr.get(i).toString();
                    JSONObject oneJsonElement = new JSONObject(oneStringElement);
                    String trackId = oneJsonElement.get("id").toString();
                    String trackSingerAndTitle = oneJsonElement.get("title").toString();

                    String trackSinger = "";
                    String trackTitle = "";

                    if (trackSingerAndTitle.contains("-")) {
                        trackSinger = trackSingerAndTitle.split("-")[0].trim();
                        trackTitle = trackSingerAndTitle.split("-")[1].trim();
                    } else if (trackSingerAndTitle.contains("/")) {
                        trackSinger = trackSingerAndTitle.split("/")[0].trim();
                        trackTitle = trackSingerAndTitle.split("/")[1].trim();
                    } else {
                        trackTitle = trackSingerAndTitle;
                    }

                    String songLink = String.format(ULR_FORMAT_soundCloudPlayLink, trackId);

                    Track track = new Track();
                    track.id = trackIdCommon++;
                    track.title = trackTitle;
                    track.artist = trackSinger;
                    track.url = new URL(songLink);

                    track.id = trackIdTopList;
                    FindMusicStrategy.trackListFinal.add(track);
                }
            }
        } catch (Exception e) {
            updateScreen.changeVisibility(mainActivity.mProgress, 8);
        }
        finally {

            latch.countDown();
        }
    }


    public static void getTracksFromMuzofond(String searhString, FindMusicStrategy findMusicStrategy, MainActivity mainActivity, UpdateScreen updateScreen) {

        String url = String.format(URL_FORMAT_muzofond, searhString);

        try {

            Document document = Jsoup.connect(url).userAgent("Mozilla/5.0").referrer("www.google.com.ua").get();

            if (document != null) {
                Elements trackClassElements = document.select("[class=item]");
                int id = 0;
                int findLinksProgress = 0;

                if (trackClassElements.size() <= 2) {
                    updateScreen.showInfoToast("Nothing was found");
                } else {
                    for (Element element : trackClassElements) {

                        String foundedSinger = "";
                        String foundedTitle = "";
                        String foundedUrl = "";

                        foundedUrl = element.select("[class=dl]").attr("href");

                        foundedSinger = element.select("[class=artist]").text();

                        foundedTitle = element.select("[class=track]").text();

                        Track track = new Track();
                        track.id = id++;
                        track.title = foundedSinger;
                        track.artist = foundedTitle;
                        track.url = new URL(foundedUrl);

                        int a = 9;
                        findMusicStrategy.trackListFinal.add(track);

                        mainActivity.mProgress.setProgress(findLinksProgress++);
                    }
                }
                mainActivity.mProgress.setProgress(100);
            } else {
                updateScreen.showInfoToast("Nothing was found");
                mainActivity.mProgress.setProgress(100);
                updateScreen.changeVisibility(mainActivity.mProgress, 8);
            }

        } catch (Exception e) {
            updateScreen.showInfoToast("Nothing was found");
            mainActivity.mProgress.setProgress(100);
            updateScreen.changeVisibility(mainActivity.mProgress, 8);
        }
    }
}
