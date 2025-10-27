package ch.sectioninformatique.template.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

public class TestControllerHelper {

    public static ResultActions performTest(
            MockMvc mockMvc,
            String requestTypeString,
            String endpoint,
            String token,
            MediaType contentType,
            int expectedStatus) throws Exception {

        var requestType = get(endpoint);

        if (requestTypeString.equals("GET")) {
            requestType = get(endpoint);
        } else if (requestTypeString.equals("POST")) {
            requestType = post(endpoint);
        } else if (requestTypeString.equals("PUT")) {
            requestType = put(endpoint);
        } else if (requestTypeString.equals("DELETE")) {
            requestType = delete(endpoint);
        } else {
            throw new IllegalArgumentException("Unsupported request type: " + requestTypeString);
        }

        // Perform request
        var request = mockMvc.perform(requestType
                .contentType(contentType)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));

        return request;
    }

}
