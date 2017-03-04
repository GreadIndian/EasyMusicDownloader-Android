package com.kaiv.easymusicdownloaderplus;

import android.os.Environment;
import com.kaiv.easymusicdownloaderplus.Model.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.io.*;

public class Controller {

    private MainActivity mainActivity;
    private FindMusicStrategy findMusicStrategy;
    public static String programFolderPath;
    public Controller controller;
    private UpdateScreen updateScreen;
    public DownloadAllThread downloadAllThread;
    public String counterString;
    public StartingTopHitsFindThread startingTopHitsFindThread;

    public Controller(MainActivity mainActivity, FindMusicStrategy findMusicStrategy) {
        this.mainActivity = mainActivity;
        this.findMusicStrategy = findMusicStrategy;
        controller = this;
        updateScreen = new UpdateScreen(mainActivity, findMusicStrategy, controller);
        downloadAllThread = new DownloadAllThread(controller, mainActivity, mainActivity.mProgress, updateScreen);
        startingTopHitsFindThread = new StartingTopHitsFindThread(controller, updateScreen, mainActivity);
        counterString = "";
    }

    public  <T extends Thread>  void checkInetConnectionAndStartNeededThread(T thread) {
        if (!isOnline()) {
            updateScreen.showInfoToast("Check internet connection!");
        } else {
            if ( thread.getState() == Thread.State.NEW) {
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    public void checkInetConnectionAndStarFind(String query) {

        if (!isOnline()) {
            updateScreen.showInfoToast("Check internet connection!");
        } else {
            new inputedSongFinder(this, mainActivity, query, new FindMusicStrategy()).start();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mainActivity.getSystemService(mainActivity.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void createFolder() {
        File programFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "DownloadedMusic");
        programFolderPath = programFolder.toString();

        boolean created = false;
        if (!programFolder.exists()) {
            created = programFolder.mkdirs();
        }
        if (!created & !programFolder.exists()) {
            updateScreen.showInfoToast("Cannot create folder at phone's memory...");
        }
    }
}


