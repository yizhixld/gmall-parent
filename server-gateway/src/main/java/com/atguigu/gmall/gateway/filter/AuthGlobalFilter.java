package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.util.List;

/**
 * @author yizhixld
 * @create 2020-04-29-23:03
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {
    @Autowired
    private RedisTemplate redisTemplate;

    // 匹配路径的工具类
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Value("${authUrls.url}")
    private String authUrls;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        // 获得url
        String path = request.getURI().getPath();
        // 如果是内部接口，则网关拦截不允许外部访问
        if (antPathMatcher.match("/**/inner/**", path)) {
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }
        // 获得用户Id
        String userId = getUserId(request);
        // 获得临时用户id
        String userTempId = getUserTempId(request);
        // 登录认证
        if (antPathMatcher.match("/api/**/auth/**", path)) {
            if (StringUtils.isEmpty(userId)) {
                ServerHttpResponse response = exchange.getResponse();
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }
        // 验证url，未登录状态下不允许访问
        for (String authUrl : authUrls.split(",")) {
            // 当前的url包含登录的控制器域名，但是用户Id 为空！
            if (path.indexOf(authUrl) != -1 && StringUtils.isEmpty(userId)) {
                ServerHttpResponse response = exchange.getResponse();
                //303状态码表示由于请求对应的资源存在着另一个URI，应使用重定向获取请求的资源
                response.setStatusCode(HttpStatus.SEE_OTHER);
                // 重定向到登录页面
                response.getHeaders().set(HttpHeaders.LOCATION, "http://www.gmall.com/login.html?originUrl=" + request.getURI());
                return response.setComplete();
            }
        }
        // 将userId或userTemId传递到后端
        if (!StringUtils.isEmpty(userId) || !StringUtils.isEmpty(userTempId)) {
            if(!StringUtils.isEmpty(userId)){
                request.mutate().header("userId", userId).build();
            }if(!StringUtils.isEmpty(userTempId)){
                request.mutate().header("userTempId", userTempId).build();
            }
            return chain.filter(exchange.mutate().request(request).build());
        }
        return chain.filter(exchange);

    }
    /**
    * @Description: //访问没有访问权限的接口/需要登录的接口（未登录），返回状态信息
    * @Return:
    **/
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        Result<Object> result = Result.build(null, resultCodeEnum);
        byte[] bytes = JSONObject.toJSONString(result).getBytes();
        DataBuffer wrap = response.bufferFactory().wrap(bytes);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(wrap));
    }

    /**
     * @Description: //获取用户Id
     * @Return:
     **/
    private String getUserId(ServerHttpRequest request) {
        String token = "";
        // 从header中获取token数据
        List<String> tokenList = request.getHeaders().get("token");
        if (null != tokenList) {
            token = tokenList.get(0);
        }
        // 从cookie中获取token
        else {
            MultiValueMap<String, HttpCookie> cookieMultiValueMap = request.getCookies();
            HttpCookie cookie = cookieMultiValueMap.getFirst("token");
            if (null != cookie) {
                token = URLDecoder.decode(cookie.getValue());
            }
        }
        // 根据token去缓存中获取用户id
        if(!StringUtils.isEmpty(token)){
            String useId = (String) redisTemplate.opsForValue().get("user:login:" + token);
            return useId;
        }
        return "";
    }

    /**
    * @Description: //获取临时用户id
    * @Return:
    **/
    public String getUserTempId(ServerHttpRequest request){
        String userTempId = "";
        List<String> userTempIdList = request.getHeaders().get("userTempId");
        if(!CollectionUtils.isEmpty(userTempIdList)){
            userTempId = userTempIdList.get(0);
        }else{
            MultiValueMap<String, HttpCookie> requestCookies = request.getCookies();
            HttpCookie cookie = requestCookies.getFirst("userTempId");
            if(null != cookie){
                userTempId = URLDecoder.decode(cookie.getValue());
            }
        }
        return userTempId;
    }
}
