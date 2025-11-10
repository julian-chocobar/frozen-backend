package com.enigcode.frozen_backend.common.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
    
    long id() default 1L;
    
    String username() default "testuser";
    
    String password() default "password";
    
    String[] roles() default {"USER"};
}
