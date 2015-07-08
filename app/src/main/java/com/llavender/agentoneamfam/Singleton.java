package com.llavender.agentoneamfam;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * Created by lsl017 on 7/6/2015.
 */
public class Singleton {

    private static Singleton ourInstance;
    private static ArrayList<ParseUser> listOfClients;
    private static ParseObject currentClient;
    private static ParseObject currentPolicy;
    private static ArrayList<ParseObject> images;

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

    public static ArrayList<ParseObject> getImages() {
        return images;
    }

    public static void setImages(ArrayList<ParseObject> images) {
        Singleton.images = images;
    }
}
