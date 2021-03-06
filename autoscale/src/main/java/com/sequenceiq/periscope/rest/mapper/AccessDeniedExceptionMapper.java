package com.sequenceiq.periscope.rest.mapper;

import java.nio.file.AccessDeniedException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class AccessDeniedExceptionMapper extends BaseExceptionMapper<AccessDeniedException> {

    @Override
    Response.Status getResponseStatus() {
        return Response.Status.FORBIDDEN;
    }
}
