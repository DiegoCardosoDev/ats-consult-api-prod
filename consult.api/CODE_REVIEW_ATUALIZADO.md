# Revisão de código atualizada — `consult.api`

Data: 2026-10-22  
Escopo: backend (`consult.api`) e alterações feitas para deploy (perfis, CORS, cookie, SPA e frontend Angular).

Este documento substitui o retrato anterior de `CODE_REVIEW.md`. Vários achados críticos foram corrigidos; outros continuam abertos e alguns riscos novos foram introduzidos pelas mudanças de deploy.

---

## 1. Veredito

O projeto melhorou bastante nos pontos que bloqueavam o startup e o deploy:

- O segredo JWT não tem mais fallback público e falha corretamente no startup.
- O admin de produção deixou de ser criado com senha fixa quando o perfil `prod` está ativo.
- Login inválido retorna 401 (testado em runtime).
- Registro não devolve mais o JWT no corpo; usa cookie HttpOnly.
- CORS foi centralizado e retirado dos controllers.
- O TTL padrão do JWT e do cookie foi alinhado em 8 horas.
- A validação de tecnologias deixou de carregar a tabela inteira.

Ainda **não considero o projeto pronto para produção**. Permanecem riscos relevantes de autorização, CSRF, revogação de token, rate limiting, N+1, transações e testes. Para uma demonstração controlada a um cliente, o deploy preparado é aceitável com as ressalvas da seção 6.

---

## 2. Status dos achados anteriores

Legenda: ✅ resolvido · 🟡 parcial · ❌ em aberto

| # | Achado anterior | Status | Evidência atual |
|---|---|---|---|
| 1 | Segredo JWT com fallback hardcoded | ✅ | `application.properties:21` usa `${JWT_SECRET:}`; `JwtService.java:27-45` valida presença, Base64 e mínimo de 32 bytes |
| 2 | Admin inicial com senha fixa em produção | 🟡 | `application-prod.properties:15-16` exige/envia `APP_ADMIN_*` e `InitialDataConfig` só cria com ambos definidos. Porém o perfil padrão é `dev` (`application.properties:4`) e o dev ainda tem `123456` (`application-dev.properties:4-5`) |
| 3 | Erros de autenticação retornam 500 | ✅ (auth) / 🟡 (demais) | `ApiExceptionHandler.java:47` mapeia `AuthenticationException` para 401 e `:61` mapeia `AccessDeniedException` para 403. Login inválido testado retornou 401. Ainda faltam handlers para `DataIntegrityViolationException`, `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException` e `NoResourceFoundException` |
| 4 | Registro devolve JWT no JSON | ✅ | `AuthController.java:72-79` define cookie HttpOnly e retorna apenas mensagem |
| 5 | CSRF desabilitado com autenticação por cookie | ❌ | `SecurityAuthConfig.java:58` continua com `csrf(AbstractHttpConfigurer::disable)` |
| 6 | Logout não invalida o JWT no servidor | 🟡 | O frontend agora chama `POST /auth/logout`; o servidor ainda apenas apaga o cookie (`AuthController.java:57-70`). O JWT continua válido até expirar |
| 7 | POST de tecnologias liberado para qualquer autenticado | ❌ | `SecurityAuthConfig.java:67-68` libera `/api/v1/technologies/**` para qualquer autenticado |
| 8 | Ausência de rate limiting/lockout | ❌ | Nenhuma dependência ou filtro de rate limit no projeto |
| 9 | Token 15 min × cookie 24 h | ✅ (padrão) | `application.properties:22` usa 8 h para o JWT e `:27` usa 8 h para o cookie. Ainda são duas variáveis independentes e podem divergir se alteradas separadamente |
| 10 | Exceções de autenticação engolidas pelo handler genérico | ✅ (auth) / 🟡 (geral) | Auth e acesso negado têm handlers específicos; o fallback `Exception` continua em `ApiExceptionHandler.java:92` |
| 11 | `findAll()` em memória para validar tecnologias | ✅ | `TechnologyResolver.java:26-41` usa `findAllByIds`, deduplica IDs e falha com IDs inexistentes |
| 12 | N+1 na montagem de tecnologias das vagas | ❌ | `JobPositionRepositoryAdapter.java:62-68` ainda consulta tecnologias uma vez por vaga; em `findAll()` isso é 1 + N |
| 13 | Duplicidade de mapeamento create/update | ❌ | `JobPositionController.java:40` usa `request.toDomain()` e `:76` usa `webMapper.toDomain()`; resposta também alterna `fromDomain` e `webMapper.toResponse` |
| 14 | Domínio anêmico e mutável | ❌ | `JobPosition.java` continua com `@Setter`; regras como status inicial e salário mínimo/máximo não estão no domínio |
| 15 | `User` acumula entidade JPA + `UserDetails` | ❌ | `User.java:21` continua implementando `UserDetails` diretamente |
| 16 | CORS duplicado e hardcoded | ✅ | `@CrossOrigin` removido dos controllers; configuração única em `SecurityAuthConfig.java:38-51`, com origem vinda de `app.cors.allowed-origins` |
| 17 | Sem paginação nas listagens | ❌ | `JobPositionController.java:52-69` e `TechnologyController` continuam retornando `List` completa |
| 18 | Testes praticamente inexistentes | ❌ | Só existe `ApplicationTests.java:6-11`; não há `@ActiveProfiles("test")`, apesar de existir `application-test.properties` |
| 19 | Segredo decodificado a cada requisição | ✅ | `JwtService.java:45` cria a chave uma vez no `@PostConstruct` e `:87-89` apenas retorna a chave cacheada |
| 20 | MapStruct/compiler plugin antigos | ❌ | `pom.xml:43` e `:157` usam MapStruct 1.5.5; `pom.xml:135-139` usa compiler plugin 3.11.0 com `source`/`target` |
| 21 | Pacote `com.br.ats` invertido | ❌ | `pom.xml:11` continua `com.br.ats` |

