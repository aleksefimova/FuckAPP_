package com.example.a111.fuckapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

// this class holds a list of all the sessiontitles and a timestamp so that we can find them later in the shared preferences
// from this class there is only one instance implemented in the Activity, and in the shared pref. This instance is named "SessionDictionary", it can be imagined as the map to all the "session files" in the shared prf
public class SessionDictionary {

    private ArrayList<DictionaryItem> Dictionary;

    //this constructor loads the list from the shared preferences, to find that list it has to have the key "SessionDictionary"
    public SessionDictionary(String Key, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(Key, Context.MODE_PRIVATE);
        String Djson = sharedPref.getString(Key, null);
        if (Djson != null) {
            this.Dictionary = new Gson().fromJson(Djson, new SessionDictionary.ListParameterizedType<>(DictionaryItem.class));
        }else{ //if the list doen't exist yet
            this.Dictionary = new ArrayList<>();
        }
    }

    public ArrayList<DictionaryItem> getDictionary() {
        return Dictionary;
    }

    //Saves the Sessions Dictionary to the shared pref, again, just use "SessionDictionary" as key
    public void Save(String Key, Context context){
        String json = new Gson().toJson(Dictionary); //needs to be a string to save it, converts it to a JSON
        SharedPreferences sharedPref = context.getSharedPreferences(Key, Context.MODE_PRIVATE); //reference to the sharedprf
        SharedPreferences.Editor editor = sharedPref.edit(); //Creates an Editor to write to shared preferences
        editor.putString(Key,json); //Pass the Session as a key value pair
        editor.apply(); //Saves the changes, commit() instead apply() would return if it was successful as boolean
    }

    //add a new Session to the Dictionary
    public void addSession(String sessionTitle){
        Dictionary.add(new DictionaryItem(sessionTitle));
    }

    //Update the Session: If the entry doesn't exist it gets added, if it exists just the date gets an update
    public void updateDictionary(String sessionTitle){
        boolean addneed = true; //add needed

        for (DictionaryItem item : Dictionary) {
            if (item.getSessionTitle() == sessionTitle) { //if the entry already exists
                item.setDate(Calendar.getInstance()); // just update of timestamp
                addneed = false; // no add needed
            }
        }
        if (addneed){
            addSession(sessionTitle); // new Session is added to the Dictionary
        }
    }

    // method to get the newest entry in the Dictionary (the last Session worked on)
    public String getNewest(){
        Calendar best = Calendar.getInstance();
        best.set(1971, 0, 1); //just a very old date to compare
        DictionaryItem bestItem = new DictionaryItem();
        for(DictionaryItem item : Dictionary){
            if (item.getDate().after(best)){ //if the date is newer
                best = item.getDate();
                bestItem = item;
            }
        }
        return bestItem.getSessionTitle(); //returns the newest Entry in the List
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
