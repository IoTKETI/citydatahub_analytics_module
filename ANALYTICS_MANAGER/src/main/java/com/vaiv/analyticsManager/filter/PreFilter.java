package com.vaiv.analyticsManager.filter;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.vaiv.analyticsManager.restFullApi.service.SandboxRestService;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.exception.ZuulException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Component
//@ConfigurationProperties(prefix="zuul")
public class PreFilter extends ZuulFilter {

    @Value("${zuul.isNotUseSandbox}")
    private Boolean isNotUseSandbox;
    @Value("${zuul.analyticsModuleServer}")
    private String analyticsModuleServer;
    @Value("${zuul.contextPortMap}")
    private String contextPortString;
    @Value("${zuul.contextSubpathMap}")
    private String contextSubpathString;

    private Map<String, String> contextPortMap;
    private Map<String, String> contextSubpathMap;

    @Autowired
    private SandboxRestService sandboxRestService;

    // edited start 2022.01.20
    // private final Logger log = LoggerFactory.getLogger(this.getClass());
    Logger log = LoggerFactory.getLogger(this.getClass());
    // edited end 2022.01.20

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @PostConstruct
    public void routeMapInit (){

        contextPortMap =new HashMap<String, String>();
        contextSubpathMap =new HashMap<String, String>();

        String[] contextPorts  = contextPortString.split(";");
        for(String contextPort:contextPorts)
        {
            String[] contextAndPort=contextPort.split(":");
            contextPortMap.put(contextAndPort[0],contextAndPort[1]);
        }
        
        String[] contextSubpaths = contextSubpathString.split(";");
        for(String contextSubpath:contextSubpaths)
        {
            String[] contextAndSubpath=contextSubpath.split(":");
            contextSubpathMap.put(contextAndSubpath[0],contextAndSubpath[1]);
        }
    }

    @Override
    public Object run() throws ZuulException {
        
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();

        String instanceId= (String)session.getAttribute("instancePk");
        String userId=(String)session.getAttribute("userId");
        String userRole=(String)session.getAttribute("userRole");
        
        String uri=request.getRequestURI();

        String module = ctx.get(FilterConstants.SERVICE_ID_KEY).toString();
        // String module="";
        //  if (uri.contains("hue")) {
        //     module = "hue";
        // } else if(uri.contains("nifi")){
        //     module = "nifi";
        // } else {
        //     module = "analyticsModule";
        // }

        int requestPort=request.getServerPort();

        Integer instanceIdNum = -1;
        String forwardIp = "";

        if(instanceId !=null && instanceId.trim().length()>0) {
            instanceIdNum=Integer.valueOf(instanceId.trim());
        }else{
            log.info("instancePk가"+instanceId+" Session에 정상적으로 입력되지 않았습니다.");
            return null;
        }

        //1. 전달 받은 정보를  출력
        // log.debug
        log.info("Request Param Received  Information---------------------------");
        log.info("URI : " + uri);

        // 2.2.2. Forward 할 IP를 디비에서 가져옴

        if(isNotUseSandbox !=null && isNotUseSandbox){
            forwardIp=analyticsModuleServer;
        }else if(instanceIdNum != -1 && userId !=null && (userRole.equals("admin") || userRole.equals("Analytics_Admin"))) {
            forwardIp=sandboxRestService.getPrivateIpaddressWithInstanceId(instanceIdNum);
        }else{
            log.info("instanceId:"+instanceIdNum+", userId : "+userId+", userRole : "+userRole+"세션 정보가 비정상적인 값입니다.");
            return null;
        }

        log.info("forwardIp : " + forwardIp);

        // if(forwardIp == null || forwardIp.equals("")){
        //     log.info("instanceId:"+instanceIdNum+", userId : "+userId+", userRole : "+userRole+"데이터베이스에 해당 정보가 인스턴스 정보가 없습니다.");
        //     return null;
        // }

        //2.2.3. Forward 할 URL을 구성
        String forwardUrl=request.getScheme()+"://";
        if(forwardIp.equals("")){
            forwardUrl += "localhost";
        }else {
            forwardUrl += forwardIp;
        }

        Boolean isPortSet=false;

        for(String key : contextPortMap.keySet()){
            if(module.equals(key)){
                forwardUrl+=(":"+contextPortMap.get(key));
                isPortSet=true;
                break;
            }
        }
        if(!isPortSet){
            forwardUrl+=":"+requestPort;
        }

        log.info("forwardUrl : "+forwardUrl);

        //2.2.4. Forward 할 URL을 설정
        try {
            ctx.setRouteHost(new URL(forwardUrl));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    public int ordinalIndexOf(String str, String substr, int n) {
        int pos = -1;
        do {
            pos = str.indexOf(substr, pos + 1);
        } while (n-- > 0 && pos != -1);
        return pos;
    }

}
