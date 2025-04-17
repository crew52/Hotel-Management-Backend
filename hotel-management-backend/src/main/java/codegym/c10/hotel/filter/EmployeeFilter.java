package codegym.c10.hotel.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter tự động lọc nhân viên đã nghỉ việc trong các request GET
 */
@Component
public class EmployeeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        // Kiểm tra nếu là request lấy danh sách nhân viên
        if (request.getRequestURI().matches("/api/employees.*") && request.getMethod().equals("GET")) {
            // Nếu không có parameter includeDeleted hoặc deleted, thêm parameter deleted=false
            if (request.getParameter("includeDeleted") == null && request.getParameter("deleted") == null) {
                request = new CustomHttpServletRequestWrapper(request);
            }
        }
        filterChain.doFilter(request, response);
    }
    
    /**
     * Wrapper cho HttpServletRequest để thêm parameter mặc định
     */
    private static class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String[]> customParameters;
        
        public CustomHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
            
            // Sao chép tất cả parameters hiện tại
            customParameters = new HashMap<>(request.getParameterMap());
            
            // Thêm parameter deleted=false
            customParameters.put("deleted", new String[] {"false"});
        }
        
        @Override
        public String getParameter(String name) {
            String[] values = customParameters.get(name);
            if (values != null && values.length > 0) {
                return values[0];
            }
            return super.getParameter(name);
        }
        
        @Override
        public Map<String, String[]> getParameterMap() {
            return customParameters;
        }
        
        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(customParameters.keySet());
        }
        
        @Override
        public String[] getParameterValues(String name) {
            return customParameters.get(name);
        }
    }
} 