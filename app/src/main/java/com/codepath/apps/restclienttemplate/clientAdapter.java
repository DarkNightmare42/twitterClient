package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;

import java.util.List;

public class clientAdapter extends RecyclerView.Adapter<clientAdapter.ViewHolder> {


    Context context;
    List<Tweet> tweets;

    public clientAdapter(Context context, List<Tweet> tweets){
        this.context = context;
        this.tweets = tweets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tweet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tweet tweet = tweets.get(position);
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {

        return tweets.size();
    }

    //clear tweets for swipe refresh
    public void clear(){
        tweets.clear();
        notifyDataSetChanged();
    }

    //reload tweets with refreshed data
    public void addAll(List<Tweet> tweetList){
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView profileImage;
        TextView twitterName;
        TextView tweetText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            twitterName = itemView.findViewById(R.id.twitterName);
            tweetText = itemView.findViewById(R.id.tweetText);
        }

        public void bind(Tweet tweet){
            tweetText.setText(tweet.body);
            twitterName.setText(tweet.user.screenName);
            Glide.with(context).load(tweet.user.profileImageUrl).into(profileImage);
            Log.d("onFill", "data filled" + tweetText);
        }
    }
}
