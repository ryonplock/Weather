/**
 * 
 */
package edu.washington.shan;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

/**
 * @author shan@uw.edu
 *
 */
public class Utils
{
    
    static public String getTodayInFormat(SimpleDateFormat dateFormat)
    {
        java.util.Date date = new java.util.Date();
        return dateFormat.format(date);
    }
    
    /**
     * 
     */
    static public void showAboutDialogBox(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dlgView = inflater.inflate(R.layout.help_dialog_layout, null);

        WebView webview = (WebView) dlgView.findViewById(R.id.help_dialog_layout_webView1);
        webview.loadUrl("file:///android_asset/readme.html");
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.help_dialog_title));
        //builder.setIcon(R.drawable.ic_tab_about); // sets the top left icon
        builder.setView(dlgView);

        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }
    
    static public void showAboutDialogBoxOnFirstRun(Context context) {
        if(isFirstTimeRunFlagSet(context)) {
            showAboutDialogBox(context);
        }
    }
    
    /**
     * Returns true if this is first time app is launched
     * @return
     */
    static public boolean isFirstTimeRunFlagSet(Context context){
        // Determine if this is the first time running the app
        // Shared preference for 'initialized' is stored in 
        // MainActivity.xml preference file.
        SharedPreferences sharedPref = ((Activity)context).getPreferences(Context.MODE_PRIVATE);
        if(!sharedPref.getBoolean("initialized", false))
            return true;
        return false;
    }
    
    /**
     * Clears 'first time run' flag
     */
    static public void clearFirstTimeRunFlag(Context context){
        SharedPreferences sharedPref = ((Activity)context).getPreferences(Context.MODE_PRIVATE);
        // Now set the "initialized" flag
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("initialized", true);
        editor.commit();
    }
 
}
