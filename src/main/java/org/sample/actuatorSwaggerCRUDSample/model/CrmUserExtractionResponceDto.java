package org.sample.actuatorSwaggerCRUDSample.model;

public class CrmUserExtractionResponceDto {
    private CrmUserDao crmUserDao;


    public CrmUserExtractionResponceDto(CrmUserDao crmUserDao) {
        this.crmUserDao = crmUserDao;
    }

    public CrmUserDao getCrmUserDao() {
        return crmUserDao;
    }

    public void setCrmUserDao(CrmUserDao crmUserDao) {
        this.crmUserDao = crmUserDao;
    }
}
