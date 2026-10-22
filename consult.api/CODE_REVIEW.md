# Revisão Arquitetural — `consult.api`

> **Documento histórico.** Vários achados abaixo já foram corrigidos. Consulte `CODE_REVIEW_ATUALIZADO.md` para o status atual.

Backend Java 21 + Spring Boot 4.1.1 + Spring Security + JJWT 0.12.6 + PostgreSQL/Flyway.

---

## 1. Resumo executivo

O projeto tem uma base boa: separação por módulos (`job`, `technology`, `security`, `shared`), uso de portas/adaptadores, casos de uso explícitos, Flyway, MapStruct, DTOs e tratamento global de erros. Porém, há problemas relevantes de segurança e inconsistências arquiteturais que impedem considerar o código pronto para produção.

| Severidade | Achado | Local |
|---|---|---|
| CRÍTICO | Segredo JWT com fallback hardcoded no repositório | `application.properties:14` |
| CRÍTICO | Admin inicial criado com senha fixa `123456` | `InitialDataConfig.java:24-31` |
| CRÍTICO | Credenciais inválidas de login retornam 500, não 401 | `ApiExceptionHandler.java:62-74` + `AuthController.java:36-38` |
| CRÍTICO | Registro retorna JWT no corpo, quebrando a estratégia HttpOnly | `AuthController.java:74-77`, `AuthService.java:37-38` |
| ALTO | CSRF desabilitado mesmo usando autenticação por cookie | `SecurityAuthConfig.java:52` |
| ALTO | Logout não invalida o JWT no servidor | `AuthController.java:59-72` |
| ALTO | POST de tecnologias liberado para qualquer usuário autenticado | `SecurityAuthConfig.java:53-61` |
| ALTO | Ausência de rate limiting/lockout em login e registro | `AuthController.java:34`, `:74` |
| ALTO | Token expira em 15 min, mas cookie dura 24 h | `application.properties:15`, `AuthController.java:48` |
| ALTO | Exceções de autenticação/autorização engolidas pelo handler genérico | `ApiExceptionHandler.java:62` |
| MÉDIO | `findAll()` em memória para validar tecnologias | `CreateJobPositionUseCaseImpl.java:35-39` |
| MÉDIO | N+1 na montagem de tecnologias das vagas | `JobPositionRepositoryAdapter.java:62-72` |
| MÉDIO | Duplicidade de mapeamento request/response (create vs update) | `JobPositionController.java:41` vs `:77` |
| MÉDIO | Domínio anêmico e mutável, com validações espalhadas | `JobPosition.java`, `Location.java`, `Technology.java` |
| MÉDIO | `User` acumula entidade JPA + `UserDetails` | `User.java:21` |
| MÉDIO | CORS duplicado (`@CrossOrigin` + configuração global) | `SecurityAuthConfig.java:36-46`, controllers |
| MÉDIO | Sem paginação em listagens | `FindJobPositionUseCaseImpl.java:19-21` |
| MÉDIO | Testes praticamente inexistentes e perfil de teste não ativado | `ApplicationTests.java:6-11` |
| BAIXO | Segredo decodificado a cada requisição | `JwtService.java:63-67` |
| BAIXO | Versões antigas de MapStruct/compiler plugin | `pom.xml:43`, `:136` |
| BAIXO | Pacote `com.br.ats` (ordem de domínio invertida) | `pom.xml:11` |

---

## 2. Arquitetura e camadas

### Pontos fortes

- Boa intenção de Arquitetura Hexagonal: `domain/usecase`, `domain/port/out`, `infrastructure/adapter/in/web`, `infrastructure/persistence`.
- Casos de uso separados por operação (`Create`, `Find`, `Update`, `Delete`), com interfaces claras.
- Configuração explícita de beans para casos de uso, mantendo o domínio sem dependência de Spring.
- Uso de MapStruct para persistência e web, com mappers gerados.
- Migrations Flyway versionadas e `ddl-auto=validate`, o que é uma boa prática.
- Tratamento global de erros com payload padronizado (`ApiError`).

### Problemas estruturais

1. **Domínio anêmico e mutável**
   - `JobPosition`, `Location` e `Technology` usam `@Setter` e não protegem invariantes (`JobPosition.java:10-27`).
   - Regras como "minSalary <= maxSalary", "status inicial", "tecnologias válidas" ficam nos casos de uso ou não existem.
   - Os casos de uso alteram o objeto recebido (`jobPosition.setStatus(...)`, `jobPosition.setTechnologies(...)`), gerando efeitos colaterais.
   - Recomendação: torná-los imutáveis (records ou classes com construtores validantes) e retornar novas instâncias, ou pelo menos concentrar validações no construtor/fábrica.

