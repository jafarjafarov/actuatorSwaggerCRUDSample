package org.sample.actuatorSwaggerCRUDSample.service;

import com.google.common.base.Throwables;
import org.sample.actuatorSwaggerCRUDSample.custom.exception.GlobalCommonException;
import org.sample.actuatorSwaggerCRUDSample.custom.exception.MongoDocumentNotFoundException;
import org.sample.actuatorSwaggerCRUDSample.mapper.CrmCustomerMapper;
import org.sample.actuatorSwaggerCRUDSample.configuration.logging.util.CommonLogger;
import org.sample.actuatorSwaggerCRUDSample.model.crm.business.CrmCustomer;
import org.sample.actuatorSwaggerCRUDSample.model.mongo.crm.document.CrmCustomerMongoDocument;
import org.sample.actuatorSwaggerCRUDSample.repository.mongo.crm.CrmCustomerMongoRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
public class CrmCustomerDBService implements ICrmCustomerService {

    private final CommonLogger LOGGER;
    private CrmCustomerMongoRepository crmCustomerMongoRepository;
    private CrmCustomerMapper crmCustomerMapper;

    public CrmCustomerDBService(@Qualifier("crmCustomerMongoRepository") CrmCustomerMongoRepository crmCustomerMongoRepository,
                                CrmCustomerMapper crmCustomerMapper,
                                @Qualifier("trace-logger") CommonLogger LOGGER){
        this.crmCustomerMongoRepository = crmCustomerMongoRepository;
        this.crmCustomerMapper = crmCustomerMapper;
        this.LOGGER = LOGGER;
    }

    private final String CRM_CUSTOMER_EXCEPTION_FAILED_UPDATE = "CRM_CUSTOMER_EXCEPTION_FAILED_UPDATE";
    private final String CRM_CUSTOMER_EXCEPTION_FAILED_SAVE = "CRM_CUSTOMER_EXCEPTION_FAILED_SAVE";
    private final String CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_EXCEPTION = "CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_EXCEPTION";
    private final String CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_NOT_FOUND = "CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_NOT_FOUND";
    private final String CRM_CUSTOMER_MONGO_DOCUMENT_BY_NAME_EXCEPTION = "CRM_CUSTOMER_MONGO_DOCUMENT_BY_NAME_EXCEPTION";
    private final String CRM_CUSTOMER_MONGO_DOCUMENT_BY_NAME_NOT_FOUND = "CRM_CUSTOMER_MONGO_DOCUMENT_BY_NAME_NOT_FOUND";

    @Transactional("crmMongoTransactionManager")
    @Override
    public CrmCustomer update(CrmCustomer crmCustomer) {
        CrmCustomerMongoDocument crmCustomerMongoDocument;
        try {
            LOGGER.trace("Extracting customer by id from crm customers repository","id",crmCustomer.getId());
            crmCustomerMongoDocument = crmCustomerMongoRepository.findById(crmCustomer.getId()).orElse(null);

            Objects.requireNonNull(crmCustomerMongoDocument,()->{
                LOGGER.debug("CRM customer mongo document with given id was not found","id",crmCustomer.getId());
                throw new MongoDocumentNotFoundException(CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_NOT_FOUND, crmCustomer.getId());
            });
            crmCustomerMapper.updateCrmCustomerMongoDocumentByCrmCustomer(crmCustomerMongoDocument,crmCustomer);
            crmCustomerMongoRepository.save(crmCustomerMongoDocument);
        }
        catch (MongoDocumentNotFoundException mongoDocumentNotFoundException){
            throw mongoDocumentNotFoundException;
        }
        catch (DataAccessException dataAccessException){
            LOGGER.error("CRM Mongo repository has thrown exception during document update",new HashMap<String, String>() {{
                put("dataAccessExceptionCanonicalName",dataAccessException.getClass().getCanonicalName());
                put("dataAccessExceptionMessage", dataAccessException.getMessage());
                put("dataAccessExceptionStackTraceAsString", Throwables.getStackTraceAsString(dataAccessException));
            }});
            throw new GlobalCommonException(CRM_CUSTOMER_EXCEPTION_FAILED_UPDATE,dataAccessException);
        }
        return crmCustomerMapper.crmCustomerMongoDocumentToCrmCustomer(crmCustomerMongoDocument);
    }

