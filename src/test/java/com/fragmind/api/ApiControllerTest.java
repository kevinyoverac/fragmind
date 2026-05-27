package com.fragmind.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Test
    void fullFlow() throws Exception {
        JsonNode reg = om.readTree(mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"u@example.com\",\"password\":\"Password123!\",\"displayName\":\"User\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        String token = reg.path("data").path("accessToken").asText();

        JsonNode ws = om.readTree(mvc.perform(post("/api/v1/workspaces").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Research\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        String wsId = ws.path("data").path("id").asText();

        mvc.perform(get("/api/v1/workspaces").header("Authorization", "Bearer " + token)).andExpect(status().isOk());

        mvc.perform(post("/api/v1/fragments").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"workspaceId\":\""+wsId+"\",\"selectedText\":\"abc\",\"source\":{\"url\":\"https://example.com/a\"}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.source.domain").value("example.com"));

        mvc.perform(post("/api/v1/tags").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"workspaceId\":\""+wsId+"\",\"name\":\"backend\"}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/v1/tags").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"workspaceId\":\""+wsId+"\",\"name\":\"Backend\"}"))
                .andExpect(status().isConflict());

        JsonNode op = om.readTree(mvc.perform(post("/api/v1/ai-operations").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"workspaceId\":\""+wsId+"\",\"type\":\"WORKSPACE_SUMMARY\"}"))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString());
        String opId = op.path("data").path("id").asText();

        mvc.perform(get("/api/v1/ai-operations/"+opId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }
}
