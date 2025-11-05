feat(auth): add optional social login (Google/Naver) with JWT + profile upload (flagged)

Added
- src/main/resources/application.yml: feature flags (app.social.enabled), JWT and upload defaults
- src/main/resources/application-social.yml: social profile with OAuth2 client configs
- build.gradle: add spring-boot-starter-oauth2-client
- src/main/java/com/busanit501/__team_back/config/WebConfig.java: static mapping /public/** → file:${app.upload.dir}
- src/main/java/com/busanit501/__team_back/config/SocialSecurityConfig.java: social-only security filter chain with oauth2Login
- src/main/java/com/busanit501/__team_back/security/oauth/CustomOAuth2UserService.java: provider mapping + upsert
- src/main/java/com/busanit501/__team_back/security/oauth/OAuth2SuccessHandler.java: issue JWT and redirect/JSON
- src/main/java/com/busanit501/__team_back/security/jwt/JwtService.java: social JWT creator/parser using app.jwt.*
- src/main/java/com/busanit501/__team_back/controller/AuthController.java: GET /auth/me
- src/main/java/com/busanit501/__team_back/controller/HealthController.java: GET /health
- src/main/java/com/busanit501/__team_back/controller/ProfileController.java: POST /users/me/profile-image
- src/main/java/com/busanit501/__team_back/service/user/ProviderImageCacheService.java: optional provider image caching

Changed
- src/main/java/com/busanit501/__team_back/config/SecurityConfig.java: activate only when app.social.enabled=false
- src/main/java/com/busanit501/__team_back/domain/user/APIUser.java: add provider/profile fields, role, updatedAt; dynamic authorities
- src/main/java/com/busanit501/__team_back/domain/user/UserRepository.java: add findByProviderId
- README.md: endpoints and PowerShell test commands

Notes
- Existing username/password login preserved. Social flow enabled only with profile `social`.
- JWT for social uses app.jwt.secret and includes claims: sub(mid), name, role, img.
- Static files served at /public/** from ${app.upload.dir}.

