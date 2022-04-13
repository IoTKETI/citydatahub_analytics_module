package com.vaiv.analyticsManager.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

@Component
public class ErrorFilter extends ZuulFilter{

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        log.info("ERROR Filter's ctx string : " + ctx.toString());
        log.debug("debug");
        log.error("error");

        Object e = ctx.get("error.exception");

        if (e != null && e instanceof ZuulException) {
            ZuulException zuulException = (ZuulException)e;
            log.error("Zuul failure detected: " + zuulException.getMessage(), zuulException);
        }


        return null;
    }

    @Override
    public String filterType() {
        return FilterConstants.ERROR_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
    }
    
}
