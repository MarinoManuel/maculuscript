# Deploy da MaculuScript no servidor Ubuntu (maculuscript.sytes.net)

A aplicação Java serve **o frontend e a API** numa única porta local (8090).
O **nginx** faz apenas proxy reverso. Não há base de dados nem dependências
externas — só o JDK.

Resumo do fluxo:
```
Browser ──HTTPS──> nginx (443) ──proxy──> Java (127.0.0.1:8090)  [frontend + /api]
```

---

## 0. Antes de começar

- **DNS:** garanta que `maculuscript.sytes.net` (No-IP) aponta para o **IP público
  do servidor** (registo A). Teste: `ping maculuscript.sytes.net`.
- Tenha acesso `sudo` ao servidor.

---

## 1. Instalar o Java (JDK)

Não tinha Java instalado — instale o JDK (inclui `javac`, necessário para compilar):

```bash
sudo apt update
sudo apt install -y default-jdk
java -version      # confirme que aparece uma versao (ex.: 17 ou 21)
```

---

## 2. Obter o código (git)

Clone o repositório (ajuste a URL). Exemplo, para `/opt/maculuscript`:

```bash
sudo git clone <URL-DO-SEU-REPO> /opt/maculuscript
cd /opt/maculuscript/maculu-lexer
```

> A aplicação está na subpasta **`maculu-lexer`**. Todos os comandos seguintes
> assumem que está dentro dela.

---

## 3. Compilar

O `jflex-full-1.9.1.jar` já está no repositório (`lib/`), por isso não precisa de
baixar nada. Basta:

```bash
bash build.sh --no-run
```

Isto **gera** o `AnaLex.java` (jFlex) e **compila** tudo para `out/`
(`javac -encoding UTF-8`). No fim deve existir a pasta `out/maculu/` com os `.class`.

---

## 4. Testar manualmente (opcional mas recomendado)

```bash
bash run-api.sh 8090
```

Noutro terminal:
```bash
curl http://127.0.0.1:8090/api/reservadas | head -c 200
printf 'classe X { inteiro i = 1; }' | curl -s -X POST --data-binary @- http://127.0.0.1:8090/api/analisar
```

Deve ver JSON. Carregue `Ctrl+C` para parar.

---

## 5. Pôr a correr sempre (serviço systemd)

```bash
sudo cp deploy/maculuscript-api.service /etc/systemd/system/

# AJUSTE o WorkingDirectory para o caminho real (passo 2):
sudo nano /etc/systemd/system/maculuscript-api.service
#   WorkingDirectory=/opt/maculuscript/maculu-lexer

sudo systemctl daemon-reload
sudo systemctl enable --now maculuscript-api
sudo systemctl status maculuscript-api      # deve estar "active (running)"
```

Logs (se algo correr mal):
```bash
journalctl -u maculuscript-api -f
```

---

## 6. Configurar o nginx (proxy)

```bash
sudo cp deploy/nginx-maculuscript.conf /etc/nginx/sites-available/maculuscript
sudo ln -s /etc/nginx/sites-available/maculuscript /etc/nginx/sites-enabled/
sudo nginx -t            # testar a sintaxe
sudo systemctl reload nginx
```

> Se o seu nginx usa um único ficheiro em vez de `sites-available`, basta **colar
> o conteúdo** de `deploy/nginx-maculuscript.conf` junto dos outros `server { }`.

Teste em HTTP: abra `http://maculuscript.sytes.net` — já deve aparecer o site.

---

## 7. Ativar HTTPS (Let's Encrypt / certbot)

Se ainda não tiver o certbot:
```bash
sudo apt install -y certbot python3-certbot-nginx
```

Obtenha o certificado e deixe o certbot configurar o HTTPS + redirecionamento
automaticamente:
```bash
sudo certbot --nginx -d maculuscript.sytes.net
```

Abra **https://maculuscript.sytes.net** 🎉

---

## 8. Firewall (se usar ufw)

```bash
sudo ufw allow 'Nginx Full'     # abre 80 e 443
# a porta 8090 NAO precisa de ser aberta (fica so' em localhost)
```

---

## 9. Atualizar depois de mudanças (git pull)

```bash
cd /opt/maculuscript && sudo git pull
cd maculu-lexer && bash build.sh --no-run
sudo systemctl restart maculuscript-api
```

---

## 10. Resolução de problemas

| Sintoma | Verificar |
|---|---|
| 502 Bad Gateway | o serviço Java está a correr? `systemctl status maculuscript-api` |
| Página não abre | DNS aponta para o IP certo? porta 80/443 abertas? |
| `nginx -t` falha | sintaxe do bloco; cert ainda não existe (corra o certbot) |
| Porta 8090 ocupada | mude a porta no `.service` e no `nginx.conf` (têm de coincidir) |
| Acentos estranhos | já tratado: a API responde sempre em UTF-8 |

---

## Notas

- O **CLI continua a funcionar** para a correção/testes locais do docente:
  `java -cp out maculu.Main testes/valido_06_completo.maculu`.
- A camada web (`ServidorApi`, `JsonUtil`, `web/`) é **aditiva**: não altera o
  núcleo do analisador.
- Endpoints: `POST /api/analisar` (corpo = código), `GET /api/reservadas`.
