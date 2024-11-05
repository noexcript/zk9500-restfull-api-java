package com.zk.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;
import javax.ws.rs.core.Response;

import com.zk.utils.BitmapUtils;
import com.zk.utils.FingerprintUtils;
import com.zkteco.biometric.FingerprintSensorErrorCode;
import com.zkteco.biometric.FingerprintSensorEx;

public class FingerprintService {
    private static final int ENROLL_COUNT = 3;
    private static final int TEMPLATE_SIZE = 2048;

    private long mhDevice = 0;
    private long mhDB = 0;
    private boolean mbStop = true;
    private int fpWidth = 0;
    private int fpHeight = 0;
    private String bufferes = null;

    private byte[] lastRegTemp = new byte[TEMPLATE_SIZE];
    private int cbRegTemp = 0;
    private byte[][] regtemparray = new byte[3][TEMPLATE_SIZE];

    private boolean bRegister = false;
    private boolean bIdentify = true;
    private int iFid = 1;
    private int nFakeFunOn = 1;
    private int enroll_idx = 0;

    private byte[] imgbuf = null;
    private byte[] template = new byte[TEMPLATE_SIZE];
    private int[] templateLen = new int[1];

    private WorkThread workThread = null;
    private final FingerFingerSocketService socketServer = FingerFingerSocketService.getInstance();

    public Response openDevice() {

        FingerprintServiceSingleton service = FingerprintServiceSingleton.getInstance();

        if (service.getDevice() != 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Device is already open")
                    .build();
        }

        int ret = FingerprintSensorEx.Init();
        if (ret != FingerprintSensorErrorCode.ZKFP_ERR_OK) {
            freeSensor();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to initialize")
                    .build();
        }

        long device = FingerprintSensorEx.OpenDevice(0);
        if (device == 0) {
            freeSensor();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Failed to open device: " + ret)
                    .build();
        }

        service.setDevice(device);

        if (!initializeDevice()) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to initialize device")
                    .build();
        }

