# Login local

Guia rápido para subir o backend, o frontend e testar o login localmente. Para entender o fluxo completo de autenticação (sessão, CSRF, rate limit e frontend), consulte [`docs/fluxo-login.md`](../docs/fluxo-login.md).

## Requisitos

- Java 21
- Node.js e npm
- Docker e Docker Compose (opção recomendada)
- Portas `5432`, `8080` e `4200` disponíveis

## 1. Banco de dados

### Opção A: PostgreSQL em Docker

Se já existir um PostgreSQL local a ocupar a porta `5432`, pare-o primeiro:

```bash
sudo systemctl stop postgresql
sudo systemctl start docker
```

Depois suba o banco do projeto:

```bash
cd /home/diegocardoso/Documentos/ats-api-manager/consult.api
docker compose up -d
docker compose ps
```

O compose cria:

- Base: `ats_db`
- Utilizador: `postgres`
- Password: `postgres`
- Porta: `5432`

### Opção B: PostgreSQL local

Se preferires usar o PostgreSQL já instalado:

```bash
sudo -u postgres createdb ats_db
```

Depois define as variáveis ao arrancar o backend:

```bash
export DB_URL='jdbc:postgresql://localhost:5432/ats_db'
export DB_USERNAME='postgres'
export DB_PASSWORD='a-tua-password'
```

## 2. Backend

```bash
cd /home/diegocardoso/Documentos/ats-api-manager/consult.api

export SPRING_PROFILES_ACTIVE=dev

./mvnw spring-boot:run
```

O perfil `dev` é importante: desativa o CSRF para facilitar chamadas diretas à API e cria o administrador inicial. O Angular já trata do CSRF quando `CSRF_ENABLED=true`.

O auto-registo público está habilitado em `dev` e desativado por padrão em `prod` (`REGISTRATION_ENABLED`).

Verifica se a API subiu:

```bash
curl http://localhost:8080/actuator/health
```

Resposta esperada: `{"status":"UP"}`.

## 3. Frontend

Noutro terminal:

```bash
cd /home/diegocardoso/Documentos/ats-api-manager/ats-frontend
npm start
```

Abre:

```text
http://localhost:4200/login
```

## 4. Credenciais de desenvolvimento

```text
Email: admin@tnconsultoria.com
Senha: 123456
```

O administrador só é criado se esse email ainda não existir na base.

As variáveis `APP_ADMIN_EMAIL` e `APP_ADMIN_PASSWORD` substituem estes valores. Não uses estas credenciais em produção.

## 5. Testar o login pela API

Com o perfil `dev` ativo:

```bash
curl -i -c cookies.txt -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@tnconsultoria.com","password":"123456"}'
```

Resposta esperada: `HTTP/1.1 200` com a mensagem `Login efetuado com sucesso` e um cookie de sessão `ats_session` `HttpOnly`. A sessão fica no servidor; reiniciar o backend encerra todas as sessões.

Valida a sessão e consulta o usuário autenticado:

```bash
curl -b cookies.txt http://localhost:8080/api/v1/auth/me
```

Resposta esperada: `{"id":1,"email":"admin@tnconsultoria.com","role":"ADMIN"}`.

Termina a sessão (invalida no servidor e limpa o cookie):

```bash
curl -i -b cookies.txt -X POST http://localhost:8080/api/v1/auth/logout
```

## 6. Problemas comuns

### 403 - Acesso negado

O backend está a correr sem o perfil `dev`, portanto o CSRF está ativo. Confirma o arranque com:

```bash
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
```

Nos logs deve aparecer:

```text
The following 1 profile is active: "dev"
```

Se precisares de manter o CSRF ativo em chamadas manuais, obtém o cookie em `GET /api/v1/auth/csrf` e envia o valor no cabeçalho `X-XSRF-TOKEN`. O frontend faz isto automaticamente através do `csrf.interceptor.ts`.

### 401 - Credenciais inválidas

- Confirma o email e a password.
- Confirma que o backend arrancou com o perfil `dev`.
- Confirma que o administrador foi criado nos logs.

Para recriar a base local em Docker:

```bash
docker compose down -v
docker compose up -d
```

Atenção: `down -v` apaga os dados do volume local.

### Erro de conexão com o PostgreSQL

```bash
docker compose ps
docker compose logs -f postgres
```

Confirma também se existe outro PostgreSQL a ocupar a porta `5432`.

### Docker: permission denied

```bash
sudo usermod -aG docker "$USER"
```

Depois termina a sessão e volta a entrar para o grupo `docker` ficar ativo.

### Porta 8080 ocupada

```bash
PORT=8081 ./mvnw spring-boot:run
```

Se alterares a porta da API, confirma também a URL configurada no frontend.

## 7. Observações

- Os currículos ficam em `uploads/resumes/` por padrão; não é necessário configurar S3/R2 para testar localmente.
- O perfil `dev` destina-se apenas a desenvolvimento local.
- Para parar o backend: `Ctrl+C`.
- Para parar o banco Docker: `docker compose down` (os dados permanecem no volume).
