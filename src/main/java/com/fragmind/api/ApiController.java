package com.fragmind.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Validated
public class ApiController {
    private final InMemoryBackend backend;
    public ApiController(InMemoryBackend backend) { this.backend = backend; }

    public record RegisterReq(@Email String email, @Size(min = 8) String password, String displayName) {}
    public record LoginReq(@Email String email, @NotBlank String password) {}
    public record WorkspaceReq(@NotBlank String name, String description) {}
    public record FragmentReq(@NotBlank String workspaceId, @NotBlank String selectedText, Map<String,Object> source, String userNote) {}
    public record TagReq(@NotBlank String workspaceId, @NotBlank String name) {}
    public record AiReq(@NotBlank String workspaceId, @NotBlank String type) {}

    @PostMapping("/auth/register") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> register(@RequestBody RegisterReq req){ return Map.of("data", backend.register(req.email(), req.password(), req.displayName())); }
    @PostMapping("/auth/login")
    public Map<String,Object> login(@RequestBody LoginReq req){ return Map.of("data", backend.login(req.email(), req.password())); }

    @PostMapping("/workspaces") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> createWorkspace(@RequestHeader(value="Authorization", required=false) String auth, @RequestBody WorkspaceReq req){
        var user = backend.requireUser(auth); return Map.of("data", backend.createWorkspace(user.id(), req.name(), req.description())); }

    @GetMapping("/workspaces")
    public Map<String,Object> listWorkspaces(@RequestHeader(value="Authorization", required=false) String auth){
        var user = backend.requireUser(auth); List<?> data = backend.listWorkspaces(user.id());
        return Map.of("data", data, "pagination", Map.of("page",0,"size",data.size(),"totalElements",data.size(),"totalPages",1,"hasNext",false,"hasPrevious",false));
    }

    @PostMapping("/fragments") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> createFragment(@RequestHeader(value="Authorization", required=false) String auth, @RequestBody FragmentReq req){
        var user = backend.requireUser(auth); return Map.of("data", backend.createFragment(user.id(), req.workspaceId(), req.selectedText(), req.source(), req.userNote())); }

    @PostMapping("/tags") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> createTag(@RequestHeader(value="Authorization", required=false) String auth, @RequestBody TagReq req){
        var user = backend.requireUser(auth); return Map.of("data", backend.createTag(user.id(), req.workspaceId(), req.name())); }

    @PostMapping("/ai-operations")
    public ResponseEntity<Map<String,Object>> createAiOp(@RequestHeader(value="Authorization", required=false) String auth, @RequestBody AiReq req){
        var user = backend.requireUser(auth); return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("data", backend.createAiOp(user.id(), req.workspaceId(), req.type()))); }

    @GetMapping("/ai-operations/{id}")
    public Map<String,Object> getAiOp(@RequestHeader(value="Authorization", required=false) String auth, @PathVariable String id){
        var user = backend.requireUser(auth); return Map.of("data", backend.getAiOp(user.id(), id)); }
}
