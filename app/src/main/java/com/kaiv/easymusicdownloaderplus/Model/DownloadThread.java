package com.kaiv.easymusicdownloaderplus.Model;

import android.widget.ProgressBar;
import com.kaiv.easymusicdownloaderplus.Controller;
import com.kaiv.easymusicdownloaderplus.MainActivity;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class DownloadThread extends Thread {

    private Controller controller;
    private String trackTitle;
    private ProgressBar progressBar;
    private String url;
    private String fileName;
    private UpdateScreen updateScreen;
    private MainActivity mainActivity;

    public DownloadThread(Controller controller, String trackTitle, ProgressBar progressBar, String url, String fileName, UpdateScreen updateScreen, MainActivity mainActivity) {
        this.controller = controller;
        this.trackTitle = trackTitle;
        this.progressBar = progressBar;
        this.url = url;
        this.fileName = fileName;
        this.updateScreen = updateScreen;
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {

        if (!controller.isOnline()) {
            updateScreen.showInfoToast("Check internet connection!");
        } else {


            updateScreen.showInfoToast("Downloading started");
            updateScreen.changeTextView(mainActivity.textViewInfo, "Track is downloading...");
            int downloadedSize = 0;
            try {
                URL urlSong = new URL(url);
                URLConnection connection = urlSong.openConnection();
                connection.connect();
                int totalFileSize = connection.getContentLength();

                try (FileOutputStream fileOutput = new FileOutputStream(new File(controller.programFolderPath, fileName));
                     InputStream inputStream = new BufferedInputStream(urlSong.openStream(), 8192);) {
                    byte[] buffer = new byte[1024];
                    long total = 0;

                    while ((downloadedSize = inputStream.read(buffer)) != -1) {
                        total += downloadedSize;
                        String publishProgress = ("" + (int) ((total * 100) / totalFileSize));
                        fileOutput.write(buffer, 0, downloadedSize);
                        progressBar.setProgress(Integer.parseInt(publishProgress));
                    }
                    String infoDownload = "Completed: \n" + trackTitle;
                    updateScreen.showInfoToast(infoDownload);
                    String folderInfo = "Saved to: " + controller.programFolderPath;
                    updateScreen.changeTextView(mainActivity.textViewInfo, folderInfo);
                } catch (IOException e) {
                    updateScreen.showInfoToast("This track is not available for downloading...");
                    updateScreen.changeTextView(mainActivity.textViewInfo, "Download failed. Please try another song...");
                }
            } catch (IOException e) {
                updateScreen.showInfoToast("Problem with writing track to phone's memory");
            }
        }
    }
}
