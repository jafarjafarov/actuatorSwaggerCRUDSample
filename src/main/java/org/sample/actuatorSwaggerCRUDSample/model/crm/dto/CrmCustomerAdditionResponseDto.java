package org.sample.actuatorSwaggerCRUDSample.model.crm.dto;

import org.sample.actuatorSwaggerCRUDSample.model.crm.business.CrmCustomer;

public class CrmCustomerAdditionResponseDto {
    private CrmCustomer crmCustomer;

    public CrmCustomerAdditionResponseDto(CrmCustomer crmCustomer) {
        this.crmCustomer = crmCustomer;
    }

    public CrmCustomer getCrmCustomer() {
        return crmCustomer;
    }

    public void setCrmCustomer(CrmCustomer crmCustomer) {
        this.crmCustomer = crmCustomer;
    }
}
