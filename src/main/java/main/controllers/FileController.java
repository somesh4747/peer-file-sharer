package main.controllers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import main.services.FileParser;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.nio.charset.StandardCharsets;

public class FileController {
    private final FileParser fileParser;
    private final HttpServer server;
    private final String uploadDir;
    private final ExecutorService executorService;

    public FileController(int port) throws IOException {
        this.fileParser = new FileParser();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        /*
         * directory is specified for saving files to share
         */
        this.uploadDir = System.getProperty("java.io.tmpdir") + File.separator + "Shareable-uploads";
        /*
         * *
         * here we are using executorService for con-current request handling (10
         * simultaneously)
         */
        this.executorService = Executors.newFixedThreadPool(10);

        File uploadDirFiles = new File(uploadDir);
        if (!uploadDirFiles.exists()) { // if uploaderDir is not present then make them 🤣 (have fun, need to code anyway)
            uploadDirFiles.mkdirs();
        }

        /*
         * making routes to handle requests
         */
        server.createContext("/upload", new UploadHandler(uploadDir, fileParser));
        server.createContext("/download", new DownloadHandler());
        server.createContext("/", new CORShandler());
        server.setExecutor(executorService);

    }

    public void start() {
        server.start();
        System.out.println("Api server started : " + server.getAddress());

    }

    public void stop() {
        server.stop(0);
        executorService.shutdown();
        System.out.println("Api server stopped");
    }

    private class CORShandler implements HttpHandler {
        /*
         * here (CORS) headers are sent using the response for every request,
         * so that all the origins can be allowed to process the response in javascript
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // if not a "OPTIONS" http method then no need to handle in CORS (for root {/}
            // route)

            String response = "not found";
            exchange.sendResponseHeaders(404, response.getBytes().length);


            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
                System.out.println("hello");
                return;

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }


}