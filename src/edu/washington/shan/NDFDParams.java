/**
 * 
 */
package edu.washington.shan;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author shan@uw.edu
 * 
 * This class represents parameters for a soap request
 * 
 */
/**
*
* @author sujin
*/
public class NDFDParams {
   public enum Type { 
       LATITUDE(0), LONGITUDE(1), STARTDATE(2), 
       NUMDAYS(3), UNIT(4), FORMAT(5), ZIPCODE(6);
       
       private final int index;
       Type(int index){
           this.index = index;
       }
       
       public int getIndex(){
           return index;
       }
   };
   
   // Keys
   private static final String[] KEYS =
   { "latitude", "longitude", "startDate", "numDays", "unit", "format", "zipcode" };

   // Default values to use
   private static final String[] DEFAULT_VALUES =
   { "47.611", "-122.333", "2012-01-13", "5", "e", "24 hourly", "98101" };
   
   private List<String> paramValues = new ArrayList<String>();

   public NDFDParams(Context context)
   {
       load(context);
   }
   
   public String[] getParams()
   {
       String[] ret = new String[paramValues.size()];
       paramValues.toArray(ret);
       return ret;
   }
   
   public String getParam(Type type)
   {
       if(type.getIndex() >= KEYS.length)
           throw new IndexOutOfBoundsException("type index is out of range");
       return paramValues.get(type.getIndex());
   }
   
   public void setParam(Type type, String value)
   {
       if(type.getIndex() >= KEYS.length)
           throw new IndexOutOfBoundsException("type index is out of range");
       paramValues.set(type.getIndex(), value);
   }

    public void persist(Context context)
    {
        SharedPreferences sharedPref = ((Activity) context)
                .getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        for (int n = 0; n < KEYS.length; n++)
            editor.putString(KEYS[n], paramValues.get(n));
        editor.commit();
    }
   
   public void load(Context context)
   {
       SharedPreferences prefs =
           PreferenceManager.getDefaultSharedPreferences(context);
       
       for(int n=0; n < KEYS.length; n++)
           paramValues.add(prefs.getString(KEYS[n], DEFAULT_VALUES[n]));
   }
}
