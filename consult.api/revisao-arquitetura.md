# Revisão de Arquitetura e Segurança — ATS Consult API

**Data:** 22/10/2026  
**Escopo:** backend `consult.api` e integração com o frontend Angular em `../ats-frontend`  
**Autor:** revisão técnica independente (arquitetura, segurança e integração)  
**Documento:** `consult.api/revisao-arquitetura.md`

> **Premissa importante sobre o stack:** o `pom.xml` usa **Spring Boot 4.1.1**, **Spring Framework 7.0.9**, **Spring Security 7.1.1**, **Jackson 3 (`tools.jackson`)** e **Java 21**. A premissa inicial de "Spring Boot 3" não corresponde ao projeto atual. A análise considera o stack real encontrado no repositório. O Angular declarado é **17.3**.

---

## 1. Sumário executivo

O projeto está organizado como um **monólito modular** com separação razoável por feature (`job`, `technology`, `security`, `shared`) e uma implementação consistente de **Ports & Adapters** nos módulos de vagas e tecnologias. Também há evolução clara em segurança: JWT com assinatura, `issuer`, `audience`, `jti`, versão de token, cookie `HttpOnly`, CSRF opcional, CORS centralizado, tratamento global de erros e validação de configuração no startup.

Ainda assim, **não considero o projeto pronto para produção sem a correção dos itens P0 deste relatório**. Os riscos principais não são de "funcionar", mas de **fechamento de segurança, consistência transacional e confiabilidade da suíte de testes**.

**Veredito:** apto para demonstração controlada e homologação; **go-live bloqueado** até concluir os itens P0.

### 1.1 Principais riscos encontrados

| ID | Severidade | Achado | Impacto |
|---|---|---|---|
| Q-01 | **P0 (qualidade)** | A suíte de testes atual falha: `./mvnw test` não carrega o perfil `test`, tenta um PostgreSQL local e quebra a autenticação (`SQLState 28P01`). Só existe um teste de contexto. | Não há gate confiável de CI; regressões passam despercebidas. |
| S-01 | **P0 (segurança)** | `GET /**` está `permitAll()` no `SecurityFilterChain`. Qualquer endpoint GET futuro fora de `/api` ou `/auth` nasce público. | Superfície de ataque cresce silenciosamente; falha "fail-open". |
| S-02 | **P0 (segurança)** | Rate limiting é em memória, sem limite de cardinalidade, com chave apenas `IP + ação + e-mail` no login e `IP + ação` no registo. | Bypass por rotação de e-mails/IPs, lockout global atrás de proxy e crescimento de memória. |
| S-03 | **P0/P1 (segurança)** | CSRF, logout e ciclo de vida da sessão têm decisões frágeis: CSRF desabilitado em `dev`, logout só revoga se a requisição chegar ao servidor com autenticação, token de 8 h sem refresh, e o Angular só envia `X-XSRF-TOKEN` em requisições **same-origin**. | Sessão pode continuar válida após "logout" no cliente; CSRF quebra em deploy com front e API em origens diferentes. |
| A-01 | **P1 (dados)** | O `@Version` existe na entidade, mas o mapper ignora `version` e o domínio não o transporta. O update também aceita `status` nulo e pode violar `NOT NULL`. | Concorrência otimista praticamente anulada, risco de lost update e respostas 409/500 confusas em atualizações parciais. |
| A-02 | **P1 (arquitetura)** | Domínio depende de Spring Data (`Page`/`Pageable`), use cases de domínio usam `@Transactional`, e o modelo de vagas é mutável (Lombok `@Setter`), sem records/valor imutável. | Acoplamento do núcleo ao framework e dificuldade de testar/evoluir regras sem infraestrutura. |
| I-01 | **P1 (integração)** | Não existe endpoint de sessão (`/auth/me`) nem papel do utilizador no frontend. O backend protege por role, mas a UI exibe ações de escrita para `USER` e depende de `localStorage.is_logged`. | UX incorreta e falsa sensação de autorização; não há escalada real porque o backend bloqueia, mas o contrato está incompleto. |
| I-02 | **P1 (integração)** | Contratos TypeScript usam `unknown` para create/update, misturam request e response no mesmo modelo e deixam enums como `string`. | Erros de contrato só aparecem em runtime; drift entre Angular e API. |
| S-04 | **P1 (configuração)** | Defaults perigosos em cenários de execução manual: `COOKIE_SECURE=false`, credenciais locais `postgres/postgres`, admin `123456` no perfil `dev`, `SameSite` como texto livre. | Deploy incorreto pode expor cookie sem TLS, credenciais fracas ou configuração inválida. |
| P-01 | **P2 (performance)** | Falta índice em `tb_job_position_technologies(technology_id)`, listagem de tecnologias sem paginação, consulta de `UserDetails` ao banco em toda requisição autenticada e `save()`/`delete()` carregando o agregado completo. | Degradação com volume; custo desnecessário por request. |

### 1.2 Pontos fortes

- Estrutura modular por feature, com inbound adapters, use cases, gateways e persistence adapters.
- DTOs web são `record` e as entidades JPA não são expostas diretamente na API.
- MapStruct usado para web e persistência; Flyway versionando o schema.
- `spring.jpa.open-in-view=false`, `ddl-auto=validate`, `hibernate.jdbc.time_zone=UTC`.
- JWT com chave Base64 validada (mínimo 32 bytes), `issuer`, `audience`, `jti`, expiração explícita e `HS256`.
- Revogação por `token_version`, cookie `HttpOnly`, `Secure` em produção, `SameSite` configurável.
- CORS centralizado com `allowCredentials=true` e lista de origens.
- `AuthenticationEntryPoint` e `AccessDeniedHandler` retornando o mesmo contrato `ApiError`.
- Tratamento global de exceções cobrindo validação, JSON inválido, tipo inválido, 404, 405, 409, 429 e fallback 500.
- Frontend Angular 17 standalone com `HttpClient`, interceptor de credenciais, tratamento de 401 e build de produção funcional.
- Deploy same-origin (Angular embutido no WAR) reduz CORS e simplifica cookies/CSRF.

### 1.3 Scorecard estimado

> Percentuais são uma estimativa de maturidade para priorização, não uma métrica formal.

| Dimensão | Aderência | Situação |
|---|---:|---|
| Organização modular e Ports & Adapters | 70% | Bom, com violações de pureza do domínio e módulo `security` fora do padrão. |
| DTOs, validação e contrato de erros | 60% | Records e handler global, mas validações incompletas e 404 inconsistente. |
| Segurança de autenticação/JWT | 70% | Boa base, com gaps em rate limit, CSRF/logout e configuração. |
| Autorização | 65% | Roles por URL corretas, mas `GET /**` fail-open e ausência de defesa em profundidade. |
| Integração backend-frontend | 60% | Build ok e fluxo principal alinhado, mas falta sessão/role, tipagem e tratamento de CSRF/erros. |
| Performance e escalabilidade | 55% | Consultas razoáveis na listagem, mas faltam índices, paginação de tecnologias e cache. |
| Testes e CI | 15% | Apenas um teste de contexto e atualmente quebrado sem PostgreSQL/perfil. |
| Observabilidade e operação | 50% | Actuator básico, sem correlation ID em logs nem auditoria de autenticação. |

---

## 2. Escopo, método e evidências

### 2.1 O que foi analisado

- Backend: `src/main/java`, `src/main/resources`, `src/test`, `pom.xml`, Dockerfile/compose e documentação de deploy no diretório pai.
- Frontend: `../ats-frontend/src` (app config, rotas, guards, interceptors, models, services e componentes principais).
- Banco: migrations Flyway `V1` a `V6`.
- Contrato HTTP: controllers, DTOs, mappers, security config e serviços Angular.

### 2.2 Verificações executadas

| Comando | Resultado |
|---|---|
| `./mvnw -o -q -DskipTests package` | **Sucesso**; WAR gerado em `target/consult.api-0.0.1-SNAPSHOT.war`. |
| `./mvnw -o -q test` | **Falha** no `ApplicationTests.contextLoads`: tentou conectar ao PostgreSQL local com `postgres/postgres` e recebeu `FATAL: autenticação do tipo senha falhou` (`SQLState 28P01`). O perfil `test` não foi ativado. |
| `npm run build -- --configuration development` | **Sucesso**; bundle gerado em `ats-frontend/dist/ats-frontend`. |
| `npm run build` (production) | **Sucesso**; bundle inicial de 333,44 kB (86,84 kB estimados em transferência). |
| Docker build / análise dinâmica / SAST / DAST / CVE scan | **Não executados** nesta revisão. |

### 2.3 Limitações

- Não houve execução contra PostgreSQL de produção, nem teste de carga.
- Não foi feita análise de dependências com OWASP Dependency-Check/Snyk.
- Não foi feita revisão de infraestrutura de rede além do Caddy/Render fornecidos.
- O frontend está em diretório irmão (`../ats-frontend`), não dentro do repositório `consult.api`; a integração foi analisada pelo código-fonte, não por contrato OpenAPI gerado.

---

## 3. Arquitetura e separação de camadas

### 3.1 Visão geral

