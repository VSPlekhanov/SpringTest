package com.epam.lstrsum.configuration;

import etm.contrib.aop.aopalliance.EtmMethodCallInterceptor;
import etm.contrib.integration.spring.web.SpringHttpConsoleServlet;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.NestedMonitor;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


/**
 * <p/>
 * JETM (Java™ Execution Time Measurement Library) is a small and free library, that helps locating performance problems in Java™ applications.
 * </p>
 * <p>
 * http://{server name or address}:{server.port}/{server.contextPath}/performance/ - to see aggregated performance statistics.
 * For example http://localhost:8080/experience/performance/
 **/
@Configuration
@Profile("profiling")
public class JETMConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public NestedMonitor nestedMonitor() {
        return new NestedMonitor();
    }

    @Bean
    public ServletRegistrationBean exampleServletBean() {
        ServletRegistrationBean bean = new ServletRegistrationBean(
                new SpringHttpConsoleServlet(), "/performance/*");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public EtmMethodCallInterceptor etmMethodCallInterceptor(EtmMonitor etmMonitor) {
        return new EtmMethodCallInterceptor(etmMonitor);
    }

    @Bean
    public BeanNameAutoProxyCreator beanNameAutoProxyCreator() {
        BeanNameAutoProxyCreator beanNameAutoProxyCreator = new BeanNameAutoProxyCreator();
        beanNameAutoProxyCreator.setProxyTargetClass(true);
        beanNameAutoProxyCreator.setInterceptorNames("etmMethodCallInterceptor");
        beanNameAutoProxyCreator.setBeanNames("*Controller", "*ServiceImpl", "*MockImpl", "*Mapper", "*Repository", "*Aggregator");
        return beanNameAutoProxyCreator;
    }
}
