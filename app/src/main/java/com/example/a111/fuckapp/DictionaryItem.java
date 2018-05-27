package com.example.a111.fuckapp;

import java.util.Calendar;
import java.util.Date;

/**
 * Just a Helper Class to hold the Types used in SessionDictionary.java
 */
public class DictionaryItem {

    private String SessionTitle; //The Titles of a Session
    private Calendar date; //The date the Session was created/updated

    //constructor
    public DictionaryItem(String sessionTitle){
        this.SessionTitle = sessionTitle;
        this.date = Calendar.getInstance(); //current date
    }

    //empty constructor
    public DictionaryItem(){
    }

    public String getSessionTitle() {
        return SessionTitle;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }
}
