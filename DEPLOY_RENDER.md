# Deploy no Render (passo a passo)

Este guia sobe o projeto inteiro em um único serviço do Render:

- Angular buildado e embutido no WAR do Spring Boot
- API e frontend na mesma origem (sem CORS e sem problema de cookie)
- PostgreSQL gratuito no Neon (o Postgres free do Render expira em 30 dias)

> O Render free é adequado para demonstração/teste com cliente. O serviço dorme após 15 minutos sem acesso e o primeiro acesso depois disso pode demorar cerca de 1 minuto.

---

## 1. Sessões no servidor

A autenticação usa sessões opacas mantidas em memória pelo Spring Boot. Não há segredo JWT para gerar. O timeout padrão é de 30 minutos de inatividade (`SESSION_TIMEOUT`) e o cookie é `ats_session` (`SESSION_COOKIE_NAME`).

> Como a sessão fica em memória, cada deploy/reinício do Render encerra as sessões e os usuários precisam entrar novamente. Para múltiplas réplicas, seria necessário Spring Session com Redis ou banco.

---

## 2. Criar o banco no Neon

1. Acesse https://neon.tech e crie uma conta gratuita.
2. Crie um projeto (pode ser na região US East ou na mais próxima do Brasil).
3. Abra **Connect** e copie:
   - host
   - database
   - user
   - password
4. Monte a URL JDBC no formato:

```text
jdbc:postgresql://SEU-HOST.neon.tech/SEU-DATABASE?sslmode=require
```

Exemplo:

```text
DB_URL=jdbc:postgresql://ep-cool-name-123456.us-east-2.aws.neon.tech/neondb?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=SUA_SENHA_DO_NEON
```

O `?sslmode=require` é importante para a conexão com o Neon.

---

## 3. Preparar o repositório no GitHub

O Render precisa de um repositório contendo **a raiz do projeto**, com `ats-frontend/`, `consult.api/`, `Dockerfile` e `render.yaml`.

Se o diretório `ats-frontend` tiver um `.git` próprio, remova antes de criar o repositório raiz:

```bash
cd /caminho/para/ats-api-manager
rm -rf ats-frontend/.git
```

Se ainda não existe repositório na raiz:

```bash
git init
git add .
git commit -m "Prepara deploy no Render"
git branch -M main
git remote add origin https://github.com/SEU-USUARIO/SEU-REPO.git
git push -u origin main
```

Confirme que `ats-frontend/`, `consult.api/`, `Dockerfile` e `render.yaml` aparecem no GitHub.

Não envie o arquivo `.env`; ele já está no `.gitignore`.

---

## 4. Criar o serviço no Render

1. Acesse https://render.com e entre com o GitHub.
2. Clique em **New +** e escolha **Blueprint**.
3. Selecione o repositório do projeto.
4. O Render vai ler o `render.yaml` e mostrar o serviço `ats-consult-api`.
5. Dê um nome ao Blueprint e clique em **Apply**.
6. Quando o Render pedir as variáveis secretas, preencha:

| Variável | Valor |
|---|---|
| `DB_URL` | URL JDBC do Neon |
| `DB_USERNAME` | usuário do Neon |
| `DB_PASSWORD` | senha do Neon |
| `APP_ADMIN_EMAIL` | email do administrador do sistema |
| `APP_ADMIN_PASSWORD` | senha forte do administrador |

As variáveis `SPRING_PROFILES_ACTIVE=prod`, `COOKIE_SECURE=true`, `COOKIE_SAME_SITE=Strict`, `SESSION_TIMEOUT=30m` e `SESSION_COOKIE_NAME=ats_session` já vêm definidas no `render.yaml`.

Não defina `PORT`: o Render injeta essa variável automaticamente e a aplicação já respeita.

Se quiser menor latência para o Brasil, altere no `render.yaml`:

```yaml
region: oregon
```

Para:

```yaml
region: ohio
```

---

## 5. Acompanhar o primeiro deploy

1. Abra o serviço no dashboard do Render.
2. Acompanhe a aba **Logs**.
3. O build executa:
   - `npm ci` e build do Angular
   - `mvn package` do Spring Boot
   - cópia do frontend para dentro do WAR
4. O primeiro deploy pode demorar vários minutos.
5. Você deve ver nos logs algo como:

```text
Started Application
Utilizador administrador inicial criado: SEU_EMAIL
```

6. Quando o status ficar **Live**, abra a URL:

```text
https://NOME-DO-SERVICO.onrender.com
```

---

## 6. Testar o sistema

Na URL do Render:

1. Faça login com `APP_ADMIN_EMAIL` e `APP_ADMIN_PASSWORD`.
2. Cadastre/liste uma vaga ou tecnologia.
3. Atualize a página em uma rota interna, por exemplo `/jobs/1`.
   - Deve continuar funcionando, sem 404.
4. Faça logout.
5. No DevTools do navegador, em **Application > Cookies**, confirme que o cookie `ats_session` está:
   - `HttpOnly`
   - `Secure`
   - `SameSite=Strict`
6. Confirme que o cadeado HTTPS aparece na barra de endereço.

---

## 7. Atualizações

Depois do primeiro deploy, cada `git push` na branch conectada dispara um novo deploy (Auto-Deploy).

Para forçar manualmente:

1. Abra o serviço no Render.
2. Clique em **Manual Deploy**.
3. Escolha **Deploy latest commit**.

As migrations do Flyway rodam automaticamente no startup. O banco do Neon é persistente, então os dados continuam entre deploys.

---

## 8. Problemas comuns

### Build falhou porque não achou o frontend

O `ats-frontend` provavelmente foi enviado como repositório embutido. Remova o `.git` interno e faça o push novamente:

```bash
rm -rf ats-frontend/.git
git add .
git commit -m "Remove git interno do frontend"
git push
```

### Usuários precisam de login após cada deploy

Esperado: as sessões ficam em memória e são encerradas no reinício. Se isso for um problema, configure Spring Session com Redis ou banco para persistir as sessões.

### Erro de conexão com o banco

Confira:

- `DB_URL` começa com `jdbc:postgresql://`
- o banco termina com `/SEU_DATABASE?sslmode=require`
- `DB_USERNAME` e `DB_PASSWORD` são os do Neon
- o projeto do Neon não está pausado

### Aplicação sobe mas o login não mantém a sessão

Como front e API estão na mesma origem, o cookie deve funcionar. Verifique:

- está acessando por `https://`, não por IP ou HTTP
- `COOKIE_SECURE=true`
- `COOKIE_SAME_SITE=Strict`
- o navegador não está bloqueando cookies para o domínio
- a sessão não expirou por inatividade (`SESSION_TIMEOUT=30m` por padrão)

### Primeiro acesso muito lento

É o cold start do Render free. O serviço dorme após 15 minutos sem tráfego. Para um teste com cliente, avise que o primeiro acesso pode levar ~1 minuto, ou use a opção Oracle Cloud do `DEPLOY.md`.

### Banco sumiu depois de 30 dias

O Postgres free do Render expira. Este guia usa o Neon justamente para evitar isso. Não crie um banco Postgres free no Render para este projeto.

---

## 9. Limitações do Render free

- 512 MB de RAM e 0,1 CPU
- 750 horas de instância por mês
- O serviço dorme após 15 minutos sem acesso
- Cold start de aproximadamente 1 minuto
- O Render recomenda o free tier apenas para testes, não para produção crítica

Para um ambiente always-on sem cold start, use o caminho da Oracle Cloud descrito em `DEPLOY.md`.
