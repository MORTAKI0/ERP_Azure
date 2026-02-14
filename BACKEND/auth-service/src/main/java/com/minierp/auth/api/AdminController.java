package com.minierp.auth.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AdminController {

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/admin/ping")
    public Map<String, Boolean> ping() {
        return Map.of("ok", true);
    }

    /*
    Manual tests (assumes app on localhost:8080):

    1) OWNER token -> 200
       curl:
       curl -i -H "Authorization: Bearer <OWNER_TOKEN>" http://localhost:8080/admin/ping

       PowerShell:
       Invoke-RestMethod -Method GET -Uri "http://localhost:8080/admin/ping" -Headers @{ Authorization = "Bearer <OWNER_TOKEN>" }

    2) token with roles != OWNER -> 403
       curl:
       curl -i -H "Authorization: Bearer <NON_OWNER_TOKEN>" http://localhost:8080/admin/ping

       PowerShell:
       try { Invoke-RestMethod -Method GET -Uri "http://localhost:8080/admin/ping" -Headers @{ Authorization = "Bearer <NON_OWNER_TOKEN>" } } catch { $_.Exception.Response.StatusCode.value__ }

    3) Temporarily change roles in DB for testing (roles column is TEXT):
       -- grant OWNER
       UPDATE users SET roles = 'OWNER' WHERE email = 'owner@minierp.local';

       -- remove OWNER (example: MANAGER only)
       UPDATE users SET roles = 'MANAGER' WHERE email = 'owner@minierp.local';

       -- re-login after each role change to mint a token with updated roles
    */
}