2. **`User` é ao mesmo tempo entidade JPA e `UserDetails`**
   - `User.java:21` implementa `UserDetails` diretamente.
   - Isso acopla o modelo de persistência ao Spring Security e dificulta testes e evolução.
   - Recomendação: criar um `UserPrincipal`/`AuthenticatedUser` na camada de segurança, que adapta `User` para `UserDetails`. Mantém o domínio e a persistência isolados.

3. **Módulo de segurança fora do padrão hexagonal**
   - `security` usa `model`, `repository`, `service`, `config` diretamente, sem portas/adaptadores.
   - Não é obrigatório que tudo seja hexagonal, mas a inconsistência aumenta o custo cognitivo.
   - Recomendação: padronizar a estrutura ou documentar explicitamente que `security` é uma camada transversal.

4. **Mapeamento duplicado**
   - `JobPositionController.create` usa `request.toDomain()` (`JobPositionController.java:41`), enquanto `update` usa `webMapper.toDomain(request)` (`:77`).
   - `JobPositionResponse.fromDomain` e `JobPositionWebMapper.toResponse` também coexistem.
   - Isso permite divergência de comportamento entre criação e atualização.
   - Recomendação: escolher um único caminho (preferencialmente MapStruct) e remover o outro.

5. **Adaptador de persistência conhece outro gateway**
   - `JobPositionRepositoryAdapter` injeta `TechnologyGateway` (`JobPositionRepositoryAdapter.java:21-27`) para enriquecer o agregado.
   - Funciona, mas cria acoplamento entre agregados e esconde uma consulta extra por item.
   - Recomendação: usar `@EntityGraph`/join fetch ou carregar tecnologias em lote no próprio adaptador de vaga.

6. **Inconsistência de registro de beans**
   - `JobPositionRepositoryAdapter` usa `@Component`; `TechnologyRepositoryAdapter` é registrado via `@Bean` (`TechnologyGateWayBeanConfiguration.java:21-27`).
   - Recomendação: padronizar um dos estilos.

7. **Nomenclatura**
   - `com.br.ats` deveria ser `br.com.ats` (domínio reverso correto).
   - `consult.api` como artifact é incomum; `consult-api` seria mais idiomático.

---

## 3. Segurança JWT

### 3.1 CRÍTICO — segredo JWT com fallback no código

```properties
api.security.token.secret=${JWT_SECRET:sQkU6HV8oOqxEUPgGTtaC1/KRKwGdAT7cSEf3ipsSJhirtddSxUEV0GCmgUZgPLc}
```

`application.properties:14`

Se `JWT_SECRET` não estiver definido, a aplicação sobe com um segredo conhecido publicamente no repositório. Qualquer pessoa pode forjar tokens válidos.

**Correção:** remover o fallback, falhar no startup se o segredo não existir e validar o tamanho mínimo.

```java
@ConfigurationProperties(prefix = "api.security.token")
@Validated
public record JwtProperties(
        @NotBlank String secret,
        @Positive long expiration
) {}
```

```java
@PostConstruct
void initKey() {
    byte[] keyBytes = Decoders.BASE64.decode(properties.secret());
    if (keyBytes.length < 32) {
        throw new IllegalStateException("JWT_SECRET deve ter no mínimo 256 bits");
    }
    this.signingKey = Keys.hmacShaKeyFor(keyBytes);
}
```

### 3.2 CRÍTICO — admin com senha fixa

`InitialDataConfig.java:24-31` cria `admin@tnconsultoria.com` com senha `123456`, inclusive em produção, a cada startup.

**Correção:** criar o admin apenas em perfil de desenvolvimento, lendo credenciais de variáveis de ambiente, e exigir troca de senha no primeiro acesso. Nunca versionar a senha.

### 3.3 CRÍTICO — erros de autenticação viram 500

`AuthController.java:36-38` lança `AuthenticationException`/`BadCredentialsException`. O `ApiExceptionHandler` não tem handler específico e o `@ExceptionHandler(Exception.class)` (`ApiExceptionHandler.java:62-74`) transforma isso em **500 Internal Server Error**.

**Correção:** handlers específicos.

