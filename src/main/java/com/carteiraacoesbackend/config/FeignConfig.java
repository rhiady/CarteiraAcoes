package com.carteiraacoesbackend.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.carteiraacoesbackend.clients.FeignErrorDecoder;

@Configuration
public class FeignConfig {
    @Bean ErrorDecoder errorDecoder() { return new FeignErrorDecoder(); }
}
