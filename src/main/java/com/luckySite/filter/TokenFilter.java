package com.luckySite.filter;

import com.luckySite.utils.RedisUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;

/**
 * @author mahongbin
 * @date 2019/6/18 18:15
 * @Description
 */
@Component
public class TokenFilter  extends ZuulFilter {

    private static final String TOKEN = "token";

    private static final String LOGIN_URL = "/login";

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public String filterType() {
        return null;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String token = getToken(request);
        if (StringUtils.isEmpty(token)) {
            return false;
        } else {
            return true;
        }

    }

    /**
     * if shouldFilter() is true, this method will be invoked. this method is the core method of a ZuulFilter
     * HTTP 错误 401.1 - 未经授权：访问由于凭据无效被拒绝。
     *
     * @return Some arbitrary artifact may be returned. Current implementation ignores it.
     * @throws ZuulException if an error occurs during execution.
     */

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String token = getToken(request);

        String url = request.getRequestURI();

        if(null == token){
            return false;
        }else if(-1 != url.indexOf(LOGIN_URL)){
            return true;
        }

        Object tokenObj = redisUtil.get(token);

        if (null != tokenObj) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取前端参数的token
     * @param request
     * @return
     */
    private String getToken(HttpServletRequest request){
        String token = request.getParameter(TOKEN);

        if(null == token){
            StringBuffer jb = new StringBuffer();
            String line = null;
            try {
                BufferedReader reader = request.getReader();
                while ((line = reader.readLine()) != null) {
                    jb.append(line);
                }

                if(jb.toString().isEmpty()){
                    return null;
                }
                token = JSONObject.fromObject(jb.toString()).getString(TOKEN);
            } catch (Exception e) {
                token = null;
            }
        }
        return token;
    }
}
