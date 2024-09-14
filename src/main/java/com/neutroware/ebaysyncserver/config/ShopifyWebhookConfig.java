package com.neutroware.ebaysyncserver.config;

import com.neutroware.ebaysyncserver.filter.CachingRequestFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShopifyWebhookConfig {

    @Bean
    public FilterRegistrationBean<CachingRequestFilter> cachingRequestFilter() {
        FilterRegistrationBean<CachingRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CachingRequestFilter());
        registrationBean.addUrlPatterns("/shopify/webhook");
        return registrationBean;
    }
}
