package com.sk.webauth.config;

import com.sk.webauth.service.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.apache.http.auth.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static java.util.Collections.enumeration;

@Component
public class SimpleCORSFilter extends OncePerRequestFilter {

    public static final String OWNER_EMAIL = "owner-email";
    public static final String IS_ADMIN = "Is-Admin";
    private final Logger log = LoggerFactory.getLogger(SimpleCORSFilter.class);
//    @Value("${allow.origin.headers}")
//    private String allowedOrigin;

    @Value("${web.auth.super.admins}")
    private String admin;

    @Autowired
    private AuthenticationService authenticationService;

    public SimpleCORSFilter() {
        log.info("SimpleCORSFilter init");
    }

    public boolean checkIfAdmin(String email) {
        List<String> admins = Arrays.asList(admin.split(","));
        return admins.contains(email);
    }

    private static void addResponseHeaders(HttpServletResponse res, String incomingRequestOrigin, String requestId) {
        res.setHeader("Access-Control-Allow-Origin", incomingRequestOrigin);
        res.setHeader("Access-Control-Allow-Credentials", "true");
        res.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        res.setHeader("Access-Control-Max-Age", "3600");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me, authorization, requestId");
        if (requestId != null) res.setHeader("requestId", requestId);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, @NonNull HttpServletResponse res, @NonNull FilterChain chain) throws ServletException, IOException {

        String incomingRequestOrigin = req.getHeader("Origin");

//        if (!StringUtils.hasLength(incomingRequestOrigin)) {
//            ((HttpServletResponse) res).setStatus(401);
//            log.error("Request received has no origin");
//            res.getOutputStream().write(("Request received has no origin").getBytes());
//            return;
//        }
//        log.info("received request in filter from " + incomingRequestOrigin + " at " + req.getRequestURI());
//        if (!incomingRequestOrigin.equals(allowedOrigin)) {
//            ((HttpServletResponse) res).setStatus(401);
//            log.error("Request received from " + incomingRequestOrigin + " is forbidden");
//            res.getOutputStream().write(("Requests from " + incomingRequestOrigin + " are not allowed").getBytes());
//            return;
//        }

        if ("OPTIONS".equals(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
            log.info("OPTIONS received from " + incomingRequestOrigin);
            addResponseHeaders(res, incomingRequestOrigin, null);
            return;
        }

        if (!StringUtils.hasLength(req.getHeader("authorization"))) {
            res.setStatus(401);
            log.error("Request received from " + incomingRequestOrigin + " have no authorization token");
            res.getOutputStream().write(("Request received from " + incomingRequestOrigin + " have no authorization token").getBytes());
            return;
        }

        String requestId = req.getHeader("Origin");
        HttpServletRequestWrapper wrapper;
        try {
            String owner = authenticationService.verifyToken(req.getHeader("authorization").replace("Bearer ", ""), req.getRequestURI(), requestId);
            wrapper = addOwnerRequest(req, owner);
        } catch (AuthenticationException e) {
            res.setStatus(401);
            log.error("Token verification failed for request from " + incomingRequestOrigin + " to access " + req.getRequestURI());
            res.getOutputStream().write(("JWT token verification failed").getBytes());
            return;
        }

        addResponseHeaders(res, incomingRequestOrigin, requestId);

        chain.doFilter(wrapper, res);
    }

    private HttpServletRequestWrapper addOwnerRequest(HttpServletRequest request, String owner) {
        boolean isAdmin = checkIfAdmin(owner);
        return new HttpServletRequestWrapper(request) {
            @Override
            public Enumeration<String> getHeaders(String name) {
                if (OWNER_EMAIL.equals(name)) {
                    return enumeration(Collections.singleton(owner));
                } else if (IS_ADMIN.equals(name)) {
                    return enumeration(Collections.singleton(Boolean.toString(isAdmin)));
                } else {
                    return super.getHeaders(name);
                }
            }
        };
    }
    @Override
    public void destroy() {
    }

}
