
package com.codepath.apps.restclienttemplate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetWithUser;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.apps.restclienttemplate.models.tweetDao;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class twitterHome extends AppCompatActivity {

    public static final String TAG = "twitterHome";
    private final int request_code = 5;

    tweetDao TweetDao;
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
         TweetDao = ((twitterApp) getApplicationContext()).getMyDatabase().TweetDao();


        //set title as well as add an icon to the action bar
         setTitle(" twitter");
         ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayShowHomeEnabled(true);
         actionBar.setIcon(R.drawable.ic_launcher);


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

         //divider
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        //itemDecoration.setDrawable(new ColorDrawable(R.color.white));
        viewTweets.addItemDecoration(itemDecoration);

         scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
             @Override
             public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                 Log.i(TAG, "onLoadMore: " + page);
                 loadMoreData(page);
             }
         };
         //add the scroll listener to the recyclerview
         viewTweets.addOnScrollListener(scrollListener);
         //query for existing tweets in the database
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "database query fulfilled");
                List<TweetWithUser> tweetWithUsers = TweetDao.recentItems();
                List<Tweet> tweetsFromDB = TweetWithUser.getTweetList(tweetWithUsers);
                clientAdapter.clear();
                clientAdapter.addAll(tweetsFromDB);
            }
        });

         fillTimeline();
         
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu to add items to the action bar if present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.composeBtn){
            //compose icon has been selected, navigate to compose activity
            Intent intent = new Intent(this, ComposeActivity.class);
            startActivityForResult(intent, request_code);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == request_code && resultCode == RESULT_OK){
            //get data from intent and add it to the timeline view
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            tweets.add(0, tweet);
            clientAdapter.notifyItemInserted(0);
            viewTweets.smoothScrollToPosition(0);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadMoreData(final int page) {
        client.getNextFeedPage(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i("loadMoreData", "loadMoreData");
                JSONArray jsonArray = json.jsonArray;
                try {
                    Log.i(TAG, "tweets loaded");
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
                    final List<Tweet> tweetsFromNetwork = Tweet.fromJsonArray(jsonArray);
                    clientAdapter.clear();
                    clientAdapter.addAll(tweetsFromNetwork);
                    swipeContainer.setRefreshing(false);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "saving data to database");
                            //insert users first
                            List<User> usersFromNetwork = User.fromJsonTweetArray(tweetsFromNetwork);
                            TweetDao.insertModel(usersFromNetwork.toArray(new User[0]));
                            //followed by tweets
                            TweetDao.insertModel(tweetsFromNetwork.toArray(new Tweet[0]));
                        }
                    });
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