package com.appvengers.screenprinter;

public class PrintResponse {
    boolean error;
    String msg;

    public PrintResponse(boolean error, String msg) {
        this.error = error;
        this.msg = msg;
    }

}
