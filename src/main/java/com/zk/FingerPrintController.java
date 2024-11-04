package com.zk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.zkteco.biometric.FingerprintSensorErrorCode;
import com.zkteco.biometric.FingerprintSensorEx;

@Path("fingerprint")
public class FingerPrintController {

    private long mhDevice = 0;
    private long mhDB = 0;
    private boolean mbStop = true;

    int fpWidth = 0;
    int fpHeight = 0;
    byte[] bufferes = null;

    private byte[] lastRegTemp = new byte[2048];
    private int cbRegTemp = 0;
    private byte[][] regtemparray = new byte[3][2048];

    private boolean bRegister = false;

    private boolean bIdentify = true;
    // finger id

    private int iFid = 1;

    private int nFakeFunOn = 1;

    static final int enroll_cnt = 3;

    private int enroll_idx = 0;

    private byte[] imgbuf = null;
    private byte[] template = new byte[2048];
    private int[] templateLen = new int[1];

    private WorkThread workThread = null;

    @POST
    @Path("/open")
    @Produces(MediaType.APPLICATION_JSON)
    public Response openDevice() {
        if (mhDevice != 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Dispositivo já está abero")
                    .build();

        }

        int ret = FingerprintSensorEx.Init();

        if (ret != FingerprintSensorErrorCode.ZKFP_ERR_OK) {
            this.FreeSensor();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Falha ao inicializar")
                    .build();
        }
        mhDevice = FingerprintSensorEx.OpenDevice(0);

        if (0 == mhDevice) {
            this.FreeSensor();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Falha ao abrir dispositivo" + ret + "!")
                    .build();
        }

        int deviceCount = FingerprintSensorEx.GetDeviceCount();

        if (deviceCount < 1) {
            FingerprintSensorEx.Terminate();

            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Nenhum dispositivo encontrado")
                    .build();
        }

        mhDB = FingerprintSensorEx.DBInit();

        if (mhDB == 0) {
            FingerprintSensorEx.CloseDevice(mhDevice);
            mhDevice = 0;

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Falha ao inicilizar DB")
                    .build();
        }
        int nFmt = 0; // Ansi

        FingerprintSensorEx.DBSetParameter(mhDB, 5010, nFmt);
        byte[] paramValue = new byte[4];
        int[] size = new int[1];
        size[0] = 4;
        FingerprintSensorEx.GetParameters(mhDevice, 1, paramValue, size);
        fpWidth = byteArrayToInt(paramValue);
        size[0] = 4;
        FingerprintSensorEx.GetParameters(mhDevice, 2, paramValue, size);
        fpHeight = byteArrayToInt(paramValue);
        imgbuf = new byte[fpWidth * fpHeight];
        mbStop = false;
        workThread = new WorkThread();
        workThread.start();

        return Response.ok("Dispositivo aberto com sucesso").build();

    }

