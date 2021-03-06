package org.sample.actuatorSwaggerCRUDSample.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.sample.actuatorSwaggerCRUDSample.configuration.multi.language.message.IMultiLanguageComponent;
import org.sample.actuatorSwaggerCRUDSample.mapper.CommonMapper;
import org.sample.actuatorSwaggerCRUDSample.mapper.CrmCustomerMapper;
import org.sample.actuatorSwaggerCRUDSample.model.common.dto.CommonMessageDTO;
import org.sample.actuatorSwaggerCRUDSample.model.common.dto.CommonResponseDTO;
import org.sample.actuatorSwaggerCRUDSample.model.crm.business.CrmCustomer;
import org.sample.actuatorSwaggerCRUDSample.model.crm.dto.*;
import org.sample.actuatorSwaggerCRUDSample.service.ICrmCustomerService;
import org.sample.actuatorSwaggerCRUDSample.configuration.logging.util.CommonLogger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/customers")
public class CrmCustomersController {

    private final CommonLogger LOGGER;
    private final ICrmCustomerService crmCustomerService;
    private final CrmCustomerMapper crmCustomerMapper;
    private final CommonMapper commonMapper;
    private final IMultiLanguageComponent multiLanguageComponent;

    private final CommonResponseDTO commonResponseDTO;

    public CrmCustomersController(final @Qualifier("crmCustomerDBService") ICrmCustomerService crmCustomerService,
                                  final CrmCustomerMapper crmCustomerMapper,
                                  final @Qualifier("multiLanguageFileComponent")IMultiLanguageComponent multiLanguageComponent,
                                  final @Qualifier("trace-logger") CommonLogger LOGGER,
                                  final CommonMapper commonMapper,
                                  final @Qualifier("commonResponseDTO") CommonResponseDTO commonResponseDTO){
        this.crmCustomerService = crmCustomerService;
        this.crmCustomerMapper = crmCustomerMapper;
        this.LOGGER = LOGGER;
        this.multiLanguageComponent = multiLanguageComponent;
        this.commonMapper = commonMapper;
        this.commonResponseDTO = commonResponseDTO;
    }

    private final String FIND_CRM_CUSTOMER_BY_ID_SUCCESS = "FIND_CRM_CUSTOMER_BY_ID_SUCCESS";
    private final String FIND_CRM_CUSTOMER_BY_NAME_SUCCESS = "FIND_CRM_CUSTOMER_BY_NAME_SUCCESS";
    private final String CRM_CUSTOMER_SAVED_SUCCESSFULLY = "CRM_CUSTOMER_SAVED_SUCCESSFULLY";
    private final String CRM_CUSTOMER_UPDATED_SUCCESSFULLY = "CRM_CUSTOMER_UPDATED_SUCCESSFULLY";

