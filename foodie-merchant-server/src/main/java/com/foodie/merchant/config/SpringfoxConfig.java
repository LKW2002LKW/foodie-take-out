package com.foodie.merchant.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import java.lang.reflect.Field;
import java.util.List;

@Configuration
public class SpringfoxConfig {

    @Bean
    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider) {
                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                    if (field != null) {
                        field.setAccessible(true);
                        try {
                            @SuppressWarnings("unchecked")
                            List<RequestMappingInfoHandlerMapping> mappings = (List<RequestMappingInfoHandlerMapping>) ReflectionUtils.getField(field, bean);
                            if (mappings != null) {
                                mappings.removeIf(mapping -> mapping.getPatternParser() != null);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
                return bean;
            }
        };
    }
}

