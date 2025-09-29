package com.ssssssss.milvus.config;

import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus配置类，用于初始化和配置Milvus客户端
 *
 * @author 冰点
 */
@Configuration
public class MilvusConfig {

    @Value("${milvus.host:localhost}")
    private String host;

    @Value("${milvus.port:19530}")
    private int port;

    @Value("${milvus.username:}")
    private String username;

    @Value("${milvus.password:}")
    private String password;

    @Bean
    public MilvusClient milvusClient() {
        ConnectParam.Builder connectParamBuilder = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port);
        
        // 如果配置了用户名和密码，则添加认证信息
        if (!username.isEmpty() && !password.isEmpty()) {
            connectParamBuilder.withAuthorization(username,password);
        }
        
        return new MilvusServiceClient(connectParamBuilder.build());
    }
}