### Itens menores da revisão anterior

| Achado | Status | Evidência |
|---|---|---|
| Filtro JWT silencia todos os erros | ❌ | `JwtAuthenticationFilter.java:64-66` captura `Exception` sem log |
| Token é parseado mais de uma vez por requisição | 🟡 | `JwtService.java:57-84`: `extractUsername` e `isTokenExpired` parseiam novamente; a chave já está cacheada, mas o parsing continua redundante |
| Algoritmo JWT implícito | ❌ | `JwtService.java:53` usa `signWith(getSignInKey())`; o algoritmo depende do tamanho da chave |
| Token apenas via cookie | ❌ | `JwtAuthenticationFilter.java:33-40` só procura `auth_token`; não aceita `Authorization: Bearer` |
| Cookie sem `__Host-` e sem validação de SameSite | 🟡 | Perfis e `Secure` em prod foram adicionados, mas o nome `auth_token` e o valor livre de `COOKIE_SAME_SITE` continuam |
| Política de senha/e-mail fraca | ❌ | `AuthRequest.java:3-6` não tem validação; não há normalização de e-mail; `AuthService.java:22-23` usa `RuntimeException` para e-mail duplicado |
| `User` sem estados e `role` nullable | ❌ | `User.java:33-34` não tem `nullable=false`; `getAuthorities()` em `:37-38` pode lançar NPE se `role` for nulo |
| Sem defesa em profundidade (`@EnableMethodSecurity`) | ❌ | Nenhum `@EnableMethodSecurity`/`@PreAuthorize` no projeto |
| 401/403 sem payload `ApiError` no filter chain | ❌ | Não há `authenticationEntryPoint`/`accessDeniedHandler`; API sem auth retornou 403 no teste |
| `httpBasic`/`formLogin` não desabilitados explicitamente | ❌ | `SecurityAuthConfig.java:55-76` não declara `.httpBasic(disable)`/`.formLogin(disable)` |
| `AuthRequest` sem validação | ❌ | `AuthRequest.java:3-6`; login em `AuthController.java:42` não usa `@Valid` |
| `ApiExceptionHandler` genérico demais | 🟡 | Auth, validação e `IllegalArgumentException` têm handlers; erros de integridade/JSON/tipo ainda caem em 500 |
| `DomainException` não tratado | ❌ | `ApiExceptionHandler.java` só trata as exceções concretas de not found |
| `ApiError` sem detalhes/traceId | ❌ | `ApiError.java` tem apenas timestamp, status, error, message e path |
| Versionamento `/auth` fora de `/api/v1` | ❌ | `AuthController.java` usa `/auth` |
| `@ElementCollection` EAGER | ❌ | `JobPositionEntity.java:63` usa `FetchType.EAGER` |
| Mapper cria stubs e adapter substitui | ❌ | `JobPositionMapper.java` cria `Technology` só com ID; adapter substitui em `:62-68` |
| Falta de transações | ❌ | Nenhum `@Transactional` no projeto; update/delete fazem leitura + escrita sem transação explícita |
| Sem concorrência otimista/auditoria | ❌ | Nenhum `@Version`, `createdAt` ou `updatedAt` |
| Constraints de banco incompletas | ❌ | Sem `min_salary <= max_salary`, sem unicidade de tecnologia por nome/pai, sem `CHECK` em `role` |
| `open-in-view` habilitado | ❌ | `spring.jpa.open-in-view` não está definido como `false` |
| Sem Actuator/observabilidade | ❌ | Sem `spring-boot-starter-actuator`, métricas ou correlation ID |
| Empacotamento WAR | ❌ | `pom.xml:14`; se não há Tomcat externo, `jar` simplificaria |

