package com.back.puntoventa.app.infrastructure.persistence.supabase.client;

import com.back.puntoventa.app.common.ApiException;
import com.back.puntoventa.app.config.SupabaseProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SupabaseHttpClient {

    private final RestClient restClient;
    private final String baseUrl;
    private final String serviceKey;

    public SupabaseHttpClient(RestClient restClient, SupabaseProperties properties) {
        this.restClient = restClient;
        this.baseUrl = properties.url();
        this.serviceKey = properties.serviceKey();
    }

    public Map<String, Object> signIn(String email, String password) {
        ensureConfigured();
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        try {
            Map<?, ?> response = restClient.post()
                    .uri(baseUrl + "/auth/v1/token?grant_type=password")
                    .header("apikey", serviceKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return toMap(response);
        } catch (RestClientResponseException ex) {
            throw toApiException(ex);
        }
    }

    public Map<String, Object> getUserByToken(String token) {
        ensureConfigured();
        try {
            Map<?, ?> response = restClient.get()
                    .uri(baseUrl + "/auth/v1/user")
                    .header("apikey", serviceKey)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(Map.class);
            return toMap(response);
        } catch (RestClientResponseException ex) {
            throw toApiException(ex);
        }
    }

    public Map<String, Object> createUserByAdmin(String email, String password, String nombre, String rol) {
        ensureConfigured();
        Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("nombre", nombre);
        userMetadata.put("rol", rol);

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("email_confirm", true);
        body.put("user_metadata", userMetadata);

        try {
            Map<?, ?> response = restClient.post()
                    .uri(baseUrl + "/auth/v1/admin/users")
                    .header("apikey", serviceKey)
                    .header("Authorization", "Bearer " + serviceKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return toMap(response);
        } catch (RestClientResponseException ex) {
            throw toApiException(ex);
        }
    }

    public Map<String, Object> updateUserByAdmin(String userId, Map<String, Object> payload) {
        ensureConfigured();
        try {
            Map<?, ?> response = restClient.put()
                    .uri(baseUrl + "/auth/v1/admin/users/" + userId)
                    .header("apikey", serviceKey)
                    .header("Authorization", "Bearer " + serviceKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);
            return toMap(response);
        } catch (RestClientResponseException ex) {
            throw toApiException(ex);
        }
    }

    public void deleteUserByAdmin(String userId) {
        ensureConfigured();
        try {
            restClient.delete()
                    .uri(baseUrl + "/auth/v1/admin/users/" + userId)
                    .header("apikey", serviceKey)
                    .header("Authorization", "Bearer " + serviceKey)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw toApiException(ex);
        }
    }

    public List<Map<String, Object>> select(String table, Map<String, String> queryParams) {
        ensureConfigured();
        String uri = buildRestUri(table, queryParams);
        try {
            List<?> response = restClient.get()
                    .uri(uri)
                    .header("apikey", serviceKey)
                    .header("Authorization", "Bearer " + serviceKey)
                    .retrieve()
                    .body(List.class);
            return toMapList(response);
        } catch (RestClientResponseException ex) {
            throw toApiException(ex);
        }
    }

    public List<Map<String, Object>> insert(String table, Object body, String onConflict, String prefer) {
        ensureConfigured();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/rest/v1/" + table);
        if (StringUtils.hasText(onConflict)) {
            builder.queryParam("on_conflict", onConflict);
        }

        try {
            List<?> response = restClient.post()
                    .uri(builder.build(true).toUriString())
                    .header("apikey", serviceKey)
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("Prefer", prefer)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(List.class);
            return toMapList(response);
        } catch (RestClientResponseException ex) {
            throw toApiException(ex);
        }
    }

    public List<Map<String, Object>> update(String table, Map<String, String> queryParams, Object body) {
        ensureConfigured();
        String uri = buildRestUri(table, queryParams);
        try {
            List<?> response = restClient.patch()
                    .uri(uri)
                    .header("apikey", serviceKey)
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("Prefer", "return=representation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(List.class);
            return toMapList(response);
        } catch (RestClientResponseException ex) {
            throw toApiException(ex);
        }
    }

    public List<Map<String, Object>> deleteRows(String table, Map<String, String> queryParams) {
        ensureConfigured();
        String uri = buildRestUri(table, queryParams);
        try {
            List<?> response = restClient.delete()
                    .uri(uri)
                    .header("apikey", serviceKey)
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("Prefer", "return=representation")
                    .retrieve()
                    .body(List.class);
            return toMapList(response);
        } catch (RestClientResponseException ex) {
            throw toApiException(ex);
        }
    }

    public String uploadToStorage(String bucket, String path, byte[] body, String mimeType) {
        ensureConfigured();
        try {
            Map<?, ?> response = restClient.post()
                    .uri(baseUrl + "/storage/v1/object/" + bucket + "/" + path)
                    .header("apikey", serviceKey)
                    .header("Authorization", "Bearer " + serviceKey)
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            Map<String, Object> node = toMap(response);
            Object key = node.get("Key");
            if (key instanceof String keyString && StringUtils.hasText(keyString)) {
                return keyString;
            }
            return path;
        } catch (RestClientResponseException ex) {
            throw toApiException(ex);
        }
    }

    public void deleteFromStorage(String bucket, String path) {
        ensureConfigured();
        try {
            restClient.delete()
                    .uri(baseUrl + "/storage/v1/object/" + bucket + "/" + path)
                    .header("apikey", serviceKey)
                    .header("Authorization", "Bearer " + serviceKey)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw toApiException(ex);
        }
    }

    public String buildPublicStorageUrl(String bucket, String path) {
        ensureConfigured();
        return baseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
    }

    private void ensureConfigured() {
        if (!StringUtils.hasText(baseUrl) || !StringUtils.hasText(serviceKey)) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "SUPABASE_URL y SUPABASE_SERVICE_KEY deben estar configurados");
        }
    }

    private String buildRestUri(String table, Map<String, String> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/rest/v1/" + table);
        queryParams.forEach(builder::queryParam);
        return builder.build(true).toUriString();
    }

    private Map<String, Object> toMap(Map<?, ?> response) {
        Map<String, Object> converted = new HashMap<>();
        if (response == null) {
            return converted;
        }

        response.forEach((key, value) -> {
            if (key instanceof String stringKey) {
                converted.put(stringKey, value);
            }
        });
        return converted;
    }

    private List<Map<String, Object>> toMapList(List<?> response) {
        if (response == null) {
            return List.of();
        }

        return response.stream()
                .filter(Map.class::isInstance)
                .map(item -> toMap((Map<?, ?>) item))
                .toList();
    }

    private ApiException toApiException(RestClientResponseException exception) {
        String message = exception.getStatusText();
        String body = exception.getResponseBodyAsString();
        if (StringUtils.hasText(body)) {
            message = body;
        }

        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ApiException(status, message);
    }
}
