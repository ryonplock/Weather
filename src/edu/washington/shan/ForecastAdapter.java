/**
 * 
 */
package edu.washington.shan;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author shan@uw.edu
 *
 */
public class ForecastAdapter extends BaseAdapter
{
    private static final String TAG = "ForecastAdapter";
    private LayoutInflater mInflater;
    private Forecast mForecasts;
    
    public ForecastAdapter(Context context, Forecast forecast)
    {
        mInflater = LayoutInflater.from(context);
        mForecasts = forecast;
    }
    
    @Override
    public int getCount()
    {
        return mForecasts.size();
    }
    
    public String getItem(int i)
    {
        return mForecasts.get(i);
    }
    
    public long getItemId(int i)
    {
        return i;
    }
    
    public View getView(int arg0, View arg1, ViewGroup arg2)
    {
        final ViewHolder holder;
        View v = arg1;
        if((v==null) || v.getTag() == null)
        {
            v = mInflater.inflate(R.layout.row, null);
            holder = new ViewHolder();
            holder.mImageView = (ImageView)v.findViewById(R.id.row_thumbImage);
            holder.mTitle = (TextView)v.findViewById(R.id.row_text_title);
            holder.mContent = (TextView)v.findViewById(R.id.row_text_content);
            v.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) v.getTag();
        }
        
        // parse by ,
        String item = getItem(arg0);
        Log.v(TAG, item);
        String[] tokens = item.split(",");
        if(tokens.length == 2){
            holder.mImageView.setImageResource(GetThumbImageResourceId(tokens[1]));
            holder.mDisplay = tokens[0]; // like "Rain 43(6)
        }else if(tokens.length == 1){
            // Bad data!
            holder.mImageView.setImageResource(GetThumbImageResourceId(""));
            holder.mDisplay = tokens[0]; // like "Rain 43(6)
        }
        holder.mTitle.setText(getDateToDisplay(arg0));
        holder.mContent.setText(holder.mDisplay);
        v.setTag(holder);
        return v;
    }
    
    public class ViewHolder
    {
        ImageView mImageView;
        String mDisplay;
        TextView mTitle;
        TextView mContent;
    }
    
    // TODO is there a way load this imageUrl from the web in a separate thread?
    private int GetThumbImageResourceId(String imageUrl)
    {
        if(imageUrl != null && imageUrl.length() > 0)
        {
            if(imageUrl.contains("bl")) return R.drawable.blizzard;
            if(imageUrl.contains("blowingsnow")) return R.drawable.blowingsnow;
            if(imageUrl.contains("cloudy")) return R.drawable.cloudy;
            if(imageUrl.contains("drizzle")) return R.drawable.drizzle;
            if(imageUrl.contains("fair")) return R.drawable.fair;
            if(imageUrl.contains("fdrizzle")) return R.drawable.fdrizzle;
            if(imageUrl.contains("flurries")) return R.drawable.flurries;
            if(imageUrl.contains("sctfg")) return R.drawable.fog;
            if(imageUrl.contains("freezingrain")) return R.drawable.freezingrain;
            if(imageUrl.contains("hazy")) return R.drawable.hazy;
            if(imageUrl.contains("sctn")) return R.drawable.mcloudyn;
            if(imageUrl.contains("bkn")) return R.drawable.mcloudy;
            if(imageUrl.contains("sct")) return R.drawable.mcloudy;
            if(imageUrl.contains("fewn")) return R.drawable.pcloudyn;
            if(imageUrl.contains("few")) return R.drawable.pcloudy;
            if(imageUrl.contains("rasn")) return R.drawable.rainandsnow;
            if(imageUrl.contains("ra")) return R.drawable.rain;
            if(imageUrl.contains("shwr")) return R.drawable.showers;
            if(imageUrl.contains("sleet")) return R.drawable.sleet;
            if(imageUrl.contains("smoke")) return R.drawable.smoke;
            if(imageUrl.contains("sn")) return R.drawable.snow;
            if(imageUrl.contains("snowshowers")) return R.drawable.snowshowers;
            if(imageUrl.contains("sunnyn")) return R.drawable.sunnyn;
            if(imageUrl.contains("sunny")) return R.drawable.sunny;
            if(imageUrl.contains("tstormn")) return R.drawable.tstormn;
            if(imageUrl.contains("tstorm")) return R.drawable.tstorm;
            if(imageUrl.contains("wswarning")) return R.drawable.wswarning;
            if(imageUrl.contains("wswatch")) return R.drawable.wswatch;
        }
        
        return R.drawable.na; // default
    }
    
    private String getDateToDisplay(int arg)
    {
        Calendar calendar = Calendar.getInstance();
        if(arg == 0)
        {
            return "Today";
        }
        else if(arg == 1)
        {
            return "Tomorrow";
        }
        else
        {
            calendar.add(Calendar.DATE, arg);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE"); // like "Wed"
            Date date = new Date(calendar.getTimeInMillis());
            return dateFormat.format(date);
        }
    }   
}