package org.sample.actuatorSwaggerCRUDSample.mapper;

import org.mapstruct.Mapper;
import org.sample.actuatorSwaggerCRUDSample.model.CrmUserDao;
import org.sample.actuatorSwaggerCRUDSample.model.CrmUserAdditionRequestDto;
import org.sample.actuatorSwaggerCRUDSample.model.CrmUserExtractionDto;
import org.sample.actuatorSwaggerCRUDSample.model.mongo.crm.CrmUserMongoDocument;

@Mapper(componentModel = "spring")
public interface CrmUserMapper {
    CrmUserDao crmUserAdditionRequestDtoToCrmUserDao(CrmUserAdditionRequestDto crmUserAdditionRequestDto);
    CrmUserMongoDocument crmUserDaoToCrmUserMongoDocument(CrmUserDao crmUserDao);
    CrmUserDao crmUserMongoDocumentToCrmUserDao(CrmUserMongoDocument crmUserMongoDocument);
    CrmUserExtractionDto crmUserDaoToCrmUserExtractionDto(CrmUserDao crmUserDao);
}