package com.zk.controller;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.zk.service.FingerprintService;

@Path("fingerprint")
public class FingerprintController {
    private final FingerprintService fingerprintService;

    public FingerprintController() {
        this.fingerprintService = new FingerprintService();
    }

    @GET
    @Path("/open")
    @Produces(MediaType.APPLICATION_JSON)
    public Response openDevice() {
        return fingerprintService.openDevice();
    }

    // CALL TO REGISTER FINGERPRINT ON TEMP
    @GET
    @Path("/enroll")
    @Produces(MediaType.APPLICATION_JSON)
    public Response enrollFingerprint() {
        return fingerprintService.enrollFingerprint();
    }

    // CALL TO VERIFICAR FINGERPRINT ALL PRINT OF SCREEN
    @GET
    @Path("/verify")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verify() {
        return fingerprintService.verifyFingerprint();
    }


    @POST
    @Path("/verify-fingerprint")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifyFingerprint() {
        return fingerprintService.identificarImage();
    }

    @POST
    @Path("/identify")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response identifyFingerprint() {
        return fingerprintService.identifyFingerprint();
    }

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerFingerprint() {
        return fingerprintService.registarFingerprint();
    }

    @POST
    @Path("/close")
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeDevice() {
        return fingerprintService.closeDevice();
    }
}
