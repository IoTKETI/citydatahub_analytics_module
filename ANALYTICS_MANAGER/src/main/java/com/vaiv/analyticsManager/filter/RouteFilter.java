package com.vaiv.analyticsManager.filter;

import javax.servlet.http.HttpServletRequest;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class RouteFilter extends ZuulFilter{

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {

        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        
        String uri=request.getRequestURI();
        log.info("URI : " + uri);

        log.info("Route Filter's ctx string before modify: " + ctx.toString());
        // String routeUri = "";

        // if(ctx.get(FilterConstants.PROXY_KEY).toString() =="hue_filebrowser"){
        //     routeUri = uri.replaceAll("%2F", "/");
        //     ctx.set(FilterConstants.REQUEST_URI_KEY, routeUri);
        // }
        
        // ctx.set("x-forwarded-prefix","");
        // ctx.set("requestURI","");

        // try {
        //     ctx.setRouteHost(new URL("https://bamdule.tistory.com/59"));
        // } catch (MalformedURLException e) {
        //     e.printStackTrace();
        // }
        
        log.info("Route Filter's ctx string After modify: " + ctx.toString());
        
        return null;
    }

    @Override
    public String filterType() {
        return FilterConstants.ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
    }
    
}