```text
Angular 17 (standalone)
        |
        | HTTPS + cookie HttpOnly + X-XSRF-TOKEN
        v
Spring Security FilterChain (JWT cookie/Bearer, CORS, CSRF)
        v
Inbound Adapters (Controllers + DTOs + Web Mappers)
        v
Use Cases (Create/Find/Update/Delete)
        v
Output Ports (JobPositionGateway, TechnologyGateway)
        v
Persistence Adapters (JPA + MapStruct + Spring Data)
        v
PostgreSQL (Flyway)
```

A divisão por feature é adequada para o tamanho atual do sistema. O ponto mais positivo é que `job` e `technology` seguem um desenho hexagonal consistente: controller não conhece JPA, domínio não conhece entidade, e o adapter de persistência faz a tradução.

O módulo `security`, porém, não segue o mesmo desenho: `AuthService` usa `UserRepository` e `User` (entidade JPA) diretamente, e `AuthController` orquestra `AuthenticationManager`, `JwtService`, cookie e rate limit. Para um módulo pequeno isso é pragmático, mas cria inconsistência arquitetural e dificulta testes.

### 3.2 Matriz de conformidade

| Critério | Status | Evidência | Avaliação |
|---|---|---|---|
| Organização por feature/módulo | Conforme | `job`, `technology`, `security`, `shared` | Boa separação de responsabilidades. |
| Inbound/outbound adapters | Conforme | `job/infrastructure/adapter/in/web`, `persistence/adapter` | Hexagonal aplicado corretamente nos módulos de negócio. |
| Domínio isolado de framework | **Parcial** | `JobPositionGateway.java:4-5` importa `Page`/`Pageable`; use cases usam `@Transactional` (`CreateJobPositionUseCaseImpl.java:9,25`) | O núcleo ainda depende de Spring Data e Spring TX. |
| Use cases como camada de aplicação | Parcial | `domain/usecase/impl/*` | Estão dentro de `domain`; ideal seria `application/usecase`. |
| DTOs como records | Conforme | `JobPositionRequest`, `JobPositionResponse`, `AuthRequest`, `ApiError` | Bom uso de Java 21. |
| DTOs separados para leitura/escrita | **Não conforme** | `JobPositionRequest` e `JobPositionResponse` separados, mas o DTO de request é reutilizado para create/update e o modelo Angular mistura os dois | Falta distinção semântica entre criar, atualizar e ler. |
| Validação de entrada | Parcial | `JobPositionRequest.java:15-28`; `CreateTechnologyRequest`; `AuthRequest` | Faltam limites de tamanho, dígitos, listas e validação de localização. |
| Mapeamento web | Parcial | `JobPositionWebMapper` (MapStruct) + `JobPositionResponse.fromDomain` (não usado) + `JobTechnologyResponse.fromDomain` | Duas estratégias convivem; remover código morto e padronizar. |
| Mapeamento persistência | Conforme | `JobPositionMapper`, `TechnologyMapper` | Separação correta entre entidade e domínio. |
| Entidade JPA não exposta | Conforme | Controllers retornam DTOs | Bom. |
| Domínio rico/imutável | **Não conforme** | `JobPosition.java:10-15` usa `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`; `Location` idem | Estado inválido pode ser construído por setters. |
| Tratamento global de exceções | Conforme | `ApiExceptionHandler` | Cobertura boa, com lacunas pontuais. |
| 404 padronizado | **Não conforme** | `JobPositionController.java:49-54` retorna `ResponseEntity.notFound().build()` sem `ApiError`; `TechnologyController` lança exceção | Contrato de erro inconsistente entre recursos. |
| Concorrência otimista | **Não conforme na prática** | `JobPositionEntity.java:59-61` tem `@Version`; `JobPositionMapper.java:19` ignora `version`; domínio não tem versão | Versão não é transportada; update pode sobrescrever alterações concorrentes. |
| Transações | Parcial | `@Transactional` em create/update/delete/find | Escritas transacionais, mas o limite transacional está no domínio e o update não faz merge correto. |
| Módulo de segurança no mesmo padrão | **Não conforme** | `AuthService.java:21-23` injeta `UserRepository`; `User` é entidade JPA | Funciona, mas quebra a arquitetura dos demais módulos. |
| Acoplamento entre módulos | Parcial | `JobPosition` importa `Technology`; `JobPositionRepositoryAdapter` usa `TechnologyGateway` | Aceitável em monólito modular, mas convém um `TechnologyRef` ou ID no agregado. |

### 3.3 Achados detalhados

#### A-01 (P1) — O domínio depende de Spring Data

**Evidência:**

```java
// JobPositionGateway.java:4-5
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
```

```java
// CreateJobPositionUseCaseImpl.java:9,25
import org.springframework.transaction.annotation.Transactional;
...
@Transactional
public JobPosition execute(JobPosition jobPosition) { ... }
```

**Consequência:** o domínio e os casos de uso não podem ser testados sem o ecossistema Spring; a portabilidade e a clareza da Clean Architecture ficam comprometidas. A regra "domínio não conhece framework" não é atendida.

**Recomendação:** criar tipos próprios de paginação no domínio/application e mover a transação para um decorator/adapter de aplicação.

```java
// shared/domain/PageResult.java
package com.br.ats.consult.api.shared.domain;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {
    public PageResult {
        content = List.copyOf(content);
    }

    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    public boolean first() { return page == 0; }
    public boolean last() { return page + 1 >= totalPages(); }
}
```

```java
// shared/domain/PageQuery.java
public record PageQuery(int page, int size, SortOrder sort) {
    public PageQuery {
        if (page < 0) throw new IllegalArgumentException("page deve ser >= 0");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size deve estar entre 1 e 100");
    }

    public record SortOrder(String field, Direction direction) {
        public enum Direction { ASC, DESC }
    }
}
```

```java
// application/port/out/JobPositionGateway.java
PageResult<JobPosition> findAll(PageQuery query);
Optional<JobPosition> findById(Long id);
```

```java
// application/usecase/CreateJobPositionService.java (ou decorator)
@Transactional
public JobPosition execute(CreateJobPositionCommand command) { ... }
```

> Alternativa pragmática: manter `Page`/`Pageable` se o time aceitar explicitamente o trade-off, mas documentar a exceção e não a chamar de Clean Architecture pura.

#### A-02 (P1) — Modelo de domínio anêmico e mutável

**Evidência:** `JobPosition.java:10-15` expõe `@Setter`, construtor vazio e construtor total. `Location` tem o mesmo padrão. A validação (`JobPosition.validate()`) é chamada manualmente e não impede que um objeto inválido circule.

**Consequência:** regras de negócio podem ser ignoradas; `status` pode ser nulo em update; listas podem ser alteradas fora do agregado; não há garantia de invariantes.

**Recomendação:** usar value objects/records para conceitos imutáveis e factories para o agregado. Entidades JPA continuam mutáveis por necessidade do ORM, mas o domínio não precisa ser.

```java
// domain/model/Location.java
public record Location(String city, String state, String country) {
    public Location {
        city = normalize(city);
        state = normalize(state);
        country = normalize(country);
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
```

```java
// domain/model/SalaryRange.java
public record SalaryRange(BigDecimal min, BigDecimal max) {
    public SalaryRange {
        if (min != null && min.signum() < 0) throw new DomainValidationException("Salário mínimo inválido");
        if (max != null && max.signum() < 0) throw new DomainValidationException("Salário máximo inválido");
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new DomainValidationException("Salário mínimo não pode exceder o máximo");
        }
    }
}
```

```java
// domain/model/JobPosition.java (exemplo de factory)
public final class JobPosition {
    private final Long id;
    private final String title;
    private final JobStatusEnum status;
    private final SalaryRange salary;
    // ...

    public static JobPosition create(String title, WorkModelEnum workModel,
                                     ContractTypeEnum contractType, SalaryRange salary) {
        // valida e retorna instância consistente
    }
}
```

> Não é necessário converter tudo de uma vez. Comece por `Location`, `SalaryRange` e pelas regras de criação/atualização; mantenha a entidade JPA separada.

#### A-03 (P1) — Optimistic locking não funciona de ponta a ponta

**Evidência:**

```java
// JobPositionEntity.java:59-61
@Version
@Column(nullable = false)
private Long version;
```

```java
// JobPositionMapper.java:18-22
@Mapping(target = "technologyIds", expression = "java(mapTechnologiesToEntityIds(domain.getTechnologies()))")
@Mapping(target = "version", ignore = true)
@Mapping(target = "createdAt", ignore = true)
@Mapping(target = "updatedAt", ignore = true)
JobPositionEntity toEntity(JobPosition domain);
```

O domínio (`JobPosition`) não possui `version`; o mapper zera a versão em toda conversão. O `repository.save(entity)` com `id` preenchido tende a fazer `merge` de uma entidade detacada sem versão confiável.

**Consequência:** duas atualizações concorrentes podem sobrescrever uma à outra sem `409 Conflict`; a coluna `version` existe no banco, mas não protege o fluxo.

**Recomendação:** carregar a entidade gerenciada no adapter e alterar somente os campos, preservando `version`; ou transportar `version` no domínio/DTO e usar `If-Match`. Também mapear `ObjectOptimisticLockingFailureException` para 409. Se optar pela primeira abordagem, exponha `update(Long id, JobPosition domain)` na porta `JobPositionGateway` em vez de reutilizar `save` para tudo.

