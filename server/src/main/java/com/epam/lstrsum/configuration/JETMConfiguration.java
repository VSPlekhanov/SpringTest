package com.epam.lstrsum.configuration;

import etm.contrib.aop.aopalliance.EtmMethodCallInterceptor;
import etm.contrib.console.HttpConsoleServer;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.NestedMonitor;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


/**
 * <p/>
 * JETM (Java™ Execution Time Measurement Library) is a small and free library, that helps locating performance problems in Java™ applications.
 * </p>
 *
 *  http://{server name or address}:40000 to see aggregated performance statistics.
 **/
@Configuration
@Profile("profiling")
public class JETMConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public NestedMonitor nestedMonitor() {
        return new NestedMonitor();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public HttpConsoleServer httpConsoleServer(EtmMonitor etmMonitor) {
        return new HttpConsoleServer(etmMonitor);
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
