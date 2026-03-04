package com.anandhan.SpringEcommerce.AOP;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;


@Component
@Aspect
public class PerformanceAspect {


    public static final Logger LOGGER=LoggerFactory.getLogger("PerformanceAspect.class");

    // @Around advice → runs BEFORE and AFTER the target method
    // It controls the complete method execution
    @Around(" execution(* com.anandhan.SpringEcommerce.Service.ProductService .*(..))")
    public Object Monitortime(ProceedingJoinPoint jp) throws Throwable {

        long  start = System.currentTimeMillis();

        // VERY IMPORTANT:
        // In Around advice, we are responsible for executing the target method.
        // If we don't call proceed(), the actual service method will NOT run.
        Object obj = jp.proceed();

        long end = System.currentTimeMillis();

        // Log method name + execution time
        LOGGER.info("Time taken by " +
                jp.getSignature().getName() +
                " : " +  " ms");
        // Around advice MUST return the result of the target method
        return obj;
    }

}
