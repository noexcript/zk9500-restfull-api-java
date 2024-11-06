package com.zk.utils;

public class ResponseBody {

    private String message;
    private Object object;

    public ResponseBody(String message, Object object) {
        this.message = message;
        this.object = object;
    }

    public String getMessage() {
        return this.message;
    }

    public Object getObject() {
        return this.object;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setObject(Object object){
        this.object =  object;
    }
}
