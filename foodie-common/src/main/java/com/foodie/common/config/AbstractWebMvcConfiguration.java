package com.foodie.common.config;

import com.foodie.common.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

@Slf4j
public abstract class AbstractWebMvcConfiguration extends WebMvcConfigurationSupport {

    private final WebMvcModuleMetadata metadata;

    protected AbstractWebMvcConfiguration(WebMvcModuleMetadata metadata) {
        this.metadata = metadata;
    }

    protected String getSwaggerBasePackage() {
        return metadata.getSwaggerBasePackage();
    }

    protected String getSwaggerTitle() {
        return metadata.getSwaggerTitle();
    }

    protected String getSwaggerDescription() {
        return metadata.getSwaggerDescription();
    }

    protected String[] getAllowedOrigins() {
        return metadata.getAllowedOrigins();
    }

    @Bean
    public Docket docket() {
        log.info("准备生成接口文档...");

        ApiInfo apiInfo = new ApiInfoBuilder()
                .title(getSwaggerTitle())
                .version("1.0")
                .description(getSwaggerDescription())
                .build();

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage(getSwaggerBasePackage()))
                .paths(PathSelectors.any())
                .build();
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始设置静态资源映射...");

        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new JacksonObjectMapper());
        converters.add(0, converter);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedOrigins(getAllowedOrigins())
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