---

## 3. Riscos novos introduzidos pelas alterações de deploy

### 3.1 ALTO — perfil padrão é `dev`

`application.properties:4` define:

```properties
spring.profiles.default=dev
```

Se a aplicação for iniciada sem `SPRING_PROFILES_ACTIVE=prod` (por exemplo, `java -jar app.war` direto no servidor), ela usa o perfil de desenvolvimento:

- cria o admin padrão `admin@tnconsultoria.com` / `123456` (`application-dev.properties:4-5`);
- sobe com `COOKIE_SECURE=false` por padrão (`application.properties:25`);
- pode usar o Postgres local (`application.properties:10-12`).

Os caminhos oficiais de deploy (Dockerfile, Render e compose) definem `prod`, então o risco está em execuções manuais ou futuras plataformas. Recomendação: tornar `dev` explícito e usar `prod` como padrão, ou remover o admin padrão do dev e exigir `APP_ADMIN_*` sempre.

### 3.2 MÉDIO — `anyRequest().permitAll()`

`SecurityAuthConfig.java:70` libera qualquer rota que não comece por `/api` ou `/auth`. Hoje isso serve para os arquivos estáticos e rotas do Angular, mas qualquer endpoint futuro fora desses prefixos nasce público.

Recomendação: liberar apenas os caminhos estáticos/SPA conhecidos e usar `anyRequest().denyAll()` como fallback.

### 3.3 BAIXO — `COOKIE_SAME_SITE` é texto livre

`AuthController.java:35-36` e `:88` aceitam qualquer string. Um valor inválido (`strict`, `none`, vazio) pode falhar em runtime ao montar o cookie. Validar como `Strict|Lax|None` e exigir `Secure` quando for `None`.

### 3.4 BAIXO — TTL do JWT e do cookie são independentes

`JWT_EXPIRATION_MS` (`application.properties:22`) e `COOKIE_MAX_AGE_SECONDS` (`:27`) têm o mesmo padrão, mas podem divergir. O ideal é derivar o `maxAge` do cookie da expiração do JWT, ou validar no startup que os valores são coerentes.

### 3.5 BAIXO — rotas SPA explícitas

`SpaForwardController.java:9` lista apenas `/login`, `/home`, `/jobs` e `/jobs/**`. Qualquer rota Angular nova precisa ser adicionada manualmente para funcionar em refresh direto. Se a intenção é um SPA, vale um fallback genérico que exclua `/api`, `/auth`, `/assets` e arquivos com extensão.

---

## 4. O que foi efetivamente corrigido

