package com.fragmind.api;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryBackend {
    public record User(String id, String email, String password, String displayName, String defaultWorkspaceId, String status, boolean emailVerified, String createdAt, String updatedAt) {}
    public record Workspace(String id, String userId, String name, String description, String status, String createdAt, String updatedAt) {}
    public record Fragment(String id, String userId, String workspaceId, String selectedText, Map<String,Object> source, String userNote, String status, String extractedAt, String createdAt, String updatedAt) {}
    public record Tag(String id, String userId, String workspaceId, String name, String createdAt, String updatedAt) {}
    public record AiOperation(String id, String userId, String workspaceId, String type, String status, Map<String,Object> result, String createdAt, String updatedAt) {}

    Map<String, User> users = new ConcurrentHashMap<>();
    Map<String, String> usersByEmail = new ConcurrentHashMap<>();
    Map<String, Workspace> workspaces = new ConcurrentHashMap<>();
    Map<String, Fragment> fragments = new ConcurrentHashMap<>();
    Map<String, Tag> tags = new ConcurrentHashMap<>();
    Map<String, AiOperation> aiOperations = new ConcurrentHashMap<>();

    private static String id() { return UUID.randomUUID().toString(); }
    private static String now() { return Instant.now().toString(); }
    private static String token(String userId, String kind) { return Base64.getUrlEncoder().encodeToString((userId+":"+kind+":"+now()).getBytes()); }

    public Map<String, Object> register(String email, String password, String displayName) {
        if (usersByEmail.containsKey(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED");
        if (!email.contains("@") || password.length() < 8) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        String userId = id(); String wsId = id(); String t = now();
        User u = new User(userId, email, password, displayName, wsId, "ACTIVE", false, t, t);
        Workspace ws = new Workspace(wsId, userId, "Inbox", null, "ACTIVE", t, t);
        users.put(userId, u); usersByEmail.put(email, userId); workspaces.put(wsId, ws);
        return Map.of("user", u, "accessToken", token(userId, "access"), "refreshToken", token(userId, "refresh"));
    }

    public Map<String, Object> login(String email, String password) {
        String userId = usersByEmail.get(email);
        if (userId == null || !users.get(userId).password.equals(password)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        return Map.of("user", users.get(userId), "accessToken", token(userId, "access"), "refreshToken", token(userId, "refresh"));
    }

    public User requireUser(String bearer) {
        if (bearer == null || !bearer.startsWith("Bearer ")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED");
        String[] parts = new String(Base64.getUrlDecoder().decode(bearer.substring(7))).split(":");
        if (parts.length < 2 || !users.containsKey(parts[0])) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        return users.get(parts[0]);
    }

    public Workspace createWorkspace(String userId, String name, String description) {
        String t=now(); Workspace ws = new Workspace(id(), userId, name, description, "ACTIVE", t, t); workspaces.put(ws.id, ws); return ws;
    }

    public List<Workspace> listWorkspaces(String userId) {
        return workspaces.values().stream().filter(w -> w.userId.equals(userId) && !w.status.equals("DELETED")).toList();
    }

    public Fragment createFragment(String userId, String workspaceId, String selectedText, Map<String,Object> source, String userNote) {
        Workspace ws = workspaces.get(workspaceId);
        if (ws == null || !ws.userId.equals(userId) || !ws.status.equals("ACTIVE")) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "WORKSPACE_NOT_FOUND");
        Object rawUrl = source.get("url"); if (rawUrl == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        source.putIfAbsent("domain", URI.create(rawUrl.toString()).getHost());
        String t=now(); Fragment f = new Fragment(id(), userId, workspaceId, selectedText, source, userNote, "ACTIVE", t, t, t); fragments.put(f.id, f); return f;
    }

    public Tag createTag(String userId, String workspaceId, String name) {
        boolean exists = tags.values().stream().anyMatch(t -> t.userId.equals(userId) && t.workspaceId.equals(workspaceId) && t.name.equalsIgnoreCase(name));
        if (exists) throw new ResponseStatusException(HttpStatus.CONFLICT, "TAG_ALREADY_EXISTS");
        String t=now(); Tag tag = new Tag(id(), userId, workspaceId, name, t, t); tags.put(tag.id, tag); return tag;
    }

    public AiOperation createAiOp(String userId, String workspaceId, String type) {
        long count = fragments.values().stream().filter(f -> f.workspaceId.equals(workspaceId) && f.status.equals("ACTIVE")).count();
        String t=now(); AiOperation op = new AiOperation(id(), userId, workspaceId, type, "COMPLETED", Map.of("summary", "Generated "+type, "fragmentsCount", count), t, t);
        aiOperations.put(op.id, op); return op;
    }

    public AiOperation getAiOp(String userId, String opId) {
        AiOperation op = aiOperations.get(opId);
        if (op == null || !op.userId.equals(userId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AI_OPERATION_NOT_FOUND");
        return op;
    }
}