```java
@ExceptionHandler(AuthenticationException.class)
public ResponseEntity<ApiError> handleAuthentication(
        AuthenticationException ex, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, "Credenciais inválidas", request);
}

@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<ApiError> handleAccessDenied(
        AccessDeniedException ex, HttpServletRequest request) {
    return build(HttpStatus.FORBIDDEN, "Acesso negado", request);
}
```

Também faltam handlers para `DataIntegrityViolationException` (409), `HttpMessageNotReadableException` (400), `MethodArgumentTypeMismatchException` (400) e `NoResourceFoundException` (404).

### 3.4 CRÍTICO — registro devolve token no JSON

`AuthService.register` gera o JWT (`AuthService.java:37`) e `AuthController.register` o devolve no corpo (`AuthController.java:74-77`), enquanto o login usa cookie HttpOnly (`:44-53`).

Isso quebra o modelo de segurança: o frontend pode armazenar esse token em `localStorage`, expondo-o a XSS, e o registro passa a ter um comportamento diferente do login.

**Correção:** no registro, criar o usuário e definir o mesmo cookie HttpOnly do login; retornar apenas `201 Created` com uma mensagem ou o usuário criado, sem token no corpo.

### 3.5 ALTO — CSRF desabilitado com autenticação por cookie

`SecurityAuthConfig.java:52` desabilita CSRF. Como a autenticação é feita por cookie (`withCredentials: true` no frontend), isso cria superfície de CSRF. O `SameSite=Strict` do cookie (`AuthController.java:49`) mitiga bastante, mas:

- Se o frontend e a API forem de sites diferentes, `SameSite=Strict` impede o envio do cookie e a autenticação quebra.
- Se for necessário usar `SameSite=None`, o CSRF passa a ser explorável sem proteção.

O import `CookieCsrfTokenRepository` (`SecurityAuthConfig.java:22`) está unused, sugerindo que a proteção foi pensada e depois removida.

**Recomendação:** manter `SameSite=Strict` apenas se frontend e API forem same-site e documentar isso; caso contrário, habilitar CSRF com `CookieCsrfTokenRepository` + `XSRF-TOKEN` e validar o header `X-XSRF-TOKEN` (já permitido no CORS em `:40`). O frontend já possui suporte a interceptores, então é viável.

### 3.6 ALTO — logout não invalida o token

`AuthController.logout` (`:59-72`) apenas apaga o cookie. O JWT continua válido até expirar. Um token copiado continua funcionando. Além disso, o `AuthService.logout` do frontend nem chama esse endpoint (`ats-frontend/src/app/services/auth.service.ts:32-35`).

**Recomendação:** combinar três medidas:

1. TTL curto no access token (15 min já é razoável).
2. Refresh token com rotação e revogação server-side.
3. Denylist por `jti` ou sessão server-side, se houver requisito de logout imediato.

### 3.7 ALTO — descompasso entre expiração do token e do cookie

- JWT: 900000 ms = 15 minutos (`application.properties:15`).
- Cookie: 24 horas (`AuthController.java:48`).
- Frontend: flag `is_logged` sem validade (`ats-frontend/src/app/services/auth.service.ts:24`).

Resultado: após 15 minutos, o usuário parece logado no frontend, mas recebe 401/403 da API.

**Correção:** alinhar `maxAge` do cookie ao TTL do token ou implementar refresh token. O frontend deve reagir a 401 limpando o estado local.

### 3.8 ALTO — ausência de rate limiting e lockout

`/auth/login` e `/auth/register` são públicos (`SecurityAuthConfig.java:54`) e não há limite de tentativas, CAPTCHA, lockout ou atraso progressivo. Isso permite brute force e credential stuffing.

**Recomendação:** Bucket4j, Resilience4j RateLimiter ou rate limit no API Gateway/Proxy, além de lockout temporário por conta/IP e logs de auditoria.

### 3.9 MÉDIO — filtro JWT silencia todos os erros

`JwtAuthenticationFilter.java:64-66`:

```java
} catch (Exception e) {
    // Log de erro opcional para token inválido
}
```

Problemas:

- Nenhum log, dificultando diagnóstico.
- Captura `Exception` genérica, incluindo falhas de banco ao buscar o usuário.
- Token malformado segue como anônimo, mas sem registrar o motivo.

**Correção:**

```java
} catch (JwtException | IllegalArgumentException e) {
    log.debug("JWT inválido em {}: {}", request.getRequestURI(), e.getMessage());
}
```

