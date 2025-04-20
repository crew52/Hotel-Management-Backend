package codegym.c10.hotel.security;

import codegym.c10.hotel.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.config.Customizer;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    
    private final IUserService userService;
    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtUtil jwtUtil;

    @Autowired
    public SecurityConfig(@Lazy IUserService userService, 
                         CorsConfigurationSource corsConfigurationSource,
                         JwtUtil jwtUtil) {
        this.userService = userService;
        this.corsConfigurationSource = corsConfigurationSource;
        this.jwtUtil = jwtUtil;
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter() {
        return new JwtAuthenticationTokenFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, 
                                                 JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(corsCustomizer -> corsCustomizer.configurationSource(this.corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả các request OPTIONS (quan trọng cho CORS preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Cho phép truy cập các API đăng nhập, đăng ký công khai
                        .requestMatchers("/api/auth/**").permitAll()
                        
                        // Định nghĩa quyền cho các nhóm API
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Chỉ ADMIN mới vào được /api/admin/**
                        
                        // Tất cả các API khác đều yêu cầu xác thực
                        .requestMatchers("/api/rooms/**").authenticated()
                        .requestMatchers("/api/room-categories/**").authenticated()
                        .requestMatchers("/api/permissions/**").authenticated()
                        .requestMatchers("/api/roles/**").authenticated()
                        .requestMatchers("/api/employees/**").authenticated()
                        .requestMatchers("/api/activity-logs/**").authenticated()
                        .requestMatchers("/api/logs/**").authenticated()
                        
                        // Các request còn lại cũng cần phải xác thực
                        .anyRequest().authenticated()
                );

        return http.build();


    }
}