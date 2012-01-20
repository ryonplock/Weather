/**
 * 
 */
package edu.washington.shan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * @author shan@uw.edu
 * 
 *         Represents weather forecast
 *         Implements Parcelable in order to be passed via a Bundle
 * 
 */
public class Forecast implements Parcelable
{
    private static final String TAG = "Forecast";
    
    private List<String> conditions; // weather condition such as Cloudy, Rain
    private List<String> minTemperatures;
    private List<String> maxTemperatures;
    private List<String> iconLinks;

    public Forecast()
    {
        minTemperatures = new ArrayList<String>();
        maxTemperatures = new ArrayList<String>();
        conditions = new ArrayList<String>();
        iconLinks = new ArrayList<String>();
    }

    /**
     * Constructor to allow deserialization from a string
     * @param str
     */
    public Forecast (String str)
    {
        if (str == null || str.length() == 0)
        {
            minTemperatures = new ArrayList<String>();
            maxTemperatures = new ArrayList<String>();
            conditions = new ArrayList<String>();
            iconLinks = new ArrayList<String>();
        }
        else
        {
            String[] tokens = str.split(";");
            assert tokens.length == 4;

            // split each token with a comma
            String[] items = tokens[0].split(",");
            minTemperatures = Arrays.asList(items);

            items = tokens[1].split(",");
            maxTemperatures = Arrays.asList(items);

            items = tokens[2].split(",");
            conditions = Arrays.asList(items);

            items = tokens[3].split(",");
            iconLinks = Arrays.asList(items);
        }
    }
    
    public Forecast(Parcel source)
    {
        Log.v(TAG, "ParcelData(Parcel source): time to put back parcel data");
        source.readStringList(minTemperatures);
        source.readStringList(maxTemperatures);
        source.readStringList(conditions);
        source.readStringList(iconLinks);
    }

    public void setMinTemperatures(List<String> list)
    {
        Log.v(TAG, "setMinTemperatures");
        if (list != null)
            minTemperatures = list;
    }

    public void setMaxTemperatures(List<String> list)
    {
        Log.v(TAG, "setMaxTemperatures");
        if (list != null)
            maxTemperatures = list;
    }

    public int size()
    {
        return conditions.size();
    }
    
    /**
     * Serialize temperature, condition and icon link
     * @return
     */
    public String get(int n)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append(conditions.get(n));
        
        if(n < maxTemperatures.size())
        {
            try{
                int temp = Integer.parseInt(maxTemperatures.get(n));
                sb.append(String.format(" %sF (%dC)", 
                        maxTemperatures.get(n),
                        convertToCelsius(temp)));
                    
            }catch(NumberFormatException e){
                // this probably means soap message or 
                // connection wasn't good.
                Log.e(TAG, e.getMessage(), e);
            }
        }
        if(n < iconLinks.size())
        {
            String s = String.format(",%s", iconLinks.get(n));
            sb.append(s);
        }
        return sb.toString();
    }
    
    private int convertToCelsius(int t)
    {
        float result = (t-32.0F) / 1.8F;
        return (int)result;
    }

    public void addCondition(String weatherCondition)
    {
        if (weatherCondition != null)
            conditions.add(weatherCondition);
        else
            Log.e(TAG, "weatherCondition to add was null.");
    }

    public void addIconLink(String iconLink)
    {
        if (iconLink != null)
            iconLinks.add(iconLink);
        else
            Log.e(TAG, "icon link to add was null.");
    }

    /**
     * this could return a hashcode instead
     */
    @Override
    public int describeContents()
    {
        return 0;
    }

    /**
     *  to flatten/serialize your custom object.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        Log.v(TAG, "writeToParcel..."+ flags);
        dest.writeStringList(minTemperatures);
        dest.writeStringList(maxTemperatures);
        dest.writeStringList(conditions);
        dest.writeStringList(iconLinks);
    }

    /**
     * Creator is required by Parcelable class
     *
     */
    public class MyCreator implements Parcelable.Creator<Forecast> {
        public Forecast createFromParcel(Parcel source) {
              return new Forecast(source);
        }
        public Forecast[] newArray(int size) {
              return new Forecast[size];
        }
    }

    /**
     * It is not appropriate to place any Parcel data in to persistent storage.
     * Thus we need custom serialize / deserialize methods to
     * persist the object in shared preferences and back.
     * @return
     */
    public String serialize()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(serializeArrayList(minTemperatures));
        sb.append(";");
        sb.append(serializeArrayList(maxTemperatures));
        sb.append(";");
        sb.append(serializeArrayList(conditions));
        sb.append(";");
        sb.append(serializeArrayList(iconLinks));
        return sb.toString();
    }
    
    private String serializeArrayList(List<String> ary)
    {
        StringBuilder sb = new StringBuilder();
        for(int n=0; n<ary.size(); n++)
        {
            sb.append(ary.get(n));
            if(n < conditions.size()-1)
                sb.append(",");
        }
        return sb.toString();
    }
}
