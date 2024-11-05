package com.zk.service;

public class FingerprintServiceSingleton {

    private static final FingerprintServiceSingleton INSTANCE = new FingerprintServiceSingleton();

    private long mhDevice = 0;
    private long mhDB = 0;
    private boolean bRegister = false;
    private boolean bIdentify = true;
    private int enroll_idx = 0;

    private FingerprintServiceSingleton() { }

    public static FingerprintServiceSingleton getInstance() {
        return INSTANCE;
    }

    public long getDevice() {
        return mhDevice;
    }

    public void setDevice(long device) {
        this.mhDevice = device;
    }

    public long getDb() {
        return mhDB;
    }

    public void setDb(long db) {
        this.mhDB = db;
    }

    public boolean isRegister() {
        return bRegister;
    }

    public void setRegister(boolean bRegister) {
        this.bRegister = bRegister;
    }

    public boolean isIdentify() {
        return bIdentify;
    }

    public void setIdentify(boolean bIdentify) {
        this.bIdentify = bIdentify;
    }

    public void resetState() {
        this.bRegister = false;
        this.bIdentify = true;
        this.enroll_idx = 0;
    }

    public int getEnrollIdx() {
        return enroll_idx;
    }

    public void setEnrollIdx(int enroll_idx) {
        this.enroll_idx = enroll_idx;
    }
}

