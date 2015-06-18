package com.payu.ratel.register;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;

import org.springframework.remoting.caucho.HessianServiceExporter;

import com.payu.ratel.context.filter.TracingFilter;


/**
 * This implementation of {@link org.springframework.remoting.caucho.HessianExporter} just adds an invocation of
 * {@link TracingFilter} around default request handler. This trick is used to
 * use the filter in older servlet containers without need to statically
 * configure it in web.xml
 */
public class RatelHessianServiceExporter extends HessianServiceExporter {

    private static TracingFilter tracingFilter = new TracingFilter();

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        // This ugly trick makes it backward-compatibile with older servlet-api
        // versions,
        // while still enabling one to use the filter standalone]
        if (request.getMethod().equals(HttpMethod.HEAD)) {
            // if service defines the custom healthcheck - use it

            defaultHealthcheckHandler(response);
        } else {
            tracingFilter.doFilter(request, response, new FilterChainToHessianExporter());
        }
    }

    private void defaultHealthcheckHandler(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private final class FilterChainToHessianExporter implements FilterChain {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            RatelHessianServiceExporter.super.handleRequest((HttpServletRequest) request, (HttpServletResponse) response);
        }
    }
}
