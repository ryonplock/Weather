/**
 * 
 */
package edu.washington.shan;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

/**
 * @author shan@uw.edu
 * 
 *         Parses Ndfd soap XML response sent from the server
 * 
 */
public class NDFDXmlParser_NDFDgenByDay
{
    private static final String TAG = "NDFDXmlParser_NDFDgenByDay";

    // Tag names and attribute names to parse for
    private static final String TEMPERATURE = "temperature";
    private static final String MINIMUM = "minimum";
    private static final String MAXIMUM = "maximum";
    private static final String WEATHER = "weather";
    private static final String WEATHER_CONDITIONS = "weather-conditions";
    private static final String WEATHER_SUMMARY = "weather-summary";
    private static final String CONDITIONS_ICON = "conditions-icon";
    private static final String ICON_LINK = "icon-link";
    private static final String VALUE = "value";
    private static final String INVALID = "invalid";
    
    private Forecast forecast;

    public NDFDXmlParser_NDFDgenByDay(Forecast forecast)
    {
        this.forecast = forecast;
    }

    public boolean parse(String xmlString)
    {
        boolean result = false;
        try
        {
            parsePrivate(xmlString);
            result = true;
        }
        catch (XmlPullParserException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        return result;
    }

    /**
     * Parses the Ndfd soap response XML and populates forecast object from it
     * 
     * @param xmlString
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void parsePrivate(String xmlString) throws XmlPullParserException,
            IOException
    {
        if (xmlString == null || xmlString.length() == 0)
            return;

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        // factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput(new StringReader(xmlString));
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_DOCUMENT)
            {
                Log.v(TAG, "Start document");
            }
            else if (eventType == XmlPullParser.START_TAG)
            {
                String name = xpp.getName();
                //Log.v(TAG, "Start tag: " + name);
                if (name.equalsIgnoreCase(TEMPERATURE))
                {
                    String attrValue = readAttributeValue(xpp, "type",
                            INVALID);
                    if (attrValue.equalsIgnoreCase(MAXIMUM))
                        readMaxTemperatureValues(xpp);
                    else if (attrValue.equalsIgnoreCase(MINIMUM))
                        readMinTemperatureValues(xpp);
                }
                else if (name.equalsIgnoreCase(WEATHER))
                {
                    readWeatherConditions(xpp);
                }
                else if (name.equalsIgnoreCase(CONDITIONS_ICON))
                {
                    readIconLinks(xpp);
                }
            }
            eventType = xpp.next();
        }
    }

    /**
     * Reads icon-link tags and add them to forecast
     * 
     * @param xpp
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void readIconLinks(XmlPullParser xpp)
            throws XmlPullParserException, IOException
    {
        int eventType = xpp.next();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_TAG)
            {
                // Read a tag
                // <icon-link>http://www.nws.noaa.gov/weather/images/fcicons/bkn.jpg</icon-link>
                if (xpp.getName().equalsIgnoreCase(ICON_LINK))
                {
                    this.forecast.addIconLink(xpp.nextText());
                }
            }
            else if (eventType == XmlPullParser.END_TAG)
            {
                // exit when we get to "</conditions_icon>"
                if (xpp.getName().equalsIgnoreCase(CONDITIONS_ICON))
                    break;
            }
            eventType = xpp.next();
        }
    }

    /**
     * Reads weather-conditions for multiple days and adds them to forecast
     * 
     * @param xpp
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void readWeatherConditions(XmlPullParser xpp)
            throws XmlPullParserException, IOException
    {
        int eventType = xpp.next();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_TAG)
            {
                // Read a tag <weather-conditions weather-summary="Mostly Cloudy"/>
                if (xpp.getName().equalsIgnoreCase(WEATHER_CONDITIONS))
                {
                    String attrValue = readAttributeValue(xpp, WEATHER_SUMMARY,
                            INVALID);
                    this.forecast.addCondition(attrValue);
                }
            }
            else if (eventType == XmlPullParser.END_TAG)
            {
                // exit when we get to "</weather>"
                if (xpp.getName().equalsIgnoreCase(WEATHER))
                    break;
            }
            eventType = xpp.next();
        }
    }

    /**
     * Reads minimum temperature and adds them to forecast.
     * 
     * @param xpp
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void readMinTemperatureValues(XmlPullParser xpp)
            throws XmlPullParserException, IOException
    {
        this.forecast.setMinTemperatures(readTemperatureValues(xpp));
    }

    /**
     * Reads maximum temperatures and adds them to forecast.
     * 
     * @param xpp
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void readMaxTemperatureValues(XmlPullParser xpp)
            throws XmlPullParserException, IOException
    {
        this.forecast.setMaxTemperatures(readTemperatureValues(xpp));
    }

    /**
     * Reads temperature values from a tag like <value>37</value>. xpp must be
     * pointing to <temperature> tag when calling this method.
     * 
     * @param xpp
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private List<String> readTemperatureValues(XmlPullParser xpp)
            throws XmlPullParserException, IOException
    {
        List<String> values = new ArrayList<String>();

        int eventType = xpp.next();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_TAG)
            {
                if (xpp.getName().equalsIgnoreCase(VALUE))
                {
                    String value = xpp.nextText();
                    if(value.length() != 0)
                        values.add(value);
                    else
                     // sometimes you get an empty value like <value type="nil" />
                        values.add(INVALID); 
                }
            }
            else if (eventType == XmlPullParser.END_TAG)
            {
                // exit when we get to "</temperature>"
                if (xpp.getName().equalsIgnoreCase(TEMPERATURE))
                    break;
            }
            eventType = xpp.next();
        }
        return values;
    }

    /**
     * Finds an attribute of 'name' and returns its value. If an attribute of
     * 'name' is not found the return value is 'def'.
     * 
     * @param xpp   Parser
     * @param name  Name of the attribute to search for
     * @param def   Default value to return if an attribute is not found
     * @return
     */
    private String readAttributeValue(XmlPullParser xpp, String name, String def)
    {
        int count = xpp.getAttributeCount();
        for (int n = 0; n < count; n++)
        {
            String attrName = xpp.getAttributeName(n);
            if (attrName != null && attrName.equalsIgnoreCase(name))
                return xpp.getAttributeValue(n);
        }
        return def;
    }
}
