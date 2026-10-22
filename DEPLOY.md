# Deploy para teste remoto

O `Dockerfile` da raiz builda o Angular e o Spring Boot em um unico container. O Angular e servido pela propria API, entao front e back ficam na **mesma origem** (sem CORS e sem problema de cookie entre dominios).

> O deploy assume um monorepo com `ats-frontend/` e `consult.api/` no mesmo repositorio. Se `ats-frontend/.git` existir, remova-o antes de criar o repositorio raiz (`rm -rf ats-frontend/.git`) ou trate o frontend como submodulo. Caso contrario o Git/Render pode nao incluir o frontend no build.

## Antes de comecar

Gere um segredo JWT valido (Base64 com no minimo 32 bytes):

```bash
openssl rand -base64 48
```

Separe tambem as credenciais do banco e do administrador inicial:

```text
APP_ADMIN_EMAIL=admin@seudominio.com
APP_ADMIN_PASSWORD=uma-senha-forte
```

## Opcao A - Render + Neon (mais rapido)

O Render free dorme depois de 15 minutos sem acesso; o primeiro acesso depois disso pode levar ~1 minuto. O Postgres free do Render expira em 30 dias, por isso use o Neon.

1. Crie um banco gratuito no Neon e copie a connection string.
2. Converta para JDBC:

```text
DB_URL=jdbc:postgresql://SEU-HOST.neon.tech/neondb?sslmode=require
DB_USERNAME=SEU_USUARIO
DB_PASSWORD=SUA_SENHA
```

3. Suba o projeto para um repositorio no GitHub contendo `ats-frontend/`, `consult.api/`, `Dockerfile` e `render.yaml`.
4. No Render: **New > Blueprint**, selecione o repositorio e aplique o `render.yaml`.
5. Preencha as variaveis secretas:

| Variavel | Valor |
|---|---|
| `JWT_SECRET` | saida do `openssl rand -base64 48` |
| `DB_URL` | URL JDBC do Neon |
| `DB_USERNAME` | usuario do Neon |
| `DB_PASSWORD` | senha do Neon |
| `APP_ADMIN_EMAIL` | email do admin |
| `APP_ADMIN_PASSWORD` | senha forte do admin |

6. Aguarde o deploy e acesse `https://NOME-DO-SERVICO.onrender.com`.

O Render injeta `PORT`; a aplicacao ja respeita essa variavel.

## Opcao B - Oracle Cloud Always Free + Docker

Essa opcao nao dorme e e mais adequada para um teste mais longo. A conta exige cartao para verificacao, mas o tier Always Free nao cobra se voce ficar dentro dos limites.

1. Crie uma VM Always Free (Ampere ARM, Ubuntu 24.04). 1 OCPU e 6 GB ja bastam.
2. Libere as portas **80** e **443** no Security List da VCN e no firewall da VM.
3. Instale Docker e Git:

```bash
sudo apt update
sudo apt install -y git
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
```

Saia e entre novamente no SSH para o grupo `docker` valer.

4. Aponte um dominio/subdominio para o IP publico da VM. Para teste, o DuckDNS e gratuito:

```text
DOMAIN=ats-seu-nome.duckdns.org
```

5. Clone o projeto na VM, crie o `.env` e edite os valores:

```bash
git clone URL_DO_SEU_REPO ats-api-manager
cd ats-api-manager
cp .env.example .env
nano .env
```

6. Suba tudo:

```bash
docker compose --env-file .env -f docker-compose.prod.yml up -d --build
```

7. Acesse `https://SEU_DOMINIO`. O Caddy emite o certificado TLS automaticamente.

Para atualizar depois de um `git pull`:

```bash
docker compose --env-file .env -f docker-compose.prod.yml up -d --build
```

## Como funciona localmente

O perfil `dev` e o padrao e mantem o comportamento anterior:

```bash
# banco
docker compose -f consult.api/docker-compose.yml up -d

# API
export JWT_SECRET="$(openssl rand -base64 48)"
cd consult.api && ./mvnw spring-boot:run

# frontend
cd ats-frontend && npm start
```

No perfil `dev` o admin padrao continua sendo `admin@tnconsultoria.com` / `123456`. Em producao o admin so e criado se `APP_ADMIN_EMAIL` e `APP_ADMIN_PASSWORD` estiverem definidos.

## Variaveis de ambiente

| Variavel | Obrigatoria em prod | Descricao |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | sim (`prod`) | Ativa as configuracoes de producao |
| `JWT_SECRET` | sim | Base64 com >= 32 bytes |
| `DB_URL` | sim | JDBC do Postgres |
| `DB_USERNAME` | sim | Usuario do banco |
| `DB_PASSWORD` | sim | Senha do banco |
| `APP_ADMIN_EMAIL` | opcional | Cria o admin inicial |
| `APP_ADMIN_PASSWORD` | opcional | Senha do admin inicial |
| `COOKIE_SECURE` | nao | `true` em HTTPS |
| `COOKIE_SAME_SITE` | nao | `Lax` no mesmo dominio |
| `CORS_ALLOWED_ORIGINS` | nao | Só se o front estiver em outro dominio |
| `JWT_EXPIRATION_MS` | nao | Padrao 8 horas |
| `DOMAIN` | so no compose | Dominio usado pelo Caddy |

Se o frontend for hospedado separado da API (por exemplo, Angular na Netlify e API no Render), configure:

```text
CORS_ALLOWED_ORIGINS=https://SEU-FRONT
COOKIE_SECURE=true
COOKIE_SAME_SITE=None
```

`SameSite=None` so funciona com `Secure=true` e HTTPS.

## Checklist de teste

- Login e logout funcionam.
- Listagem e cadastro de vagas/tecnologias funcionam.
- Atualizar a pagina em `/jobs/1` nao da 404.
- O navegador mostra o cadeado HTTPS.
- O cookie `auth_token` aparece como `HttpOnly`, `Secure` e `SameSite=Lax`.
- A primeira resposta do Render free pode demorar por causa do cold start.

## Problemas comuns

- `JWT_SECRET nao esta definida`: defina a variavel no painel/compose.
- API retorna 403 apos login: o cookie nao foi enviado. Verifique `Secure`, `SameSite`, dominio e CORS.
- Flyway falha: confira `DB_URL`, usuario/senha e se o banco permite conexao SSL.
- Porta 8080 ocupada localmente: use `PORT=8081 ./mvnw spring-boot:run`.
