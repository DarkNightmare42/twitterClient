package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class twitterHome extends AppCompatActivity {

    public static final String TAG = "twitterHome";

    twitterAppClient client;
    RecyclerView viewTweets;
    List<Tweet> tweets;
    clientAdapter clientAdapter;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_home);
         client = twitterApp.getRestClient(this);

         //code for actionbar to add icon to header
         //ActionBar actionBar = getSupportActionBar();
         //actionBar.setDisplayHomeAsUpEnabled(true);
         //actionBar.setIcon(R.drawable.ic_launcher);

         swipeContainer = findViewById(R.id.swipeContainer);
        // Scheme colors for animation
        swipeContainer.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );

         swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
             @Override
             public void onRefresh() {
                 Log.i(TAG, "fetching data for refresh");
                 fillTimeline();
             }
         });

         viewTweets = findViewById(R.id.rvTweets);
         tweets = new ArrayList<>();
         clientAdapter = new clientAdapter(this, tweets);
         LinearLayoutManager layoutManager = new LinearLayoutManager(this);
         viewTweets.setLayoutManager(layoutManager);
         viewTweets.setAdapter(clientAdapter);
         scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
             @Override
             public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                 Log.i(TAG, "onLoadMore: " + page);
                 loadMoreData(page);
             }
         };
         //add the scroll listener to the recyclerview
         viewTweets.addOnScrollListener(scrollListener);
         fillTimeline();
         
    }

    private void loadMoreData(final int page) {
        client.getNextFeedPage(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i("loadMoreData", "loadMoreData");
                JSONArray jsonArray = json.jsonArray;
                try {
                    List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                    clientAdapter.addAll(tweets);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i("onLoadFailure", response + throwable);

            }
        }, (tweets.get(tweets.size() - 1).id));
    }

    private void fillTimeline() {
        client.getFeed(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d("onFillSuccess", "timeline filled" + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    clientAdapter.clear();
                    clientAdapter.addAll(Tweet.fromJsonArray(jsonArray));
                    swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    Log.e(TAG, "error",  e);

                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d("onFillFailure", response + throwable);
            }
        });
    }
}