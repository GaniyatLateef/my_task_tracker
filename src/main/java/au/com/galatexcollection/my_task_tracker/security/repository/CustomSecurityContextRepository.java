package au.com.galatexcollection.my_task_tracker.security.repository;


import au.com.galatexcollection.my_task_tracker.security.service.TokenProviderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.DeferredSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

//Extracts the token from the header sent by the user and verifies the token.
@Component
public class CustomSecurityContextRepository implements SecurityContextRepository {

    private final TokenProviderService tokenProviderService;

    public CustomSecurityContextRepository( final TokenProviderService tokenProviderService) {
        this.tokenProviderService = tokenProviderService;
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {

        HttpServletRequest request = requestResponseHolder.getRequest();
        var bearerToken = request.getHeader(AUTHORIZATION); // Bearer eyJhbG

        if (bearerToken != null && !bearerToken.isBlank()) {
            var token = bearerToken.substring(7);

            var optUserDetails = tokenProviderService.getUserDetailsFromToken(token);

            if (optUserDetails.isPresent()) {
                var userDetails = optUserDetails.get();

                var authRequest = new UsernamePasswordAuthenticationToken(userDetails,
                    null, userDetails.getAuthorities());

                return new SecurityContextImpl(authRequest);
            }
        }

        return new SecurityContextImpl();
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request,
                            HttpServletResponse response) {}

    @Override
    public  boolean containsContext(HttpServletRequest request) {
        return false;
    }
}
