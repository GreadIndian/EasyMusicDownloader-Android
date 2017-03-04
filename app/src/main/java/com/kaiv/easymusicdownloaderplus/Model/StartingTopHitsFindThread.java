package com.kaiv.easymusicdownloaderplus.Model;

import com.kaiv.easymusicdownloaderplus.Controller;
import com.kaiv.easymusicdownloaderplus.MainActivity;
import org.json.JSONException;

import java.io.IOException;

import static com.kaiv.easymusicdownloaderplus.Model.FindMusicStrategy.namesTopHitsList;
import static com.kaiv.easymusicdownloaderplus.Model.FindMusicStrategy.trackListFinal;

public class StartingTopHitsFindThread extends Thread {

    private Controller controller;
    private UpdateScreen updateScreen;
    private MainActivity mainActivity;

    public StartingTopHitsFindThread(Controller controller, UpdateScreen updateScreen, MainActivity mainActivity) {
        this.controller = controller;
        this.updateScreen = updateScreen;
        this.mainActivity = mainActivity;
    }

    public void run() {

        try {

            trackListFinal.clear();
            namesTopHitsList.clear();

            updateScreen.changeTextAtButton(mainActivity.button, mainActivity.buttonIsSearchingText);
            updateScreen.changeVisibility(mainActivity.mProgress, 0);
            updateScreen.changeVisibility(mainActivity.searchView, 8);
            updateScreen.changeVisibility(mainActivity.textViewSearchText, 0);

            updateScreen.changeEnabled(mainActivity.textViewSearchText, false);
            updateScreen.changeEnabled(mainActivity.button, false);

            updateScreen.showInfoToast("Search was started");

            MainActivity.mProgress.setProgress(0);
            MainActivity.mProgress.setMax(100);

            FindMusicStrategy.getTracksFromShazam("https://www.shazam.com/shazam/v2/en/UA/web/-/tracks/web_chart_world");

            FindMusicStrategy.createListForTopHits(FindMusicStrategy.namesTopHitsList, updateScreen, mainActivity);

            mainActivity.mProgress.setProgress(100);

            final int foundedCountTracks = trackListFinal.size();
            updateScreen.showInfoToast("Search was finished. Founded: " + foundedCountTracks + " tracks");

            String counterString = Integer.toString(foundedCountTracks) + " tracks was found";
            updateScreen.changeTextView(mainActivity.textViewInfo, counterString);

            updateScreen.changeVisibility(mainActivity.mProgress, 8);
            updateScreen.changeEnabled(mainActivity.button, true);

            updateScreen.changeVisibility(mainActivity.searchView, 0);
            updateScreen.changeEnabled(mainActivity.textViewSearchText, true);


            mainActivity.runOnUiThread(new Runnable() {
                public void run() {
                    mainActivity.searchView.setIconified(true);
                    mainActivity.searchView.clearFocus();
                }
            });

            updateScreen.start();


        } catch (IOException e) {
            updateScreen.showInfoToast("Some problem with writing on phones memory...");
            e.printStackTrace();
        } catch (JSONException e) {
            updateScreen.showInfoToast("Some problem with finding music... Please Try again.");
        }
    }

}
