package au.com.galatexcollection.my_task_tracker.security.service;

import au.com.galatexcollection.my_task_tracker.model.TokenResponse;
import au.com.galatexcollection.my_task_tracker.security.CustomUserDetails;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;


@Service
public class TokenProviderService {
    private static final Logger LOG = LoggerFactory.getLogger(TokenProviderService.class);

    private final CustomUserDetailsService userDetailsService;
    private final long expiryInMs; //ms (5min)
    private final Algorithm algorithm;

    private static final String ISSUER_URI = "https://galatexcollection.com.au";

    public TokenProviderService(CustomUserDetailsService userDetailsService,
                                @Value("${app.config.token.secretKey}") String secretkey,
                                @Value("${app.config.token.expiryInMs}") long expiryInMs) {
        this.userDetailsService = userDetailsService;
        this.expiryInMs = expiryInMs;
        this.algorithm = Algorithm.HMAC256(secretkey);
    }

    public TokenResponse generateToken(Authentication authentication) {
        try {
            if (authentication.isAuthenticated()) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                Date now = new Date();
                Date expireAt = new Date(now.getTime() + expiryInMs);

                String token = JWT.create()
                    .withSubject(String.valueOf(userDetails.getId()))
                    .withIssuer(ISSUER_URI)
                    .withClaim("username", userDetails.getUsername())
                    .withIssuedAt(now)
                    .withExpiresAt(expireAt)
                    .sign(algorithm);

                return new TokenResponse(token, "Bearer", expiryInMs / 1000);
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        }

        return null;
    }


    public Optional<CustomUserDetails> getUserDetailsFromToken(String token) {
        try {

            JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(TokenProviderService.ISSUER_URI)
                .build();

            DecodedJWT decodedJWT = verifier.verify(token);

            String subject = decodedJWT.getSubject();
            String username = decodedJWT.getClaim("username").asString();

            if (subject != null && username != null) {
                var optUser = userDetailsService.getUserById(subject);
                if (optUser.isPresent()) {
                    var user = optUser.get();
                    if (user.getUsername().equalsIgnoreCase(username)) {
                        return Optional.of(CustomUserDetails.createUserDetails(user));
                    }
                }
            }
        }catch (Exception ex) {
            LOG.error(ex.getMessage());
        }

        return Optional.empty();
    }
}


