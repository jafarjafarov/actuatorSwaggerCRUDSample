package org.sample.actuatorSwaggerCRUDSample.custom.exception.handler;

import com.google.common.base.Throwables;
import org.sample.actuatorSwaggerCRUDSample.configuration.logging.util.CommonLogger;
import org.sample.actuatorSwaggerCRUDSample.configuration.multi.language.IMultiLanguageComponent;
import org.sample.actuatorSwaggerCRUDSample.custom.exception.CrmUserEntityNotFoundException;
import org.sample.actuatorSwaggerCRUDSample.custom.exception.GlobalHandledException;
import org.sample.actuatorSwaggerCRUDSample.custom.exception.MongoDocumentNotFoundException;
import org.sample.actuatorSwaggerCRUDSample.mapper.CommonMapper;
import org.sample.actuatorSwaggerCRUDSample.model.common.dto.CommonMessageDTO;
import org.sample.actuatorSwaggerCRUDSample.model.common.dto.CommonResponseDTO;
import org.sample.actuatorSwaggerCRUDSample.model.common.dto.ErrorDesriptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;


@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class CommonRestExceptionHandler extends ResponseEntityExceptionHandler {

    private final IMultiLanguageComponent multiLanguageComponent;
    private final CommonLogger LOGGER;
    private final CommonMapper commonMapper;
    private final CommonResponseDTO commonResponseDTO;

    private final String THERE_IS_NO_LISTENER_FOR_ROUTE = "THERE_IS_NO_LISTENER_FOR_ROUTE";
    private final String METHOD_IS_NOT_SUPPORTED = "METHOD_IS_NOT_SUPPORTED";
    private final String HTTP_REQUEST_IS_NOT_READABLE = "HTTP_REQUEST_IS_NOT_READABLE";
    private final String HTTP_REQUEST_FAILED_COMMON_VALIDATION = "HTTP_REQUEST_FAILED_COMMON_VALIDATION";
    private final String REST_GENERAL_EXCEPTION_HANDLING = "REST_GENERAL_EXCEPTION_HANDLING";

    public CommonRestExceptionHandler(final @Qualifier("multiLanguageFileComponent")IMultiLanguageComponent multiLanguageComponent,
                                      final @Qualifier("trace-logger") CommonLogger LOGGER,
                                      final CommonMapper commonMapper,
                                      final @Qualifier("commonResponseDTO") CommonResponseDTO commonResponseDTO){
        this.multiLanguageComponent = multiLanguageComponent;
        this.LOGGER = LOGGER;
        this.commonMapper=commonMapper;
        this.commonResponseDTO=commonResponseDTO;
    }

    @ExceptionHandler(MongoDocumentNotFoundException.class)
    public ResponseEntity mongoDocumentNotFoundExceptionHandler(MongoDocumentNotFoundException mongoDocumentNotFoundException) {
        ErrorDesriptor errorDesriptor = mongoDocumentNotFoundException.getErrorDesriptor();
        commonResponseDTO.setStatusCodeMessageDtoErrorDescriptorAndInitDate(
                HttpStatus.NOT_FOUND.value(),
                new CommonMessageDTO("error",
                        errorDesriptor.getMessageKey(),
                        errorDesriptor.getMessage()),
                errorDesriptor);
        return new ResponseEntity(commonMapper.cloneCommonResponseDTO(commonResponseDTO),HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CrmUserEntityNotFoundException.class)
    public ResponseEntity crmUserEntityNotFoundExceptionHandler(CrmUserEntityNotFoundException crmUserEntityNotFoundException) {
        ErrorDesriptor errorDesriptor = crmUserEntityNotFoundException.getErrorDesriptor();
        commonResponseDTO.setStatusCodeMessageDtoErrorDescriptorAndInitDate(
                HttpStatus.NOT_FOUND.value(),
                new CommonMessageDTO("error",
                        errorDesriptor.getMessageKey(),
                        errorDesriptor.getMessage()),
                errorDesriptor);
        return new ResponseEntity(commonMapper.cloneCommonResponseDTO(commonResponseDTO),HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GlobalHandledException.class)
    public ResponseEntity globalHandledException(GlobalHandledException globalHandledException) {
        ErrorDesriptor errorDesriptor = globalHandledException.getErrorDesriptor();
        commonResponseDTO.setStatusCodeMessageDtoErrorDescriptorAndInitDate(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                new CommonMessageDTO("error",
                        errorDesriptor.getMessageKey(),
                        errorDesriptor.getMessage()),
                errorDesriptor);
        return new ResponseEntity(commonMapper.cloneCommonResponseDTO(commonResponseDTO),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity exceptionHandler(Exception exception) {
        ErrorDesriptor errorDesriptor = new ErrorDesriptor(exception.getStackTrace()[0].getClassName(),
                REST_GENERAL_EXCEPTION_HANDLING,
                exception.getMessage(),
                exception.getClass().getCanonicalName());
        LOGGER.fatal("ResponseEntityExceptionHandler caught unhandled exception, logging it's class, message and stack trace",new HashMap<String, String>() {{
            put("restHandledExceptionCanonicalName", exception.getClass().getCanonicalName());
            put("restHandledExceptionMessage", exception.getMessage());
            put("restHandledExceptionStackTraceAsString", Throwables.getStackTraceAsString(exception));
        }});
        commonResponseDTO.setStatusCodeMessageDtoErrorDescriptorAndInitDate(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                new CommonMessageDTO("error",
                        errorDesriptor.getMessageKey(),
                        errorDesriptor.getMessage()),
                errorDesriptor);
        return new ResponseEntity(commonMapper.cloneCommonResponseDTO(commonResponseDTO), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    protected ResponseEntity handleNoHandlerFoundException(NoHandlerFoundException noHandlerFoundException, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ErrorDesriptor errorDesriptor = new ErrorDesriptor(noHandlerFoundException.getStackTrace()[0].getClassName(),
                THERE_IS_NO_LISTENER_FOR_ROUTE,
                String.format(multiLanguageComponent.getMessageByKey(THERE_IS_NO_LISTENER_FOR_ROUTE), noHandlerFoundException.getRequestURL()),
                noHandlerFoundException.getClass().getCanonicalName());
        commonResponseDTO.setStatusCodeMessageDtoErrorDescriptorAndInitDate(
                HttpStatus.NOT_FOUND.value(),
                new CommonMessageDTO("error",
                        errorDesriptor.getMessageKey(),
                        errorDesriptor.getMessage()),
                errorDesriptor);
        return new ResponseEntity(commonMapper.cloneCommonResponseDTO(commonResponseDTO), HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException httpRequestMethodNotSupportedException, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String route = ((ServletWebRequest)request).getRequest().getRequestURI();
        String method = ((ServletWebRequest)request).getRequest().getMethod();
        ErrorDesriptor errorDesriptor = new ErrorDesriptor(httpRequestMethodNotSupportedException.getStackTrace()[0].getClassName(),
                METHOD_IS_NOT_SUPPORTED,
                String.format(multiLanguageComponent.getMessageByKey(METHOD_IS_NOT_SUPPORTED), method,route),
                httpRequestMethodNotSupportedException.getClass().getCanonicalName());
        commonResponseDTO.setStatusCodeMessageDtoErrorDescriptorAndInitDate(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                new CommonMessageDTO("error",
                        errorDesriptor.getMessageKey(),
                        errorDesriptor.getMessage()),
                errorDesriptor);
        return new ResponseEntity(commonMapper.cloneCommonResponseDTO(commonResponseDTO), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException httpMessageNotReadableException, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String route = ((ServletWebRequest)request).getRequest().getRequestURI();
        String method = ((ServletWebRequest)request).getRequest().getMethod();
        ErrorDesriptor errorDesriptor = new ErrorDesriptor(httpMessageNotReadableException.getStackTrace()[0].getClassName(),
                HTTP_REQUEST_IS_NOT_READABLE,
                String.format(multiLanguageComponent.getMessageByKey(HTTP_REQUEST_IS_NOT_READABLE),route,method),
                httpMessageNotReadableException.getClass().getCanonicalName());
        commonResponseDTO.setStatusCodeMessageDtoErrorDescriptorAndInitDate(
                HttpStatus.BAD_REQUEST.value(),
                new CommonMessageDTO("error",
                        errorDesriptor.getMessageKey(),
                        errorDesriptor.getMessage()),
                errorDesriptor);
        return new ResponseEntity(commonMapper.cloneCommonResponseDTO(commonResponseDTO), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String route = ((ServletWebRequest)request).getRequest().getRequestURI();
        String method = ((ServletWebRequest)request).getRequest().getMethod();
        ErrorDesriptor errorDesriptor = new ErrorDesriptor(ex.getStackTrace()[0].getClassName(),
                HTTP_REQUEST_FAILED_COMMON_VALIDATION,
                String.format(multiLanguageComponent.getMessageByKey(HTTP_REQUEST_FAILED_COMMON_VALIDATION),route,method),
                ex.getClass().getCanonicalName());

        commonResponseDTO.setStatusCodeMessageDtoErrorDescriptorAndInitDate(
                HttpStatus.BAD_REQUEST.value(),
                new CommonMessageDTO("error",
                        errorDesriptor.getMessageKey(),
                        errorDesriptor.getMessage()),
                errorDesriptor);

        for (FieldError fieldError:ex.getBindingResult().getFieldErrors())
            commonResponseDTO.getMessages().add(new CommonMessageDTO("error",
                    fieldError.getDefaultMessage(),
                    String.format(multiLanguageComponent.getMessageByKey(fieldError.getDefaultMessage()),fieldError.getField())));

        return new ResponseEntity(commonMapper.cloneCommonResponseDTO(commonResponseDTO), HttpStatus.BAD_REQUEST);
    }
}