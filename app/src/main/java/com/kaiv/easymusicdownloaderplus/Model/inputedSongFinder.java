package com.kaiv.easymusicdownloaderplus.Model;

import com.kaiv.easymusicdownloaderplus.Controller;
import com.kaiv.easymusicdownloaderplus.MainActivity;


public class inputedSongFinder extends Thread {

    private static Controller controller;
    private static MainActivity mainActivity;
    private static String query;
    private FindMusicStrategy findMusicStrategy;
    private UpdateScreen updateScreen;

    public inputedSongFinder(Controller controller, MainActivity mainActivity, String query, FindMusicStrategy findMusicStrategy) {
        this.controller = controller;
        this.mainActivity = mainActivity;
        this.query = query;
        this.findMusicStrategy = findMusicStrategy;
    }

    @Override
    public void run() {

        FindMusicStrategy.trackListFinal.clear();

        updateScreen = new UpdateScreen(mainActivity, findMusicStrategy, controller);

        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                updateScreen.changeVisibility(mainActivity.mProgress, 0);
                mainActivity.searchView.setIconified(true);
                mainActivity.searchView.clearFocus();
            }
        });

        updateScreen.showInfoToast("Search was started");

        searchLinksforTracks();
    }

    public void searchLinksforTracks() {

        LinksFinder.getTracksFromMuzofond(query, findMusicStrategy, mainActivity, updateScreen);

        final int foundedCountTracks = findMusicStrategy.trackListFinal.size();
        if (foundedCountTracks > 0) {
            updateScreen.showInfoToast("Search was finished. Founded: " + foundedCountTracks + " tracks");

            String counterString = Integer.toString(foundedCountTracks) + " tracks was found";
            updateScreen.changeTextView(mainActivity.textViewInfo, counterString);
            updateScreen.changeVisibility(mainActivity.mProgress, 8);

            updateScreen.start();
        }
        else
        {
            updateScreen.showInfoToast("Nothing was found");
            mainActivity.mProgress.setProgress(100);
            updateScreen.changeVisibility(mainActivity.mProgress, 8);
        }
    }
}
