package com.zk.utils;

import java.io.IOException;

import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class NotFoundFilter implements ContainerResponseFilter {

    @Override
    public void filter(javax.ws.rs.container.ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) throws IOException {
        if (responseContext.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
            responseContext.setEntity(new ResponseBody("Rota não encontrada", null));
            responseContext.getHeaders().putSingle("Content-Type", MediaType.APPLICATION_JSON);
        }
    }
}