```java
// JobPositionRepositoryAdapter — update preservando versão
@Override
public JobPosition update(Long id, JobPosition domain) {
    JobPositionEntity entity = repository.findById(id)
            .orElseThrow(() -> new JobPositionNotFoundException(id));

    JobPositionEntity mapped = mapper.toEntity(domain);
    entity.setTitle(mapped.getTitle());
    entity.setDescription(mapped.getDescription());
    entity.setStatus(mapped.getStatus());
    entity.setWorkModel(mapped.getWorkModel());
    entity.setContractType(mapped.getContractType());
    entity.setEducationLevel(mapped.getEducationLevel());
    entity.setEnglishLevel(mapped.getEnglishLevel());
    entity.setMinSalary(mapped.getMinSalary());
    entity.setMaxSalary(mapped.getMaxSalary());
    entity.setLocation(mapped.getLocation());
    entity.setTechnologyIds(mapped.getTechnologyIds());

    repository.save(entity);
    return findById(id).orElseThrow();
}
```

```java
// ApiExceptionHandler
@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
public ResponseEntity<ApiError> handleOptimisticLock(
        ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
    return response(HttpStatus.CONFLICT, request,
            "O recurso foi alterado por outro utilizador. Recarregue e tente novamente.");
}
```

#### A-04 (P1) — Semântica de `PUT` e campos nulos

**Evidência:**

```java
// JobPositionRequest.java:18
JobStatusEnum status,
```

`status` é opcional no request, mas `JobPositionEntity.status` é `nullable = false` (`JobPositionEntity.java:33-35`). O update copia o request para o domínio e chama `validate()`, que não valida `status`.

**Consequência:** um `PUT` sem `status` pode chegar ao banco com `NULL` e gerar `DataIntegrityViolationException` (409) ou comportamento inesperado. Um `PUT` também pode sobrescrever campos omitidos com `null`, dependendo do mapeamento.

**Recomendação:** separar `CreateJobPositionRequest` e `UpdateJobPositionRequest`; exigir `status` no update (ou implementar `PATCH` com merge); validar invariantes no domínio; nunca aceitar `null` em campo `NOT NULL`.

```java
public record UpdateJobPositionRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 10_000) String description,
        @NotNull JobStatusEnum status,
        @NotNull WorkModelEnum workModel,
        @NotNull ContractTypeEnum contractType,
        // ...
        @NotNull List<@Positive Long> technologyIds
) {}
```

#### A-05 (P1) — 404 inconsistente e erros de validação incompletos

**Evidência:**

```java
// JobPositionController.java:49-54
return findJobPositionUseCase.findById(id)
        .map(domain -> ResponseEntity.ok(webMapper.toResponse(domain)))
        .orElse(ResponseEntity.notFound().build());
```

O `TechnologyController` lança `TechnologyNotFoundException`, que é convertida em `ApiError`; o de vagas devolve 404 sem corpo. O frontend tem fallback, mas consumidores externos recebem contratos diferentes.

**Recomendação:** lançar `JobPositionNotFoundException` também na leitura ou retornar `ApiError` explicitamente.

```java
@GetMapping("/{id}")
public JobPositionResponse findById(@PathVariable Long id) {
    return findJobPositionUseCase.findById(id)
            .map(webMapper::toResponse)
            .orElseThrow(() -> new JobPositionNotFoundException(id));
}
```

#### A-06 (P2) — Duplicação de estratégias de mapeamento

Há MapStruct (`JobPositionWebMapper`) e factories estáticas (`JobPositionResponse.fromDomain`, `LocationResponse.fromDomain`, `JobTechnologyResponse.fromDomain`). `JobPositionResponse.fromDomain` não é usado. `JobTechnologyResponse` converte `category` para `String`, enquanto `TechnologyResponse` mantém o enum.

**Recomendação:** escolher uma estratégia por camada:
- Web: MapStruct para todos os responses ou factories estáticas para todos, não ambos.
- Contrato: padronizar `category` como enum/string em todos os endpoints.
- Remover `AuthResponse` e `JobPositionResponse.fromDomain` se não forem usados.

#### A-07 (P2) — Módulo `security` fora do padrão Clean Architecture

`AuthService` (`security/service/AuthService.java`) usa diretamente `UserRepository` e `User` (entidade JPA). `AuthController` coordena autenticação, JWT, cookie, CSRF e rate limit. Para evoluir, separe:

```text
security/
  domain/model/UserAccount, Role
  application/port/out/UserAccountGateway
  application/usecase/RegisterUser, LoginUser, LogoutUser
  infrastructure/persistence/UserJpaEntity, UserRepositoryAdapter
  infrastructure/web/AuthController
```

Isso permite testar login/registro sem subir JPA e mantém a entidade JPA longe do caso de uso.

#### A-08 (P2) — Empacotamento e nomenclatura

- `pom.xml:14` empacota `war`; se não há Tomcat externo, `jar` é mais simples.
- `pom.xml:11` usa `com.br.ats`, domínio invertido; o padrão seria `br.com.ats`.
- Metadados vazios (`name`, `description`, `url`, `licenses`, `developers`, `scm`) prejudicam publicação/CI.
- `TechnologyGateWayBeanConfiguration` tem typo no nome (cosmético).

---

## 4. Segurança e autenticação

### 4.1 Fluxo atual

1. O frontend chama `GET /auth/csrf` (ou `/api/v1/auth/csrf`) para obter o cookie `XSRF-TOKEN`.
2. `POST /auth/login` valida credenciais via `DaoAuthenticationProvider` + BCrypt.
3. O servidor gera JWT HS256, define o cookie `auth_token` (`HttpOnly`, `Secure` em prod, `SameSite`) e retorna apenas uma mensagem.
4. Requisições seguintes enviam o cookie; `JwtAuthenticationFilter` resolve o token, valida assinatura/issuer/audience/expiração/versão, carrega `UserDetails` do banco e popula o `SecurityContext`.
5. `POST /auth/logout` incrementa `token_version` (se autenticado) e limpa o cookie.
6. O frontend usa `withCredentials` e `X-XSRF-TOKEN` via `withXsrfConfiguration`.

### 4.2 Matriz de conformidade

| Controle | Status | Evidência | Avaliação |
|---|---|---|---|
| Segredo JWT sem fallback | Conforme | `application.properties:26`; `JwtService.java:36-61` | Falha no startup se ausente/inválido; mínimo de 32 bytes. |
| Algoritmo explícito | Conforme | `JwtService.java:75` (`HS256`) | Bom. |
| Assinatura validada | Conforme | `JwtService.java:79-86` | `verifyWith(signingKey)`. |
| `issuer`/`audience` | Conforme | `JwtService.java:28-31,68-69,82-83` | Requeridos na emissão e validação. |
| Expiração | Conforme | `JwtService.java:72,99-107` | 8 h por padrão; sem refresh. |
| `jti` | Conforme | `JwtService.java:73` | Presente, mas não há denylist por `jti`. |
| Revogação no logout | Parcial | `AuthController.java:61-69`; `AuthService.java:44-48` | Só revoga se a requisição chegar autenticada; invalida todas as sessões do utilizador. |
| Cookie `HttpOnly` | Conforme | `AuthCookieService.java:35-40` | Excelente para reduzir roubo de token por XSS. |
| Cookie `Secure` | Parcial | `application-prod.properties:9`; base `application.properties:32` | Correto em prod; default `false` se o perfil não for aplicado. |
| `SameSite` | Parcial | `AuthCookieService.java:17,40` | Texto livre; não valida `None` com `Secure`; `Strict` pode afetar fluxos cross-site futuros. |
| CORS | Conforme parcial | `SecurityAuthConfig.java:58-71` | Allowlist + credenciais; faltam headers expostos e validação de `*` com credenciais. |
| CSRF | Parcial | `SecurityAuthConfig.java:87-94`; `application-dev.properties:8` | Ativo em prod, desativado em dev; same-origin no Angular; handler não-XOR. |
| Autorização por rota | Conforme parcial | `SecurityAuthConfig.java:106-118` | Roles corretas para escrita; `GET /**` é fail-open. |
| Autorização por método | Parcial | `@EnableMethodSecurity` em `SecurityAuthConfig.java:37`, sem `@PreAuthorize` | Defesa em profundidade não utilizada. |
| Rate limiting | **Não conforme** | `RateLimitService.java` | Em memória, sem limite de cardinalidade, bypass por chave e problema atrás de proxy. |
| Respostas 401/403 JSON | Conforme | `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler` | Mesmo contrato `ApiError`; faltam `WWW-Authenticate`/`Cache-Control`. |
| Password hashing | Conforme | `SecurityAuthConfig.java:134-138` | BCrypt; limite de 72 caracteres no DTO de autenticação. |
| Política de senha | Parcial | `RegisterRequest.java:6` | Mínimo 8, sem complexidade, sem verificação de vazamento, sem reset/MFA. |
| Enumeração de e-mail | Parcial | `AuthService.java:29-31`; `EmailAlreadyRegisteredException` | Registro revela existência (409). |
| Headers de segurança | Parcial | Configuração default do Spring Security | Sem CSP, `Referrer-Policy` ou `Permissions-Policy` explícitos. |
| Auditoria/observabilidade de auth | Parcial | Logs pontuais | Sem `last_login`, eventos de login/logout, correlation ID em todos os logs. |
| Actuator | Conforme | `application.properties:51-53` | Apenas `health`/`info`, detalhes ocultos. |
| Segredos/defaults | Parcial | `application.properties:8-10`; `application-dev.properties:4-5` | Credenciais locais e admin `123456`; risco se perfil errado for usado. |

