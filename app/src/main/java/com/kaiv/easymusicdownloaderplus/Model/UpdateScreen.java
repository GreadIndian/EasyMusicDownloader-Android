package com.kaiv.easymusicdownloaderplus.Model;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.kaiv.easymusicdownloaderplus.Controller;
import com.kaiv.easymusicdownloaderplus.MainActivity;
import com.kaiv.easymusicdownloaderplus.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.kaiv.easymusicdownloaderplus.Model.FindMusicStrategy.trackListFinal;

public class UpdateScreen {

    private MainActivity mainActivity;
    private FindMusicStrategy findMusicStrategy;
    private Controller controller;
    private boolean isPlaing;
    private ImageView previousPlayView;
    private static MediaPlayer mp;
    private String UrlCurrentPlayedSong = "";
    private UpdateScreen updateScreen;
    public static ArrayList<Track> sortedTrackList;
    final Bitmap playPic;
    final Bitmap pausePic;
    final Bitmap loadPic;
    public static volatile int urlResponceCode = 0;

    public UpdateScreen(MainActivity mainActivity, FindMusicStrategy findMusicStrategy, Controller controller) {
        this.mainActivity = mainActivity;
        this.findMusicStrategy = findMusicStrategy;
        this.updateScreen = this;
        this.controller = controller;
        playPic = BitmapFactory.decodeResource(mainActivity.getResources(), R.mipmap.play_icon);
        pausePic = BitmapFactory.decodeResource(mainActivity.getResources(), R.mipmap.pause_icon);
        loadPic = BitmapFactory.decodeResource(mainActivity.getResources(), R.mipmap.download_icon);
    }

    public void start() {

        stopIfPlaying();

        clearLinearLayout();

        sortTrackList();

        isPlaing = false;


        for (final Track currentTrack : sortedTrackList) {

            final String trackArtist = currentTrack.artist;
            final String trackTitle = currentTrack.title;
            final String trackUrl = String.valueOf(currentTrack.url);

            final ProgressBar progressBar = new ProgressBar(mainActivity, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams paramsPogBar = new LinearLayout.LayoutParams(MATCH_PARENT, 22);
            progressBar.setLayoutParams(paramsPogBar);
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.CYAN));

            // create horizontal Relative Layout
            final RelativeLayout horizontalRelativeLayout = new RelativeLayout(mainActivity);
            horizontalRelativeLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            //horizontalRelativeLayout.setBackgroundColor(Color.parseColor("#beefed"));

            // set left element
            RelativeLayout.LayoutParams horizontLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            horizontLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            horizontLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);

            // show play pic
            final ImageView playPicView = new ImageView(mainActivity);
            playPicView.setImageBitmap(playPic);
            playPicView.setId(111);
            playPicView.setPadding(10, 0, 0, 0);
            playPicView.setLayoutParams(horizontLayoutParams);

            // show title + artist at vertical Layout
            horizontalRelativeLayout.addView(playPicView);

