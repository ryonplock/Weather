/**
 * 
 */
package edu.washington.shan;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @author shan@uw.edu
 * 
 * A worker thread to send and receive a soap request/response
 *
 */
public class WorkerThreadRunnable implements Runnable {
	
	private static final String TAG = "WorkerThreadRunnable";
	
    // NDFD server and wsdl
	private static final String URL = "http://graphical.weather.gov/xml/SOAP_server/ndfdXMLserver.php";
    private static final String NAMESPACE = "http://graphical.weather.gov/xml/DWMLgen/wsdl/ndfdXML.wsdl";
    
	private Handler mHandler;
	private NDFDParams mArgs;
	
	/**
	 * Worker thread constructor
	 * @param context Context under which to create DbAdapter and manage cursor
	 * @param handler Callback function if the caller wants to be notified when worker thread is complete. May be null.
	 * @param args  NDFDParams
	 */
	public WorkerThreadRunnable(Handler handler, NDFDParams args)
	{
		mHandler = handler;
		mArgs = args;
	}

    /**
    Builds a SOAP request like this:
    <ndf:NDFDgenByDay soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
        <latitude xsi:type="xsd:decimal">47.853</latitude>
        <longitude xsi:type="xsd:decimal">-122.283</longitude>
        <startDate xsi:type="xsd:date">2012-01-13</startDate>
        <numDays xsi:type="xsd:integer">3</numDays>
        <Unit xsi:type="dwml:unitType" xmlns:dwml="http://graphical.weather.gov/xml/DWMLgen/schema/DWML.xsd">e</Unit>
        <format xsi:type="dwml:formatType" xmlns:dwml="http://graphical.weather.gov/xml/DWMLgen/schema/DWML.xsd">24 hourly</format>
     </ndf:NDFDgenByDay>
    */
	@Override
	public void run() {
	    Forecast forecast = null;
	    
        try
        {
            String[] latlon = callLatLonListZipCode(mArgs.getParam(NDFDParams.Type.ZIPCODE));
            
            if(latlon.length == 2)
                forecast = callNDFDgenByDay(latlon[0], latlon[1] );
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        catch (XmlPullParserException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
	    finally
	    {
	        informFinish(forecast);
	    }
	}
	
	/*
    Name: LatLonListZipCode
    Binding: ndfdXMLBinding
    Endpoint: http://graphical.weather.gov/xml/SOAP_server/ndfdXMLserver.php
    SoapAction: http://graphical.weather.gov/xml/DWMLgen/wsdl/ndfdXML.wsdl#LatLonListZipCode
    Style: rpc
    Input:
      use: encoded
      namespace: http://graphical.weather.gov/xml/DWMLgen/wsdl/ndfdXML.wsdl
      encodingStyle: http://schemas.xmlsoap.org/soap/encoding/
      message: LatLonListZipCodeRequest
      parts:
        zipCodeList: xsd:string
    Output:
      use: encoded
      namespace: http://graphical.weather.gov/xml/DWMLgen/wsdl/ndfdXML.wsdl
      encodingStyle: http://schemas.xmlsoap.org/soap/encoding/
      message: LatLonListZipCodeResponse
      parts:
        listLatLonOut: xsd:string
    Namespace: http://graphical.weather.gov/xml/DWMLgen/wsdl/ndfdXML.wsdl
    Transport: http://schemas.xmlsoap.org/soap/http
    Documentation: Returns the latitude and longitude pairs corresponding to a list of one or more zip codes. Supports zip codes for the Continental United States, Alaska, Hawaii, and Puerto Rico only. Provides points in a format suitable for use in calling multi-point functions NDFDgenLatLonList and NDFDgenByDayLatLonList.
	*/
	
	/**
	 * Returns latitude and longitude for a zip code
	 * @param zip code for which to find the latitude and longitude
	 * @throws XmlPullParserException 
	 * @throws IOException 
	 */
	private String[] callLatLonListZipCode(String zipcode) throws IOException, XmlPullParserException
	{
	    if(zipcode == null || zipcode.length() == 0)
	        throw new IllegalArgumentException("zipcode must be valid");
	    
	    String[] results = null;
	    
        final String SOAP_ACTION = "http://graphical.weather.gov/xml/DWMLgen/wsdl/ndfdXML.wsdl#LatLonListZipCode";
        
        SoapObject request = new SoapObject(NAMESPACE, "ndf:NDFDgenByDay");
        request.addAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");
        request.addProperty("zipCodeList", zipcode);
        
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        
        HttpTransportSE transportSE = new HttpTransportSE(URL);
        transportSE.call(SOAP_ACTION, envelope);
        if(envelope.getResponse() != null)
        {
            String strXml = envelope.getResponse().toString();
            NDFDXmlParser_LatLonListZipCode parser = new NDFDXmlParser_LatLonListZipCode();
            String result = parser.parse(strXml);
            if(result != null)
                results = result.split(",");
        }
        return results;
	}
	
	/*
    Name: NDFDgenByDay
    Binding: ndfdXMLBinding
    Endpoint: http://graphical.weather.gov/xml/SOAP_server/ndfdXMLserver.php
    SoapAction: http://graphical.weather.gov/xml/DWMLgen/wsdl/ndfdXML.wsdl#NDFDgenByDay
    Style: rpc
    Input:
      use: encoded
      namespace: http://graphical.weather.gov/xml/DWMLgen/wsdl/ndfdXML.wsdl
      encodingStyle: http://schemas.xmlsoap.org/soap/encoding/
      message: NDFDgenByDayRequest
      parts:
        latitude: xsd:decimal
        longitude: xsd:decimal
        startDate: xsd:date
        numDays: xsd:integer
        Unit: xsd:string
        format: xsd:string
    Output:
      use: encoded
      namespace: http://graphical.weather.gov/xml/DWMLgen/wsdl/ndfdXML.wsdl
      encodingStyle: http://schemas.xmlsoap.org/soap/encoding/
      message: NDFDgenByDayResponse
      parts:
        dwmlByDayOut: xsd:string
    Namespace: http://graphical.weather.gov/xml/DWMLgen/wsdl/ndfdXML.wsdl
    Transport: http://schemas.xmlsoap.org/soap/http
    Documentation: Returns National Weather Service digital weather forecast data. Supports latitudes and longitudes for the Continental United States, Hawaii, Guam, and Puerto Rico only. Allowable values for the input variable "format" are "24 hourly" and "12 hourly". The input variable "startDate" is a date string representing the first day (Local) of data to be returned. The input variable "numDays" is the integer number of days for which the user wants data. Allowable values for the input variable "Unit" are "e" for U.S. Standare/English units and "m" for Metric units.
	*/
	private Forecast callNDFDgenByDay(String latitude, String longitude) throws IOException, XmlPullParserException
	{
        final String SOAP_ACTION = "http://graphical.weather.gov/xml/DWMLgen/wsdl/ndfdXML.wsdl#NDFDgenByDay";
        
        Forecast forecast = null;
        
        SoapObject request = new SoapObject(NAMESPACE, "ndf:NDFDgenByDay");
        request.addAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");
        request.addProperty("latitude", latitude);
        request.addProperty("longitude", longitude);
        request.addProperty("startDate", mArgs.getParam(NDFDParams.Type.STARTDATE));
        request.addProperty("numDays", mArgs.getParam(NDFDParams.Type.NUMDAYS));
        request.addProperty("Unit", mArgs.getParam(NDFDParams.Type.UNIT));
        request.addProperty("format", mArgs.getParam(NDFDParams.Type.FORMAT));
        
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        
        HttpTransportSE transportSE = new HttpTransportSE(URL);
        //transportSE.debug = true; // DEBUG ONLY
        transportSE.call(SOAP_ACTION, envelope);
        //String responseDump = transportSE.responseDump; // DEBUG ONLY - must set .debug=true
        if(envelope.getResponse() != null)
        {
            String strXml = envelope.getResponse().toString();
            forecast = new Forecast();
            NDFDXmlParser_NDFDgenByDay parser = new NDFDXmlParser_NDFDgenByDay(forecast);
            parser.parse(strXml);
        }
        return forecast;
	}
	
	/**
	 * Notify the caller of the worker thread completion
	 * @param results
	 */
	public void informFinish(Forecast forecast)
	{
		Log.v(TAG, "Finished retreiving data");
		
		// Return the status
		Bundle bundle = new Bundle();
		bundle.putParcelable(Constants.KEY_STATUS, forecast);
		// note that forecast can be null. The receiver must be aware and handle that. 
		
		if(mHandler != null)
		{
			Message msg = mHandler.obtainMessage();
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}
	}

}