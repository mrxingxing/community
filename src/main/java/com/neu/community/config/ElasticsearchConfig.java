package com.neu.community.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Configuration
public class ElasticsearchConfig {
    @Bean
    public TransportClient getClient() throws UnknownHostException{
        InetSocketAddress socket = new InetSocketAddress(InetAddress.getByName("172.26.70.109"),9300);
        TransportAddress node = new TransportAddress(socket);
        Settings settings = Settings.builder().put("cluster.name","nowcoder").build();
        TransportClient transportClient = new PreBuiltTransportClient(settings);
        transportClient.addTransportAddress(node);
        return transportClient;
    }
}
