package com.kaiv.easymusicdownloaderplus.Model;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.NotificationCompat;
import android.widget.ProgressBar;
import com.kaiv.easymusicdownloaderplus.Controller;
import com.kaiv.easymusicdownloaderplus.MainActivity;
import com.kaiv.easymusicdownloaderplus.R;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import static com.kaiv.easymusicdownloaderplus.Controller.programFolderPath;
import static com.kaiv.easymusicdownloaderplus.Model.UpdateScreen.sortedTrackList;

public class DownloadAllThread extends Thread {

    private MainActivity mainActivity;
    private Controller controller;
    private ProgressBar progressBar;
    private UpdateScreen updateScreen;
    private int downloadedSize = 0;
    public volatile boolean isNotStopped = true;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyManager;
    int id = 1;

    public DownloadAllThread(Controller controller, MainActivity mainActivity, ProgressBar progressBar, UpdateScreen updateScreen) {
        this.controller = controller;
        this.mainActivity = mainActivity;
        this.progressBar = progressBar;
        this.updateScreen = updateScreen;
    }

    public void terminate() {
        isNotStopped = false;
    }

    @Override
    public void run() {

        mNotifyManager = (NotificationManager) mainActivity.getSystemService(mainActivity.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mainActivity);
        mBuilder.setContentTitle("Easy Music Downloader").setContentText("Download in progress").setSmallIcon(R.mipmap.download_icon);

        Intent notifyIntent = new Intent(mainActivity, MainActivity.class);
        notifyIntent.setAction(Intent.ACTION_MAIN);
        notifyIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(mainActivity, 0, notifyIntent, 0);
        mBuilder.setContentIntent(pendingIntent);

        checkInetIsMobile();

        updateScreen.changeTextAtButton(mainActivity.button, mainActivity.buttonStopDownload);

        updateScreen.showInfoToast("Downloading started");

        int downloadCounter = 0;

        mainActivity.mProgress.setProgress(0);
        mainActivity.mProgress.setMax(100);

        updateScreen.changeVisibility(mainActivity.mProgress, 0);
        updateScreen.changeVisibility(mainActivity.searchView, 8);
        updateScreen.changeVisibility(mainActivity.textViewSearchText, 0);

        updateScreen.changeTextView(mainActivity.textViewSearchText, "Downloading 1st file");

        for (Track currentTrack : sortedTrackList) {
            if (isNotStopped) {

                String downloadedCount = downloadCounter + " downloaded from " + sortedTrackList.size();
                mBuilder.setContentText(downloadedCount);
                mBuilder.setProgress(100, downloadCounter, false);
                mNotifyManager.notify(id, mBuilder.build());
                updateScreen.changeTextView(mainActivity.textViewSearchText, downloadedCount);

                String trackArtist = currentTrack.title;
                String trackTitle = currentTrack.artist;
                URL urlSong = currentTrack.url;
                String fileName = trackTitle + "-" + trackArtist + ".mp3";

                try {

                    URLConnection connection = urlSong.openConnection();
                    connection.connect();
                    int totalFileSize = connection.getContentLength();

                    FileOutputStream fileOutput = new FileOutputStream(new File(programFolderPath, fileName));
                    InputStream inputStream = new BufferedInputStream(urlSong.openStream(), 1024);


                    byte[] buffer = new byte[1024];
                    long total = 0;

                    while ((downloadedSize = inputStream.read(buffer)) != -1) {
                        total += downloadedSize;
                        String publishProgress = ("" + (int) ((total * 100) / totalFileSize));
                        fileOutput.write(buffer, 0, downloadedSize);
                        progressBar.setProgress(Integer.parseInt(publishProgress));
                    }
                    fileOutput.close();
                    inputStream.close();

                } catch (Exception e) {
                    updateScreen.showInfoToast("This track is not available...");
                }

                downloadCounter++;
            }
        }
        updateScreen.changeTextView(mainActivity.textViewSearchText, "Enter song name to search");
        String allLoadedMessage = "Saved to: " + programFolderPath;
        updateScreen.changeTextView(mainActivity.textViewInfo, allLoadedMessage);

        finished();
    }


    private void checkInetIsMobile() {
        boolean isMobileDataEnabled = Settings.Global.getInt(mainActivity.getContentResolver(), "mobile_data", 1) == 1;
        if (isMobileDataEnabled) {
            updateScreen.showInfoToast("Attention: downloading using mobile data!");
        }
    }

    void finished() {

        mBuilder.setContentText("Download complete");
        mBuilder.setProgress(100, 100, false);
        mNotifyManager.notify(id, mBuilder.build());


        updateScreen.changeTextView(mainActivity.textViewSearchText, "Enter song name to search");
        updateScreen.changeVisibility(mainActivity.mProgress, 8);
        updateScreen.changeVisibility(mainActivity.searchView, 0);
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                mainActivity.searchView.setIconified(true);
                mainActivity.searchView.clearFocus();
            }
        });
    }
}
