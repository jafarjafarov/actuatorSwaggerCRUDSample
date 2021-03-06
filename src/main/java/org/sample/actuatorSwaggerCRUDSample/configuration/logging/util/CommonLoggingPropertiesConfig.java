package org.sample.actuatorSwaggerCRUDSample.configuration.logging.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class CommonLoggingPropertiesConfig{
    private final String infoBuildArchiveBaseName;
    private String hostName;
    private String hostAddress;

    public CommonLoggingPropertiesConfig(@Value("${info.build.archiveBaseName}") String infoBuildArchiveBaseName) throws UnknownHostException {
        this.infoBuildArchiveBaseName = infoBuildArchiveBaseName;
    }

    public String getInfoBuildArchiveBaseName() {
        return infoBuildArchiveBaseName;
    }

    public String getHostName() {
        return hostName;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public void setHostNameAndHostAddress(String hostName,String hostAddress) {
        setHostName(hostName);
        setHostAddress(hostAddress);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void setHostNameAndHostAddressAfterStartUp() throws UnknownHostException {
        setHostName(InetAddress.getLocalHost().getHostName());
        setHostAddress(InetAddress.getLocalHost().getHostAddress());
        CommonLoggingObject.commonLoggingPropertiesConfig = this;
    }
}
