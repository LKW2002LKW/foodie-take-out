package com.foodie.merchant.config;

import com.foodie.common.json.JacksonObjectMapper;
import com.foodie.merchant.interceptor.JwtTokenMerchantInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

/**
 * Web配置类：注册拦截器、配置消息转换器等
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenMerchantInterceptor jwtTokenMerchantInterceptor;

    /**
     * 注册自定义拦截器
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");

        registry.addInterceptor(jwtTokenMerchantInterceptor)
                .addPathPatterns("/merchant/**")
                .excludePathPatterns(
                        "/merchant/admin/login",     // 登录接口不拦截
                        "/merchant/admin/register"   // 注册接口不拦截
                );
    }

    /**
     * 配置Swagger接口文档
     */
    @Bean
    public Docket docket() {
        log.info("准备生成接口文档...");

        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("吃货联盟商户管理端接口文档")
                .version("1.0")
                .description("吃货联盟商户管理端接口文档")
                .build();

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.foodie.merchant.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 设置静态资源映射（Swagger）
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始设置静态资源映射...");

        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 扩展Spring MVC消息转换器
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");

        // 创建消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // 设置对象转换器，底层使用Jackson将Java对象转为json
        converter.setObjectMapper(new JacksonObjectMapper());
        // 将消息转换器对象追加到mvc框架的转换器集合中（索引0表示优先使用）
        converters.add(0, converter);
    }

    /**
     * 全局CORS配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedOrigins("http://localhost:5177") // 或者前端地址 http://localhost:8080
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}