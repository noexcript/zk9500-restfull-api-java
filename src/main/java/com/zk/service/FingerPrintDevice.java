package com.zk.service;

import com.zkteco.biometric.FingerprintSensorErrorCode;
import com.zkteco.biometric.FingerprintSensorEx;

public class FingerPrintDevice {
    private long mhDevice = 0;
    private long mhDB = 0;

    public boolean openDevice() {
        if (this.mhDevice != 0) {
            return false;
        }

        int ret = FingerprintSensorEx.Init();

        if (ret != FingerprintSensorErrorCode.ZKFP_ERR_OK) {
            this.closeDevice();
            return false;
        }

        this.mhDevice = FingerprintSensorEx.OpenDevice(0);

        if (this.mhDevice == 0) {
            this.closeDevice();

            return false;
        }

        this.mhDB = FingerprintSensorEx.DBInit();

        return this.mhDB != 0;
    }

    public void closeDevice() {
        this.FreeSensor();
    }

    private void FreeSensor() {
        if (this.mhDB != 0) {
            FingerprintSensorEx.DBFree(mhDB);
            this.mhDB = 0;
        }
        if (this.mhDevice != 0) {
            FingerprintSensorEx.CloseDevice(this.mhDevice);
            this.mhDevice = 0;
        }

        FingerprintSensorEx.Terminate();
    }

    public long getDeviceHandle() {
        return this.mhDevice;
    }

    public long getDatabaseHandle() {
        return this.mhDB;
    }

}
