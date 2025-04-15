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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.Customizer;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity()
public class SecurityConfig {
    @Autowired
    @Lazy
    private IUserService userService;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    @Lazy
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService); // không cần cast nữa
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(corsCustomizer -> corsCustomizer.configurationSource(this.corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Không tạo session phía server
                .authenticationProvider(authenticationProvider()) // Cấu hình AuthenticationProvider
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class) // Thêm filter JWT trước filter mặc định
                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả các request OPTIONS (quan trọng cho CORS preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Cho phép truy cập các API đăng nhập, đăng ký công khai
                        .requestMatchers("/api/auth/**").permitAll()

                        // --- QUAN TRỌNG: Định nghĩa các quy tắc phân quyền cho các API khác tại đây ---
                        // Ví dụ:
                         .requestMatchers("/api/admin/**").hasRole("ADMIN") // Chỉ ADMIN mới vào được /api/admin/**
                        // .requestMatchers("/api/rooms/**").hasAnyRole("ADMIN", "RECEPTIONIST") // ADMIN hoặc RECEPTIONIST
                        // .requestMatchers(HttpMethod.GET, "/api/public/info").permitAll() // API công khai khác

                        // Các request còn lại cần phải xác thực
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}