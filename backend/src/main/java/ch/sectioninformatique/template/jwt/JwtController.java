package ch.sectioninformatique.template.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jwt")
public class JwtController {

    // Endpoint to receive the token via HTTP POST
    @PostMapping("token")
    public String receiveToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            String noToken = "No token received or incorrect token format!";
            System.out.println(noToken);
            return noToken;
        }

        // Extract the token from the "Authorization" header
        String token = authorizationHeader.substring(7); // Remove "Bearer " from the beginning

        // Log the received token to the console
        String receivedToken = "Received JWT Token: " + token;
        System.out.println(receivedToken);

        // Decode the JWT token
        DecodedJWT decodedJWT = decodeToken(token);

        // Extract and log the JWT components
        StringBuilder decodedInfo = new StringBuilder();

        if (decodedJWT != null) {
            decodedInfo.append("\nHeader: ").append(decodedJWT.getHeader()).append("\n");
            decodedInfo.append("\nPayload: ").append(decodedJWT.getPayload()).append("\n");
            decodedInfo.append("\nSignature: ").append(decodedJWT.getSignature()).append("\n");
            decodedInfo.append("\nClaims: ").append(decodedJWT.getClaims()).append("\n");
        } else {
            decodedInfo.append("Failed to decode token.");
        }

        // Optionally, return a response with the decoded information
        System.out.println(decodedInfo.toString());
        return decodedInfo.toString();
    }

    // Helper method to decode JWT token
    private DecodedJWT decodeToken(String token) {
        try {
            return JWT.decode(token);
        } catch (Exception e) {
            System.out.println("Error decoding token: " + e.getMessage());
            return null;
        }
    }
}

