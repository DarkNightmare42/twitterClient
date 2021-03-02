package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    public static final String tag = "ComposeActivity";
    public static final int max_length = 140;
    EditText etCompose;
    Button tweetBtn;
    TextView textCount;

    twitterAppClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        client = twitterApp.getRestClient(this);
        etCompose = findViewById(R.id.etCompose);
        tweetBtn = findViewById(R.id.tweetBtn);
        tweetBtn.setEnabled(false);

        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Fires right as the text is being changed (even supplies the range of text)

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // Fires right before text is changing
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Fires right after the text has changed
                if(s.length() > max_length || s.length() < 1){
                    Toast.makeText(ComposeActivity.this, "We can't really chirp that!", Toast.LENGTH_SHORT).show();
                    tweetBtn.setEnabled(false);
                }
                else tweetBtn.setEnabled(true);
            }
        });

        setTitle(" New Tweet");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.ic_launcher);

        //button listen to publish text
        tweetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = etCompose.getText().toString();
                if(content.isEmpty()){
                    Toast.makeText(ComposeActivity.this, "Can't tweet nothin'!", Toast.LENGTH_LONG).show();
                    return;
                }
                if(content.length() > max_length){
                    Toast.makeText(ComposeActivity.this, "Whoa there! Too many chirpers!", Toast.LENGTH_LONG).show();
                    return;
                }
                //publish tweet via api call
                client.sendTweet(new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(tag, "onSuccess tweet published");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(tag, "tweet sent" + tweet.body);
                            Intent intent = new Intent();
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            setResult(RESULT_OK, intent); //set results
                            finish(); //closes the activity
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(tag, "onFailure to publish tweet");
                    }
                    }, content);
            }

        });

    }
}