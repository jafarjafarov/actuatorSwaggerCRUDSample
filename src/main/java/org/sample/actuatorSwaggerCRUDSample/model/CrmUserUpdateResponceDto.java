package org.sample.actuatorSwaggerCRUDSample.model;

public class CrmUserUpdateResponceDto {
    private CrmClient crmClient;

    public CrmUserUpdateResponceDto(CrmClient crmClient) {
        this.crmClient = crmClient;
    }

    public CrmClient getCrmClient() {
        return crmClient;
    }

    public void setCrmClient(CrmClient crmClient) {
        this.crmClient = crmClient;
    }
}
