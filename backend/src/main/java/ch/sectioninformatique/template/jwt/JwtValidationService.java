package ch.sectioninformatique.packbase.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;

@Service
public class JwtValidationService {

    // Inject the tenantID from the configuration file
    @Value("${tenant-id}")
    private String tenantID;

    // Azure JWKS URL template
    private static final String JWKS_URL = "https://login.microsoftonline.com/%s/discovery/v2.0/keys";

    // Method to validate JWT token
    public DecodedJWT validateToken(String token) {
        try {
            // Decode JWT to get the 'kid' from the header
            DecodedJWT decodedJWT = JWT.decode(token);
            String kid = decodedJWT.getKeyId();

            // Get the public key for validation from Azure JWKS endpoint
            RSAPublicKey publicKey = getPublicKey(kid);

            if (publicKey != null) {
                // Validate token signature using the retrieved public key
                Algorithm algorithm = Algorithm.RSA256(publicKey, null);
                return JWT.require(algorithm)
                        .withIssuer("https://login.microsoftonline.com/" + tenantID + "/v2.0")
                        .build()
                        .verify(token); // Will throw exception if the token is invalid
            } else {
                throw new Exception("Public key for kid " + kid + " not found.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error validating token: " + e.getMessage(), e);
        }
    }

    // Method to retrieve the public key using the 'kid' from Azure JWKS endpoint
    private RSAPublicKey getPublicKey(String kid) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format(JWKS_URL, tenantID); // Construct the JWKS URL

        // Fetch the JWKS (JSON Web Key Set) response from Azure
        Map<String, Object> jwksResponse = restTemplate.getForObject(url, Map.class);

        // Extract the 'keys' array from the response
        List<Map<String, Object>> keys = (List<Map<String, Object>>) jwksResponse.get("keys");

        // Loop through keys to find the matching 'kid'
        for (Map<String, Object> key : keys) {
            if (kid.equals(key.get("kid"))) {
                // Convert the modulus (n) and exponent (e) into an RSAPublicKey
                String n = (String) key.get("n");
                String e = (String) key.get("e");
                return createRSAPublicKey(n, e);
            }
        }

        // If no key with the matching 'kid' is found
        throw new RuntimeException("Public key not found for kid: " + kid);
    }

    // Helper method to create RSAPublicKey from modulus (n) and exponent (e)
    private RSAPublicKey createRSAPublicKey(String modulus, String exponent) {
        try {
            // Convert the modulus and exponent from Base64URL
            byte[] modulusBytes = java.util.Base64.getUrlDecoder().decode(modulus);
            byte[] exponentBytes = java.util.Base64.getUrlDecoder().decode(exponent);

            // Create RSAPublicKey from modulus and exponent
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            java.security.spec.RSAPublicKeySpec keySpec = new java.security.spec.RSAPublicKeySpec(
                    new java.math.BigInteger(1, modulusBytes),
                    new java.math.BigInteger(1, exponentBytes));
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Error creating RSAPublicKey: " + e.getMessage(), e);
        }
    }
}
