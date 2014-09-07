package com.brunocascio.cualferiado.Services;

/**
 * Created by d3m0n on 23/08/14.
 */
public class SyncEvent {

    private String message;
    private String type;

    public SyncEvent(){
        this.message = "";
        this.type = "";
    }

    public SyncEvent(String s, String type) {
        this.message = s;
        this.type = type;
    }

    public SyncEvent(String s) {
        this.message = s;
        this.type = "success";
    }

    public String getMessage(){
        return this.message;
    }

    public String getType(){
        return this.type;
    }
}
