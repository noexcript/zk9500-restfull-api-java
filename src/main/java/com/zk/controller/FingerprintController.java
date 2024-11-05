package com.zk.controller;

import java.util.Map;

import javax.ws.rs.Consumes;
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

    @POST
    @Path("/open")
    @Produces(MediaType.APPLICATION_JSON)
    public Response openDevice() {
        return fingerprintService.openDevice();
    }

    @POST
    @Path("/enroll")
    @Produces(MediaType.APPLICATION_JSON)
    public Response enrollFingerprint() {
        return fingerprintService.enrollFingerprint();
    }

    @POST
    @Path("/verify")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifyFingerprint(Map<String, Object> request) {
        return fingerprintService.verifyFingerprint();
    }

    @POST
    @Path("/identify")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response identifyFingerprint(Map<String, Object> request) {
        return fingerprintService.identifyFingerprint();
    }

    @POST
    @Path("/close")
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeDevice() {
        return fingerprintService.closeDevice();
    }
}
