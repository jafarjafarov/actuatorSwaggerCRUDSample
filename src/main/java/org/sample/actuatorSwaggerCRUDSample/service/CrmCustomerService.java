package org.sample.actuatorSwaggerCRUDSample.service;

import org.sample.actuatorSwaggerCRUDSample.configuration.multi.language.IMultiLanguageComponent;
import org.sample.actuatorSwaggerCRUDSample.custom.exception.GlobalHandledException;
import org.sample.actuatorSwaggerCRUDSample.custom.exception.MongoDocumentNotFoundException;
import org.sample.actuatorSwaggerCRUDSample.mapper.CrmCustomerMapper;
import org.sample.actuatorSwaggerCRUDSample.configuration.logging.util.CommonLogger;
import org.sample.actuatorSwaggerCRUDSample.model.CrmCustomer;
import org.sample.actuatorSwaggerCRUDSample.model.mongo.crm.document.CrmCustomerMongoDocument;
import org.sample.actuatorSwaggerCRUDSample.repository.mongo.crm.CrmCustomerMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class CrmCustomerService implements ICrmCustomerService {

    private final CommonLogger LOGGER;
    private CrmCustomerMongoRepository crmCustomerMongoRepository;
    private CrmCustomerMapper crmCustomerMapper;
    private final IMultiLanguageComponent multiLanguageComponent;

    private final String CRM_CUSTOMER_EXCEPTION_FAILED_UPDATE = "CRM_CUSTOMER_EXCEPTION_FAILED_UPDATE";
    private final String CRM_CUSTOMER_EXCEPTION_FAILED_SAVE = "CRM_CUSTOMER_EXCEPTION_FAILED_SAVE";
    private final String CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_EXCEPTION = "CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_EXCEPTION";
    private final String CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_NOT_FOUND = "CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_NOT_FOUND";
    private final String CRM_CUSTOMER_MONGO_DOCUMENT_BY_NAME_EXCEPTION = "CRM_CUSTOMER_MONGO_DOCUMENT_BY_NAME_EXCEPTION";
    private final String CRM_CUSTOMER_MONGO_DOCUMENT_BY_NAME_NOT_FOUND = "CRM_CUSTOMER_MONGO_DOCUMENT_BY_NAME_NOT_FOUND";

    public CrmCustomerService(@Autowired @Qualifier("crmCustomerMongoRepository") CrmCustomerMongoRepository crmCustomerMongoRepository,
                              @Autowired CrmCustomerMapper crmCustomerMapper,
                              @Autowired @Qualifier("trace-logger") CommonLogger LOGGER,
                              @Autowired @Qualifier("multiLanguageFileComponent")IMultiLanguageComponent multiLanguageComponent){
        this.crmCustomerMongoRepository = crmCustomerMongoRepository;
        this.crmCustomerMapper = crmCustomerMapper;
        this.LOGGER = LOGGER;
        this.multiLanguageComponent = multiLanguageComponent;
    }

    @Transactional("crmMongoTransactionManager")
    @Override
    public CrmCustomer update(CrmCustomer crmCustomer) {
        CrmCustomerMongoDocument crmCustomerMongoDocument;
        try {
            LOGGER.trace("Extracting customer by id from crm customers repository","id",crmCustomer.getId());
            crmCustomerMongoDocument = crmCustomerMongoRepository.findById(crmCustomer.getId()).orElse(null);
            Objects.requireNonNull(crmCustomerMongoDocument,()->{
                LOGGER.debug(String.format("CRM customer mongo document with %s id was not found",crmCustomer.getId()));
                throw new MongoDocumentNotFoundException(CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_NOT_FOUND,String.format(multiLanguageComponent.getMessageByKey(CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_NOT_FOUND),crmCustomer.getId()));
            });
            crmCustomerMapper.updateCrmCustomerMongoDocumentByCrmCustomer(crmCustomerMongoDocument,crmCustomer);
            crmCustomerMongoRepository.save(crmCustomerMongoDocument);
        }
        catch (MongoDocumentNotFoundException mongoDocumentNotFoundException){
            throw mongoDocumentNotFoundException;
        }
        catch (DataAccessException dataAccessException){
            LOGGER.error(String.format("CRM Mongo repository has thrown exception during document update : %s", dataAccessException.getMessage()),"dataAccessException",dataAccessException);
            throw new GlobalHandledException(CRM_CUSTOMER_EXCEPTION_FAILED_UPDATE,String.format(multiLanguageComponent.getMessageByKey(CRM_CUSTOMER_EXCEPTION_FAILED_UPDATE), dataAccessException.getMessage()));
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
            LOGGER.error(String.format("CRM Mongo repository has thrown exception during document save : %s", dataAccessException.getMessage()),"dataAccessException",dataAccessException);
            throw new GlobalHandledException(CRM_CUSTOMER_EXCEPTION_FAILED_SAVE,String.format(multiLanguageComponent.getMessageByKey(CRM_CUSTOMER_EXCEPTION_FAILED_SAVE), dataAccessException.getMessage()));
        }
        return crmCustomerMapper.crmCustomerMongoDocumentToCrmCustomer(crmCustomerMongoDocument);
    }

    @Override
    public CrmCustomer findById(String id){
        CrmCustomerMongoDocument crmCustomerMongoDocument;
        try{
            crmCustomerMongoDocument = crmCustomerMongoRepository.findById(id).orElse(null);
        }
        catch (DataAccessException dataAccessException){
            LOGGER.error(String.format("CRM customers mongo repository has thrown exception during document by id extraction : %s", dataAccessException.getMessage()),"dataAccessException",dataAccessException);
            throw new GlobalHandledException(CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_EXCEPTION,String.format(multiLanguageComponent.getMessageByKey(CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_EXCEPTION), dataAccessException.getMessage()));
        }

        Objects.requireNonNull(crmCustomerMongoDocument,()->{
            LOGGER.debug(String.format("CRM customer mongo document with %s id was not found",id));
            throw new MongoDocumentNotFoundException(CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_NOT_FOUND,String.format(multiLanguageComponent.getMessageByKey(CRM_CUSTOMER_MONGO_DOCUMENT_BY_ID_NOT_FOUND),id));
        });
        return crmCustomerMapper.crmCustomerMongoDocumentToCrmCustomer(crmCustomerMongoDocument);
    }

    @Override
    public List<CrmCustomer> findByName(String name) {
        List<CrmCustomerMongoDocument> crmCustomerMongoDocumentList;
        try {
            crmCustomerMongoDocumentList = crmCustomerMongoRepository.findAllByName(name);
        }catch (DataAccessException dataAccessException){
            LOGGER.error(String.format("CRM customers mongo repository has thrown exception during documents by name extraction : %s", dataAccessException.getMessage()),"dataAccessException",dataAccessException);
            throw new GlobalHandledException(CRM_CUSTOMER_MONGO_DOCUMENT_BY_NAME_EXCEPTION,String.format(multiLanguageComponent.getMessageByKey(CRM_CUSTOMER_MONGO_DOCUMENT_BY_NAME_EXCEPTION), dataAccessException.getMessage()));
        }

        if (CollectionUtils.isEmpty(crmCustomerMongoDocumentList)){
            LOGGER.debug(String.format("There was not any CRM customer mongo documents with %s name",name));
            throw new MongoDocumentNotFoundException(CRM_CUSTOMER_MONGO_DOCUMENT_BY_NAME_NOT_FOUND,String.format(multiLanguageComponent.getMessageByKey(CRM_CUSTOMER_MONGO_DOCUMENT_BY_NAME_NOT_FOUND),name));
        }
        return crmCustomerMapper.crmCustomerMongoDocumentListToCrmCustomerList(crmCustomerMongoDocumentList);
    }
}