    @Transactional("crmMongoTransactionManager")
    @Override
    public CrmCustomer save(CrmCustomer crmCustomer){
        CrmCustomerMongoDocument crmCustomerMongoDocument = crmCustomerMapper.crmCustomerToCrmCustomerMongoDocument(crmCustomer);
        crmCustomerMongoDocument.setDtstamp(LocalDateTime.now());
        try {
            crmCustomerMongoDocument = crmCustomerMongoRepository.save(crmCustomerMongoDocument);
        }
        catch (DataAccessException dataAccessException){
            LOGGER.error("CRM Mongo repository has thrown exception during document save",new HashMap<String, String>() {{
                put("dataAccessExceptionCanonicalName",dataAccessException.getClass().getCanonicalName());
                put("dataAccessExceptionMessage", dataAccessException.getMessage());
                put("dataAccessExceptionStackTraceAsString", Throwables.getStackTraceAsString(dataAccessException));
            }});
            throw new GlobalCommonException(CRM_CUSTOMER_EXCEPTION_FAILED_SAVE,dataAccessException);
        }
        return crmCustomerMapper.crmCustomerMongoDocumentToCrmCustomer(crmCustomerMongoDocument);
    }

    @Override
    public CrmCustomer findById(String id){
        CrmCustomerMongoDocument crmCustomerMongoDocument;
        try{
            crmCustomerMongoDocument = crmCustomerMongoRepository.findById(id).orElseThrow(() ->
                    {
                        LOGGER.debug("CRM customer mongo document with given id was not found","id",id);
                        return new MongoDocumentNotFoundException(CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_NOT_FOUND, id);
                    }
            );
        }
        catch (DataAccessException dataAccessException){
            LOGGER.error("CRM customers mongo repository has thrown exception during document by id extraction",new HashMap<String, String>() {{
                put("dataAccessExceptionCanonicalName",dataAccessException.getClass().getCanonicalName());
                put("dataAccessExceptionMessage", dataAccessException.getMessage());
                put("dataAccessExceptionStackTraceAsString", Throwables.getStackTraceAsString(dataAccessException));
            }});
            throw new GlobalCommonException(CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_EXCEPTION, dataAccessException);
        }

        return crmCustomerMapper.crmCustomerMongoDocumentToCrmCustomer(crmCustomerMongoDocument);
    }

    @Override
    public List<CrmCustomer> findByName(String name) {
        List<CrmCustomerMongoDocument> crmCustomerMongoDocumentList;
        try {
            crmCustomerMongoDocumentList = crmCustomerMongoRepository.findAllByName(name);
        }catch (DataAccessException dataAccessException){
            LOGGER.error("CRM customers mongo repository has thrown exception during documents by name extraction",new HashMap<String, String>() {{
                put("dataAccessExceptionCanonicalName",dataAccessException.getClass().getCanonicalName());
                put("dataAccessExceptionMessage", dataAccessException.getMessage());
                put("dataAccessExceptionStackTraceAsString", Throwables.getStackTraceAsString(dataAccessException));
            }});
            throw new GlobalCommonException(CRM_CUSTOMER_MONGO_DOCUMENT_BY_NAME_EXCEPTION, dataAccessException);
        }

        if (CollectionUtils.isEmpty(crmCustomerMongoDocumentList)){
            LOGGER.debug("There was not any CRM customer mongo documents with given name","name",name);
            throw new MongoDocumentNotFoundException(CRM_CUSTOMER_MONGO_DOCUMENT_BY_NAME_NOT_FOUND, name);
        }
        return crmCustomerMapper.crmCustomerMongoDocumentListToCrmCustomerList(crmCustomerMongoDocumentList);
    }
}
