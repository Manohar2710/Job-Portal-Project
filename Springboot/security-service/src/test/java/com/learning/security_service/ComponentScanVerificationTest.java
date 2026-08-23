package com.learning.security_service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies that @SpringBootApplication on SecurityServiceApplication (package
 * com.learning.security_service) picks up all beans defined in the sibling
 * package com.learning.security — specifically the auth-service beans that are
 * NOT registered by SecurityModuleAutoConfiguration.
 *
 * If any assertion fails it means the component scan does NOT reach
 * com.learning.security and a scanBasePackages fix is required.
 */
@SpringBootTest
@ActiveProfiles("test")
class ComponentScanVerificationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("AuthBeanConfig (DaoAuthenticationProvider, PasswordEncoder) is registered")
    void authBeanConfig_isRegistered() {
        assertThat(context.containsBean("authBeanConfig"))
                .as("AuthBeanConfig must be picked up by component scan")
                .isTrue();
        assertThat(context.containsBean("passwordEncoder"))
                .as("PasswordEncoder bean must be defined")
                .isTrue();
        assertThat(context.containsBean("authenticationProvider"))
                .as("AuthenticationProvider bean must be defined")
                .isTrue();
    }

    @Test
    @DisplayName("UserDetailsServiceImpl is registered and wired")
    void userDetailsServiceImpl_isRegistered() {
        assertThat(context.containsBean("userDetailsServiceImpl"))
                .as("UserDetailsServiceImpl must be picked up by component scan — " +
                    "if missing, DaoAuthenticationProvider has no UserDetailsService → every login returns 401")
                .isTrue();
    }

    @Test
    @DisplayName("AuthController is registered")
    void authController_isRegistered() {
        assertThat(context.containsBean("authController"))
                .as("AuthController must be picked up by component scan")
                .isTrue();
    }

    @Test
    @DisplayName("AuthServiceImpl is registered")
    void authServiceImpl_isRegistered() {
        assertThat(context.containsBean("authServiceImpl"))
                .as("AuthServiceImpl must be picked up by component scan")
                .isTrue();
    }

    @Test
    @DisplayName("SecurityModuleAutoConfiguration beans are registered via auto-config")
    void securityModuleBeans_areRegistered() {
        assertThat(context.containsBean("jwtServiceImpl"))
                .as("JwtServiceImpl must be registered via SecurityModuleAutoConfiguration")
                .isTrue();
        assertThat(context.containsBean("jwtAuthenticationFilter"))
                .as("JwtAuthenticationFilter must be registered via SecurityModuleAutoConfiguration")
                .isTrue();
        assertThat(context.containsBean("securityFilterChain"))
                .as("SecurityFilterChain must be registered via SecurityModuleAutoConfiguration")
                .isTrue();
    }
}
