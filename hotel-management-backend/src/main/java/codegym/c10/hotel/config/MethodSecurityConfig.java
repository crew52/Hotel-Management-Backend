package codegym.c10.hotel.config;

import codegym.c10.hotel.security.CustomPermissionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Configuration class for method-level security.
 * Enables the use of @PreAuthorize, @PostAuthorize, and @Secured annotations
 * and configures a custom permission evaluator.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MethodSecurityConfig {
    
    @Autowired
    private CustomPermissionEvaluator customPermissionEvaluator;
    
    /**
     * Configures the method security expression handler to use our custom permission evaluator.
     * This allows us to use hasPermission() expressions in @PreAuthorize annotations.
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(customPermissionEvaluator);
        return expressionHandler;
    }
} 