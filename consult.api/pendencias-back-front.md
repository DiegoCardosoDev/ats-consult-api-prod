# Pendências por Backend e Frontend

> Baseado em `revisao-arquitetura.md` e no estado da implementação em 2026-10-22.

## Situação atual

- **P0-2 a P0-6:** implementados.
- **P0-1:** parcial. O perfil `test` usa H2; faltam Testcontainers PostgreSQL e pipeline CI.
- **Módulos ATS:** clientes, candidatos, processos seletivos e candidatura com currículo PDF implementados (Flyway V7 + API + Angular).
- **Currículos:** armazenamento local por padrão; adapter S3-compatible (Cloudflare R2/MinIO/AWS S3) disponível via `RESUME_STORAGE=s3`. Falta validar com um bucket real.

---

## Backend

### Armazenamento de currículos

- **Local (padrão):** `RESUME_STORAGE=local` e `RESUME_UPLOAD_DIR=uploads/resumes`.
- **Cloudflare R2/S3:** `RESUME_STORAGE=s3`, `RESUME_S3_BUCKET`, `RESUME_S3_REGION=auto`, `RESUME_S3_ENDPOINT=https://<account-id>.r2.cloudflarestorage.com`, `RESUME_S3_ACCESS_KEY`, `RESUME_S3_SECRET_KEY` e `RESUME_S3_FORCE_PATH_STYLE=false`.
- **MinIO:** usar `RESUME_S3_ENDPOINT=http://localhost:9000` e `RESUME_S3_FORCE_PATH_STYLE=true`.
- O prefixo das chaves é configurável com `RESUME_S3_PATH_PREFIX` (padrão `resumes`).

### P0

- [ ] Adicionar Testcontainers PostgreSQL para validar o schema/Flyway em banco real.
- [ ] Criar pipeline CI para executar `./mvnw verify` sem PostgreSQL local.

### P1

- [ ] Criar `GET /api/v1/auth/me` e retornar `role`/sessão no login.
- [ ] Remover o alias `/auth` e manter apenas `/api/v1/auth`.
- [ ] Adicionar headers de segurança: CSP, Referrer-Policy e Permissions-Policy.
- [ ] Adicionar `Cache-Control: no-store` e `WWW-Authenticate` em respostas 401/403.
- [ ] Remover defaults inseguros: DB `postgres/postgres`, cookie `Secure=false` por padrão e admin `123456` em dev.
- [ ] Criar índices para consultas por tecnologia e outras consultas críticas.
- [ ] Adicionar paginação/busca de tecnologias.
- [ ] Adicionar whitelist de campos de ordenação.
- [ ] Adicionar auditoria mínima: `last_login`, eventos de login/logout e correlation ID em MDC.
- [ ] Ampliar testes de use cases e segurança.
- [ ] Adicionar atualização/encerramento de processos seletivos.
- [x] Evoluir armazenamento local de currículos para storage de objetos (adapter S3-compatible).
- [ ] Validar o storage S3/R2 com um bucket real e documentar as credenciais de produção.
- [ ] Definir candidatura pública (portal do candidato) ou manter fluxo interno autenticado.
- [ ] Ativar cobertura de testes no CI.

### P2

- [ ] Remover Spring Data do domínio com `PageResult`/`PageQuery`.
- [ ] Mover transações para application/decorator.
- [ ] Tornar value objects imutáveis (`Location`, `SalaryRange`) e criar factories do agregado.
- [ ] Separar `security` em domain/application/infrastructure com `UserAccountGateway`.
- [ ] Implementar refresh token rotativo, access token curto e revogação por dispositivo (`jti`).
- [ ] Adicionar OpenAPI/Swagger.
- [ ] Adicionar cache de catálogo de tecnologias e de `UserPrincipal`.
- [ ] Implementar gestão de utilizadores/roles, verificação de e-mail, reset de senha e, se necessário, MFA.
- [ ] Trocar WAR por JAR, se não houver Tomcat externo.
- [ ] Corrigir pacote `br.com.ats` e metadados do POM.
- [ ] Configurar pool/timeouts, batch fetch, cache HTTP e health check no `/actuator/health`.

### P3

- [ ] Implementar rate limit distribuído com Redis.
- [ ] Adicionar auditoria completa de alterações de vagas.
- [ ] Implementar soft delete/recuperação de vagas.
- [ ] Adicionar métricas de negócio e dashboards.
- [ ] Adicionar validação de CAPTCHA/proteção anti-bot no backend.
- [ ] Revisar requisitos de LGPD/privacidade para dados de candidatos.

---

## Frontend

### P1

- [ ] Consumir `GET /api/v1/auth/me`.
- [ ] Criar `roleGuard` e esconder ações de escrita para `USER`.
- [ ] Atualizar todas as chamadas de `/auth` para `/api/v1/auth`.
- [ ] Separar tipos Angular de request/response.
- [ ] Remover `unknown` e tipar todos os enums.
- [ ] Centralizar tratamento de erros e notificações.
- [ ] Tratar 403 de CSRF globalmente com refresh + uma retentativa.
- [ ] Adaptar dropdown de tecnologias para paginação/busca.
- [ ] Adicionar edição de candidatos e clientes.
- [ ] Exibir/gerir o estado dos processos seletivos na UI.

### P2

- [ ] Gerar client TypeScript a partir do OpenAPI.
- [ ] Validar contrato backend/frontend no CI.
- [ ] Implementar fluxo de refresh token no interceptor.
- [ ] Criar UI de gestão de utilizadores/roles.
- [ ] Usar cache de catálogo de tecnologias.
- [ ] Ajustar telas para soft delete/recuperação de vagas.

### P3

- [ ] Adicionar widget de CAPTCHA.
- [ ] Criar telas/dashboards de métricas.
- [ ] Adicionar avisos de privacidade/LGPD.
- [ ] Adicionar testes E2E e testes de carga do frontend.

---

## Transversal

- [ ] Pipeline CI com backend e frontend.
- [ ] Cobertura de testes no CI.
- [ ] Testcontainers no pipeline.
- [ ] Testes E2E.
- [ ] Testes de carga com k6/Gatling.
- [ ] Chaos básico.

---

## Ordem sugerida

1. P0-1: Testcontainers + CI.
2. P1-1/P1-2: `/auth/me`, roles e padronização de rotas.
3. P1-3/P1-4: contratos tipados e tratamento global de erros/CSRF.
4. P1-5/P1-6: headers de segurança e defaults seguros.
5. P1-7/P1-9: índices, busca de tecnologias, sort e auditoria.
6. P2/P3 conforme prioridade de produto.