Não capture `Exception` amplo no filtro; deixe erros de infraestrutura subirem.

### 3.10 MÉDIO — validação e parsing do token

`JwtService.java:33-36` chama `extractUsername` e `isTokenExpired`, e cada um chama `extractAllClaims`, que faz `parseSignedClaims` novamente (`:50-61`). O token é parseado 3 vezes por requisição.

Além disso:

- `getSignInKey()` decodifica Base64 e recria a chave a cada chamada (`:63-67`). Cachear no `@PostConstruct`.
- Não há `issuer`, `audience`, `jti` nem clock skew. Para um único serviço não é bloqueante, mas para múltiplos serviços é recomendável validar `issuer`/`audience`.
- O algoritmo é implícito em `signWith(getSignInKey())` (`:29`). Fixar `HS512` explicitamente e validar o tamanho da chave torna a intenção clara.
- `parseSignedClaims` já valida assinatura e expiração; `isTokenExpired` é redundante, embora inofensivo.

**Sugestão de desenho:**

```java
private SecretKey key; // inicializada uma vez

public boolean isTokenValid(String token, UserDetails user) {
    try {
        Claims claims = parse(token);
        return claims.getSubject().equals(user.getUsername())
                && claims.getExpiration().after(new Date());
    } catch (JwtException | IllegalArgumentException e) {
        log.debug("Token inválido: {}", e.getMessage());
        return false;
    }
}
```

### 3.11 MÉDIO — token só via cookie

`JwtAuthenticationFilter.java:33-40` procura apenas o cookie `auth_token`. Clientes não-browser (mobile, integrações, CLI) não conseguem autenticar.

**Recomendação:** aceitar `Authorization: Bearer <token>` como alternativa, mantendo o cookie para o SPA. Cuidado para não criar duas políticas divergentes de expiração/revogação.

### 3.12 MÉDIO — cookie sem endurecimento de produção

`AuthController.java:44-50`:

- `secure(cookieSecure)` com default `false` (`application.properties:17`). Em produção, precisa ser `true`.
- Sem prefixo `__Host-`.
- Sem `domain` explícito (ok para same-origin).
- `SameSite=Strict` é bom para same-site, mas impede fluxos cross-site legítimos.

**Recomendação:** usar perfis (`application-dev.properties`, `application-prod.properties`) e tornar `secure=true` obrigatório em produção. Considere `__Host-auth_token` (exige `Secure`, `Path=/`, sem `Domain`).

### 3.13 MÉDIO — política de senha e e-mail

- `RegisterRequest.java:9` exige apenas tamanho mínimo 8. Não há checagem de senhas vazadas, complexidade ou bloqueio de senhas comuns.
- O limite de 72 caracteres não equivale a 72 bytes; o BCrypt trunca em 72 bytes. Para Unicode, pode haver truncamento silencioso.
- Não há normalização de e-mail (`trim`/`toLowerCase`). Como o PostgreSQL é case-sensitive por padrão, `User@x.com` e `user@x.com` podem virar contas distintas.
- `AuthService.java:23-25` usa `RuntimeException` genérica para e-mail duplicado, resultando em 500. Deveria ser uma exceção de domínio mapeada para 409 Conflict.
- Há corrida entre `findByEmail` e `save`; duas requisições simultâneas podem violar a constraint. Trate `DataIntegrityViolationException`.

### 3.14 BAIXO — `User` sem estados de conta

`User.java:51-54` retorna sempre `true` para conta expirada, bloqueada, credenciais expiradas e habilitada. Não existe coluna `enabled` em `tb_users` (`V5__create_table_users.sql`). Portanto, não é possível desativar uma conta sem apagá-la.

`User.java:33-34` também não declara `nullable = false` no `role`; se `role` for nulo, `getAuthorities()` lança NPE (`:38`).

---

## 4. Configuração do Spring Security

### 4.1 Regras de autorização incompletas

`SecurityAuthConfig.java:53-61` protege `job-positions`, mas deixa `technologies` cair no `anyRequest().authenticated()`.

Consequência: qualquer usuário com role `USER` pode criar tecnologias (`POST /api/v1/technologies`), o que provavelmente não é o desejado.

**Correção:**

```java
.requestMatchers(HttpMethod.POST, "/api/v1/technologies/**")
    .hasAnyRole("ADMIN", "TECH_RECRUITER")
```

