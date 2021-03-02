package com.codepath.apps.restclienttemplate;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.codepath.apps.restclienttemplate.models.SampleModel;
import com.codepath.apps.restclienttemplate.models.SampleModelDao;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.apps.restclienttemplate.models.tweetDao;

@Database(entities={SampleModel.class, Tweet.class, User.class}, version=3)
public abstract class MyDatabase extends RoomDatabase {
    public abstract SampleModelDao sampleModelDao();
    public abstract tweetDao TweetDao();

    // Database name to be used
    public static final String NAME = "MyDataBase";
}
