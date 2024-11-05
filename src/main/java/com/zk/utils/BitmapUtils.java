package com.zk.utils;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {
    public static void writeBitmap(byte[] imageBuf, int nWidth, int nHeight, String path) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path);
                DataOutputStream dos = new DataOutputStream(fos)) {

            int w = (((nWidth + 3) / 4) * 4);
            writeBitmapFileHeader(dos, w, nHeight);
            writeBitmapInfoHeader(dos, nWidth, nHeight, w);
            writeColorTable(dos);
            writeImageData(dos, imageBuf, nWidth, nHeight, w);
        }
    }

    private static void writeBitmapFileHeader(DataOutputStream dos, int w, int nHeight) throws IOException {
        int bfType = 0x424d;
        int bfSize = 54 + 1024 + w * nHeight;
        int bfReserved1 = 0;
        int bfReserved2 = 0;
        int bfOffBits = 54 + 1024;

        dos.writeShort(bfType);
        dos.write(FingerprintUtils.intToByteArray(bfSize), 0, 4);
        dos.write(FingerprintUtils.intToByteArray(bfReserved1), 0, 2);
        dos.write(FingerprintUtils.intToByteArray(bfReserved2), 0, 2);
        dos.write(FingerprintUtils.intToByteArray(bfOffBits), 0, 4);
    }

    private static void writeBitmapInfoHeader(DataOutputStream dos, int nWidth, int nHeight, int w) throws IOException {
        int biSize = 40;
        int biPlanes = 1;
        int biBitcount = 8;
        int biCompression = 0;
        int biSizeImage = w * nHeight;
        int biXPelsPerMeter = 0;
        int biYPelsPerMeter = 0;
        int biClrUsed = 0;
        int biClrImportant = 0;

        dos.write(FingerprintUtils.intToByteArray(biSize), 0, 4);
        dos.write(FingerprintUtils.intToByteArray(nWidth), 0, 4);
        dos.write(FingerprintUtils.intToByteArray(nHeight), 0, 4);
        dos.write(FingerprintUtils.intToByteArray(biPlanes), 0, 2);
        dos.write(FingerprintUtils.intToByteArray(biBitcount), 0, 2);
        dos.write(FingerprintUtils.intToByteArray(biCompression), 0, 4);
        dos.write(FingerprintUtils.intToByteArray(biSizeImage), 0, 4);
        dos.write(FingerprintUtils.intToByteArray(biXPelsPerMeter), 0, 4);
        dos.write(FingerprintUtils.intToByteArray(biYPelsPerMeter), 0, 4);
        dos.write(FingerprintUtils.intToByteArray(biClrUsed), 0, 4);
        dos.write(FingerprintUtils.intToByteArray(biClrImportant), 0, 4);
    }

    private static void writeColorTable(DataOutputStream dos) throws IOException {
        for (int i = 0; i < 256; i++) {
            dos.writeByte(i);
            dos.writeByte(i);
            dos.writeByte(i);
            dos.writeByte(0);
        }
    }

    private static void writeImageData(DataOutputStream dos, byte[] imageBuf, int nWidth, int nHeight, int w)
            throws IOException {
        byte[] filter = null;
        if (w > nWidth) {
            filter = new byte[w - nWidth];
        }

        for (int i = 0; i < nHeight; i++) {
            dos.write(imageBuf, (nHeight - 1 - i) * nWidth, nWidth);
            if (w > nWidth)
                dos.write(filter, 0, w - nWidth);
        }
    }
}