Também vale definir explicitamente o que é público (ex.: `GET /api/v1/job-positions` para candidatos) em vez de "tudo autenticado".

### 4.2 Falta de defesa em profundidade

As regras estão apenas no `SecurityFilterChain`. Não há `@EnableMethodSecurity` nem `@PreAuthorize` nos casos de uso/controllers.

**Recomendação:** adicionar `@EnableMethodSecurity` e anotações nos pontos sensíveis. Isso protege contra novas rotas que escapem do matcher central.

### 4.3 Respostas 401/403 inconsistentes

Não há `authenticationEntryPoint` nem `accessDeniedHandler`. O `@RestControllerAdvice` não intercepta erros gerados no filter chain. O resultado tende a ser 401/403 sem o payload `ApiError`, ou até 403 onde se espera 401.

**Correção:**

```java
.exceptionHandling(ex -> ex
    .authenticationEntryPoint((req, res, e) -> writeJson(res, 401, "Não autenticado"))
    .accessDeniedHandler((req, res, e) -> writeJson(res, 403, "Acesso negado")))
```

### 4.4 `httpBasic` e `formLogin`

Não estão desabilitados explicitamente. Em Spring Security moderno o comportamento padrão pode variar conforme a versão. Seja explícito:

```java
.httpBasic(AbstractHttpConfigurer::disable)
.formLogin(AbstractHttpConfigurer::disable)
.logout(AbstractHttpConfigurer::disable)
```

### 4.5 CORS duplicado e hardcoded

- Configuração global em `SecurityAuthConfig.java:36-46`.
- `@CrossOrigin` em `AuthController.java:23`, `JobPositionController.java:20` e `TechnologyController.java:17`.
- A origem `http://localhost:4200` está fixa no código.
- Os `@CrossOrigin` de `JobPositionController` e `TechnologyController` não definem `allowCredentials = "true"`, o que pode conflitar com o envio de cookies em CORS.

**Recomendação:** remover todos os `@CrossOrigin` e manter apenas a configuração global, com origem vinda de propriedade (`app.cors.allowed-origins`) por perfil.

### 4.6 Sessão stateless

`SessionCreationPolicy.STATELESS` (`:63`) está correto para JWT.

---

## 5. API, DTOs e tratamento de erros

### 5.1 `AuthRequest` sem validação

`AuthRequest.java:3-6` não tem `@NotBlank`/`@Email`, e o login não usa `@Valid` (`AuthController.java:35`). Requisições com corpo vazio ou nulo chegam ao `AuthenticationManager` e hoje resultam em 500.

**Correção:** aplicar validação e mapear erros de validação para 400.

### 5.2 Dois fluxos de autenticação

- Login devolve mensagem + cookie (`AuthController.java:44-56`).
- Registro devolve token no JSON (`:74-77`).

Isso é inconsistente e perigoso, como já destacado. Padronize: ambos devem definir cookie HttpOnly; o token nunca deve aparecer no corpo.

### 5.3 `ApiExceptionHandler` genérico demais

`ApiExceptionHandler.java:62-74` captura `Exception`. Isso mascara erros de framework e de segurança como 500:

- `AuthenticationException` → deveria ser 401.
- `AccessDeniedException` → deveria ser 403.
- `HttpMessageNotReadableException` → deveria ser 400.
- `DataIntegrityViolationException` → deveria ser 409.
- `MethodArgumentTypeMismatchException` → deveria ser 400.

**Recomendação:** manter o fallback, mas adicionar handlers específicos. Considere usar `ProblemDetail` (RFC 9457), suportado pelo Spring 6+, para padronizar a resposta.

### 5.4 `DomainException` não utilizado no handler

Existe `DomainException` (`shared/domain/exception/DomainException.java`), mas o handler registra apenas as exceções concretas (`JobPositionNotFoundException`, `TechnologyNotFoundException`). Se novas exceções de domínio forem criadas, cairão no 500 genérico.

**Correção:** adicionar um handler para `DomainException` e mapear por tipo ou por um campo de status.

### 5.5 Erros de validação sem detalhamento estruturado

`ApiError` (`ApiError.java`) tem apenas `message`. Para validação de formulário, o ideal é um campo `errors: [{field, message}]`, além de um `traceId` para correlação de logs.

### 5.6 Versionamento inconsistente

