package edu.washington.shan;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity {
    
    private static final String TAG="MainActivity";
    private static final int ACTIVITY_SETTINGS = 0;
    private ForecastAdapter mAdapter;
    private Thread mWorkerThread;
    private Handler mHandler;
    private ProgressBar mProgressBar;
    private NDFDParams mParams;

    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
        mHandler = new Handler(mCallback);
        mParams = new NDFDParams(this);
        mParams.setParam(NDFDParams.Type.STARTDATE,
                Utils.getTodayInFormat(new SimpleDateFormat("yyyy-MM-dd")));
        
        restoreCachedForecast();
        sync(false);
        
        Utils.showAboutDialogBoxOnFirstRun(this);
        Utils.clearFirstTimeRunFlag(this);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.mainmenu_settings)
        {
            // Launch to SettingsPrefActivity screen
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, ACTIVITY_SETTINGS);
        }
        else if(item.getItemId() == R.id.mainmenu_help)
        {
            Utils.showAboutDialogBox(this);
        }
        
        // Returning true ensures that the menu event is not be further processed.
        return true;
    }
   
    /* (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        
        Log.v(TAG, "onActivityResult requestCode:" + requestCode +
                " resultCode:" + resultCode);
        
        if(requestCode == ACTIVITY_SETTINGS) // returned from settings menu
        {
            // update forecast with the new zip
            // For now we don't care if zip is changed. Just update forecast
            SharedPreferences sharedPref = 
                PreferenceManager.getDefaultSharedPreferences(this);
            // TODO pull the constant zip code out
            String zipcode = sharedPref.getString("settings_zipcode", "98101");
            mParams.setParam(NDFDParams.Type.ZIPCODE, zipcode);
            sync(true);
        }
    }

    private Handler.Callback mCallback = new Handler.Callback() 
    {
        @Override
        public boolean handleMessage(Message msg) {
            Log.v(TAG, "Handler.Callback entered");
            
            // The background thread returned.
            // Stop the progressbar.
            mProgressBar.setVisibility(ProgressBar.GONE);
            
            Bundle bundle = msg.getData();
            if(bundle != null)
            {
                Forecast forecast = bundle.getParcelable(Constants.KEY_STATUS);
                if(forecast != null)
                {
                    Log.v(TAG, "Data retrieval succeeded");
                    
                    // Update the list
                    fillData(forecast);
                    
                    // Get and update the timestamp
                    String updatedTime = Utils.getTodayInFormat(
                        new SimpleDateFormat(Constants.LAST_UPDATED_TIME_FORMAT));
                    TextView textview = (TextView)findViewById(R.id.updatedTime);
                    textview.setText("Updated on " + updatedTime);
                    
                    // Mark the time upon a successful retrieval of data
                    markSyncTime();
                    saveForecast(forecast);
                }
                else
                {
                    Log.v(TAG, "Data retrieval failed");
                    Toast.makeText(getApplicationContext(), 
                            getResources().getString(R.string.data_retrieval_failed), 
                            Toast.LENGTH_SHORT).show();
                }
            }
            
            return false;
        }
    };
    
    /**
     * Restore from a cached forecast object
     */
    private void restoreCachedForecast()
    {
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        long lastSyncedAt = sharedPref.getLong(Constants.KEY_LASTSYNCTIME, -1);
        if(lastSyncedAt != -1)
        {
            // Restore cached forecast and display
            int numOfDays = sharedPref.getInt(Constants.KEY_NUM_DAYS_FORECAST, -1);
            if(numOfDays != -1)
            {
                Forecast forecast = new Forecast(sharedPref
                        .getString(Constants.KEY_FORECAST,""));
                fillData(forecast);
            }
            
            // Get the last updated time
            Date lastSyncedDate = new Date(lastSyncedAt);
            SimpleDateFormat dateFormat = 
                new SimpleDateFormat(Constants.LAST_UPDATED_TIME_FORMAT);
            String updatedTime = dateFormat.format(lastSyncedDate);
            TextView textview = (TextView)findViewById(R.id.updatedTime);
            textview.setText("Updated on " + updatedTime);

        }
    }

    /**
     * Save the forecast to shared pref so that it can be retrieved later
     * @param forecast
     */
    private void saveForecast(Forecast forecast)
    {
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(Constants.KEY_NUM_DAYS_FORECAST, 
                Constants.NUM_DAYS_FORECAST);
        editor.putString(Constants.KEY_FORECAST, forecast.serialize());
        editor.commit();
    }
    
    private void fillData(Forecast forecast)
    {
        mAdapter = new ForecastAdapter(this, forecast);
        this.setListAdapter(mAdapter);
    }
    
    /**
     * Returns true if it started a background thread to retreive the forecast.
     * Returns false otherwise.
     * @return
     */
    private boolean sync(boolean force)
    {
        Log.v(TAG, "Entering sync");
        
        // Has it been x hours since the last sync?
        if(force || okToSync(Constants.UPDATE_FREQUENCE_IN_HOURS)){
            Log.v(TAG, "Attempting to sync...");
            // Set the progress bar visibility
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            mWorkerThread = new Thread(new WorkerThreadRunnable(mHandler, mParams));
            mWorkerThread.start();
            return false;
        }
        return true;
    }

    /**
     * Returns true if it's been x hours since last sync or
     * if it's the first time calling this function, or
     * input argument hours is less than or equal to zero.
     * @param hours
     * @return
     */
    private boolean okToSync(long hours)
    {
        Log.v(TAG, "Entering okToSync");
        
        if(hours <= 0)
            return true;
        
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        long lastSyncedAt = sharedPref.getLong(Constants.KEY_LASTSYNCTIME, -1);
        if(lastSyncedAt == -1)
            return true; // if the preference is not set it means it's first time.
        
        long offset = hours * 60L * 60L * 1000L; // hours->milliseconds
        long now = Calendar.getInstance().getTimeInMillis();
        if(lastSyncedAt + offset <= now)
            return true;
        
        return false;
    }
    
    /**
     * Writes a timestamp for 'last synced time' in a shared preference
     */
    private void markSyncTime()
    {
        Log.v(TAG, "Entering markSyncTime");
        
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(Constants.KEY_LASTSYNCTIME, Calendar.getInstance().getTimeInMillis());
        editor.commit();
    }
  
}