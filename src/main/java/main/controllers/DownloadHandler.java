package main.controllers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DownloadHandler implements HttpHandler {


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET");
        headers.add("Content-Type", "application/zip");
        headers.add("Content-Disposition", "attachment; filename=\"files.zip\"");


        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            String res = "Error : Method Not Allowed";
            exchange.sendResponseHeaders(405, 0);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(res.getBytes());
            }
            return;

        }


        String query = exchange.getRequestURI().getQuery();
        String port = query.substring(5);


        /*
         * checking if port is integer or not
         * */
        try {
            Integer.parseInt(port);

        } catch (NumberFormatException e) {
            exchange.sendResponseHeaders(405, 0);
            String res = "Invalid port number";
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(res.getBytes());
            }
            return;
        }

        // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
        // need to understand this part from LLM (ring buffer logic)
        // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
        try (
                Socket socket = new Socket("localhost", Integer.parseInt(port));
                InputStream socketInput = socket.getInputStream();
                ZipOutputStream zos = new ZipOutputStream(exchange.getResponseBody());
                BufferedInputStream bufferedInput = new BufferedInputStream(socketInput)
        ) {
            exchange.sendResponseHeaders(200, 0);

            byte[] marker = "\n---END_OF_FILE---\n".getBytes();
            int markerLength = marker.length;

            while (true) {
                // 1. Read header
                StringBuilder headerBuilder = new StringBuilder();
                int ch;
                while ((ch = bufferedInput.read()) != -1) {
                    if (ch == '\n') break;
                    headerBuilder.append((char) ch);
                }

                if (ch == -1) break; // EOF

                String header = headerBuilder.toString();
                if (!header.startsWith("Filename: ")) break;

                String fileName = header.substring(10).trim();
                System.out.println("Zipping: " + fileName);

                zos.putNextEntry(new ZipEntry(fileName));

                // 2. Stream file content and detect marker
                byte[] buffer = new byte[4096];
                byte[] window = new byte[markerLength];
                int windowPos = 0;
                int matchCount = 0;

                while (true) {
                    int b = bufferedInput.read();
                    if (b == -1) break;

                    // Shift bytes in window to the left
                    if (matchCount < markerLength) {
                        window[matchCount++] = (byte) b;
                    } else {
                        zos.write(window[0]); // Write the oldest byte in window
                        System.arraycopy(window, 1, window, 0, markerLength - 1);
                        window[markerLength - 1] = (byte) b;
                    }

                    // Check for marker match
                    if (matchCount == markerLength) {
                        boolean isMarker = true;
                        for (int i = 0; i < markerLength; i++) {
                            if (window[i] != marker[i]) {
                                isMarker = false;
                                break;
                            }
                        }
                        if (isMarker) {
                            break; // Found marker, stop reading this file
                        }
                    }
                }

                // Write remaining bytes (except marker)
                if (matchCount < markerLength) {
                    zos.write(window, 0, matchCount);
                } else {
                    zos.write(window, 0, markerLength - 1); // Write everything except the marker
                }

                zos.closeEntry();
            }

            zos.finish();
        } catch (Exception e) {
            exchange.sendResponseHeaders(400, 0);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write("Error : Download Error".getBytes());
            }
            System.out.print("Error in downloading files: ");
            System.out.println(e.getMessage());
            throw new RuntimeException(e);

        }
    }
    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // for handling single file transfer
    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

    //        try (Socket socket = new Socket("localhost", Integer.parseInt(port))) {
    //            InputStream socketInput = socket.getInputStream();
    //            OutputStream socketOutput = socket.getOutputStream();
    //
    //            // Read the header (filename) sent by FileParser
    //            StringBuilder headerBuilder = new StringBuilder();
    //            int ch;
    //            while ((ch = socketInput.read()) != -1) {
    //                if (ch == '\n') break;
    //                headerBuilder.append((char) ch);
    //            }
    //            String header = headerBuilder.toString();
    //            String fileName = "file";
    //
    //
    //            if (header.startsWith("Filename: ")) {
    //                fileName = header.substring(10).trim();
    //            }
    //
    //            headers.add("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    //            exchange.sendResponseHeaders(200, 0);
    //
    //            try (OutputStream os = exchange.getResponseBody()) {
    //                byte[] buffer = new byte[4096];
    //                int bytesRead;
    //                while ((bytesRead = socketInput.read(buffer)) != -1) {
    ////                    socketOutput.write(buffer, 0, bytesRead);
    //                    os.write(buffer, 0, bytesRead);
    //                }
    //
    //            }
    //    }


}
