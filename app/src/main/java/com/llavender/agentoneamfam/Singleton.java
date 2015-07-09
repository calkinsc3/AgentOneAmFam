package com.llavender.agentoneamfam;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lsl017 on 7/6/2015.
 */
public class Singleton {

    private static Singleton ourInstance;
    private static ArrayList<ParseUser> listOfClients;
    private static ParseObject currentClient;
    private static ParseObject currentPolicy;
    private static ArrayList<ParseObject> mediaFiles;
    private static List<ParseObject> claims;
    private static List<String> comments;
    private static List<Object> images;
    public static List<ParseObject> uploads;
    public static String PREFERENCES = "AmFam";
    public static final int IMAGE = 0;
    public static final int CLIENT = 1;
    public static final int POLICY = 2;
    public static final int CLAIM = 3;

    protected Singleton() {

    }

    public static Singleton getInstance() {
        if(ourInstance == null){
            ourInstance = new Singleton();
        }
        return ourInstance;
    }

    public static ArrayList<ParseUser> getListOfClients() {
        if(listOfClients == null){
            listOfClients = new ArrayList<>();
        }
        return listOfClients;
    }

    public static void setListOfClients(ArrayList<ParseUser> listOfClients) {
        Singleton.listOfClients = listOfClients;
    }

    public static ParseObject getCurrentClient() {
        return currentClient;
    }

    public static void setCurrentClient(ParseObject currentClient) {
        Singleton.currentClient = currentClient;
    }

    public static ParseObject getCurrentPolicy() {
        return currentPolicy;
    }

    public static void setCurrentPolicy(ParseObject currentPolicy) {
        Singleton.currentPolicy = currentPolicy;
    }

    public static ArrayList<ParseObject> getMediaFiles() {
        return mediaFiles;
    }

    public static void setMediaFiles(ArrayList<ParseObject> images) {
        Singleton.mediaFiles = images;
    }

    public static List<ParseObject> getClaims() {
        return claims;
    }

    public static void setClaims(List<ParseObject> claims) {
        Singleton.claims = claims;
    }

    public static List<String> getComments() {
        return comments;
    }

    public static void setComments(List<String> comments) {
        Singleton.comments = comments;
    }

    public static List<Object> getImages() {
        return images;
    }

    public static void setImages(List<Object> images) {
        Singleton.images = images;
    }

    public static List<ParseObject> getUploads() {
        return uploads;
    }

    public static void setUploads(List<ParseObject> uploads) {
        Singleton.uploads = uploads;
    }
}