### 4.3 Achados detalhados

#### S-01 (P0) — `GET /**` é fail-open

**Evidência:**

```java
// SecurityAuthConfig.java:116-118
.requestMatchers("/api/**").authenticated()
.requestMatchers(HttpMethod.GET, "/**").permitAll()
.anyRequest().denyAll()
```

A intenção é liberar SPA/estáticos, mas a regra libera **qualquer GET** que não seja `/api/**` nem `/auth/**`. Um futuro `GET /admin/metrics`, `/internal/...`, `/actuator/env` (se exposto) ou um endpoint fora de `/api` nasce público. O `anyRequest().denyAll()` final nunca é alcançado para GET.

**Recomendação:** listar explicitamente apenas os caminhos públicos e manter `anyRequest().denyAll()`.

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/error").permitAll()
        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
        .requestMatchers("/api/v1/auth/csrf", "/api/v1/auth/login", "/api/v1/auth/register").permitAll()
        .requestMatchers("/", "/index.html", "/favicon.ico", "/assets/**",
                "/*.js", "/*.css", "/*.map", "/*.ico", "/*.png", "/*.svg", "/*.webmanifest",
                "/login", "/home", "/jobs", "/jobs/**").permitAll()
        .requestMatchers("/api/v1/auth/logout").authenticated()
        .requestMatchers(HttpMethod.GET, "/api/v1/job-positions/**").authenticated()
        .requestMatchers(HttpMethod.GET, "/api/v1/technologies/**").authenticated()
        .requestMatchers(HttpMethod.POST, "/api/v1/job-positions/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
        .requestMatchers(HttpMethod.PUT, "/api/v1/job-positions/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
        .requestMatchers(HttpMethod.DELETE, "/api/v1/job-positions/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
        .requestMatchers(HttpMethod.POST, "/api/v1/technologies/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
        .requestMatchers("/api/**").authenticated()
        .anyRequest().denyAll()
)
```

> Se houver novas rotas SPA, prefira um `SpaForwardController` com padrão explícito ou um `ResourceResolver` que exclua `/api`, `/auth`, `/actuator` e arquivos com extensão.

#### S-02 (P0) — Rate limiting contornável e não limitado em memória

**Evidência:**

```java
// RateLimitService.java:15-52
private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();
...
private void cleanupIfNeeded(long now) {
    if (attempts.size() < 1_000) return;
    attempts.entrySet().removeIf(entry -> now - entry.getValue().windowStart >= windowSeconds);
}
```

```java
// AuthController.java:45-47
String email = normalizeEmail(request.email());
String rateLimitKey = clientKey(httpRequest, "login") + ":" + email;
rateLimitService.check(rateLimitKey);
```

Problemas:
- A chave inclui o e-mail, então um atacante pode variar o e-mail e obter 10 tentativas por combinação IP+e-mail.
- Não há limite por IP isolado nem por conta isolada; ataques distribuídos passam.
- `cleanupIfNeeded` só limpa quando o mapa já tem 1.000 entradas; se todas estiverem ativas, o mapa cresce sem limite.
- `request.getRemoteAddr()` atrás de Caddy/Render pode ser o IP do proxy ou depender de `ForwardedHeaderFilter`; validar em runtime. Se todos compartilharem o mesmo IP, um atacante pode causar lockout global.
- Não é distribuído: múltiplas instâncias multiplicam o limite.

**Recomendação:** usar dois buckets (IP e conta), cache limitado (Caffeine) ou Redis, e um limite global por IP. Exemplo com Caffeine (adicione `com.github.ben-manes.caffeine:caffeine` ao `pom.xml`):

```java
@Component
public class RateLimitService {

    @Value("${app.security.rate-limit.max-attempts:10}")
    private int maxAttempts;

    @Value("${app.security.rate-limit.window-seconds:60}")
    private long windowSeconds;

    private Cache<String, AtomicInteger> attempts;

    @PostConstruct
    void init() {
        attempts = Caffeine.newBuilder()
                .maximumSize(100_000)
                .expireAfterWrite(Duration.ofSeconds(windowSeconds))
                .build();
    }

    public void check(String key) {
        AtomicInteger counter = attempts.get(key, ignored -> new AtomicInteger());
        int current = counter.incrementAndGet();
        if (current > maxAttempts) {
            throw new TooManyRequestsException(windowSeconds);
        }
    }

    public void reset(String key) {
        attempts.invalidate(key);
    }
}
```

```java
// AuthController — chaves separadas
String ipKey = "login:ip:" + clientIp(httpRequest);
String accountKey = "login:account:" + email;
rateLimitService.check(ipKey);
rateLimitService.check(accountKey);
```

```java
private String clientIp(HttpServletRequest request) {
    // Só confie em X-Forwarded-For se o proxy for confiável e configurado.
    // Ideal: server.forward-headers-strategy=native com Tomcat RemoteIpValve
    // e uma allowlist de proxies.
    return request.getRemoteAddr();
}
```

Para produção com mais de uma instância, Redis com `INCR` + `EXPIRE` é o caminho natural. Adicione também um limite para `/auth/csrf` e considere CAPTCHA após N falhas persistentes.

#### S-03 (P0/P1) — CSRF, logout e ciclo de sessão

**CSRF:** em produção o CSRF está ativo (`application-prod.properties:14`), mas em `dev` é desabilitado (`application-dev.properties:8`). Isso é aceitável para desenvolvimento local, mas exige disciplina para nunca subir `dev` publicamente. O handler configurado é `CsrfTokenRequestAttributeHandler` (`SecurityAuthConfig.java:93`), que não aplica a proteção BREACH do `XorCsrfTokenRequestAttributeHandler`; para uma API same-origin com double-submit isso pode ser aceitável, mas deve ser uma decisão documentada.

**Angular + CSRF cross-origin:** o Angular 17 envia o header `X-XSRF-TOKEN` automaticamente apenas para requisições **same-origin**. Como o deploy oficial serve o Angular pelo próprio Spring (`Dockerfile` copia `dist/ats-frontend/browser` para `src/main/resources/static`), isso funciona. Se o frontend for hospedado em outra origem com CORS e CSRF ativo, o header não será enviado e todos os POST/PUT/DELETE falharão com 403. Nesse cenário, é necessário um interceptor manual que leia o cookie `XSRF-TOKEN` e envie o header.

**Logout:** `AuthService.logout()` no Angular (`auth.service.ts:32-42`) engole erros e limpa o estado local mesmo se o servidor não responder. Como o cookie `auth_token` é `HttpOnly`, o cliente não consegue apagá-lo; se o `POST /logout` falhar por CSRF/rede, o cookie continua válido até expirar. O servidor só incrementa `token_version` se `authentication != null` (`AuthController.java:61-69`). O token de 8 h não tem refresh; o logout invalida **todas** as sessões do utilizador.

**Recomendação:**
- Exigir autenticação em `/api/v1/auth/logout` e garantir CSRF válido.
- No frontend, só limpar o estado local após confirmação do servidor ou tentar novamente; em caso de falha, exibir erro.
- Reduzir access token para 15–30 min com refresh token rotativo, ou manter 8 h com janela de inatividade e revogação por dispositivo (`jti` + denylist).
- Documentar se o CSRF é same-origin/double-submit e adicionar validação de startup para `prod`.
- Considerar rotação do token CSRF após login.

```java
// Validação fail-fast de configuração de segurança
@Configuration
@RequiredArgsConstructor
public class SecurityConfigValidator {
    private final Environment environment;

    @Value("${app.security.csrf.enabled:true}")
    private boolean csrfEnabled;

    @Value("${app.security.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${app.security.cookie.same-site:Strict}")
    private String cookieSameSite;

    @PostConstruct
    void validate() {
        boolean prod = environment.acceptsProfiles(Profiles.of("prod"));
        if (prod && !csrfEnabled) {
            throw new IllegalStateException("CSRF não pode ser desabilitado em produção");
        }
        if (prod && !cookieSecure) {
            throw new IllegalStateException("COOKIE_SECURE deve ser true em produção");
        }
        if ("None".equalsIgnoreCase(cookieSameSite) && !cookieSecure) {
            throw new IllegalStateException("SameSite=None exige COOKIE_SECURE=true");
        }
    }
}
```

#### S-04 (P1) — Defaults e credenciais de conveniência

**Evidência:**

```properties
# application.properties:8-10
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/ats_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}

# application.properties:32
app.security.cookie.secure=${COOKIE_SECURE:false}
```

```properties
# application-dev.properties:4-5
app.seed-admin.email=${APP_ADMIN_EMAIL:admin@tnconsultoria.com}
app.seed-admin.password=${APP_ADMIN_PASSWORD:123456}
```

O perfil `prod` sobrescreve `Secure` e não cria admin com senha fixa, e o Docker/Render/compose definem `SPRING_PROFILES_ACTIVE=prod`. Ainda assim, execuções manuais (`java -jar`) sem perfil podem usar defaults locais; a ausência de `JWT_SECRET` impede o startup, o que ajuda, mas as credenciais de banco e o cookie inseguro continuam sendo um risco se o segredo for fornecido.

**Recomendação:**
- Remover os defaults de usuário/senha do banco no `application.properties` e exigir variáveis; use um `application-local.properties` para conveniência.
- Tornar `app.security.cookie.secure` default `true` e definir `false` explicitamente apenas em `application-dev.properties`.
- Remover a senha `123456` do `dev`; usar `APP_ADMIN_PASSWORD` obrigatório ou um profile `seed` separado.
- Validar `APP_ADMIN_PASSWORD` com tamanho mínimo no startup.
- Validar `SameSite` como enum (`Strict`, `Lax`, `None`).

#### S-05 (P1) — Enumeração de e-mail e política de conta

`POST /auth/register` retorna `409` com mensagem que confirma a existência do e-mail (`EmailAlreadyRegisteredException`). Isso permite enumeração de contas. O login usa mensagem genérica, o que é bom. Além disso, todo registo cria `Role.USER`; não há endpoint para promover a `TECH_RECRUITER` nem para criar utilizadores. Na prática, `TECH_RECRUITER` só existe via seed manual/DB.

**Recomendação:**
- Responder de forma genérica no registo (ex.: sempre 202 "se o e-mail for válido, enviaremos instruções") ou aceitar o trade-off e documentar.
- Implementar gestão de utilizadores/roles por ADMIN, ou seed explícito de recrutadores.
- Adicionar verificação de e-mail, reset de senha e, se o domínio exigir, MFA.
- Adicionar `last_login`, `created_at`, `updated_at` e auditoria de eventos.

#### S-06 (P1) — Headers de segurança e respostas de autenticação

O Spring Security aplica headers padrão, mas não há CSP, `Referrer-Policy` ou `Permissions-Policy` explícitos. Os handlers de 401/403 também não definem `Cache-Control: no-store` nem `WWW-Authenticate`.

```java
http.headers(headers -> headers
        .contentSecurityPolicy(csp -> csp.policyDirectives(
                "default-src 'self'; " +
                "script-src 'self'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data:; " +
                "font-src 'self'; " +
                "connect-src 'self'; " +
                "frame-ancestors 'none'; " +
                "base-uri 'self'; " +
                "form-action 'self'"))
        .referrerPolicy(referrer -> referrer.policy(
                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
        .frameOptions(frame -> frame.deny())
        .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31_536_000))
);
```

```java
// RestAuthenticationEntryPoint
response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
```

> A CSP acima é um ponto de partida; valide com os estilos/scripts reais do Angular. Evite `unsafe-inline` em `script-src`.

#### S-07 (P2) — JWT e consulta ao banco em toda requisição

`JwtAuthenticationFilter` chama `userDetailsService.loadUserByUsername` em cada request autenticada (`JwtAuthenticationFilter.java:46-53`). Isso é necessário para refletir mudanças de role/enabled e a `token_version`, mas custa uma consulta ao banco por request. Para alto volume:
- cache curto (ex.: 30–60 s) de `UserPrincipal` por e-mail, invalidado em logout/role change; ou
- access token curto + refresh, mantendo a consulta apenas no refresh; ou
- claims de role no token com `token_version` para revogação, aceitando janela de inconsistência.

Não remova a verificação de `token_version` sem um mecanismo equivalente de revogação. Para rotação de chave, adicione um `kid` ao header e mantenha a chave anterior aceita durante uma janela de transição.

#### S-08 (P2) — Detalhes de autorização

- `@EnableMethodSecurity` está ativo, mas não há `@PreAuthorize`. Adicione em operações sensíveis para defesa em profundidade:
  ```java
  @PreAuthorize("hasAnyRole('ADMIN', 'TECH_RECRUITER')")
  @PostMapping
  public ResponseEntity<JobPositionResponse> create(...) { ... }
  ```
- As regras `PUT`/`DELETE` de `/api/v1/technologies/**` existem na configuração, mas não há endpoints correspondentes no `TechnologyController`. Remova ou implemente.
- `POST /auth/logout` está `permitAll` por `/auth/**`. Funciona, mas o ideal é `authenticated()` e CSRF válido.
- `/auth/**` e `/api/v1/auth/**` são aliases (`AuthController.java:24`). Padronize um único caminho versionado e remova o alias.
- Considere `@PreAuthorize` também em métodos de consulta que possam vir a retornar dados sensíveis.

#### S-09 (P2) — Actuator, trace e auditoria

`/actuator/health` e `/actuator/info` estão públicos e sem detalhes, o que é bom. Porém:
- `ApiError.traceId` aceita qualquer `X-Request-Id` do cliente, sem limite de tamanho/sanitização; se for para logs, valide (UUID ou `[A-Za-z0-9-]{1,64}`).
- Não há filtro de correlação populando MDC; os logs não têm o mesmo `traceId` da resposta.
- Não há auditoria de login/logout/falhas/role changes.
- Render health check aponta para `/` (SPA), não para `/actuator/health`; prefira readiness/liveness da API.

---

## 5. Integração backend-frontend

### 5.1 Matriz de endpoints

| Método | Caminho | Autorização atual | Request | Response | Consumidor Angular |
|---|---|---|---|---|---|
| GET | `/auth/csrf`, `/api/v1/auth/csrf` | Público | — | 204 + cookie `XSRF-TOKEN` | `ensureCsrfToken()` |
| POST | `/auth/login`, `/api/v1/auth/login` | Público + CSRF em prod | `AuthRequest` | 200 `MessageResponse` + cookie `auth_token` | `AuthService.login` |
| POST | `/auth/register`, `/api/v1/auth/register` | Público + CSRF em prod | `RegisterRequest` | 201 `MessageResponse` + cookie | `AuthService.register` (sem UI) |
| POST | `/auth/logout`, `/api/v1/auth/logout` | Público na config; revoga se autenticado | — | 200 `MessageResponse` + cookie expirado | `AuthService.logout` |
| GET | `/api/v1/job-positions` | Autenticado | `page`, `size`, `technologyId` | `PageResponse<JobPositionResponse>` | `JobPositionService.findAll` |
| GET | `/api/v1/job-positions/{id}` | Autenticado | — | 200 `JobPositionResponse` ou 404 sem corpo | `findById` |
| POST | `/api/v1/job-positions` | `ADMIN`, `TECH_RECRUITER` | `JobPositionRequest` | 201 `JobPositionResponse` | `create` |
| PUT | `/api/v1/job-positions/{id}` | `ADMIN`, `TECH_RECRUITER` | `JobPositionRequest` | 200 `JobPositionResponse` | `update` |
| DELETE | `/api/v1/job-positions/{id}` | `ADMIN`, `TECH_RECRUITER` | — | 204 | `delete` |
| GET | `/api/v1/technologies` | Autenticado | — | `List<TechnologyResponse>` | `TechnologyService.findAll` |
| GET | `/api/v1/technologies/{id}` | Autenticado | — | `TechnologyResponse` | não usado |
| GET | `/api/v1/technologies/parent/{parentId}` | Autenticado | — | `List<TechnologyResponse>` | não usado |
| POST | `/api/v1/technologies` | `ADMIN`, `TECH_RECRUITER` | `CreateTechnologyRequest` | 201 `TechnologyResponse` | não usado |
| PUT/DELETE | `/api/v1/technologies/**` | Configurado, sem controller | — | 404/405 | não usado |

### 5.2 Matriz de conformidade

| Critério | Status | Evidência | Avaliação |
|---|---|---|---|
| URL base configurável | Conforme | `environment.ts`, `environment.prod.ts` | `localhost:8080` em dev, same-origin em prod. |
| Credenciais/cookies | Conforme | `credentialsInterceptor` (`auth.interceptor.ts:6-8`) | `withCredentials=true` em todas as requisições. |
| Token no cliente | Conforme | Cookie `HttpOnly`; frontend não lê o JWT | Boa decisão de segurança. |
| CSRF | Parcial | `app.config.ts:16-19`; dev desabilita CSRF | Same-origin funciona; cross-origin não envia header automaticamente. |
| Tratamento de 401 | Conforme | `auth.interceptor.ts:10-22` | Limpa estado e redireciona, exceto em `/auth/`. |
| Tratamento de 403/429/500 | Parcial | `api-error.util.ts`; componentes | Fallback local; sem refresh de CSRF ou tratamento global. |
| Contrato de erro | Conforme | `api-error.model.ts` | Alinhado ao `ApiError` do backend. |
| Paginação | Conforme | `PageResponse<T>` e `JobPositionService` | `page/size` zero-based; backend limita a 100. |
| Tipagem TypeScript | Parcial | `strict: true`, mas `create/update(job: unknown)` | O `unknown` anula a segurança de tipos no ponto crítico. |
| Modelos request/response | **Não conforme** | `job-position.model.ts:10-24` | Um único `JobPosition` mistura `id`/`technologies` (response) e `technologyIds` (request). |
| Enums tipados | Parcial | `JobStatus`/`WorkModel` unions; `contractType`/`educationLevel`/`englishLevel` strings | Inconsistência de contrato. |
| Sessão/role no frontend | **Não conforme** | `auth.service.ts:44-46` usa `localStorage.is_logged` | Não há `/auth/me` nem role; guard só verifica um flag de UI. |
| Guard de autenticação | Conforme parcial | `auth.guard.ts:5-17` | Funciona como UX, não como segurança; backend é a autoridade. |
| Logout | Parcial | `auth.service.ts:32-42` | Fire-and-forget; falha do servidor não é tratada. |
| SPA fallback | Parcial | `SpaForwardController.java:9` | Cobre rotas atuais; novas rotas exigem atualização. |
| Contrato gerado/OpenAPI | **Não conforme** | Não há OpenAPI/client gerado | Risco de drift entre os dois lados. |
| Build | Conforme | `npm run build` com sucesso | Angular 17.3 standalone. |

### 5.3 Achados detalhados

#### I-01 (P1) — Falta endpoint de sessão e autorização por role na UI

O backend não retorna role no login e não expõe `/auth/me`. O frontend só sabe `is_logged`, então:
- `USER` vê botões de criar/editar/apagar vagas, mas recebe 403 ao tentar.
- A rota `/jobs/new` é acessível a qualquer autenticado.
- O `localStorage.is_logged` pode ficar obsoleto após expiração do cookie e pode ser manipulado (não é escalada de privilégio, pois a API valida, mas é UX/segurança enganosa).

**Backend sugerido:**

```java
public record SessionResponse(Long id, String email, String role) {}

@GetMapping("/me")
public SessionResponse me(Authentication authentication) {
    UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
    return new SessionResponse(principal.getId(), principal.getUsername(), principal.getRole().name());
}
```

Adicione `/api/v1/auth/me` como `authenticated()` e mantenha o login retornando `SessionResponse` no corpo (sem o token):

```java
return ResponseEntity.ok(new LoginResponse("Login efetuado com sucesso", session));
```

**Frontend sugerido:**

```ts
export type Role = 'ADMIN' | 'TECH_RECRUITER' | 'USER';

export interface Session {
  id: number;
  email: string;
  role: Role;
}
```

```ts
// auth.service.ts
private sessionSignal = signal<Session | null>(null);
readonly session = this.sessionSignal.asReadonly();

loadSession(): Observable<Session> {
  return this.http.get<Session>(`${this.apiUrl}/me`).pipe(
    tap((session) => this.sessionSignal.set(session))
  );
}

hasAnyRole(...roles: Role[]): boolean {
  const role = this.sessionSignal()?.role;
  return role != null && roles.includes(role);
}
```

```ts
// role.guard.ts
export const roleGuard = (...roles: Role[]): CanActivateFn => () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.hasAnyRole(...roles) ? true : router.createUrlTree(['/home']);
};
```

```ts
{ path: 'jobs/new', component: JobPositionFormComponent, canActivate: [authGuard, roleGuard('ADMIN', 'TECH_RECRUITER')] },
{ path: 'jobs/edit/:id', component: JobPositionFormComponent, canActivate: [authGuard, roleGuard('ADMIN', 'TECH_RECRUITER')] }
```

> Enquanto `/me` não existir, uma alternativa é retornar `role` no corpo do login e guardar apenas esse dado não sensível em memória; nunca trate `is_logged` como autorização.

#### I-02 (P1) — Contratos TypeScript frouxos e acoplados

**Evidência:** `job-position.service.ts:31-37` usa `unknown`; `job-position.model.ts` tem `technologyIds` e `technologies` no mesmo tipo; `contractType`, `educationLevel` e `englishLevel` são `string`.

**Recomendação:** separar comandos de leitura e escrita, tipar enums e usar um tipo de payload explícito.

```ts
// models/job-position-enums.model.ts
export type JobStatus = 'DRAFT' | 'OPEN' | 'PAUSED' | 'CLOSED';
export type WorkModel = 'REMOTE' | 'HYBRID' | 'ON_SITE';
export type ContractType = 'CLT' | 'PJ' | 'CLT_OR_PJ' | 'FREELANCE' | 'INTERNSHIP' | 'TEMPORARY';
export type EducationLevel = 'HIGH_SCHOOL' | 'ASSOCIATE' | 'BACHELORS' | 'SPECIALIZATION' | 'MASTERS' | 'DOCTORATE';
export type EnglishLevel = 'NONE' | 'BASIC' | 'INTERMEDIATE' | 'ADVANCED' | 'FLUENT';
```

```ts
// models/job-position.model.ts
export interface JobPositionRequest {
  title: string;
  description?: string;
  status?: JobStatus;
  workModel: WorkModel;
  contractType: ContractType;
  educationLevel?: EducationLevel;
  englishLevel?: EnglishLevel;
  minSalary?: number;
  maxSalary?: number;
  location?: LocationModel;
  technologyIds?: number[];
}

export interface JobPositionResponse {
  id: number;
  title: string;
  description?: string;
  status: JobStatus;
  workModel: WorkModel;
  contractType: ContractType;
  educationLevel?: EducationLevel;
  englishLevel?: EnglishLevel;
  minSalary?: number;
  maxSalary?: number;
  location?: LocationModel;
  technologies: TechnologyModel[];
}
```

```ts
create(jobPosition: JobPositionRequest): Observable<JobPositionResponse> {
  return this.http.post<JobPositionResponse>(this.apiUrl, jobPosition);
}

update(id: number, job: JobPositionRequest): Observable<JobPositionResponse> {
  return this.http.put<JobPositionResponse>(`${this.apiUrl}/${id}`, job);
}
```

#### I-03 (P1) — CSRF cross-origin e recuperação de 403

No deploy same-origin atual, o `withXsrfConfiguration` funciona. Se o frontend for separado:

```ts
export const csrfInterceptor: HttpInterceptorFn = (req, next) => {
  const isMutating = !['GET', 'HEAD', 'OPTIONS'].includes(req.method.toUpperCase());
  if (!isMutating) return next(req);

  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
  const token = match ? decodeURIComponent(match[1]) : null;
  return next(token ? req.clone({ setHeaders: { 'X-XSRF-TOKEN': token } }) : req);
};
```

Além disso, trate 403 de CSRF com um refresh do token e **uma** retentativa, em vez de deixar o utilizador preso. Evite loops.

#### I-04 (P1) — Tratamento de erros fragmentado

Hoje cada componente chama `extractApiErrorMessage` e há `console.error`/`alert`/`confirm` misturados. Centralize em um `NotificationService` e em um interceptor global:

```ts
export const apiErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const notifications = inject(NotificationService);
  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        if (error.status === 403) {
          notifications.error('Não tem permissão para esta operação.');
        } else if (error.status === 429) {
          notifications.error('Demasiadas tentativas. Aguarde e tente novamente.');
        } else if (error.status >= 500) {
          notifications.error('Erro temporário no servidor. Tente mais tarde.');
        }
      }
      return throwError(() => error);
    })
  );
};
```

#### I-05 (P2) — Defaults de negócio divergentes

O backend assume `DRAFT` quando `status` é nulo (`CreateJobPositionUseCaseImpl.java:29-31`), enquanto o formulário Angular envia `OPEN` por padrão (`job-position-form.component.ts`, `initForm`). Defina o default em um único lugar (preferencialmente no domínio/API) e documente no contrato. O rótulo `DRAFT` também não aparece em `getStatusLabel` do frontend.

#### I-06 (P2) — Categoria de tecnologia com tipos diferentes

`JobTechnologyResponse.category` é `String`, enquanto `TechnologyResponse.category` é `TechnologyCategoryEnum` e o modelo Angular usa `string`. Padronize: ou ambos usam enum, ou ambos usam string documentada. O ideal é um único `TechnologySummary` compartilhado no contrato.

#### I-07 (P2) — SPA fallback e performance de carregamento

`SpaForwardController` lista rotas manualmente. Considere um fallback que encaminhe qualquer rota sem extensão para `index.html`, excluindo `/api`, `/auth`, `/actuator` e assets. No Angular, use lazy loading para as features:

```ts
{
  path: 'jobs',
  loadComponent: () => import('./features/job-position/components/job-position-list/job-position-list.component')
    .then(m => m.JobPositionListComponent),
  canActivate: [authGuard]
}
```

---

## 6. Performance, acoplamento e operação

### 6.1 Achados

| ID | Severidade | Achado | Evidência | Impacto | Recomendação |
|---|---|---|---|---|---|
| P-01 | P2 | Falta índice para busca por tecnologia | `V2__create_table_job_positions.sql:14-19`; PK é `(job_position_id, technology_id)` | `findByTechnologyId` faz scan na tabela de associação | Criar índice em `technology_id`. |
| P-02 | P2 | Listagem de tecnologias sem paginação | `TechnologyController.java:40-47`; `FindTechnologyUseCase.findAll()` | Crescimento linear; frontend carrega tudo no dropdown | Paginar + endpoint de busca; cache do catálogo. |
| P-03 | P2 | Consulta de `UserDetails` por request | `JwtAuthenticationFilter.java:46-53` | 1 query extra por request autenticada | Cache curto ou access token curto + refresh. |
| P-04 | P2 | `save()` carrega tecnologias após gravar | `JobPositionRepositoryAdapter.java:39-44` | Query adicional e montagem de domínio em toda escrita | Retornar entidade/DTO sem recarregar, ou projetar response. |
| P-05 | P2 | `delete()` busca o agregado completo antes de apagar | `DeleteJobPositionUseCaseImpl.java` + `JobPositionRepositoryAdapter.findById` | Carrega entidade + element collection + tecnologias só para checar existência | Usar `existsById`/`deleteById` com tratamento de corrida ou delete direto. |
| P-06 | P2 | `Pageable` aceita qualquer `sort` | `JobPositionController.java:56-59` | Sort inválido/propriedade arbitrária gera erro 500 ou queries inesperadas | Whitelist de campos (`id`, `title`, `createdAt`, etc.). |
| P-07 | P2 | `@ElementCollection` como `List` sem ordem | `JobPositionEntity.java:79-86` | Bag sem ordenação; delete+insert em updates; duplicatas se não deduplicar | Se ordem não importa, usar `Set<Long>`; se importa, `@OrderColumn`. |
| P-08 | P2 | Sem `default_batch_fetch_size`/batch de element collections | `application.properties` | Múltiplas queries ao carregar várias entidades | Configurar `hibernate.default_batch_fetch_size` (ex.: 50). |
| P-09 | P2 | Sem tuning de pool/timeouts | `application.properties` | Pool Hikari default e timeouts genéricos | Definir `maximum-pool-size`, `connection-timeout`, `idle-timeout`, `max-lifetime`. |
| P-10 | P3 | Bundle Angular sem lazy loading | `app.routes.ts` | Bundle inicial maior que o necessário | Lazy loading por feature e budgets. |
| P-11 | P3 | Sem cache HTTP para assets/index | Configuração estática padrão | Cache inconsistente/baixo reaproveitamento | Hash nos assets; `index.html` no-cache; assets longos. |
| P-12 | P3 | Caddy sem headers de segurança | `Caddyfile` | Depende só do Spring | Centralizar headers no Spring ou no Caddy. |

### 6.2 Correção importante sobre N+1

A listagem de vagas **não** está em N+1 no código atual: `JobPositionRepositoryAdapter.toDomainList` coleta todos os `technologyIds` da página e faz **uma** consulta em lote via `technologyGateway.findAllByIds` (`JobPositionRepositoryAdapter.java:79-98`). Isso é um ponto positivo e deve ser preservado.

As consultas que ainda merecem atenção são:
- `findById`/`save` carregam tecnologias adicionais (esperado em parte, mas `save` pode evitar).
- `delete` carrega o agregado inteiro sem necessidade.
- cada request autenticada consulta `UserDetails`.
- listagem de tecnologias é ilimitada.

### 6.3 Migração sugerida para índices

```sql
-- V7__add_performance_indexes.sql
CREATE INDEX IF NOT EXISTS idx_job_position_tech_technology_id
    ON tb_job_position_technologies (technology_id);

CREATE INDEX IF NOT EXISTS idx_job_positions_status_updated_at
    ON tb_job_positions (status, updated_at DESC);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_lower_unique
    ON tb_users (LOWER(email));

ALTER TABLE tb_technologies
    ADD CONSTRAINT chk_technologies_category
    CHECK (category IN ('LANGUAGE', 'FRAMEWORK', 'DATABASE', 'DEVOPS_INFRA',
                        'CLOUD_PROVIDER', 'AI_LLM', 'TESTING_TOOL', 'ARCHITECTURE_STYLE',
                        'STORAGE', 'SERVERLESS', 'OTHER')) NOT VALID;
```

> `NOT VALID` evita bloquear a migration em dados existentes; valide depois com `VALIDATE CONSTRAINT`.

### 6.4 Cache e paginação de tecnologias

```java
@GetMapping
public ResponseEntity<PageResponse<TechnologyResponse>> findAll(
        @RequestParam(required = false) String search,
        @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
    // use case com Pageable/PageQuery
}
```

Para o dropdown do formulário, um endpoint `GET /api/v1/technologies/options` com apenas `id`, `name`, `parentId` e cache de 5–15 min é mais eficiente que carregar o catálogo completo. Invalide o cache ao criar tecnologia.

---

## 7. Testes, qualidade e CI

### 7.1 Situação atual

- Existe apenas `ApplicationTests.contextLoads` (`src/test/java/.../ApplicationTests.java:6-11`).
- `application-test.properties` existe, mas **não é ativado** por `@ActiveProfiles("test")` e também não define um datasource de teste.
- A dependência H2 existe, mas o teste tentou PostgreSQL porque o perfil não foi aplicado.
- O teste falhou com autenticação do PostgreSQL local (`28P01`), não por falha de código de negócio.
- Não há testes de:
  - casos de uso (criar/atualizar/apagar vaga, resolver tecnologias);
  - segurança (rotas públicas/protegidas, roles, CSRF, JWT expirado/revogado);
  - contrato HTTP (`MockMvc`/`WebTestClient`);
  - migrations Flyway em banco real;
  - componentes/serviços Angular (specs geradas não cobrem o fluxo).
- Não há pipeline CI, lint, cobertura ou análise estática configurados.

### 7.2 Correção mínima da suíte

Opção recomendada: **Testcontainers com PostgreSQL**, para validar as migrations reais (H2 não reproduz `BIGSERIAL`, `TIMESTAMPTZ`, `LOWER(...)` e constraints da mesma forma).

```java
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class ApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void contextLoads() {
    }
}
```

Adicione ao `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

Se preferir H2 por velocidade, configure `MODE=PostgreSQL` e desative/valide as migrations com cuidado. Para este projeto, Testcontainers é mais fiel.

### 7.3 Matriz de testes recomendada

| Camada | Testes |
|---|---|
| Domínio | `JobPosition`/`SalaryRange`/`Location`: campos obrigatórios, salário negativo, min > max, status. |
| Use cases | create com tecnologias válidas/inexistentes, update com lista vazia/nula, delete inexistente. |
| Persistência | `@DataJpaTest` + Testcontainers: mapeamento, `@ElementCollection`, optimistic lock, migrations. |
| Segurança | `@WebMvcTest`/`MockMvc`: rotas públicas, 401 sem cookie, 403 por role, CSRF, cookie flags, JWT expirado/versão. |
| Contrato | `MockMvc` validando `ApiError` e `PageResponse`; JSON schema/OpenAPI. |
| Frontend | `AuthService`, interceptor 401/403, guards, formulário de vaga, erro de API. |
| E2E | Playwright/Cypress: login, criar/editar/apagar vaga, logout, sessão expirada. |

### 7.4 CI mínimo

```yaml
# .github/workflows/ci.yml (exemplo)
name: ci
on: [push, pull_request]
jobs:
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      - run: ./mvnw -B verify
  frontend:
    runs-on: ubuntu-latest
    defaults: { run: { working-directory: ats-frontend } }
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '20', cache: npm, cache-dependency-path: ats-frontend/package-lock.json }
      - run: npm ci
      - run: npm run build
      - run: npm test -- --watch=false --browsers=ChromeHeadless
```

Inclua:
- `maven-enforcer-plugin` para Java 21/Maven;
- Spotless/Checkstyle e SpotBugs/ErrorProne (backend);
- ESLint/Prettier (frontend);
- OWASP Dependency-Check ou Renovate/Dependabot;
- cobertura mínima (JaCoCo) para use cases e segurança.

---

## 8. Plano de ação priorizado

### P0 — Bloqueadores antes de go-live

| # | Ação | Arquivos principais | Esforço | Critério de aceite |
|---|---|---|---|---|
| P0-1 | Corrigir a suíte de testes: `@ActiveProfiles("test")`, Testcontainers PostgreSQL e CI verde | `ApplicationTests.java`, `pom.xml`, `application-test.properties`, CI | 1–2 d | `./mvnw verify` passa em ambiente limpo, sem PostgreSQL local. |
| P0-2 | Fechar autorização: remover `GET /** permitAll`, listar apenas SPA/estáticos/auth e manter `denyAll` | `SecurityAuthConfig.java`, `SpaForwardController.java` | 0,5–1 d | Teste de segurança prova que rota GET não listada retorna 401/403. |
| P0-3 | Endurecer rate limiting: buckets por IP e por conta, cache limitado/distribuído, IP atrás de proxy validado | `RateLimitService.java`, `AuthController.java`, config | 1–2 d | Teste com 11 tentativas por IP e por conta retorna 429; mapa não cresce sem limite. |
| P0-4 | Definir modelo de sessão/CSRF/logout: CSRF same-origin documentado, logout autenticado, tratar falha no cliente, validar config em prod | `SecurityAuthConfig.java`, `AuthController.java`, `auth.service.ts` | 2–3 d | Logout revoga no servidor; CSRF não quebra no deploy escolhido; startup falha com config insegura. |
| P0-5 | Corrigir update/optimistic locking: preservar versão, exigir campos `NOT NULL`, mapear conflito para 409 | `JobPositionMapper.java`, adapter, DTOs, `ApiExceptionHandler` | 2–3 d | Teste concorrente recebe 409; `PUT` sem `status` retorna 400, não 500/409. |
| P0-6 | Completar validação e padronizar 404/erros | DTOs, `JobPositionController`, handler | 1 d | Campos acima dos limites retornam 400 com `ApiError`; 404 de vaga tem corpo padronizado. |

### Status de implementação P0 (2026-10-22)

| Item | Status | Evidência |
|---|---|---|
| P0-1 | Parcial | `ApplicationTests` usa `@ActiveProfiles("test")`; `application-test.properties` usa H2 em modo PostgreSQL com Flyway desativado. `./mvnw -o test` passa sem PostgreSQL local. Testcontainers/CI não foram adicionados porque Docker e os módulos Testcontainers não estão disponíveis no ambiente. |
| P0-2 | Implementado | `GET /** permitAll` removido; rotas públicas explícitas; `anyRequest().denyAll()`; testes de autorização em `SecurityAuthorizationTest`. |
| P0-3 | Implementado | Buckets separados por IP e conta, limite de cardinalidade, reset apenas da conta e teste de 429 após 10 tentativas. |
| P0-4 | Implementado | Logout autenticado, cliente só limpa estado após sucesso (ou 401), validação de `SameSite`/CSRF/`Secure` em `prod` com testes. |
| P0-5 | Implementado | `JobPositionGateway.update` carrega entidade gerenciada, preserva `@Version`, `PUT` exige `status`, e conflito é mapeado para 409. |
| P0-6 | Implementado | Limites de validação nos DTOs; 404 de vaga agora retorna `ApiError`; sort inválido retorna 400. |

### P1 — Curto prazo (1–2 semanas)

| # | Ação | Critério de aceite |
|---|---|---|
| P1-1 | Criar `/api/v1/auth/me` e retornar role/sessão no login; adicionar `roleGuard` e esconder ações de escrita para `USER` | UI não mostra criar/editar/apagar para `USER`; backend continua bloqueando. |
| P1-2 | Padronizar autenticação em `/api/v1/auth` e remover alias `/auth`; atualizar Angular | Um único caminho documentado; testes de contrato atualizados. |
| P1-3 | Separar tipos Angular de request/response, remover `unknown`, tipar todos os enums | `tsc`/build falha se o payload divergir do contrato. |
| P1-4 | Centralizar tratamento de erros/notificações; tratar 403 de CSRF com refresh e uma retentativa | Nenhum `alert`/`confirm` nativo nos fluxos principais; 403 de CSRF recuperável. |
| P1-5 | Adicionar headers de segurança (CSP, Referrer-Policy, Permissions-Policy) e `WWW-Authenticate`/`no-store` em 401/403 | Scanner básico não aponta headers ausentes; login continua funcionando. |
| P1-6 | Remover defaults inseguros: DB sem usuário/senha padrão, cookie `Secure` default true, sem admin `123456` em dev, validar `SameSite` | Startup falha se config de prod for insegura. |
| P1-7 | Adicionar índices, paginação/busca de tecnologias e whitelist de sort | `findByTechnologyId` usa índice; dropdown não carrega catálogo inteiro; sort inválido retorna 400. |
| P1-8 | Adicionar testes de segurança e de use cases; ativar cobertura no CI | Casos críticos cobertos; CI bloqueia regressão. |
| P1-9 | Adicionar auditoria mínima: `last_login`, eventos de login/logout, correlation ID em MDC | Logs correlacionáveis; eventos de autenticação rastreáveis. |

### P2 — Evolução (2–6 semanas)

| # | Ação | Benefício |
|---|---|---|
| P2-1 | Remover Spring Data do domínio (`PageResult`/`PageQuery`) e mover transações para application/decorator | Domínio testável e independente de framework. |
| P2-2 | Tornar value objects imutáveis (`Location`, `SalaryRange`) e criar factories do agregado | Invariantes garantidas; menos estados inválidos. |
| P2-3 | Separar `security` em domain/application/infrastructure, com `UserAccountGateway` | Consistência arquitetural e testes mais simples. |
| P2-4 | Implementar refresh token rotativo, access token curto e revogação por dispositivo (`jti`) | Menor janela de exposição e melhor UX. |
| P2-5 | Adicionar OpenAPI/Swagger e gerar client TypeScript ou validar contrato no CI | Elimina drift backend/frontend. |
| P2-6 | Cache de catálogo de tecnologias e de `UserPrincipal` com invalidação | Menos queries e melhor latência. |
| P2-7 | Implementar gestão de utilizadores/roles, verificação de e-mail, reset de senha e, se necessário, MFA | Ciclo de vida de conta completo. |
| P2-8 | Trocar WAR por JAR (se não houver Tomcat externo), corrigir pacote `br.com.ats` e metadados do POM | Build/deploy mais simples e publicação viável. |
| P2-9 | Configurar pool/timeouts, batch fetch, cache HTTP e health check no `/actuator/health` | Operação mais previsível em produção. |

### P3 — Backlog

- CAPTCHA/proteção anti-bot no login.
- Rate limit distribuído com Redis.
- Auditoria completa (quem alterou qual vaga).
- Soft delete/recuperação de vagas.
- Métricas de negócio e dashboards.
- Testes de carga (k6/Gatling) e chaos básico.
- Revisão de LGPD/privacidade para dados de candidatos (quando existirem).

---

## 9. Conclusão

O projeto tem uma base arquitetural boa para o porte atual: módulos claros, uso correto de ports/adapters nos domínios de vagas e tecnologias, DTOs com records, Flyway, tratamento global de erros e uma implementação de JWT acima da média para projetos desse tamanho. A decisão de usar cookie `HttpOnly` e servir o Angular na mesma origem foi acertada.

O que separa o projeto de produção não é reescrever a arquitetura, mas **fechar lacunas de segurança e confiabilidade**:

1. **Segurança fail-closed** (remover `GET /** permitAll`, rate limit robusto, CSRF/logout coerentes).
2. **Consistência de dados** (optimistic locking real, update sem nulos, validação completa).
3. **Contrato de integração** (`/auth/me`, roles na UI, tipos TypeScript, erros/CSRF).
4. **Testes e CI** (hoje a suíte nem carrega o contexto; sem isso, nenhuma correção fica protegida contra regressão).

Com os P0 concluídos e os P1 encaminhados, o sistema passa de "demonstração controlada" para um patamar defensável de homologação/produção inicial.

---

## Apêndice A — Evidências de execução

```text
$ ./mvnw -o -q -DskipTests package
# sucesso; target/consult.api-0.0.1-SNAPSHOT.war gerado

$ ./mvnw -o -q test
# falha em ApplicationTests.contextLoads
# Caused by: org.postgresql.util.PSQLException:
# FATAL: autenticação do tipo senha falhou para o usuário "postgres" (SQLState 28P01)

$ npm run build -- --configuration development
# sucesso; dist/ats-frontend gerado

$ npm run build
# sucesso; bundle inicial 333,44 kB (86,84 kB estimados)
```

## Apêndice B — Arquivos-chave revisados

Backend:
- `pom.xml`
- `src/main/resources/application*.properties`
- `src/main/resources/db/migration/V1..V6`
- `src/main/java/com/br/ats/consult/api/security/**`
- `src/main/java/com/br/ats/consult/api/job/**`
- `src/main/java/com/br/ats/consult/api/technology/**`
- `src/main/java/com/br/ats/consult/api/shared/**`
- `src/test/**`

Frontend:
- `../ats-frontend/src/app/app.config.ts`
- `../ats-frontend/src/app/app.routes.ts`
- `../ats-frontend/src/app/core/**`
- `../ats-frontend/src/app/services/auth.service.ts`
- `../ats-frontend/src/app/features/**`
- `../ats-frontend/src/environments/**`

Deploy:
- `../Dockerfile`, `../docker-compose.prod.yml`, `../Caddyfile`, `../render.yaml`, `../.env.example`

## Apêndice C — Checklist de go-live

- [ ] `./mvnw verify` e `npm test` verdes em CI limpo.
- [ ] Nenhuma rota pública além de auth/health/SPA/estáticos; teste automatizado cobre.
- [ ] Rate limit por IP e por conta, com cache limitado; teste de 429.
- [ ] CSRF coerente com o modelo de deploy; logout revoga no servidor e falha é tratada no cliente.
- [ ] `COOKIE_SECURE=true`, `SameSite` válido e `JWT_SECRET` forte em produção.
- [ ] `SPRING_PROFILES_ACTIVE=prod` obrigatório no deploy; sem credenciais default.
- [ ] Update com optimistic locking e 409; `PUT` não aceita campos `NOT NULL` nulos.
- [ ] 404/400/401/403/409/429/500 com `ApiError` padronizado.
- [ ] `/auth/me` + roles na UI; `USER` não vê ações de escrita.
- [ ] Contratos TypeScript sem `unknown` nos payloads.
- [ ] Índices de performance aplicados; paginação/busca de tecnologias.
- [ ] Headers de segurança e health check configurados.
- [ ] Logs com correlation ID e auditoria de autenticação.
- [ ] Backup/restore do PostgreSQL testado.
- [ ] Análise de dependências (CVE) e SAST/DAST executadas.
