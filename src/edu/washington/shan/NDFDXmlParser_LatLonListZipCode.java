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
 */
public class NDFDXmlParser_LatLonListZipCode
{

/*
    Class designed to parse a soap response sent 
    from the server for LatLonListZipCode soap method.
    <?xml version='1.0'?>
    <dwml version='1.0' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='http://graphical.weather.gov/xml/DWMLgen/schema/DWML.xsd'>
        <latLonList>47.6103,-122.334</latLonList>
    </dwml>
*/
    
    private static final String TAG = "NDFDXmlParser_LatLonListZipCode";

    private static final String ELEMENTNAME = "latLonList";

    public NDFDXmlParser_LatLonListZipCode()
    {
    }

    /**
     * 
     * @param xmlString XML string to parse
     * @return Return latitude, longitude separated by a comma like "47.6103,-122.334". Null if parsing fails.
     */
    public String parse(String xmlString)
    {
        try
        {
            return parsePrivate(xmlString);
        }
        catch (XmlPullParserException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Parses the Ndfd soap response XML and populates forecast object from it
     * 
     * @param xmlString
     * @throws XmlPullParserException
     * @throws IOException
     */
    private String parsePrivate(String xmlString) throws XmlPullParserException,
            IOException
    {
        if (xmlString == null || xmlString.length() == 0)
            return null;

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
                if (name.equalsIgnoreCase(ELEMENTNAME))
                {
                    return xpp.nextText();
                }
            }
            eventType = xpp.next();
        }
        return null;
    }
}
