package com.lucky.jacklamb.enums;

public enum Code {

    ERROR("HTTP Status 500 Internal Server Error",500),
    NOTFOUND("HTTP Status 404 Not Found",404),
    REFUSED("HTTP Status 403 Blocking Access",403);

    public String errTitle;

    public int code;

    private Code(String errTitle,int code){
        this.errTitle=errTitle;
        this.code=code;
    }
}
