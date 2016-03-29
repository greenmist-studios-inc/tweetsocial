package com.greenmiststudios.tweetsocial;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.greenmiststudios.tweetsocial.adapter.TweetAdapter;
import com.greenmiststudios.tweetsocial.helper.SimpleDividerItemDecoration;
import com.greenmiststudios.tweetsocial.tasks.SearchTask;

import butterknife.Bind;
import butterknife.ButterKnife;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.TwitterException;

public class MainActivity extends AppCompatActivity implements
        SearchView.OnQueryTextListener, SearchView.OnCloseListener, SearchTask.OnSearchResultListener {

    private static final String EXTRA_QUERY = "query";

    public static final int PAGE_COUNT = 100;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.listView)
    RecyclerView recyclerView;

    @Bind(R.id.search_view)
    SearchView searchView;

    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.no_tweets)
    TextView noTweets;

    private TweetAdapter adapter;
    private String query;
    private LinearLayoutManager layoutManager;
    private QueryResult lastResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        MainApplication.getInstance().startLocationUpdates(this, false);

        setSupportActionBar(toolbar);

        adapter = new TweetAdapter(this);
        recyclerView.setAdapter(adapter);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        recyclerView.addOnScrollListener(mRecyclerViewOnScrollListener);

        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_QUERY)) {
            searchView.setQuery(savedInstanceState.getString(EXTRA_QUERY), false);
            search(savedInstanceState.getString(EXTRA_QUERY));
        }

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.accent));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (TextUtils.isEmpty(query)) {
                swipeRefreshLayout.setRefreshing(false);
                return;
            }
            search(query);
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_QUERY, query);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        recyclerView.removeOnScrollListener(mRecyclerViewOnScrollListener);
        super.onDestroy();
    }

    private void searchNext() {
        if (lastResult == null || lastResult.nextQuery() == null) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        internalSearch(lastResult.nextQuery(), new SearchTask.OnSearchResultListener() {
            @Override
            public void onSearchResult(QueryResult result) {
                Log.d(TAG, "New page results for " + result.getQuery() + " : " + result.getTweets().size());
                lastResult = result;
                adapter.addTweets(result.getTweets());
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(TwitterException e) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    private void internalSearch(Query query, SearchTask.OnSearchResultListener listener) {
        Log.d(TAG, "Searching for tweets containing: " + query);

        swipeRefreshLayout.setRefreshing(true);
        this.query = query.getQuery();
        query.setCount(PAGE_COUNT);
        new SearchTask(listener).execute(query);
    }

    private void search(String query) {
        if (TextUtils.isEmpty(query.trim())) return;
        internalSearch(new Query(query), this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == ConnectionResult.SUCCESS) {
            MainApplication.getInstance().startLocationUpdates(this, true);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onClose() {
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        search(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }

    @Override
    public void onSearchResult(QueryResult result) {
        Log.d(TAG, "Search results for " + result.getQuery() + " : " + result.getTweets().size());
        lastResult = result;
        adapter.setTweets(result.getTweets());
        noTweets.setVisibility(result.getTweets().size() == 0 ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        recyclerView.scrollToPosition(0);
    }

    @Override
    public void onError(TwitterException e) {
        Log.e(TAG, "Error loading search: ", e);
        swipeRefreshLayout.setRefreshing(false);
        Snackbar.make(findViewById(android.R.id.content), R.string.error_base, Snackbar.LENGTH_LONG).show();
    }

    private RecyclerView.OnScrollListener mRecyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView,
                                         int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

            if (!swipeRefreshLayout.isRefreshing()) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= PAGE_COUNT) {
                    searchNext();
                }
            }
        }
    };
}
