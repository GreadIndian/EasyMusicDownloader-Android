package com.kaiv.easymusicdownloaderplus.Model;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestUrlThread extends Thread {

    private String url;
    private UpdateScreen updateScreen;

    public TestUrlThread(String url, UpdateScreen updateScreen) {
        this.url = url;
        this.updateScreen = updateScreen;
    }

    @Override
    public void run() {
        try {
            URL urla = new URL(url);
            HttpURLConnection huc = (HttpURLConnection) urla.openConnection();
            huc.setRequestMethod("HEAD");
            huc.connect();
            updateScreen.urlResponceCode = huc.getResponseCode();
        } catch (IOException e) {
        }
    }
}
