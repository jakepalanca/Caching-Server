// ----- AttestationValidator.java -----
package jakepalanca.caching.server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code AttestationValidator} class is responsible for validating
 * iOS App Attest tokens to ensure that incoming requests originate from
 * the authorized iOS application.
 *
 * <p>This class fetches Apple's public keys, caches them, and uses them to
 * verify the signatures and claims of JWT tokens provided by the iOS app.</p>
 */
public class AttestationValidator {

    private static final Logger logger = LoggerFactory.getLogger(AttestationValidator.class);

    // URL to fetch Apple's public keys for JWT verification
    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";

    // Cache to store fetched public keys, keyed by 'kid'
    private final Map<String, RSAPublicKey> publicKeysCache = new HashMap<>();

    // HTTP client for making requests to Apple's servers
    private final CloseableHttpClient httpClient;

    // ObjectMapper for parsing JSON responses
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructs a new {@code AttestationValidator} instance and initializes the HTTP client.
     */
    public AttestationValidator() {
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Validates the provided attestation token to ensure it originates from the authorized iOS app.
     *
     * @param attestationToken the JWT attestation token received from the iOS app
     * @return {@code true} if the token is valid and verified; {@code false} otherwise
     */
    public boolean validateAttestationToken(String attestationToken) {
        try {
            // Decode the JWT without verification to extract the header
            DecodedJWT decodedJWT = JWT.decode(attestationToken);
            String keyId = decodedJWT.getKeyId();

            if (keyId == null || keyId.isEmpty()) {
                logger.warn("Attestation token is missing 'kid' in header.");
                return false;
            }

            RSAPublicKey publicKey = publicKeysCache.get(keyId);

            // If the public key is not cached, fetch and cache it
            if (publicKey == null) {
                publicKey = fetchAndCachePublicKey(keyId);
                if (publicKey == null) {
                    logger.warn("Public key with kid {} could not be found.", keyId);
                    return false;
                }
            }

            // Create an Algorithm instance with the fetched public key
            Algorithm algorithm = Algorithm.RSA256(publicKey, null);

            // Verify the token's signature and claims
            algorithm.verify(decodedJWT);

            String issuer = decodedJWT.getIssuer();
            if (!"https://appleid.apple.com".equals(issuer)) {
                logger.warn("Invalid issuer: {}", issuer);
                return false;
            }

            // Check token expiration
            Date expiresAt = decodedJWT.getExpiresAt();
            if (expiresAt == null || expiresAt.before(Date.from(Instant.now()))) {
                logger.warn("Attestation token has expired.");
                return false;
            }

            // If all checks pass, the token is valid
            return true;

        } catch (Exception e) {
            logger.error("Error validating attestation token: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Fetches Apple's public keys and caches them for future validations.
     *
     * @return {@code true} if keys were successfully fetched and cached; {@code false} otherwise
     */
    private boolean fetchAndCachePublicKeys() {
        HttpGet request = new HttpGet(APPLE_KEYS_URL);
        request.addHeader("Accept", "application/json");

        HttpClientResponseHandler<Boolean> responseHandler = response -> {
            int statusCode = response.getCode();
            if (statusCode == 200) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                JsonNode keysNode = objectMapper.readTree(responseBody).get("keys");
                if (keysNode != null && keysNode.isArray()) {
                    for (JsonNode keyNode : keysNode) {
                        String kid = keyNode.get("kid").asText();
                        String kty = keyNode.get("kty").asText();
                        String alg = keyNode.get("alg").asText();
                        String use = keyNode.get("use").asText();
                        String n = keyNode.get("n").asText();
                        String e = keyNode.get("e").asText();

                        if ("RSA".equals(kty) && "sig".equals(use)) {
                            RSAPublicKey publicKey = constructRSAPublicKey(n, e);
                            if (publicKey != null) {
                                publicKeysCache.put(kid, publicKey);
                            }
                        }
                    }
                    logger.info("Successfully fetched and cached Apple's public keys.");
                    return true;
                } else {
                    logger.warn("Invalid keys format received from Apple.");
                    return false;
                }
            } else {
                logger.warn("Failed to fetch Apple's public keys. HTTP Status Code: {}", statusCode);
                return false;
            }
        };

        try {
            return httpClient.execute(request, responseHandler);
        } catch (IOException e) {
            logger.error("IOException while fetching Apple's public keys: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Fetches and caches the public key corresponding to the provided Key ID (kid).
     *
     * @param kid the Key ID to fetch the public key for
     * @return the {@link RSAPublicKey} if found and successfully parsed; {@code null} otherwise
     */
    private RSAPublicKey fetchAndCachePublicKey(String kid) {
        // Fetch all keys and cache them
        if (fetchAndCachePublicKeys()) {
            return publicKeysCache.get(kid);
        }
        return null;
    }

    /**
     * Constructs an {@link RSAPublicKey} from the provided modulus and exponent.
     *
     * @param n the modulus value encoded in Base64URL
     * @param e the exponent value encoded in Base64URL
     * @return the constructed {@link RSAPublicKey} or {@code null} if construction fails
     */
    private RSAPublicKey constructRSAPublicKey(String n, String e) {
        try {
            byte[] modulusBytes = Base64.getUrlDecoder().decode(n);
            byte[] exponentBytes = Base64.getUrlDecoder().decode(e);

            java.math.BigInteger modulus = new java.math.BigInteger(1, modulusBytes);
            java.math.BigInteger exponent = new java.math.BigInteger(1, exponentBytes);

            java.security.spec.RSAPublicKeySpec keySpec = new java.security.spec.RSAPublicKeySpec(modulus, exponent);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception ex) {
            logger.error("Error constructing RSAPublicKey: {}", ex.getMessage(), ex);
            return null;
        }
    }
}
