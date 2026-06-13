package maculu;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

/**
 * Servidor HTTP da MaculuScript usando APENAS o que vem no JDK
 * ({@code com.sun.net.httpserver}) — sem dependências externas.
 *
 * Esta classe é a camada web: serve o frontend estático e expõe a API REST.
 * NÃO altera o núcleo do analisador — apenas chama
 * {@link AnalisadorLexico#analisar(String)}. A execução local/CLI ({@link Main})
 * continua a funcionar exactamente como antes.
 *
 * Endpoints:
 *   POST /api/analisar     corpo = código MaculuScript (texto cru, UTF-8)
 *                          devolve JSON { temErros, tokens, erros, tabelaSimbolos }
 *   GET  /api/reservadas   devolve JSON com o mapeamento reservada -> Java
 *   GET  /                 serve os ficheiros estáticos da pasta web/ (index.html, ...)
 *
 * Uso:
 *   java -cp out maculu.ServidorApi [porta] [pastaWeb]
 *   (porta por omissão 8080; pastaWeb por omissão "web"; também lê env PORT)
 *
 * Liga-se a 127.0.0.1 (localhost): o nginx faz o proxy reverso público.
 */
public class ServidorApi {

    private static final long TAMANHO_MAXIMO = 2_000_000; // 2 MB de código por pedido

    public static void main(String[] args) throws IOException {
        int porta = 8080;
        if (args.length >= 1) {
            porta = Integer.parseInt(args[0]);
        } else if (System.getenv("PORT") != null) {
            porta = Integer.parseInt(System.getenv("PORT"));
        }

        String pastaWeb = (args.length >= 2) ? args[1] : "web";
        Path webRoot = Paths.get(pastaWeb).toAbsolutePath().normalize();

        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", porta), 0);
        servidor.createContext("/api/analisar", new AnalisarHandler());
        servidor.createContext("/api/reservadas", new ReservadasHandler());
        servidor.createContext("/", new EstaticoHandler(webRoot));
        servidor.setExecutor(Executors.newFixedThreadPool(8));
        servidor.start();

        System.out.println("Servidor MaculuScript a correr em http://127.0.0.1:" + porta);
        System.out.println("Pasta web: " + webRoot);
        System.out.println("Endpoints: POST /api/analisar | GET /api/reservadas");
    }

    // ---------------------------------------------------------------- helpers

    private static void responder(HttpExchange ex, int codigo, String tipoConteudo, byte[] corpo)
            throws IOException {
        Headers h = ex.getResponseHeaders();
        h.set("Content-Type", tipoConteudo);
        // CORS aberto: simplifica testes e uso a partir de qualquer página.
        h.set("Access-Control-Allow-Origin", "*");
        h.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        h.set("Access-Control-Allow-Headers", "Content-Type");
        ex.sendResponseHeaders(codigo, corpo.length == 0 ? -1 : corpo.length);
        if (corpo.length > 0) {
            try (OutputStream os = ex.getResponseBody()) {
                os.write(corpo);
            }
        } else {
            ex.close();
        }
    }

    private static void responderJson(HttpExchange ex, int codigo, String json) throws IOException {
        responder(ex, codigo, "application/json; charset=utf-8", json.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean trataPreflight(HttpExchange ex) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            responder(ex, 204, "text/plain", new byte[0]);
            return true;
        }
        return false;
    }

    // ---------------------------------------------------------------- /api/analisar

    static class AnalisarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            try {
                if (trataPreflight(ex)) {
                    return;
                }
                if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                    responderJson(ex, 405, "{\"erro\":\"Use POST com o código no corpo do pedido.\"}");
                    return;
                }
                byte[] entrada = ex.getRequestBody().readAllBytes();
                if (entrada.length > TAMANHO_MAXIMO) {
                    responderJson(ex, 413, "{\"erro\":\"Código demasiado grande.\"}");
                    return;
                }
                String codigo = new String(entrada, StandardCharsets.UTF_8);
                ResultadoAnalise resultado = AnalisadorLexico.analisar(codigo);
                responderJson(ex, 200, JsonUtil.resultadoJson(resultado));
            } catch (Exception e) {
                // A camada web nunca deve rebentar: devolve 500 com mensagem.
                responderJson(ex, 500, "{\"erro\":\"" + JsonUtil.escape(String.valueOf(e.getMessage())) + "\"}");
            }
        }
    }

    // ---------------------------------------------------------------- /api/reservadas

    static class ReservadasHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            try {
                if (trataPreflight(ex)) {
                    return;
                }
                responderJson(ex, 200, JsonUtil.reservadasJson(new TabelaPalavrasReservadas()));
            } catch (Exception e) {
                responderJson(ex, 500, "{\"erro\":\"" + JsonUtil.escape(String.valueOf(e.getMessage())) + "\"}");
            }
        }
    }

    // ---------------------------------------------------------------- estáticos (frontend)

    static class EstaticoHandler implements HttpHandler {
        private final Path webRoot;

        EstaticoHandler(Path webRoot) {
            this.webRoot = webRoot;
        }

        @Override
        public void handle(HttpExchange ex) throws IOException {
            try {
                if (trataPreflight(ex)) {
                    return;
                }
                String caminho = ex.getRequestURI().getPath();
                if (caminho == null || caminho.equals("/")) {
                    caminho = "/index.html";
                }
                Path ficheiro = webRoot.resolve(caminho.substring(1)).normalize();
                // Protecção mínima contra path traversal (../).
                if (!ficheiro.startsWith(webRoot) || !Files.exists(ficheiro) || Files.isDirectory(ficheiro)) {
                    responder(ex, 404, "text/plain; charset=utf-8",
                            "404 - nao encontrado".getBytes(StandardCharsets.UTF_8));
                    return;
                }
                responder(ex, 200, tipoConteudo(ficheiro), Files.readAllBytes(ficheiro));
            } catch (Exception e) {
                responder(ex, 500, "text/plain; charset=utf-8",
                        ("500 - " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
            }
        }

        private static String tipoConteudo(Path f) {
            String n = f.getFileName().toString().toLowerCase();
            if (n.endsWith(".html")) return "text/html; charset=utf-8";
            if (n.endsWith(".css"))  return "text/css; charset=utf-8";
            if (n.endsWith(".js"))   return "application/javascript; charset=utf-8";
            if (n.endsWith(".json")) return "application/json; charset=utf-8";
            if (n.endsWith(".svg"))  return "image/svg+xml";
            if (n.endsWith(".ico"))  return "image/x-icon";
            if (n.endsWith(".png"))  return "image/png";
            return "application/octet-stream";
        }
    }
}
