// File: src/main/java/com/pahanaedu/pahanasuite/security/AuthFilter.java
package com.pahanaedu.pahanasuite.security;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * AuthFilter protects secured pages by checking user login session.
 * Session timeout is extended for cashier use (8 hours).
 * Public resources (login page, static files) are accessible without login.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    // Session timeout in minutes (8 hours)
    private static final int SESSION_TIMEOUT_MINUTES = 8 * 60;

    // Public URLs that do not require login
    private static final List<String> PUBLIC_URLS = Arrays.asList(
            "/login",
            "/login.jsp",
            "/css/",
            "/js/",
            "/images/",
            "/favicon.ico"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // Allow public URLs without login
        if (isPublicUrl(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedIn = session != null &&
                Boolean.TRUE.equals(session.getAttribute("isLoggedIn"));

        if (isLoggedIn) {
            // Check if session expired by last activity time
            if (isSessionExpired(session)) {
                session.invalidate();
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
                return;
            }

            // Update last activity timestamp
            session.setAttribute("lastActivity", System.currentTimeMillis());

            chain.doFilter(request, response);
        } else {
            // Not logged in - redirect to login page
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
        }
    }

    private boolean isSessionExpired(HttpSession session) {
        Long lastActivity = (Long) session.getAttribute("lastActivity");
        if (lastActivity == null) {
            return false; // new session or no activity recorded yet
        }

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastActivity;
        long timeoutMillis = SESSION_TIMEOUT_MINUTES * 60 * 1000L;

        return elapsed > timeoutMillis;
    }

    private boolean isPublicUrl(String path) {
        return PUBLIC_URLS.stream().anyMatch(publicUrl ->
                publicUrl.endsWith("/") ? path.startsWith(publicUrl) : path.equals(publicUrl)
        );
    }

    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("AuthFilter initialized with session timeout " + SESSION_TIMEOUT_MINUTES + " minutes.");
    }

    @Override
    public void destroy() {}
}
