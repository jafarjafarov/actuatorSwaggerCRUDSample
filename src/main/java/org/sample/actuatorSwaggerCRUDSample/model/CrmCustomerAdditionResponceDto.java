package org.sample.actuatorSwaggerCRUDSample.model;

public class CrmCustomerAdditionResponceDto {
    private CrmCustomer crmCustomer;

    public CrmCustomerAdditionResponceDto(CrmCustomer crmCustomer) {
        this.crmCustomer = crmCustomer;
    }

    public CrmCustomer getCrmCustomer() {
        return crmCustomer;
    }

    public void setCrmCustomer(CrmCustomer crmCustomer) {
        this.crmCustomer = crmCustomer;
    }
}