        return startCapture();
    }

    private boolean initializeDevice() {
        FingerprintServiceSingleton service = FingerprintServiceSingleton.getInstance();
        long mhDB = FingerprintSensorEx.DBInit();
        if (mhDB == 0) {
            FingerprintSensorEx.CloseDevice(service.getDevice());
            service.setDevice(0);
            return false;
        }
        service.setDb(mhDB);

        FingerprintSensorEx.DBSetParameter(mhDB, 5010, 0); // Ansi format
        setupDeviceParameters();
        return true;
    }

    private void setupDeviceParameters() {

        FingerprintServiceSingleton service = FingerprintServiceSingleton.getInstance();
        byte[] paramValue = new byte[4];
        int[] size = new int[1];
        size[0] = 4;

        FingerprintSensorEx.GetParameters(service.getDevice(), 1, paramValue, size);
        fpWidth = FingerprintUtils.byteArrayToInt(paramValue);

        size[0] = 4;
        FingerprintSensorEx.GetParameters(service.getDevice(), 2, paramValue, size);
        fpHeight = FingerprintUtils.byteArrayToInt(paramValue);

        imgbuf = new byte[fpWidth * fpHeight];
    }

    private Response startCapture() {
        mbStop = false;
        CompletableFuture<String> future = new CompletableFuture<>();
        workThread = new WorkThread(future);
        workThread.start();

        return future.handle((base64Image, ex) -> {
            if (ex != null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Erro ao capturar imagem: " + ex.getMessage())
                        .build();
            }

            if (base64Image != null && !base64Image.isEmpty()) {
                return Response.ok(base64Image).build(); // Retorna a imagem base64
            } else {
                return Response.ok("Conectado com sucesso").build(); // Retorna mensagem de sucesso
            }
        }).join();

    }

    public Response enrollFingerprint() {
        FingerprintServiceSingleton service = FingerprintServiceSingleton.getInstance();
        if (service.getDevice() == 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Please turn on the device")
                    .build();
        }

        if (!service.isRegister()) {
            service.setEnrollIdx(0); // Reseta o índice de registro
            service.setRegister(true);
            return Response.ok("Please place your finger three times").build();
        }

        return Response.ok("Registration successful").build();
    }

    public Response verifyFingerprint() {
        FingerprintServiceSingleton service = FingerprintServiceSingleton.getInstance();
        if (service.getDevice() == 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Please turn on the device")
                    .build();
        }

        resetState();
        return Response.ok("Verification successful").build();
    }

    public Response identifyFingerprint() {
        FingerprintServiceSingleton service = FingerprintServiceSingleton.getInstance();
        if (service.getDevice() == 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Please turn on the device")
                    .build();
        }

        if (service.isRegister()) {
            service.setEnrollIdx(0);
            service.setRegister(false);

        }

        if (!service.isIdentify()) {
            service.setIdentify(true);
        }

        return Response.ok("Identification successful").build();
    }

    public Response closeDevice() {
        freeSensor();
        return Response.ok("Device closed successfully").build();
    }

    private void freeSensor() {
        FingerprintServiceSingleton service = FingerprintServiceSingleton.getInstance();
        mbStop = true;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (service.getDb() != 0) {
            FingerprintSensorEx.DBFree(service.getDb());
            service.setDb(0);
        }

        if (service.getDevice() != 0) {
            FingerprintSensorEx.CloseDevice(service.getDevice());
            service.setDevice(0);
        }

        service.resetState();
        FingerprintSensorEx.Terminate();

    }

    private void resetState() {
        FingerprintServiceSingleton service = FingerprintServiceSingleton.getInstance();
        if (service.isRegister()) {
            service.setEnrollIdx(0);
            service.setRegister(false);

        }
        if (service.isIdentify()) {
            service.setIdentify(false);
        }
    }

    private class WorkThread extends Thread {
        private final CompletableFuture<String> future;

        public WorkThread(CompletableFuture<String> future) {
            this.future = future;
        }

        @Override
        public void run() {
            FingerprintServiceSingleton service = FingerprintServiceSingleton.getInstance();
            while (!mbStop) {
                templateLen[0] = TEMPLATE_SIZE;
                int ret = FingerprintSensorEx.AcquireFingerprint(service.getDevice(), imgbuf, template, templateLen);

                if (ret == 0) {
                    if (checkFakeStatus()) {
                        continue;
                    }

                    bufferes = captureFingerprint(imgbuf);
                    processTemplate(template, templateLen[0]);
                    future.complete(bufferes);
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean checkFakeStatus() {
            FingerprintServiceSingleton service = FingerprintServiceSingleton.getInstance();
            if (nFakeFunOn == 1) {
                byte[] paramValue = new byte[4];
                int[] size = new int[1];
                size[0] = 4;

                int ret = FingerprintSensorEx.GetParameters(service.getDevice(), 2004, paramValue, size);
                int nFakeStatus = FingerprintUtils.byteArrayToInt(paramValue);

                return ret == 0 && (byte) (nFakeStatus & 31) != 31;
            }
            return false;
        }
    }

    private String captureFingerprint(byte[] imgBuf) {
        try {
            BitmapUtils.writeBitmap(imgBuf, fpWidth, fpHeight, "fingerprint.png");
            BufferedImage image = ImageIO.read(new File("fingerprint.png"));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);

            byte[] imageInBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageInBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void processTemplate(byte[] template, int len) {
        FingerprintServiceSingleton service = FingerprintServiceSingleton.getInstance();
        if (service.isRegister()) {
            processRegistration(template);
        } else {
            processVerificationOrIdentification(template);
        }
        socketServer.broadcastData("newImage", bufferes);
    }

    private void processRegistration(byte[] template) {

        FingerprintServiceSingleton service = FingerprintServiceSingleton.getInstance();
        int[] fid = new int[1];
        int[] score = new int[1];
        int ret = FingerprintSensorEx.DBIdentify(service.getDb(), template, fid, score);

        if (ret == 0) {
            service.setRegister(false);
            service.setEnrollIdx(0);
            return;
        }

        if (service.getEnrollIdx() > 0 && FingerprintSensorEx.DBMatch(service.getDb(),
                regtemparray[service.getEnrollIdx() - 1], template) <= 0) {
            return;
        }

        System.arraycopy(template, 0, regtemparray[service.getEnrollIdx()], 0, TEMPLATE_SIZE);
        service.setEnrollIdx(service.getEnrollIdx() + 1);

        if (service.getEnrollIdx() == ENROLL_COUNT) {
            finalizeRegistration();
        }
    }

    private void finalizeRegistration() {
        FingerprintServiceSingleton service = FingerprintServiceSingleton.getInstance();
        int[] retLen = new int[1];
        retLen[0] = TEMPLATE_SIZE;
        byte[] regTemp = new byte[retLen[0]];

        int ret = FingerprintSensorEx.DBMerge(service.getDb(), regtemparray[0], regtemparray[1], regtemparray[2],
                regTemp, retLen);
        if (ret == 0) {
            ret = FingerprintSensorEx.DBAdd(service.getDb(), iFid, regTemp);
            if (ret == 0) {
                iFid++;
                cbRegTemp = retLen[0];
                System.arraycopy(regTemp, 0, lastRegTemp, 0, cbRegTemp);
            }
        }
        service.setRegister(false);
    }

    private void processVerificationOrIdentification(byte[] template) {
        FingerprintServiceSingleton service = FingerprintServiceSingleton.getInstance();
        if (service.isIdentify()) {
            int[] fid = new int[1];
            int[] score = new int[1];
            FingerprintSensorEx.DBIdentify(service.getDb(), template, fid, score);
        } else if (cbRegTemp > 0) {
            FingerprintSensorEx.DBMatch(service.getDb(), lastRegTemp, template);
        }
    }
}