`/api/v1/job-positions` e `/api/v1/technologies` são versionados; `/auth` não é. Não é um erro grave, mas convém padronizar (`/api/v1/auth`) ou documentar a exceção.

### 5.7 Falta de paginação e ordenação

`GET /api/v1/job-positions` retorna `List` completa (`JobPositionController.java:53-70`). Com volume, isso vira problema de memória, latência e abuso. Use `Pageable`/`Page` e limites máximos.

---

## 6. Persistência e performance

### 6.1 `findAll()` em memória nos casos de uso

`CreateJobPositionUseCaseImpl.java:35-39`:

```java
technologyGateway.findAll().stream()
        .filter(tech -> techIds.contains(tech.getId()))
        .toList();
```

Isso carrega a tabela inteira de tecnologias para filtrar alguns IDs. O `UpdateJobPositionUseCaseImpl.java:34-36` faz o mesmo.

Já existe `findAllByIds` no gateway (`TechnologyGateway.java:46`). Use-o:

```java
List<Technology> valid = technologyGateway.findAllByIds(techIds);
if (valid.size() != new HashSet<>(techIds).size()) {
    throw new InvalidTechnologyException(techIds);
}
```

Além da performance, hoje IDs inválidos são **silenciosamente descartados**, o que pode mascarar erros do cliente. O correto é retornar 400/404.

### 6.2 N+1 na listagem de vagas

`JobPositionRepositoryAdapter.toDomainWithTechnologies` (`:62-72`) executa `findAllByIds` para cada vaga. Em `findAll()`, isso vira 1 + N consultas.

**Recomendação:** carregar as tecnologias de todas as vagas em uma única consulta, ou usar `@EntityGraph`/join fetch. Alternativamente, mapear a associação como `@ManyToMany` com `TechnologyEntity` e usar fetch join.

### 6.3 `@ElementCollection` EAGER

`JobPositionEntity.java:63` usa `fetch = FetchType.EAGER` para `technologyIds`. Isso carrega a coleção em toda consulta de vaga, mesmo quando não é necessária, e contribui para o N+1.

**Recomendação:** `LAZY` + fetch join/`@EntityGraph` nos casos que realmente precisam.

### 6.4 Mapper cria stubs e o adapter substitui

`JobPositionMapper.mapEntityToTechnologies` (`:22-27`) cria `Technology` apenas com ID; depois o adapter substitui pelas tecnologias completas (`:62-72`). É redundante e confuso.

**Recomendação:** o mapper de persistência deve ignorar `technologies`; o adapter monta o agregado completo em um único passo.

### 6.5 Falta de transações

Não há `@Transactional` nos casos de uso. Operações como `findById` + `delete` (`DeleteJobPositionUseCaseImpl.java:18-20`) não são atômicas. `Update` também faz leitura + escrita sem transação explícita.

**Recomendação:** anotar casos de uso de escrita com `@Transactional` e leituras com `@Transactional(readOnly = true)`. Como o domínio não depende de Spring, aplique a anotação na configuração de beans ou em uma camada de aplicação.

### 6.6 Falta de concorrência otimista e auditoria

Não há `@Version` nas entidades nem campos `createdAt`/`updatedAt`. Atualizações concorrentes podem causar lost update. Considere `@Version` e `@CreatedDate`/`@LastModifiedDate` com `@EntityListeners`.

### 6.7 Constraints de banco

- Não há constraint para `min_salary <= max_salary`.
- Não há unicidade de tecnologia por nome/pai.
- `role` não tem `CHECK`.
- `V5` cria índice implícito no e-mail via `UNIQUE`, o que é bom.

### 6.8 `open-in-view`

Não está desabilitado. O default é `true`, o que mantém a sessão aberta durante a renderização e pode mascarar problemas de lazy loading.

**Recomendação:** `spring.jpa.open-in-view=false`.

---

## 7. Configuração, build e testes

### 7.1 Configuração sensível e ambientes

`application.properties` contém:

- Senha do Postgres hardcoded (`:6`).
- Fallback do segredo JWT (`:14`).
- `show-sql=true` e `format_sql=true` (`:10-11`), inadequados para produção.
- `cookie.secure=false` (`:17`).

**Recomendação:** separar `application-dev.properties` e `application-prod.properties`; usar variáveis de ambiente/Secret Manager; desabilitar `show-sql` em produção; exigir `secure=true` em produção.

O `docker-compose.yml` também usa `postgres/postgres` (`:10-11`), aceitável para desenvolvimento local, mas nunca para produção.

