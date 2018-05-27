package com.example.a111.fuckapp;

import android.app.AlertDialog;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MappingSession implements LifecycleObserver {

    private String SessionTitle; //Default for the SessionTitle is the Date and Time
    private ArrayList<MappingPoint> Markers;

    //Constructor to make an "empty" MappingSession with giving a context
    public MappingSession(Context context){
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); //set the Format for the Date
        this.SessionTitle = df.format(Calendar.getInstance().getTime()); //date as default of SessionTitle. new Date(System.currentTimeMillis()) is another option
        this.Markers = new ArrayList<>();
    }

    //the Constructor to make a MappingSession Object from session Title, markers Context. When calling it from the Activity put "this" for the context value
    public MappingSession(String sessionTitle, ArrayList<MappingPoint> markers, Context context){
        this.SessionTitle = sessionTitle;
        this.Markers = markers;
    }

    //Constructor to make a Mapping Session Object from the shared preferences. When calling it from the Activity put "this" for the context value
    public MappingSession(String sessionTitle, Context context){
        this.SessionTitle = sessionTitle;
        SharedPreferences sharedPref = context.getSharedPreferences(this.SessionTitle, Context.MODE_PRIVATE);
        String json = sharedPref.getString(SessionTitle, null);
        this.Markers = new Gson().fromJson(json, new ListParameterizedType<>(MappingPoint.class));
    }

    //Save the Session to shared preferences, also gets fired when the activity gets on_Stop() to save the Session when the Activity is closed
    //@OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void Save(Context context){
        String DicName = "SessionDictionary"; //Name of the Dictionary
        String json = new Gson().toJson(Markers); //needs to be a string to save it, converts it to a JSON
        SharedPreferences sharedPref = context.getSharedPreferences(this.SessionTitle, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit(); //Creates an Editor to write to shared preferences
        editor.putString(SessionTitle,json); //Pass the Session as a key value pair
        editor.apply(); //Saves the changes, commit() instead apply() would return if it was successful as boolean

        SessionDictionary sessionDictionary = new SessionDictionary(DicName, context); //Dictionary gets opened
        sessionDictionary.updateDictionary(SessionTitle); //gets also updated
        sessionDictionary.Save(DicName, context); //and saved again
    }

    //Saves the Session to shared preferences after asking for a new Title if the Title is a date (default). can be used when the user leafs the activity to get to the list of Sessions
    public void SaveSession(final Context context){ //description at ExportSession()
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        try {
            df.parse(SessionTitle); //check if SessionTitle is a date

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Session Name").setMessage("Enter the Name for this Session");
            final EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    SessionTitle = input.getText().toString();
                    Save(context);
                }
            })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            Save(context);
                            dialog.cancel();
                        }
                    });

            // show it
            builder.show();
        }
        catch(ParseException e){
            Save(context);
        }
    }

    // add a Point to the Session
    public void addPoint(MappingPoint mappingPoint){
        this.Markers.add(mappingPoint);
    }

    //Exports the Session after asking for a new Title if the Title is default
    public void ExportSession(final Context context){
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        try {
            df.parse(SessionTitle); //check if SessionTitle is a date

            AlertDialog.Builder builder = new AlertDialog.Builder(context); //build an Dialog Alert
            builder.setTitle("Session Name").setMessage("Enter the Name for this Session");
            final EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_TEXT); //set the InputType
            builder.setView(input);

            //add ok and cancel buttons
            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    SessionTitle = input.getText().toString(); //adds the new Title
                    Export(context); //exports
                }
            })
            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    Export(context); //no new Title is entered, so it exports with the default
                    dialog.cancel();
                }
            });

            // show the alertDialog
            builder.show();
        }
        catch(ParseException e){
            Export(context); //the title was already an individual one, just exports without asking
        }
    }

    // Adds a Session to the map (the Markers)
    public void ApplySessiontoMap(GoogleMap map){
        for(MappingPoint point : Markers){
            map.addMarker(point.toMarkerOptions());
        }
    }

    //Exporting the session to external Storage
    private void Export(Context context){

        String LOG_TAG = "SessionExportError";
        String FileName = SessionTitle +".txt";
        String state = Environment.getExternalStorageState(); //get the state of the external storage
        if (!Environment.MEDIA_MOUNTED.equals(state)) { //check if it is not mounted
            Log.e(LOG_TAG, "No external storage mounted");
        }else {
            try {
                File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Mapping Sessions"); //Get the Path of the Downloads-Dictionary and the folder  "Mapping Sessions"
                if (!root.exists()) { //if the folder does not exist...
                    root.mkdirs(); //... add it
                }
                File file = new File(root, FileName); //get to our File
                file.createNewFile(); //create it if it doesn't exist
                FileWriter writer = new FileWriter(file); //Filewriter to write our new file
                writer.append(new Gson().toJson(Markers)); //Add a Json of your Markers to the File
                writer.flush(); //just send it!
                writer.close();
                Toast.makeText(context, "Saved to downloads", Toast.LENGTH_SHORT).show(); //shows a little "Saved" massage on the Display
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // this class implements the Interface ParameterizedType so that Gson().fromJson can properly interprete the MarkerOptions class (and every other Class)
    private static class ListParameterizedType<X> implements ParameterizedType {

        private Class<?> wrapped;

        private ListParameterizedType(Class<X> wrapped){
            this.wrapped = wrapped;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[] {wrapped};
        }

        @Override
        public Type getRawType(){
            return ArrayList.class;
        }

        @Override
        public Type getOwnerType(){
            return null;
        }
    }

}
