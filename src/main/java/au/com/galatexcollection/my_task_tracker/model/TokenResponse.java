package au.com.galatexcollection.my_task_tracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenResponse (
    @JsonProperty(value = "access_token") String accessToken,
    @JsonProperty(value = "token_type") String tokenType,
    @JsonProperty(value = "expires_in") long expiresIn){ }
