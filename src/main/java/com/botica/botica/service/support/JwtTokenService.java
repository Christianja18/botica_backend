package com.botica.botica.service.support;

import com.botica.botica.exception.UnauthorizedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int MIN_SECRET_BYTES = 32;
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> CLAIMS_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    @Value("${botica.auth.jwt.secret:}")
    private String configuredSecret;

    private byte[] signingKey;

    @PostConstruct
    void initializeSigningKey() {
        if (configuredSecret != null && !configuredSecret.isBlank()) {
            signingKey = configuredSecret.getBytes(StandardCharsets.UTF_8);
            if (signingKey.length < MIN_SECRET_BYTES) {
                throw new IllegalStateException("BOTICA_JWT_SECRET debe tener al menos 32 bytes");
            }
            return;
        }

        signingKey = new byte[64];
        new SecureRandom().nextBytes(signingKey);
        log.warn("BOTICA_JWT_SECRET no configurado; usando secreto efimero. Los tokens se invalidan al reiniciar.");
    }

    public String generateToken(Integer userId, Instant expiresAt) {
        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", String.valueOf(userId));
        payload.put("iat", Instant.now().getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public JwtClaims validateToken(String token) {
        String[] parts = token == null ? new String[0] : token.split("\\.");
        if (parts.length != 3) {
            throw new UnauthorizedException("El formato del token es invalido");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);
        if (!constantTimeEquals(expectedSignature, parts[2])) {
            throw new UnauthorizedException("La firma del token es invalida");
        }

        Map<String, Object> claims = decodeClaims(parts[1]);
        String subject = String.valueOf(claims.get("sub"));
        long expiresAt = readLongClaim(claims, "exp");

        if (Instant.ofEpochSecond(expiresAt).isBefore(Instant.now())) {
            throw new UnauthorizedException("La sesion expiro");
        }

        try {
            return new JwtClaims(Integer.parseInt(subject), Instant.ofEpochSecond(expiresAt));
        } catch (NumberFormatException ex) {
            throw new UnauthorizedException("El token no contiene un usuario valido");
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo generar el token", ex);
        }
    }

    private Map<String, Object> decodeClaims(String encodedPayload) {
        try {
            return objectMapper.readValue(BASE64_URL_DECODER.decode(encodedPayload), CLAIMS_TYPE);
        } catch (RuntimeException | IOException ex) {
            throw new UnauthorizedException("El token no contiene claims validos");
        }
    }

    private long readLongClaim(Map<String, Object> claims, String claimName) {
        Object value = claims.get(claimName);
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (RuntimeException ex) {
            throw new UnauthorizedException("El token no contiene expiracion valida");
        }
    }

    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(signingKey, HMAC_ALGORITHM));
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo firmar el token", ex);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
        byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
        if (leftBytes.length != rightBytes.length) {
            return false;
        }
        int result = 0;
        for (int index = 0; index < leftBytes.length; index++) {
            result |= leftBytes[index] ^ rightBytes[index];
        }
        return result == 0;
    }

    public record JwtClaims(Integer userId, Instant expiresAt) {}
}
