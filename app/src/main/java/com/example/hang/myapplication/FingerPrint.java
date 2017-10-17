package com.example.hang.myapplication;

import java.util.List;
import java.util.Map;

/**
 * Created by hang on 10/16/17.
 */


public class FingerPrint {
    private String position;
    //goal is either learning or tracking
    private String goal;
    //key is macAddress  (represents some specific AP)
    //value  (60 strength val from this AP)
    private Map<String, List<Integer>> map;

    public FingerPrint(String goal, String position, Map<String, List<Integer>> map) {
        this.position = position;
        this.map = map;
        this.goal = goal;
    }
}