    @ApiOperation(
            value = "Extraction of crm customer by id from mongo db",
            notes = "Nothing super fishy, just extraction of crm customer by auto generated mongo db id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Crm customer data was successfully extracted"),
            @ApiResponse(code = 400, message = "Request is in unreadable format or could not pass request validation"),
            @ApiResponse(code = 404, message = "Crm customer mongo document with id was not found"),
            @ApiResponse(code = 500, message = "Technical mismatch, advising to contact for the logs, to handle the issue")
    })
    @GetMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponseDTO<CrmCustomerExtractionResponseDto>> findCustomerById(@PathVariable("id") String id){
        LOGGER.trace("Extracting customer by id from crm customers service","id",id);
        CrmCustomer crmCustomer = crmCustomerService.findById(id);
        LOGGER.info(String.format("Extracted crm customer by %s id from crm customers service",id),"crmCustomer", crmCustomer);
        CrmCustomerExtractionResponseDto crmCustomerExtractionResponseDto = new CrmCustomerExtractionResponseDto(crmCustomer);
        commonResponseDTO.setStatusCodeMessageDtoDataAndInitDate(
                                HttpStatus.OK.value(),
                new CommonMessageDTO("success",
                        FIND_CRM_CUSTOMER_BY_ID_SUCCESS,
                        String.format(multiLanguageComponent.getMessageByKey(FIND_CRM_CUSTOMER_BY_ID_SUCCESS), id)),
                crmCustomerExtractionResponseDto);
        return ResponseEntity.ok(commonMapper.cloneCommonResponseDTO(commonResponseDTO));
    }

    @ApiOperation(
            value = "Extraction of crm customers list by name from mongo db",
            notes = "Nothing super fishy, just extraction of crm customers list by name")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Crm customers list data was successfully extracted"),
            @ApiResponse(code = 400, message = "Request is in unreadable format or could not pass request validation"),
            @ApiResponse(code = 404, message = "There was not any crm customer mongo document with such name"),
            @ApiResponse(code = 500, message = "Technical mismatch, advising to contact for the logs, to handle the issue")
    })
    @GetMapping(value = "/attributes/name/{name}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponseDTO<CrmCustomerByNameExtractionResponseDto>> findCustomersByName(@PathVariable("name") String name){
        LOGGER.trace("Extracting list of customers by name from crm customers service","name",name);
        List<CrmCustomer> crmCustomerList = crmCustomerService.findByName(name);
        LOGGER.info(String.format("Extracted crm customers by %s name from crm customers service",name),"crmCustomerList", crmCustomerList);
        CrmCustomerByNameExtractionResponseDto crmCustomerByNameExtractionResponseDto = new CrmCustomerByNameExtractionResponseDto(crmCustomerList);
        commonResponseDTO.setStatusCodeMessageDtoDataAndInitDate(
                HttpStatus.OK.value(),
                new CommonMessageDTO("success",
                        FIND_CRM_CUSTOMER_BY_NAME_SUCCESS,
                        String.format(multiLanguageComponent.getMessageByKey(FIND_CRM_CUSTOMER_BY_NAME_SUCCESS),name)),
                crmCustomerByNameExtractionResponseDto);
        return ResponseEntity.ok(commonMapper.cloneCommonResponseDTO(commonResponseDTO));
    }

    @ApiOperation(
            value = "Addition of crm customer to mongo db",
            notes = "Nothing super fishy, just addition of crm customer to mongo db")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Crm customer was successfully saved"),
            @ApiResponse(code = 400, message = "Request is in unreadable format or could not pass request validation"),
            @ApiResponse(code = 500, message = "Technical mismatch, advising to contact for the logs, to handle the issue")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponseDTO<CrmCustomerAdditionResponseDto>> addCustomer(@RequestBody @Valid CrmCustomerAdditionRequestDto crmCustomerAdditionRequestDto) {
        LOGGER.trace("Adding customer via crm customers save service","crmCustomerAdditionRequestDto", crmCustomerAdditionRequestDto);
        CrmCustomer crmCustomer = crmCustomerMapper.crmCustomerAdditionRequestDtoToCrmCustomer(crmCustomerAdditionRequestDto);
        crmCustomer = crmCustomerService.save(crmCustomer);
        LOGGER.trace("Customer saved at crm customers service","crmCustomer", crmCustomer);
        CrmCustomerAdditionResponseDto crmCustomerAdditionResponseDto = new CrmCustomerAdditionResponseDto(crmCustomer);
        commonResponseDTO.setStatusCodeMessageDtoDataAndInitDate(
                HttpStatus.OK.value(),
                new CommonMessageDTO("success",
                        CRM_CUSTOMER_SAVED_SUCCESSFULLY,
                        multiLanguageComponent.getMessageByKey(CRM_CUSTOMER_SAVED_SUCCESSFULLY)),
                crmCustomerAdditionResponseDto);
        return ResponseEntity.ok(commonMapper.cloneCommonResponseDTO(commonResponseDTO));
    }

    @ApiOperation(
            value = "Update of crm customer at mongo db",
            notes = "Nothing super fishy, just update of crm customer at mongo db")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Crm customer was successfully updated"),
            @ApiResponse(code = 400, message = "Request is in unreadable format or could not pass request validation"),
            @ApiResponse(code = 404, message = "Crm customer mongo document with id was not found"),
            @ApiResponse(code = 500, message = "Technical mismatch, advising to contact for the logs, to handle the issue")
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponseDTO<CrmCustomerUpdateResponseDto>> updateCustomer(@RequestBody CrmCustomerUpdateRequestDto crmCustomerUpdateRequestDto) {
        LOGGER.trace("Updating crmUserEntity with given dto","crmCustomerUpdateRequestDto", crmCustomerUpdateRequestDto);
        CrmCustomer crmCustomer = crmCustomerMapper.crmCustomerUpdateRequestDtoToCrmCustomer(crmCustomerUpdateRequestDto);
        crmCustomer = crmCustomerService.update(crmCustomer);
        LOGGER.info(String.format("Extracted crm customer by %s id from crm customers service after update",crmCustomer.getId()),"crmCustomer", crmCustomer);
        CrmCustomerUpdateResponseDto crmCustomerUpdateResponseDto = new CrmCustomerUpdateResponseDto(crmCustomer);
        commonResponseDTO.setStatusCodeMessageDtoDataAndInitDate(
                HttpStatus.OK.value(),
                new CommonMessageDTO("success",
                        CRM_CUSTOMER_UPDATED_SUCCESSFULLY,
                        multiLanguageComponent.getMessageByKey(CRM_CUSTOMER_UPDATED_SUCCESSFULLY)),
                crmCustomerUpdateResponseDto);
        return ResponseEntity.ok(commonMapper.cloneCommonResponseDTO(commonResponseDTO));
    }
}