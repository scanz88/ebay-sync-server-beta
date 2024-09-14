package com.neutroware.ebaysyncserver.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

public class CachingRequestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Wrap the HttpServletRequest in a ContentCachingRequestWrapper
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
        System.out.println("caching request filter being applied");
        chain.doFilter(wrappedRequest, response);
    }
}
