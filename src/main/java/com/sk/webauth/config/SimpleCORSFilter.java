package com.sk.webauth.config;

import com.sk.webauth.service.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.auth.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SimpleCORSFilter extends OncePerRequestFilter {

    private final Logger log = LoggerFactory.getLogger(SimpleCORSFilter.class);
    @Value("${allow.origin.headers}")
    private String allowedOrigin;

    @Autowired
    private AuthenticationService authenticationService;

    public SimpleCORSFilter() {
        log.info("SimpleCORSFilter init");
    }

    private static void addResponseHeaders(HttpServletResponse res, String incomingRequestOrigin) {
        res.setHeader("Access-Control-Allow-Origin", incomingRequestOrigin);
        res.setHeader("Access-Control-Allow-Credentials", "true");
        res.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        res.setHeader("Access-Control-Max-Age", "3600");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me, authorization");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {

        String incomingRequestOrigin = req.getHeader("Origin");
        if (!StringUtils.hasLength(incomingRequestOrigin)) {
            ((HttpServletResponse) res).setStatus(401);
            log.error("Request received has no origin");
            res.getOutputStream().write(("Request received has no origin").getBytes());
            return;
        }
        log.info("received request in filter from " + incomingRequestOrigin + " at " + req.getRequestURI());
        if (!incomingRequestOrigin.equals(allowedOrigin)) {
            ((HttpServletResponse) res).setStatus(401);
            log.error("Request received from " + incomingRequestOrigin + " is forbidden");
            res.getOutputStream().write(("Requests from " + incomingRequestOrigin + " are not allowed").getBytes());
            return;
        }

        if ("OPTIONS".equals(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
            log.info("OPTIONS received from " + incomingRequestOrigin);
            addResponseHeaders(res, incomingRequestOrigin);
            return;
        }

        if (!StringUtils.hasLength(req.getHeader("authorization"))) {
            ((HttpServletResponse) res).setStatus(401);
            log.error("Request received from " + incomingRequestOrigin + " have no authorization token");
            res.getOutputStream().write(("Request received from " + incomingRequestOrigin + " have no authorization token").getBytes());
            return;
        }


        try {
            authenticationService.verifyToken(req.getHeader("authorization").replace("Bearer ", ""), req.getRequestURI());
        } catch (AuthenticationException e) {
            ((HttpServletResponse) res).setStatus(401);
            log.error("Token verification failed for request from " + incomingRequestOrigin + " to access " + req.getRequestURI());
            res.getOutputStream().write(("JWT token verification failed").getBytes());
            return;
        }

        addResponseHeaders(res, incomingRequestOrigin);
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
    }

}