1. **JWT sem fallback público**: `application.properties:21` e `JwtService.java:27-45`.
2. **Admin de produção por variável de ambiente**: `application-prod.properties:15-16` e `InitialDataConfig`.
3. **401 para credenciais inválidas**: `ApiExceptionHandler.java:47-59`; testado em runtime.
4. **Registro sem token no corpo**: `AuthController.java:72-79`.
5. **CORS centralizado**: `SecurityAuthConfig.java:38-51`; `@CrossOrigin` removido dos três controllers.
6. **Cookie de produção com `Secure` e SameSite configurável**: `AuthController.java:82-89`.
7. **TTL alinhado por padrão**: 8 h para token e cookie.
8. **Validação de tecnologias eficiente**: `TechnologyResolver.java:26-41`.
9. **Chave JWT cacheada**: `JwtService.java:45,87-89`.
10. **Logout do frontend chama a API**: `ats-frontend/src/app/services/auth.service.ts` (o endpoint ainda não revoga o token).

---

## 5. Pendências por severidade

### P0 — antes de entregar ao cliente

1. Eliminar o risco do perfil `dev` como padrão (seção 3.1).
2. Restringir `POST /api/v1/technologies` a `ADMIN`/`TECH_RECRUITER`.
3. Adicionar rate limiting em `/auth/login` e `/auth/register`.
4. Decidir CSRF: manter desabilitado apenas com same-site documentado ou habilitar `CookieCsrfTokenRepository`.
5. Implementar revogação de token no logout ou reduzir a janela de validade e documentar.
6. Corrigir e-mail duplicado para 409 e validar `AuthRequest` com 400.

### P1 — curto prazo

7. Eliminar N+1 na listagem de vagas e revisar o `@ElementCollection` EAGER.
8. Adicionar `@Transactional` em escrita e `open-in-view=false`.
9. Adicionar paginação em vagas e tecnologias.
10. Configurar `authenticationEntryPoint`/`accessDeniedHandler` com `ApiError`.
11. Adicionar `@EnableMethodSecurity` e `@PreAuthorize` nos pontos sensíveis.
12. Trocar o `catch (Exception)` do filtro JWT por `JwtException | IllegalArgumentException` e logar.
13. Normalizar e-mail e tratar a corrida de cadastro com `DataIntegrityViolationException`.
14. Criar testes de segurança (MockMvc), JWT e casos de uso; ativar perfil `test`/Testcontainers.

### P2 — evolução

15. Desacoplar `UserDetails` da entidade JPA.
16. Tornar o domínio rico/imutável.
17. Unificar o mapeamento web e remover stubs do mapper de persistência.
18. Adicionar `issuer`/`audience`/`jti`, algoritmo explícito e rotação de chave.
19. Adicionar Actuator, health checks, logs estruturados e correlation ID.
20. Atualizar MapStruct/compiler plugin e avaliar troca de WAR para JAR.
21. Adicionar constraints de banco para salário, `role` e unicidade de tecnologias.

---

## 6. Validação executada nesta revisão

Builds:

```text
npm run build        -> sucesso
./mvnw package       -> sucesso (WAR gerado)
```

Runtime com Postgres local:

```text
GET  /                  -> 200
GET  /jobs/1            -> 200 (SPA fallback)
GET  /jobs/edit/1       -> 200 (SPA fallback)
GET  /api/v1/technologies (sem auth) -> 403
POST /auth/login (inválido)          -> 401
POST /auth/login (válido, perfil prod) -> 200
Set-Cookie: auth_token=...; Max-Age=28800; Secure; HttpOnly; SameSite=Lax
```

O `docker build` não foi executado nesta máquina porque o daemon Docker não está acessível ao usuário atual (`/var/run/docker.sock: permission denied`).

---

## 7. Conclusão

As correções de deploy resolveram os bloqueios mais graves de configuração e startup: segredo JWT, admin de produção, CORS, cookie e tratamento de erro de login. O projeto está em condição razoável para uma demonstração remota controlada.

Para produção, a prioridade agora não é mais "fazer subir", e sim endurecer autenticação e autorização: perfil padrão seguro, restrição de tecnologias, CSRF, revogação de token, rate limiting e respostas 401/403 padronizadas. Em paralelo, N+1, transações e testes são as dívidas técnicas que mais tendem a causar problemas quando o volume ou o número de usuários aumentar.
