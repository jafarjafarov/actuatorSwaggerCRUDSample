package org.sample.actuatorSwaggerCRUDSample.service;

import com.google.common.base.Throwables;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.sample.actuatorSwaggerCRUDSample.configuration.logging.util.CommonLogger;


import org.sample.actuatorSwaggerCRUDSample.custom.exception.CrmUserEntityNotFoundException;
import org.sample.actuatorSwaggerCRUDSample.custom.exception.CrmUserInvalidCredentialsException;
import org.sample.actuatorSwaggerCRUDSample.custom.exception.GlobalCommonException;
import org.sample.actuatorSwaggerCRUDSample.mapper.CrmUserMapper;
import org.sample.actuatorSwaggerCRUDSample.model.crm.business.CrmUser;
import org.sample.actuatorSwaggerCRUDSample.model.mysql.crm.entity.CrmUserEntity;
import org.sample.actuatorSwaggerCRUDSample.repository.mysql.crm.CrmUserRepository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.userdetails.User;



import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.stream.Collectors;

@Service
public class CrmUserDBService implements ICrmUserService{

    private final CrmUserRepository crmUserRepository;
    private final CommonLogger LOGGER;
    private final CrmUserMapper crmUserMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final String AUTHENTICATION_SIGNATURE_KEY;
    private final Long TOKEN_ACTIVITY_PERIOD_MS;

    public CrmUserDBService(final CrmUserRepository crmUserRepository,
                            final @Qualifier("trace-logger") CommonLogger LOGGER,
                            final CrmUserMapper crmUserMapper,
                            final BCryptPasswordEncoder bCryptPasswordEncoder,
                            final @Qualifier("crmUsersJwtSecurityAuthenticationManager") AuthenticationManager authenticationManager,
                            final @Value("${local.crm.user.security.authenticated.jwt.signature.key}") String AUTHENTICATION_SIGNATURE_KEY,
                            final @Value("${local.crm.user.security.jwt.token.activity.period.ms}") Long TOKEN_ACTIVITY_PERIOD_MS)
    {
        this.crmUserRepository = crmUserRepository;
        this.LOGGER=LOGGER;
        this.crmUserMapper=crmUserMapper;
        this.bCryptPasswordEncoder=bCryptPasswordEncoder;
        this.authenticationManager=authenticationManager;
        this.AUTHENTICATION_SIGNATURE_KEY=AUTHENTICATION_SIGNATURE_KEY;
        this.TOKEN_ACTIVITY_PERIOD_MS=TOKEN_ACTIVITY_PERIOD_MS;
    }


    private final String CRM_USER_REPOSITORY_EXCEPTION_FAILED_SAVE = "CRM_USER_REPOSITORY_EXCEPTION_FAILED_SAVE";
    private final String CRM_USER_INVALID_CREDENTIALS_EXCEPTION = "CRM_USER_INVALID_CREDENTIALS_EXCEPTION";

    @Transactional("mySqlCRMTransactionManager")
    @Override
    public CrmUser save(CrmUser crmUser,String unencryptedPassword) {
        CrmUserEntity crmUserEntity = crmUserMapper.crmUserToCrmUserEntity(crmUser);
        crmUserEntity.setCryptedPassword(bCryptPasswordEncoder.encode(unencryptedPassword));
        crmUserEntity.setRegistrationDate(LocalDateTime.now());
        try {
            crmUserEntity = crmUserRepository.save(crmUserEntity);
            return crmUserMapper.crmUserEntityToCrmUser(crmUserEntity);
        }
        catch (DataAccessException dataAccessException){
            LOGGER.error("CRM Users repository has thrown exception during entity save",new HashMap<String, String>() {{
                put("dataAccessExceptionCanonicalName",dataAccessException.getClass().getCanonicalName());
                put("dataAccessExceptionMessage", dataAccessException.getMessage());
                put("dataAccessExceptionStackTraceAsString", Throwables.getStackTraceAsString(dataAccessException));
            }});
            throw new GlobalCommonException(CRM_USER_REPOSITORY_EXCEPTION_FAILED_SAVE, dataAccessException);
        }
    }

    @Override
    public String loginWithTokenInReturn(String login,String password) {
        final User user;

        try {
            user = (User) authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login,password)).getPrincipal();
        }
        catch (BadCredentialsException badCredentialsException){
            LOGGER.debug("CRM Users repository has thrown badCredentials exception during authorization",new HashMap<String, String>() {{
                put("badCredentialsException", badCredentialsException.getMessage());
                put("badCredentialsException", Throwables.getStackTraceAsString(badCredentialsException));
            }});
            throw new CrmUserInvalidCredentialsException(CRM_USER_INVALID_CREDENTIALS_EXCEPTION, badCredentialsException);
        }
        catch (InternalAuthenticationServiceException internalAuthenticationServiceException){
            Throwable cause = internalAuthenticationServiceException.getCause();
            if (cause instanceof CrmUserEntityNotFoundException)
                throw (CrmUserEntityNotFoundException)cause;
            else if (cause instanceof GlobalCommonException)
                throw (GlobalCommonException)cause;
            else
                throw internalAuthenticationServiceException;
        }

        List<String> authorities = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        long currentTimeMillis = System.currentTimeMillis();
        return  Jwts.builder()
                .signWith(SignatureAlgorithm.HS512,AUTHENTICATION_SIGNATURE_KEY)
                .setClaims(
                        new HashMap<String, Object>(){{
                            put("login",user.getUsername());
                            put("audience","internal.azericard");
                            put("issuer","Azericard LLC");
                        }}
                )
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + TOKEN_ACTIVITY_PERIOD_MS))
                .claim("authorities", authorities)
                .compact();
    }
}
