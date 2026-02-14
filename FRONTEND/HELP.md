# Mini-ERP Frontend Auth Manual Tests

1. Login success
- Open `/login`, submit valid email/password.
- Expect redirect to `/dashboard`.

2. Login wrong password (401 invalid_credentials)
- Submit valid email with wrong password.
- Expect form-level error message from backend envelope.

3. Login empty email (400 validation_failed)
- Submit form with empty email.
- Expect field-level error mapped to the `email` control from `fieldErrors`.

4. Protected route redirect
- While logged out, open `/dashboard`.
- Expect redirect to `/login?returnUrl=%2Fdashboard`.

5. 401 refresh flow without access token
- Login successfully.
- Clear in-memory access token from devtools/Angular state without clearing sessionStorage refresh token.
- Trigger a protected API call.
- Expect initial 401, single-flight refresh on `/auth/refresh`, then original request retried and succeeds.

6. Logout and expired session behavior
- Click `Logout` on `/dashboard`.
- Expect local session cleared and redirect to `/login`.
- If later a protected request happens with invalid/expired refresh token, expect redirect to `/login?reason=session_expired`.

7. 403 routing
- Trigger a backend `403 forbidden` response on any API call.
- Expect redirect to `/access-denied` without forced logout.
