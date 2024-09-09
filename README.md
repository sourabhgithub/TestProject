import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class IpRestrictionFilter extends OncePerRequestFilter {

    // List of allowed IP addresses
    private static final List<String> ALLOWED_IPS = List.of(
        "192.168.1.10",  // Example IP address 1
        "192.168.1.20"   // Example IP address 2
    );

    private final RequestMatcher protectedUrlPattern;

    public IpRestrictionFilter(RequestMatcher protectedUrlPattern) {
        this.protectedUrlPattern = protectedUrlPattern;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Check if the request URL matches the protected pattern
        if (protectedUrlPattern.matches(request)) {
            String remoteAddr = request.getRemoteAddr();

            // Allow or block request based on IP
            if (ALLOWED_IPS.contains(remoteAddr)) {
                filterChain.doFilter(request, response);  // Allow the request
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");  // Block the request
                return;
            }
        } else {
            filterChain.doFilter(request, response);  // Continue the request processing if URL is not protected
        }
    }
}


2. Configure Spring Security to Use the Filter
Create a Spring Security configuration class to register the custom filter. You can configure the filter to be applied only to specific URLs related to your Kafka producer.

java
Copy code
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public IpRestrictionFilter ipRestrictionFilter() {
        return new IpRestrictionFilter(new AntPathRequestMatcher("/kafka/**")); // Apply to /kafka/ URLs
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(ipRestrictionFilter(), IpRestrictionFilter.class)  // Add our custom IP filter
            .authorizeRequests()
                .antMatchers("/kafka/**").authenticated()  // Protect /kafka/ endpoints
                .anyRequest().permitAll();  // Allow other requests
    }
}
Explanation
IpRestrictionFilter Class:

The filter checks incoming requests' IP addresses. If the IP is not in the ALLOWED_IPS list, it returns a 403 Forbidden response.
protectedUrlPattern:

This defines which URLs are protected by the IP restriction. For example, all URLs starting with /kafka/ are protected.
Spring Security Configuration (SecurityConfig):

Registers the custom filter IpRestrictionFilter and applies it to URLs matching /kafka/**.
Ensures that only requests from allowed IP addresses can access the Kafka producer endpoints.
Notes
Customize Allowed IPs: Modify the ALLOWED_IPS list to match the specific IPs you want to allow.
Enhance Security: Combine IP filtering with other security mechanisms such as mTLS (Mutual TLS), authentication, and authorization for better security.
Testing: Ensure to test the filter with different IP addresses to verify that it blocks or allows requests as expected.
By following these steps, you can implement an IP-based restriction filter for your Kafka producer endpoints in a Spring Boot application.












