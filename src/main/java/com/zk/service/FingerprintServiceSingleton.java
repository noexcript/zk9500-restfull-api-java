package com.zk.service;

public class FingerprintServiceSingleton {

    private static final FingerprintServiceSingleton INSTANCE = new FingerprintServiceSingleton();

    private long mhDevice = 0;
    private long mhDB = 0;
    private boolean bRegister = false;
    private boolean bIdentify = true;
    private int enroll_idx = 0;
    private boolean isCaptureRunning = false;
    private byte[] imgBuf = null;
    private int width = 0;
    private int cbRegTemp = 0;
    private int height = 0;
    private int iFid = 1;

    private FingerprintServiceSingleton() {
    }

    public static FingerprintServiceSingleton getInstance() {
        return INSTANCE;
    }

    public boolean isCaptureRunning() {
        return isCaptureRunning;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setRegTemp(int cbRegTemp) {
        this.cbRegTemp = cbRegTemp;
    }

    public int getRegTemp() {
        return this.cbRegTemp;
    }

    public int getFid() {
        return this.iFid;
    }

    public void setFid(int iFid) {
        this.iFid = iFid;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeigth() {
        return this.height;
    }

    public void setCaptureRunning(boolean isCaptureRunning) {
        this.isCaptureRunning = isCaptureRunning;
    }

    public void setImageBuf(byte[] imgBuf) {
        this.imgBuf = imgBuf;
    }

    public byte[] getImageBuf() {
        return this.imgBuf;
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
        this.isCaptureRunning = false;
        this.imgBuf = null;
        this.cbRegTemp = 0;
        this.iFid = 1;
    }

    public int getEnrollIdx() {
        return enroll_idx;
    }

    public void setEnrollIdx(int enroll_idx) {
        this.enroll_idx = enroll_idx;
    }
}