    @POST
    @Path("/enroll")
    @Produces(MediaType.APPLICATION_JSON)
    public Response enrollFingerPrint() {

        if (0 == mhDevice) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Ligar o dispositivo").build();
        }
        if (!bRegister) {

            enroll_idx = 0;
            bRegister = true;

            return Response.ok("Por favor o teu dedo três vezes").build();

        }
        return Response.ok("Cadastro com sucesso").build();

    }

    @POST
    @Path("/verify")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifyFingerprint(Map<String, Object> request) {

        if (0 == mhDevice) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Ligar o dispositivo").build();
        }
        if (bRegister) {
            enroll_idx = 0;
            bRegister = false;
        }
        if (bIdentify) {
            bIdentify = false;
        }
        return Response.ok("Verificado com sucesso").build();
    }

    @POST
    @Path("/identify")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response identifyFingerprint(Map<String, Object> request) {
        if (0 == mhDevice) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Ligar o dispositivo").build();
        }
        if (bRegister) {
            enroll_idx = 0;
            bRegister = false;
        }
        if (!bIdentify) {
            bIdentify = true;
        }
        return Response.ok("Identificado com sucesso").build();
    }

    @POST
    @Path("/close")
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeDevice() {
        FreeSensor();
        return Response.ok("Device closed successfully").build();
    }

    private void FreeSensor() {
        mbStop = true;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (0 != mhDB) {
            FingerprintSensorEx.DBFree(mhDB);
            mhDB = 0;
        }
        if (0 != mhDevice) {
            FingerprintSensorEx.CloseDevice(mhDevice);
            mhDevice = 0;
        }
        FingerprintSensorEx.Terminate();
    }

    private class WorkThread extends Thread {

        @Override
        public void run() {
            super.run();
            int ret = 0;
            while (!mbStop) {
                templateLen[0] = 2048;
                if (0 == (ret = FingerprintSensorEx.AcquireFingerprint(mhDevice, imgbuf, template, templateLen))) {
                    if (nFakeFunOn == 1) {
                        byte[] paramValue = new byte[4];
                        int[] size = new int[1];
                        size[0] = 4;
                        int nFakeStatus = 0;
                        // GetFakeStatus
                        ret = FingerprintSensorEx.GetParameters(mhDevice, 2004, paramValue, size);
                        nFakeStatus = byteArrayToInt(paramValue);
                        System.out.println("ret = " + ret + ",nFakeStatus=" + nFakeStatus);
                        if (0 == ret && (byte) (nFakeStatus & 31) != 31) {
                            return;
                        }
                    }

                    OnCatpureOK(imgbuf);
                    OnExtractOK(template, templateLen[0]);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }

            }
        }

        private void runOnUiThread(Runnable runnable) {
            // TODO Auto-generated method stub

        }
    }

    private void OnCatpureOK(byte[] imgBuf) {
        try {
            writeBitmap(imgBuf, fpWidth, fpHeight, "fingerprint.png");

            BufferedImage image = ImageIO.read(new File("fingerprint.png"));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);

            byte[] imageInBytes = baos.toByteArray();

            // Crie um JSON que contém a imagem em Base64
            String base64Image = Base64.getEncoder().encodeToString(imageInBytes);
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("image", base64Image);

            System.out.println(responseMap);

            // return Response.ok(responseMap).build(); // Retorna o JSON com a imagem em
            // Base64
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public static byte[] changeByte(int data) {
        return intToByteArray(data);
    }

    public static void writeBitmap(byte[] imageBuf, int nWidth, int nHeight,
            String path) throws IOException {
        java.io.FileOutputStream fos = new java.io.FileOutputStream(path);
        java.io.DataOutputStream dos = new java.io.DataOutputStream(fos);

        int w = (((nWidth + 3) / 4) * 4);
        int bfType = 0x424d; // 位图文件类型（0—1字节）
        int bfSize = 54 + 1024 + w * nHeight;// bmp文件的大小（2—5字节）
        int bfReserved1 = 0;// 位图文件保留字，必须为0（6-7字节）
        int bfReserved2 = 0;// 位图文件保留字，必须为0（8-9字节）
        int bfOffBits = 54 + 1024;// 文件头开始到位图实际数据之间的字节的偏移量（10-13字节）

        dos.writeShort(bfType); // 输入位图文件类型'BM'
        dos.write(changeByte(bfSize), 0, 4); // 输入位图文件大小
        dos.write(changeByte(bfReserved1), 0, 2);// 输入位图文件保留字
        dos.write(changeByte(bfReserved2), 0, 2);// 输入位图文件保留字
        dos.write(changeByte(bfOffBits), 0, 4);// 输入位图文件偏移量

        int biSize = 40;// 信息头所需的字节数（14-17字节）
        int biWidth = nWidth;// 位图的宽（18-21字节）
        int biHeight = nHeight;// 位图的高（22-25字节）
        int biPlanes = 1; // 目标设备的级别，必须是1（26-27字节）
        int biBitcount = 8;// 每个像素所需的位数（28-29字节），必须是1位（双色）、4位（16色）、8位（256色）或者24位（真彩色）之一。
        int biCompression = 0;// 位图压缩类型，必须是0（不压缩）（30-33字节）、1（BI_RLEB压缩类型）或2（BI_RLE4压缩类型）之一。
        int biSizeImage = w * nHeight;// 实际位图图像的大小，即整个实际绘制的图像大小（34-37字节）
        int biXPelsPerMeter = 0;// 位图水平分辨率，每米像素数（38-41字节）这个数是系统默认值
        int biYPelsPerMeter = 0;// 位图垂直分辨率，每米像素数（42-45字节）这个数是系统默认值
        int biClrUsed = 0;// 位图实际使用的颜色表中的颜色数（46-49字节），如果为0的话，说明全部使用了
        int biClrImportant = 0;// 位图显示过程中重要的颜色数(50-53字节)，如果为0的话，说明全部重要

        dos.write(changeByte(biSize), 0, 4);// 输入信息头数据的总字节数
        dos.write(changeByte(biWidth), 0, 4);// 输入位图的宽
        dos.write(changeByte(biHeight), 0, 4);// 输入位图的高
        dos.write(changeByte(biPlanes), 0, 2);// 输入位图的目标设备级别
        dos.write(changeByte(biBitcount), 0, 2);// 输入每个像素占据的字节数
        dos.write(changeByte(biCompression), 0, 4);// 输入位图的压缩类型
        dos.write(changeByte(biSizeImage), 0, 4);// 输入位图的实际大小
        dos.write(changeByte(biXPelsPerMeter), 0, 4);// 输入位图的水平分辨率
        dos.write(changeByte(biYPelsPerMeter), 0, 4);// 输入位图的垂直分辨率
        dos.write(changeByte(biClrUsed), 0, 4);// 输入位图使用的总颜色数
        dos.write(changeByte(biClrImportant), 0, 4);// 输入位图使用过程中重要的颜色数

        for (int i = 0; i < 256; i++) {
            dos.writeByte(i);
            dos.writeByte(i);
            dos.writeByte(i);
            dos.writeByte(0);
        }

        byte[] filter = null;
        if (w > nWidth) {
            filter = new byte[w - nWidth];
        }

        for (int i = 0; i < nHeight; i++) {
            dos.write(imageBuf, (nHeight - 1 - i) * nWidth, nWidth);
            if (w > nWidth)
                dos.write(filter, 0, w - nWidth);
        }
        dos.flush();
        dos.close();
        fos.close();
    }

    public static byte[] intToByteArray(final int number) {
        byte[] abyte = new byte[4];
        // "&" 与（AND），对两个整型操作数中对应位执行布尔代数，两个位都为1时输出1，否则0。
        abyte[0] = (byte) (0xff & number);
        // ">>"右移位，若为正数则高位补0，若为负数则高位补1
        abyte[1] = (byte) ((0xff00 & number) >> 8);
        abyte[2] = (byte) ((0xff0000 & number) >> 16);
        abyte[3] = (byte) ((0xff000000 & number) >> 24);

        return abyte;
    }

    public static int byteArrayToInt(byte[] bytes) {
        int number = bytes[0] & 0xFF;
        number |= ((bytes[1] << 8) & 0xFF00);
        number |= ((bytes[2] << 16) & 0xFF0000);
        number |= ((bytes[3] << 24) & 0xFF000000);
        return number;
    }

    private void OnExtractOK(byte[] template, int len) {
        if (bRegister) {
            int[] fid = new int[1];
            int[] score = new int[1];
            int ret = FingerprintSensorEx.DBIdentify(mhDB, template, fid, score);
            if (ret == 0) {
                // textArea.setText("the finger already enroll by " + fid[0] + ",cancel
                // enroll");
                bRegister = false;
                enroll_idx = 0;
                return;
            }
            if (enroll_idx > 0 && FingerprintSensorEx.DBMatch(mhDB, regtemparray[enroll_idx - 1], template) <= 0) {
                // textArea.setText("please press the same finger 3 times for the enrollment");
                return;
            }
            System.arraycopy(template, 0, regtemparray[enroll_idx], 0, 2048);
            enroll_idx++;
            if (enroll_idx == 3) {
                int[] _retLen = new int[1];
                _retLen[0] = 2048;
                byte[] regTemp = new byte[_retLen[0]];

                if (0 == (ret = FingerprintSensorEx.DBMerge(mhDB, regtemparray[0], regtemparray[1], regtemparray[2],
                        regTemp, _retLen)) &&
                        0 == (ret = FingerprintSensorEx.DBAdd(mhDB, iFid, regTemp))) {
                    iFid++;
                    cbRegTemp = _retLen[0];
                    System.arraycopy(regTemp, 0, lastRegTemp, 0, cbRegTemp);
                    // Base64 Template
                    // textArea.setText("enroll succ");
                } else {
                    // textArea.setText("enroll fail, error code=" + ret);
                }
                bRegister = false;
            } else {
                // textArea.setText("You need to press the " + (3 - enroll_idx) + " times
                // fingerprint");
            }
        } else {
            if (bIdentify) {
                int[] fid = new int[1];
                int[] score = new int[1];
                int ret = FingerprintSensorEx.DBIdentify(mhDB, template, fid, score);
                if (ret == 0) {
                    // textArea.setText("Identify succ, fid=" + fid[0] + ",score=" + score[0]);
                } else {
                    // textArea.setText("Identify fail, errcode=" + ret);
                }

            } else {
                if (cbRegTemp <= 0) {
                    // textArea.setText("Please register first!");
                } else {
                    int ret = FingerprintSensorEx.DBMatch(mhDB, lastRegTemp, template);
                    if (ret > 0) {
                        // textArea.setText("Verify succ, score=" + ret);
                    } else {
                        // textArea.setText("Verify fail, ret=" + ret);
                    }
                }
            }
        }
    }

}
