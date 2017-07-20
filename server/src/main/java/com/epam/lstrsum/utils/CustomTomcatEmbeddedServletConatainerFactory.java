package com.epam.lstrsum.utils;

import lombok.Setter;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * This class is able to set embedded tomcat properties which not available
 * from application.properties or through AbstractConfigurableEmbeddedServletContainer.
 */
@Component
@ConfigurationProperties(prefix = "server.tomcat")
public class CustomTomcatEmbeddedServletConatainerFactory extends TomcatEmbeddedServletContainerFactory {

    @Setter
    private int maxSwallowSize = -1;

    @Override
    protected void customizeConnector(Connector connector) {
        super.customizeConnector(connector);
        if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol) {
            ((AbstractHttp11Protocol<?>) connector.getProtocolHandler()).setMaxSwallowSize(maxSwallowSize);
        }
    }
}
