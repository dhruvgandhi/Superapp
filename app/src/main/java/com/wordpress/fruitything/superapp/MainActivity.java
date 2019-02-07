package com.wordpress.fruitything.superapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {
    private final MyWebChromeClient myWebChromeClient=new MyWebChromeClient();
    private final String[] INUrls={"https://amazon.com","https://ebay.com"};
///////////////////////////////////Declare////////////////////
    //Fulscreen
    private View mCustomView=null;
    private ViewGroup mContentViewParent;
    private View mContentView;
    private String nightMode="Night Mode \uD83C\uDF1A";
    private ValueCallback<Uri[]> mFilePathCallback=null;
    private String mCameraPhotoPath;

    private void showPrivacyPolicy()
    {
        final WebView privacyPolicy=findViewById(R.id.privacyPolicy);
        final Button privacyOk=findViewById(R.id.privacyOk);
        privacyOk.setVisibility(View.VISIBLE);
        privacyOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                privacyPolicy.setVisibility(View.GONE);
                privacyOk.setVisibility(View.GONE);
                //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            }
        });
        privacyPolicy.setVisibility(View.VISIBLE);
        privacyPolicy.loadUrl("file:///android_asset/privacypolicysa.html");
    }


    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%ON CREATE METHOD%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        setTheme(R.style.MyAppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ScrollView mainMenu=findViewById(R.id.MainMenu);
        CardView cardSocial=findViewById(R.id.cardSocial);
        CardView cardShop=findViewById(R.id.cardShop);
        CardView cardPay=findViewById(R.id.cardPay);
        CardView cardTravel= findViewById(R.id.cardTravel);
        final WebView myWebView = findViewById(R.id.myWebView);
        final ListView listView = findViewById(R.id.listView);
        final TextView transparentBack = findViewById(R.id.transparentBack);
        final TabLayout tabLayout = findViewById(R.id.tabLayout);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        //START MENU
        mainMenu.setVisibility(View.VISIBLE);
        onClickCard(cardSocial,0);
        onClickCard(cardShop,1);
        onClickCard(cardPay,2);
        onClickCard(cardTravel,3);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false);
        if(!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getString(R.string.pref_previously_started), Boolean.TRUE);
            edit.apply();
            showPrivacyPolicy();
        }
          //Access all Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]
                            {       Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.INTERNET,
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            }
                    ,10);
        }
        //Find Which Country;
        Context context=getApplicationContext();
        String locale = context.getResources().getConfiguration().locale.getDisplayCountry();
        //If country is India Change amazon Site URL
        //TextView t=(TextView) findViewById(R.id.infoSocial);
        //t.setText(locale);
        if(locale.matches("India"))
        {
            INUrls[0]="https://amazon.in";
            INUrls[1]="https://ebay.in";
        }
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setGeolocationEnabled(true);
        //Tab layout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showTab(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                showTab(tab);

            }
        });

        //error solved of unselecting tab
        transparentBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.setVisibility(View.INVISIBLE);
                transparentBack.setVisibility(View.GONE);
            }
        });

        final ImageView about= findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about.setVisibility(View.GONE);
                tabLayout.setVisibility(View.VISIBLE);
            }
        });
        //swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                if(myWebView.getUrl().equals("file:///android_asset/error.html"))
                {
                    if(myWebView.canGoBack())
                    {
                        myWebView.goBack();
                    }
                }
                else
                {
                    myWebView.reload();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        //Fix links
        myWebView.setWebViewClient(new MyWebViewClient());

       // MyWebChromeClient myWebChromeClient = new MyWebChromeClient();
        myWebView.setWebChromeClient(myWebChromeClient);

        //Performance improve
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSaveFormData(true);
        webSettings.setSavePassword(true);
        webSettings.setEnableSmoothTransition(true);
        webSettings.setAllowFileAccess(true);
        //Instagram Infinite Loading prblm
        CookieManager.getInstance().setAcceptCookie(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(myWebView, true);
        }
    }



    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%Go Back%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        WebView myWebView= findViewById(R.id.myWebView);
        SwipeRefreshLayout swipeRefreshLayout=findViewById(R.id.swipeRefreshLayout);
        ListView listView=findViewById(R.id.listView);

        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            if(swipeRefreshLayout.getVisibility()==View.GONE && listView.getVisibility()!=View.VISIBLE )
            {
                return super.onKeyDown(keyCode, event);
            }
            if(listView.getVisibility()==View.VISIBLE)
            {
                hideListView();
                return true;
            }

            if((!myWebView.canGoBack())&& mCustomView==null && listView.getVisibility()!=View.VISIBLE )
            {
                swipeRefreshLayout.setVisibility(View.GONE);
                return true;
            }


            if(myWebView.canGoBack() && mCustomView==null && listView.getVisibility()!=View.VISIBLE )
            {
                myWebView.goBack();
                return true;
            }
        }


        //if it wasnt then do default
        return super.onKeyDown(keyCode, event);
    }

    //To Hide listView
    private void hideListView()
    {
        final ListView listView = findViewById(R.id.listView);
        final TextView transparentBack = findViewById(R.id.transparentBack);
        transparentBack.setVisibility(View.GONE);
        if(listView.getVisibility()==View.VISIBLE)
            listView.setVisibility(View.INVISIBLE);
    }

    //Actions done when List Item is clicked
    private void listItemClick(String url, int colorID)
    {
        WebView myWebView= findViewById(R.id.myWebView);
        findViewById(R.id.privacyPolicy).setVisibility(View.GONE);
        findViewById(R.id.privacyOk).setVisibility(View.GONE);
        hideListView();
        myWebView.loadUrl(url);
        changeStatusBarColor(colorID);
    }

    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%Main Menu Card %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    private void onClickCard(CardView card, final int tabID)
    {
        final TabLayout tabLayout= findViewById(R.id.tabLayout);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabLayout.Tab t=tabLayout.getTabAt(tabID);
                if(t!=null)
                    t.select();
                //showTab(tabLayout.getTabAt(tabID));
            }
        });
    }

    /////////////////issue of file
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp =SimpleDateFormat.getDateTimeInstance().toString();// new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    ///////////////////SOlved issue of FIle access????????????????????
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if(requestCode != 1/*INPUT_FILE_REQUEST_CODE*/ || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        Uri[] results = null;

        // Check that the response is a good one
        if(resultCode == Activity.RESULT_OK) {
            if(data == null) {
                // If there is not data, then we may have taken a photo
                if(mCameraPhotoPath != null) {
                    results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                }
            } else {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
        }

        mFilePathCallback.onReceiveValue(results);
        mFilePathCallback = null;
    }

    ///%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%Show Tab And List for onTabSelected() and onReselected()%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    private void showTab(TabLayout.Tab tab) {
        final ScrollView mainMenu= findViewById(R.id.MainMenu);
        final SwipeRefreshLayout swipeRefreshLayout= findViewById(R.id.swipeRefreshLayout);
        final WebView myWebView = findViewById(R.id.myWebView);
        final ListView listView = findViewById(R.id.listView);
       // final TextView transparentBack = (TextView) findViewById(R.id.transparentBack);
        final TabLayout tabLayout = findViewById(R.id.tabLayout);
        switch (tab.getPosition()) {
            case 0:
                String[] social = {"FaceBook", "Instagram", "Twitter"};
                showList(social);
               // showListView0();
                //link of listViews content
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        swipeRefreshLayout.setVisibility(View.VISIBLE);
                        //mainMenu.setVisibility(View.GONE);
                        switch (position) {
                            case 0:
                                listItemClick("https://facebook.com",R.color.fbflipkartpaytmColor);
                                break;
                            case 1:
                                listItemClick("https://instagram.com",R.color.instaColor);
                                break;
                            case 2:
                                listItemClick("https://twitter.com",R.color.twitterColor);
                                break;

                        }
                    }
                });
                //
                break;
            case 1:
                String[] shopping = {"Flipkart", "Amazon", "eBay"};
                showList(shopping);
                //showListView1();
                //link of listViews content
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        swipeRefreshLayout.setVisibility(View.VISIBLE);
                       // mainMenu.setVisibility(View.GONE);
                        switch (position) {
                            case 0:
                                listItemClick("https://flipkart.com",R.color.fbflipkartpaytmColor);
                                break;
                            case 1:
                                listItemClick(INUrls[0],R.color.amazonebayColor);
                                break;
                            case 2:
                                listItemClick(INUrls[1],R.color.amazonebayColor);
                                break;
                        }
                    }
                });
                break;
            case 2:
                String[] wallets = {"PayTM", "Freecharge"};
                showList(wallets);
                //showListView2();
                //link of listViews content
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        swipeRefreshLayout.setVisibility(View.VISIBLE);
                        //mainMenu.setVisibility(View.GONE);
                        switch (position) {
                            case 0:
                                listItemClick("https://paytm.com",R.color.fbflipkartpaytmColor);
                                break;
                            case 1:
                                listItemClick("https://freecharge.in",R.color.freechargeColor);
                                break;
                        }
                    }
                });
                break;
            case 3:
                String[] travel = {"makeMYtrip","Google Flights","BookMyShow"/*,"Ola"*/};
                showList(travel);
                //showListView3();
                //link of listViews content
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        swipeRefreshLayout.setVisibility(View.VISIBLE);
                        //mainMenu.setVisibility(View.GONE);
                        switch (position) {
                            case 0:
                                listItemClick("https://makemytrip.com",R.color.youtmmtColor);
                                break;
                            case 1:
                                listItemClick("https://flights.google.com",R.color.fbflipkartpaytmColor);
                                break;
                            case 2:
                                listItemClick("https://bookmyshow.com",R.color.youtmmtColor);
                                break;
                            /*case 3:
                                listItemClick("https://m.uber.com",R.color.olaColor);
                                break;*/

                        }
                    }
                });
                break;
            case 4:
                String[] settings = {"About",nightMode,"Feedback"};
                showList(settings);
                //showListView4();
                //Link of listView content
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        switch (position) {
                            case 0:
                                hideListView();
                                findViewById(R.id.about).setVisibility(View.VISIBLE);
                                tabLayout.setVisibility(View.INVISIBLE);
                                break;
                            case 1:
                                hideListView();
                                if(findViewById(R.id.nightMode).getVisibility()!=View.VISIBLE)
                                {
                                    nightMode="Day Mode \uD83C\uDF1E";
                                    findViewById(R.id.nightMode).setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    nightMode="Night Mode \uD83C\uDF1A ";
                                    findViewById(R.id.nightMode).setVisibility(View.GONE);
                                }
                                break;
                            case 2:
                                swipeRefreshLayout.setVisibility(View.VISIBLE);
                                //mainMenu.setVisibility(View.GONE);
                                hideListView();
                                myWebView.loadUrl("https://goo.gl/forms/G9Z43NIUFDmLQF0u1");
                                break;

                        }

                    }
                });
                break;
        }
    }

    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%Show List Items in ListView%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    private void showList(String[] a) {

        findViewById(R.id.transparentBack).setVisibility(View.VISIBLE);
        findViewById(R.id.listView).setVisibility(View.VISIBLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, a);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }

    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%TO change color of Status bar%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    private void changeStatusBarColor(int colorID)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getWindow().setStatusBarColor(getResources().getColor(colorID));
    }

    private class MyWebViewClient extends WebViewClient{
        //internet error
        public void onReceivedError(WebView view,int errorcode,String description,String failingUrl){

            WebView myWebView= findViewById(R.id.myWebView);
            myWebView.loadUrl("file:///android_asset/error.html");
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            findViewById(R.id.transparentBack).setVisibility(View.VISIBLE);

        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            hideListView();
            //findViewById(R.id.transparentBack).setVisibility(View.INVISIBLE);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            if(url.startsWith("intent"))
            {
                return true;
            }
            if (
                    Uri.parse(url).getHost().matches("(.*)facebook(.*)")
                    ||Uri.parse(url).getHost().matches("(.*)instagram(.*)")
                    ||Uri.parse(url).getHost().matches("(.*)twitter(.*)")
                    ||Uri.parse(url).getHost().matches("(.*)flipkart(.*)")
                    ||Uri.parse(url).getHost().matches("(.*)amazon(.*)")
                    ||Uri.parse(url).getHost().matches("(.*)ebay(.*)")
                    ||Uri.parse(url).getHost().matches("(.*)paytm(.*)")
                    ||Uri.parse(url).getHost().matches("(.*)freecharge(.*)")
                    ||Uri.parse(url).getHost().matches("(.*)uber(.*)")
                    ||Uri.parse(url).getHost().matches("(.*)makemytrip(.*)")
                    ||Uri.parse(url).getHost().matches("(.*)google(.*)")
                    ||Uri.parse(url).getHost().matches("(.*)intent(.*)")
                    ||Uri.parse(url).getHost().matches("(.*)book(.*)")
            )
            {
                //this is my site thus show in WebView
                return false;
            }
            //otherwise launch another activity
            Intent intent= new Intent(Intent.ACTION_VIEW,Uri.parse(url));
            WebView myWebView= findViewById(R.id.myWebView);
            //GO back To Prevent Blank Page
            if(myWebView.canGoBack())
            {
                myWebView.goBack();
            }
            startActivity(intent);
            return true;
        }

    }

    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%Full Screen, Location Perm., show files chooser %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    private class MyWebChromeClient extends WebChromeClient
    {

        final FrameLayout.LayoutParams customLayoutParameters = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        /////////////////////////Location //////////////
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
                                               GeolocationPermissions.Callback callback)
        {
            callback.invoke(origin, true, false);
        }

        //////////////////////////////////////////////////FULL SCREEN/////////////////////////////////////

        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback)
        {
            onShowCustomView(view,callback);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback)
        {
        // if a view already exists then immediately terminate the new one
            if (mCustomView != null)
            {
                callback.onCustomViewHidden();
                return;
            }
            ///////////Remove main View and start custom view
            mContentView = findViewById(R.id.activity_main);
            mContentViewParent=(ViewGroup)mContentView.getParent();
            int indexOfChild = mContentViewParent.indexOfChild(mContentView);
            mCustomView=view;
            mContentViewParent.addView(mCustomView, indexOfChild +1,customLayoutParameters);
            mContentViewParent.bringChildToFront(mCustomView);
            mContentView.setVisibility(View.INVISIBLE);
            /////////////////Hide status bar/ Action bar
            mCustomView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_FULLSCREEN);
        }




        @Override
        public void onHideCustomView()
        {
            //super.onHideCustomView();
            if (mCustomView == null)
            {
                return;
            }
            ///////////Remove main View and start custom view
            mContentViewParent.bringChildToFront(mContentView);
            mContentViewParent.removeView(mCustomView);
            mContentView.setVisibility(View.VISIBLE);
            mCustomView=null;

        }

        ///////////////////SOlved issue of FIle access????????????????????
        @Override
        public boolean onShowFileChooser(
             WebView webView, ValueCallback<Uri[]> filePathCallback,
                WebChromeClient.FileChooserParams fileChooserParams)
        {
            if(mFilePathCallback != null)
            {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePathCallback;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = null;
            try
            {
                photoFile = createImageFile();
                takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
            }
            catch (IOException ex)
            {
                    // Error occurred while creating the File
            }

            // Continue only if the File was successfully created
            if (photoFile != null)
            {
                mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
            }
            else
            {
                takePictureIntent = null;
            }

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("image/*");

            Intent[] intentArray;
            if(takePictureIntent != null)
            {
                intentArray = new Intent[]{takePictureIntent};
            }
            else
            {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            startActivityForResult(chooserIntent,1);// INPUT_FILE_REQUEST_CODE);

            return true;
        }
            ///////////////////SOlved issue of FIle access????????????????????
    }
}