            // set right element
            horizontLayoutParams = new RelativeLayout.LayoutParams(120, RelativeLayout.LayoutParams.WRAP_CONTENT);
            horizontLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            horizontLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);


            // create download pic
            final ImageView loadPicView = new ImageView(mainActivity);
            loadPicView.setLayoutParams(horizontLayoutParams);
            loadPicView.setId(112);
            loadPicView.setPadding(0, 0, 10, 0);

            loadPicView.setImageBitmap(loadPic);


            // add pic to main RelativeLayout
            horizontalRelativeLayout.addView(loadPicView);

            // set center (next to left) element
            horizontLayoutParams = new RelativeLayout.LayoutParams(MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            horizontLayoutParams.addRule(RelativeLayout.RIGHT_OF, playPicView.getId());
            horizontLayoutParams.addRule(RelativeLayout.LEFT_OF, loadPicView.getId());
            horizontLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);


            // create vertical Layout for title + artist in one box
            LinearLayout linearLayoutForSong = new LinearLayout(mainActivity);
            linearLayoutForSong.setOrientation(LinearLayout.VERTICAL);
            linearLayoutForSong.setLayoutParams(horizontLayoutParams);
            linearLayoutForSong.setGravity(Gravity.LEFT);
            linearLayoutForSong.setPadding(10, 0, 10, 0);

            // create title
            TextView textViewTitle = new TextView(mainActivity);
            textViewTitle.setTextSize(16);
            textViewTitle.setTypeface(null, Typeface.BOLD);
            textViewTitle.setGravity(Gravity.LEFT);
            textViewTitle.setText(trackTitle);

            // create artist
            TextView textViewArtist = new TextView(mainActivity);
            textViewArtist.setTextSize(14);
            textViewArtist.setGravity(Gravity.LEFT);
            textViewArtist.setText(trackArtist);

            // show title + artist at vertical Layout
            linearLayoutForSong.addView(textViewTitle);
            linearLayoutForSong.addView(textViewArtist);

            // add song name to main RelativeLayout
            horizontalRelativeLayout.addView(linearLayoutForSong);

            // creating 2 horizontal lines: RelativeLayout and progress download bar
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.linearLayout.addView(horizontalRelativeLayout);
                    mainActivity.linearLayout.addView(progressBar);
                }
            });


            playPicView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if (!controller.isOnline()) {
                        updateScreen.showInfoToast("Check internet connection!");
                    }
                    if (controller.isOnline() && !updateScreen.existsUrl(trackUrl)) {
                        updateScreen.showInfoToast("Sorry. This song is unavailable now.");
                    } else {
                        if (!isPlaing) {
                            // start new song when other is paused
                            if (mp != null && !UrlCurrentPlayedSong.equals(trackUrl)) {
                                UrlCurrentPlayedSong = trackUrl;
                                previousPlayView.setImageBitmap(playPic);
                                previousPlayView = playPicView;
                                startNewSong();
                            }


                            // resuming same song
                            if (mp != null && UrlCurrentPlayedSong.equals(trackUrl)) {
                                playPicView.setImageBitmap(pausePic);
                                mp.start();
                            }

                            // start new song first time
                            else {
                                UrlCurrentPlayedSong = trackUrl;
                                playPicView.setImageBitmap(pausePic);
                                previousPlayView = playPicView;
                                playSong();

                            }
                            isPlaing = true;
                        } else {
                            // pausing same song
                            if (UrlCurrentPlayedSong.equals(trackUrl)) {
                                if (mp != null) {
                                    playPicView.setImageBitmap(playPic);
                                    mp.pause();
                                    isPlaing = false;
                                }
                            }

                            // starting new song when other is playing
                            else {
                                UrlCurrentPlayedSong = trackUrl;

                                playPicView.setImageBitmap(pausePic);
                                previousPlayView.setImageBitmap(playPic);
                                previousPlayView = playPicView;
                                startNewSong();
                                isPlaing = true;
                            }
                        }
                    }
                }
            });

            loadPicView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    final String fileName = trackTitle + "-" + trackArtist + ".mp3";
                    Thread thread = new DownloadThread(controller, trackTitle, progressBar, trackUrl, fileName, updateScreen, mainActivity);
                    thread.start();
                }
            });
        }
        changeTextAtButton(mainActivity.button, mainActivity.buttonDownloadText);
    }

    public void stopIfPlaying() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    public void startNewSong() {
        if (mp != null) {
            mp.stop();
            mp.release();
            playSong();
        }
    }

    public void playSong() {

        if (!controller.isOnline()) {
            updateScreen.showInfoToast("Check internet connection!");
        } else {
            try {
                mp = new MediaPlayer();
                mp.setDataSource(UrlCurrentPlayedSong);
                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer player) {
                        player.start();
                    }
                });
                mp.prepareAsync();
                showInfoToast("Buffering song...");
            } catch (IllegalArgumentException | IOException e) {
                showInfoToast("Problem with playing this track... Try another.");
            }
        }
    }

    public boolean existsUrl(String url) {
        TestUrlThread thread = new TestUrlThread(url, updateScreen);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
        if (urlResponceCode == 200)
            return true;
        else
            return false;
    }

    public void showInfoToast(final String showedData) {
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(mainActivity, showedData, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void changeTextAtButton(final Button button, final String showedData) {
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                button.setText(showedData);
            }
        });
    }

    public void changeTextView(final TextView showedTextView, final String showedData) {
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                showedTextView.setText(showedData);
            }
        });
    }

    public void changeVisibility(final ProgressBar progressBar, final int visibility) {
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                progressBar.setVisibility(visibility);
            }
        });
    }

    public void changeVisibility(final SearchView searchView, final int visibility) {
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                searchView.setVisibility(visibility);
            }
        });
    }

    public void changeVisibility(final TextView textView, final int visibility) {
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                textView.setVisibility(visibility);
            }
        });
    }

    public void changeVisibility(final ImageView imageView, final int visibility) {
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                imageView.setVisibility(visibility);
            }
        });
    }

    public void changeEnabled(final Button button, final boolean visibility) {
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                button.setEnabled(visibility);
            }
        });
    }

    public void changeEnabled(final TextView textView, final boolean visibility) {
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                textView.setEnabled(visibility);
            }
        });
    }

    public void sortTrackList() {
        sortedTrackList = new ArrayList<>();
        sortedTrackList.addAll(trackListFinal);
        Collections.sort(sortedTrackList, new TrackComparator());
    }

    public void clearLinearLayout() {
        changeVisibility(mainActivity.backImage, 8);
        if ((mainActivity.linearLayout).getChildCount() > 0) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.linearLayout.removeAllViews();
                }
            });
        }
    }
}