### 7.2 Testes

Só existe `contextLoads` (`ApplicationTests.java:6-11`). Além disso, o teste não ativa o perfil `test`, então usa o datasource principal (`localhost:5432`), dependendo de um Postgres local para passar.

**Recomendação:**

- Ativar `@ActiveProfiles("test")`.
- Usar Testcontainers (PostgreSQL) ou H2 em modo compatível.
- Criar testes de:
  - `JwtService` (token válido, expirado, assinatura inválida, usuário divergente).
  - `JwtAuthenticationFilter` (cookie ausente, inválido, válido).
  - Regras de autorização (MockMvc com `@WithMockUser` e roles).
  - Casos de uso (`CreateJobPositionUseCase`, validação de tecnologias).
  - `ApiExceptionHandler` (401, 403, 409, 400).

### 7.3 Build

- `pom.xml:8` usa Spring Boot 4.1.1. Verifique a compatibilidade das bibliotecas auxiliares (MapStruct, JJWT, Lombok) com essa versão.
- `pom.xml:43` usa MapStruct 1.5.5, antigo para Java 21. Considere 1.6.x.
- `pom.xml:136` usa `maven-compiler-plugin` 3.11.0. Prefira a versão gerenciada pelo parent e `<release>21</release>` em vez de `source`/`target`.
- `pom.xml:14` empacota como `war` com Tomcat `provided`. Se não há deploy em Tomcat externo, use `jar` e remova o `ServletInitializer`, simplificando o build.

### 7.4 Observabilidade

Não há Actuator, health checks, métricas ou correlation ID. Para produção, adicione `spring-boot-starter-actuator`, exponha apenas `health`/`info` e configure logs estruturados.

---

## 8. Roadmap priorizado

### P0 — antes de qualquer deploy

1. Remover o fallback do segredo JWT e falhar no startup sem segredo válido.
2. Remover o admin hardcoded com senha `123456`.
3. Mapear `AuthenticationException` para 401 e `AccessDeniedException` para 403.
4. Padronizar registro para usar cookie HttpOnly e não retornar token no corpo.
5. Restringir `POST /api/v1/technologies` a `ADMIN`/`TECH_RECRUITER`.
6. Adicionar rate limiting em login/registro.
7. Alinhar TTL do token e do cookie; corrigir o logout no frontend para chamar `/auth/logout`.

### P1 — curto prazo

8. Habilitar CSRF ou documentar formalmente a decisão com `SameSite=Strict`.
9. Implementar refresh token/revogação se logout imediato for requisito.
10. Substituir `findAll()` por `findAllByIds` e validar IDs inexistentes.
11. Eliminar N+1 (fetch join/`@EntityGraph`/carregamento em lote).
12. Adicionar paginação nas listagens.
13. Normalizar e-mail, tipar exceção de duplicidade e tratar corrida com 409.
14. Adicionar `@Transactional` e `open-in-view=false`.
15. Centralizar CORS e remover `@CrossOrigin` dos controllers.

### P2 — evolução

16. Introduzir `UserPrincipal` para desacoplar `UserDetails` da entidade JPA.
17. Tornar o domínio rico/imutável e centralizar invariantes.
18. Unificar mapeamento web (remover duplicidade `toDomain`/`fromDomain`).
19. Adicionar `issuer`/`audience`/`jti`, rotação de chaves e suporte a `Authorization: Bearer`.
20. Adicionar Testcontainers, testes de segurança e ArchUnit para proteger as fronteiras hexagonais.
21. Adicionar Actuator, métricas, logs estruturados e correlation ID.
22. Separar perfis de configuração e remover credenciais do repositório.

---

## 9. Conclusão

A arquitetura demonstra maturidade na organização por casos de uso e portas/adaptadores, e a decisão de usar cookie HttpOnly para o JWT é acertada para um SPA. Contudo, a combinação de **segredo com fallback**, **admin com senha fixa**, **erros de autenticação retornando 500**, **CSRF desabilitado com cookie** e **logout sem invalidação** representa risco real de segurança.

A boa notícia é que a maioria das correções é localizada e não exige reescrita: configuração de propriedades, handlers de exceção, regras de autorização, uso de `findAllByIds` e ajustes de TTL/cookie já elevam bastante o nível do projeto. O passo seguinte mais valioso é criar testes de segurança (MockMvc + Testcontainers) para impedir regressões nesses pontos.
