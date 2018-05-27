package com.example.a111.fuckapp;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MappingPoint {

    private LatLng latlng;
    private String PointTitle;
    private String LocatingMode = "manual"; //Should be changed to GPS when GPS is used
    private int  availableSatellites;

    //Constructor also add the Metadata attributes here
    public MappingPoint(LatLng latlng, int Satellites){
        this.latlng = latlng;
        this.availableSatellites = Satellites;
    }

    //converts the MappingPoint to a MarkerOption
    public MarkerOptions toMarkerOptions(){
        return new MarkerOptions().position(latlng).title(PointTitle);
    }

    public void setPointTitle(String pointTitle) {
        PointTitle = pointTitle;
    }

    public void setLocatingMode(String locatingMode) {
        LocatingMode = locatingMode;
    }
}
