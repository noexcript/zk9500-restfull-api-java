package com.zk.utils;
import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class CORSFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");  
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization");
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
    }
}