/**
 * 
 */
package edu.washington.shan;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * @author shan@uw.edu
 * 
 *         Represents weather forecast
 * 
 */
public class Forecast
{
    private static final String TAG = "Forecast";
    private List<String> minTemperatures;
    private List<String> maxTemperatures;
    private List<String> conditions; // weather conditions such as Cloudy, Rain
    private List<String> iconLinks;

    public Forecast()
    {
        minTemperatures = new ArrayList<String>();
        maxTemperatures = new ArrayList<String>();
        conditions = new ArrayList<String>();
        iconLinks = new ArrayList<String>();
    }

    public void setMinTemperatures(List<String> list)
    {
        Log.v(TAG, "setMinTemperatures");
        if (list != null)
            minTemperatures = list;
    }

    public List<String> getMinTemperatures()
    {
        return minTemperatures;
    }

    public void setMaxTemperatures(List<String> list)
    {
        Log.v(TAG, "setMaxTemperatures");
        if (list != null)
            maxTemperatures = list;
    }

    public List<String> getMaxTemperatures()
    {
        return maxTemperatures;
    }

    public void setConditions(List<String> list)
    {
        Log.v(TAG, "setConditions");
        if (list != null)
            conditions = list;
    }

    /**
     * Serialize temperature, condition and icon link
     * @return
     */
    public List<String> getConditions()
    {
        List<String> result = new ArrayList<String>();
        
        for(int n=0; n< conditions.size(); n++)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(conditions.get(n));
            
            if(n < maxTemperatures.size())
            {
                String s = String.format(" %sF (%dC)", 
                    maxTemperatures.get(n),
                    convertToCelsius(Integer.parseInt(maxTemperatures.get(n))));
                sb.append(s);
            }
            if(n < iconLinks.size())
            {
                String s = String.format(";%s", iconLinks.get(n));
                sb.append(s);
            }
            result.add(sb.toString());
        }
        
        return result;
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

    /*
     * public void addMinTemp(String temperature)
     * { 
     *  if(temperature != null)
     *      minTemperatures.add(temperature); else Log.e(TAG,
     *          "min temperature to add was null."); 
     * }
     * 
     * public void addMaxTemp(String temperature)
     * { 
     *  if(temperature != null)
     *      maxTemperatures.add(temperature); else Log.e(TAG,
     *          "max temperature to add was null."); 
     * }
     * 
     */
}
