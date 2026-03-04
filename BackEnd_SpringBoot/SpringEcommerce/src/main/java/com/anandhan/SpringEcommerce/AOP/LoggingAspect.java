package com.anandhan.SpringEcommerce.AOP;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class LoggingAspect {

    public static final Logger LOGGER=LoggerFactory.getLogger(LoggingAspect.class);


    @Before("execution (* com.anandhan.SpringEcommerce.Service.ProductService.*(..))")
    public void logMethodCall(JoinPoint jp) {
        LOGGER.info("Method Called --- {}", jp.getSignature().getName());
    }

    @After("execution (* com.anandhan.SpringEcommerce.Service.ProductService.*(..))")
    public void logMethodExecuted(JoinPoint jp) {
        LOGGER.info("Method Executed --- {}", jp.getSignature().getName());
    }

    @AfterThrowing("execution (* com.anandhan.SpringEcommerce.Service.ProductService.*(..))\"")
    public void logMethodCrash(JoinPoint jp) {
        LOGGER.info("Method has some issues --- {}", jp.getSignature().getName());
    }

    @AfterReturning("execution (* com.anandhan.SpringEcommerce.Service.ProductService.*(..))\"")
    public void logMethodExecutedSuccess(JoinPoint jp) {
        LOGGER.info("Method Executed succesfully --- {}", jp.getSignature().getName());
    }
}
