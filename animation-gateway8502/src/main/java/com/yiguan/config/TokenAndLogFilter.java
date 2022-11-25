package com.yiguan.config;

import cn.hutool.http.HttpStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.filter.TokenFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yiguan.bean.entity.DataResult;
import com.yiguan.common.Constant;
import com.yiguan.util.Oauth2Utils;
import io.netty.buffer.EmptyByteBuf;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: lw
 * @CreateTime: 2022-10-13  20:59
 * @Description: TODO:统一网关认证和授权，全局日志记录
 * @Version: 1.0
 */
@Component
@Order(-1)
public class TokenAndLogFilter implements GlobalFilter{

    private static Logger logger = LoggerFactory.getLogger(TokenFilter.class.getSimpleName());

    @Value("${ignore.url}")
    String ignoreUrl;
    @Value("${server.port}")
    private String port;
    @Autowired
    Oauth2Utils oauth2Utils;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        return cacheRequestBody(exchange, (serverHttpRequest) -> {
//            //判断URL是否需要忽略
//
//            //获取request请求
//            String requestPath = exchange.getRequest().getURI().getPath();
//            if(StringUtils.isNotEmpty(ignoreUrl)&& judgeUrl(ignoreUrl,requestPath)){
//                return chain.filter(exchange);
//            }
//            //获取Authorization请求头
//            String token = exchange.getRequest().getHeaders().getFirst("authorization");
//            //Authorization请求头为空，抛异常
//            if(StringUtils.isEmpty(token)) {
//                return out(exchange.getResponse(),DataResult.createByError(Constant.UNAUTHORIZED,"鉴权失败"));
//            }
//
//            // don't mutate and build if same request object
//            if (serverHttpRequest == exchange.getRequest()) {
//                return chain.filter(exchange);
//            }
//            return chain.filter(exchange.mutate().request(serverHttpRequest).build());
//        });

        //判断URL是否需要忽略

        //获取request请求
        String requestPath = exchange.getRequest().getURI().getPath();
        if(StringUtils.isNotEmpty(ignoreUrl)&& judgeUrl(ignoreUrl,requestPath)){
            return chain.filter(exchange);
        }
        //获取Authorization请求头
        String token = exchange.getRequest().getHeaders().getFirst("authorization");
        String username = exchange.getRequest().getHeaders().getFirst("username");
        //Authorization请求头为空，抛异常
        if(StringUtils.isEmpty(token)||StringUtils.isEmpty(username)) {
            return out(exchange.getResponse(),DataResult.createByError(Constant.UNAUTHORIZED,"鉴权失败"));
        }
        token = token.substring("Bearer ".length());
        //check_token
        Map<String, Object> objectMap = oauth2Utils.checkToken(token, Constant.CHECK_TOKEN_URL);
        //TODO:当前端获取请求头token与本地存储的不一致时，刷新本地token;当收到555的请求响应时，前端调用退出接口同时跳转到登录页
        if(!objectMap.containsKey("active")){
            oauth2Utils.logout(token,username);
            return out(exchange.getResponse(),DataResult.createByError(555,"token无效或过期，请重新登录"));
        }else{
            //查看token过期时间，小于两天就刷新token，token有效期7天，refresh_token有效期一个月
            if(objectMap.containsKey("exp")&&(Long.valueOf(objectMap.get("exp")+"")-System.currentTimeMillis()/1000)<24*60*60*2){
                DataResult dataResult = oauth2Utils.refreshTokenByusername(objectMap.get("user_name") + "");
                if(HttpStatus.HTTP_OK == dataResult.getStatus()){
                    //token刷新成功
                    LinkedHashMap<String,Object> data = (LinkedHashMap<String,Object>)dataResult.getData();
                    if(data.containsKey("access_token")) {
                        exchange.getResponse().getHeaders().set("token", data.get("access_token") + "");
                        //需要设置此属性前端才能从响应头拿到token
                        exchange.getResponse().getHeaders().set("Access-Control-Expose-Headers","token");
                    }
                }else {
                    oauth2Utils.logout(token,objectMap.get("user_name") + "");
                    return out(exchange.getResponse(),DataResult.createByError(555,"token过期并且refresh_token也过期,请重新登录"));
                }
            }
            //token有效,获取角色，查询redis获取可以请求的路径
            ArrayList<String> authorities = (ArrayList<String>) objectMap.get("authorities");
            if(CollectionUtils.isEmpty(authorities)){
                return out(exchange.getResponse(),DataResult.createByError(HttpStatus.HTTP_BAD_REQUEST,"该用户没有角色"));
            }
            String allowUrl = "";
            //从redis里读取角色权限
            String authIdStr = "";
            for (String authority : authorities) {
//                allowUrl += redisTemplate.opsForValue().get(authority) + ",";
                authIdStr += redisTemplate.opsForValue().get(authority) + ",";
            }
            List<String> authIds = Arrays.asList(authIdStr.split(",")).stream().distinct().collect(Collectors.toList());
            for (String authId : authIds) {
                allowUrl += redisTemplate.opsForValue().get("authorization_"+authId) + ",";
            }
            Boolean reduce = judgeUrl(allowUrl,requestPath);
            if(reduce){
                return chain.filter(exchange);
            }else {
                return out(exchange.getResponse(),DataResult.createByError(HttpStatus.HTTP_FORBIDDEN,"权限不足"));
            }
        }
    }


    public Boolean judgeUrl(String urls,String currentUrl){
        List<String> list = Arrays.asList(urls.split(","));

        List<String> newList = list.stream().distinct().filter(f -> {
            if (f.contains("*")) {//该路径下通配URL/*
                String s = f.split("\\*")[0];
                String k = s.substring(0, s.length() - 1);
                //例如通过的url为/getConfig/*,那么/getConfig和/getConfig/sda能通过,/getConfigds不能通过
                return currentUrl.startsWith(s)||currentUrl.equals(k);
            } else {
                return currentUrl.equals(f);
            }
        }).collect(Collectors.toList());
        return newList.size() > 0;
    }

    private Mono<Void> out(ServerHttpResponse response,DataResult dataResult) {
        ObjectMapper objectMapper = new ObjectMapper();
        String writeValueAsString = "";
        try {
            writeValueAsString = objectMapper.writeValueAsString(dataResult);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        byte[] bytes = writeValueAsString.getBytes(Charsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        //指定编码，否则在浏览器中会中文乱码
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }

    private void logInfo(ServerHttpRequest request, String body) {

        String uri = request.getPath().value();
        String params = request.getQueryParams().toString();
        String method = request.getMethodValue();
        String ip = request.getRemoteAddress().toString();
        String headers = request.getHeaders().entrySet()
                .stream()
                .map(entry -> entry.getKey() + ": [" + String.join(";", entry.getValue()) + "]")
                .collect(Collectors.joining("\n"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String accessDate = simpleDateFormat.format(new Date());
        StringBuilder sb = new StringBuilder();
        sb.append("\n==================================[API_CALL]==================================\n");
        sb.append("uri : " + uri + "\n");
        sb.append("method : " + method + "\n");
        sb.append("ip : " + ip + "\n");
        sb.append("params : " + params + "\n");
        sb.append("body : " + body + "\n");
        sb.append("accessDate : " + accessDate + "\n");
        sb.append("headers : { \n" + headers + "  }\n");
        sb.append("==============================================================================\n");
        logger.info(String.valueOf(sb));
    }

    private Mono<Void> cacheRequestBody(ServerWebExchange exchange, Function<ServerHttpRequest, Mono<Void>> function) {
        ServerHttpResponse response = exchange.getResponse();
        ServerHttpRequest request = exchange.getRequest();
        NettyDataBufferFactory factory = (NettyDataBufferFactory) response.bufferFactory();
        // Join all the DataBuffers so we have a single DataBuffer for the body
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .defaultIfEmpty(factory.wrap(new EmptyByteBuf(factory.getByteBufAllocator())))
                .map((dataBuffer) -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    String bodyString = new String(bytes, StandardCharsets.UTF_8);
                    logInfo(request, bodyString);
                    // 这里下面的代码我原先没写，后续的转发直接失效，因为body数据被拿出来了
                    Flux<DataBuffer> cachedFlux = Flux.defer(() -> {
                        DataBuffer buffer = exchange.getResponse().bufferFactory()
                                .wrap(bytes);
                        return Mono.just(buffer);
                    });

                    return (ServerHttpRequest) new ServerHttpRequestDecorator(
                            exchange.getRequest()) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return cachedFlux;
                        }
                    };
                }).switchIfEmpty(Mono.just(exchange.getRequest())).flatMap(function);
    }

}
