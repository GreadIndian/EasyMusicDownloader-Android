package com.kaiv.easymusicdownloaderplus;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.kaiv.easymusicdownloaderplus.Model.FindMusicStrategy;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout mainLayout;
    private Controller startingController;
    public TextView textViewInfo;
    public static ProgressBar mProgress;
    public Button button;
    public LinearLayout linearLayout;
    public SearchView searchView;
    public TextView textViewSearchText;
    public ImageView backImage;
    public String buttonDownloadText = "Download available tracks";
    public String buttonSearchText = "Find today's top 100 hits";
    public String buttonStopDownload = "Cancel downloading";
    public String buttonIsSearchingText = "Searching. Please wait...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewInfo = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);
        mProgress = (ProgressBar) findViewById(R.id.progressbar1);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        searchView = (SearchView) findViewById(R.id.searchView1);
        textViewSearchText = (TextView) findViewById(R.id.textViewSearchText);
        backImage = (ImageView) findViewById(R.id.backImage);

        mainLayout = (RelativeLayout) findViewById(R.id.activity_main);
        Drawable drawable1 =  getResources().getDrawable(R.drawable.fon);
        mainLayout.setBackgroundDrawable(drawable1);

        Drawable drawable = getResources().getDrawable(R.drawable.background);
        mProgress.setProgressDrawable(drawable);

        prepare();
    }


    private void prepare() {

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (button.getText().equals(buttonSearchText)) {
                    startNewController();
                    startingController.checkInetConnectionAndStartNeededThread(startingController.startingTopHitsFindThread);
                }
                if (button.getText().equals(buttonDownloadText)) {
                    startingController.checkInetConnectionAndStartNeededThread(startingController.downloadAllThread);
                }
                if (button.getText().equals(buttonStopDownload)) {
                    startingController.downloadAllThread.terminate();
                    textViewSearchText.setText("This song will be last");
                    button.setText(buttonSearchText);
                }
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                textViewSearchText.setVisibility(0);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                textViewSearchText.setVisibility(8);
            }
        });

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startNewController();
                startingController.checkInetConnectionAndStarFind(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void startNewController() {
        startingController = new Controller(this, new FindMusicStrategy());
        startingController.createFolder();
    }
}
