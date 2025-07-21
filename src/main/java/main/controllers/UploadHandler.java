package main.controllers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.services.FileParser;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Arrays;
import java.util.UUID;

import java.nio.charset.StandardCharsets;

public class UploadHandler implements HttpHandler {

    private static String uploadDir;
    private static FileParser fileParser;

    public UploadHandler(String s, FileParser f) {
        uploadDir = s;
        fileParser = f;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        /*
         * handles if not a [POST request]
         * */
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            String response = "method not allowed";
            exchange.sendResponseHeaders(405, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;

        }
        /* *
         * handling request headers by checking the content-type
         * */
        Headers requestHeaders = exchange.getRequestHeaders();
        String contentType = requestHeaders.getFirst("Content-Type");


        String res = "Bad Request: Content-Type must be multipart/form-data";
        if (contentType == null || !contentType.contains("multipart/form-data")) {
            System.out.println("Content-Type must be multipart/form-data " + contentType);
            exchange.sendResponseHeaders(400, res.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(res.getBytes());
            }
            return;
        }


//            exchange.sendResponseHeaders(200, -1);
        /*
         *
         * todo: upgrade this to send songs and videos [(mp4 and mp3) now in beta]
         * as of now it can take only .txt .pdf .jpg or any image category files
         * */

        String response = "got file";
        exchange.sendResponseHeaders(200, 0);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
//            System.out.println(contentType);

            /*
             * getting the boundary
             * */
            String boundary = contentType.substring(contentType.indexOf("boundary=") + 9);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(exchange.getRequestBody(), baos);
            String requestString = baos.toString("ISO_8859_1"); // converting it into string using ISO charset to preserve binary data

            // String fileName = requestString.substring(requestString.indexOf("filename=") + 10, requestString.indexOf("filename=") + 10 + 9);


            // -= -=-=-=-= -=-= -= (saving multiple files) [HTTP parsing form-data]  -=- =- =-= -=-=- =- -=
            String actualPathToSaveUploadData = uploadDir + File.separator + UUID.randomUUID().toString();
            File f = new File(actualPathToSaveUploadData);
            if (!f.exists()) { // if uploaderDir is not present then make them 🤣
                f.mkdirs();
            }


            String[] parts = requestString.split("--" + boundary);
            for (String part : parts) {
                if (part.contains("Content-Disposition") && part.contains("filename=")) {
                    // Extract filename
                    String disposition = part.substring(part.indexOf("Content-Disposition"), part.indexOf("\r\n\r\n"));
                    String fileName = disposition.split("filename=\"")[1].split("\"")[0]; // from AI

                    // if file name is not present
                    if (fileName == null) {
                        fileName = "unnamed-file" + "_" + UUID.randomUUID().toString();
                    }
                    // Extract file data (after the header and two CRLFs)
                    int dataIndex = part.indexOf("\r\n\r\n") + 4;
                    byte[] fileData = part.substring(dataIndex).getBytes(StandardCharsets.ISO_8859_1);

                    // Remove trailing boundary dashes if present
                    if (fileData[fileData.length - 2] == '-' && fileData[fileData.length - 1] == '-') {
                        fileData = Arrays.copyOf(fileData, fileData.length - 2);
                    }

                    // Save file
                    File outFile = new File(actualPathToSaveUploadData, fileName);

                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        fos.write(fileData);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }

                }
            }

            int port;
            try {
                port = fileParser.OfferFile(actualPathToSaveUploadData);
                String res1 = " " + port;
                String JSONResponse = "{\n\"port\" : " + port + "}";
                os.write(res1.getBytes()); // sending port as response
                new Thread(() -> {
                    try {
                        fileParser.startFileSharer(port);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        throw new RuntimeException(e);
                    }
                }).start();


            } catch (Exception e) {
                exchange.sendResponseHeaders(500, 0);
                String res2 = "Server Error";
                os.write(res2.getBytes());


                System.out.println("Error while processing file upload: " + e.getMessage());
                throw new RuntimeException(e);
            }


            // 1234567890   ---------------------------------
//                Pattern filenamePattern = Pattern.compile("filename=\"([^\"]+)\"");
//                Matcher matcher = filenamePattern.matcher(requestString);
//                String fileName = "uploaded_file"; // default name
//                if (matcher.find()) {
//                    fileName = matcher.group(1);
//                }
//
//
//                byte[] requestData = baos.toByteArray();
//
//                String fileHeader = "\r\n\r\n";
//                int fileStart = requestString.indexOf(fileHeader, matcher.end()) + fileHeader.length();
//                String boundaryMarker = "\r\n--" + boundary;
//                int fileEnd = requestString.indexOf(boundaryMarker, fileStart);
//                if (fileEnd == -1) fileEnd = requestData.length;
//                byte[] fileBytes = Arrays.copyOfRange(requestData, fileStart, fileEnd);

            //1234567890   -=---------------------------------


            // -=-=-=-=-=-=-= making folder for every upload -=-=-=-=-=-=-=-=

//                String actualPathToSaveUploadData = uploadDir + File.separator + UUID.randomUUID().toString();
//                File f = new File(actualPathToSaveUploadData);
//                if (!f.exists()) { // if uploaderDir is not present then make them 🤣
//                    f.mkdirs();
//                }
//                //   -------------------------
//
//                File outFile = new File(uploadDir, fileName);
//
//                try (FileOutputStream fos = new FileOutputStream(outFile)) {
//                    fos.write(fileBytes);
//
//                    Multiparser multiparser = new Multiparser(requestData, boundary);
//
//                } catch (Exception e) {
//                    System.out.println(e.getMessage());
//                }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // -=-=-=-=-=-=- for my design I don't need multiparser as of now -=-=-=-=-=-=-=

//    private static class Multiparser {
//        private final byte[] data;
//        private final String boundary;
//
//        public Multiparser(byte[] d, String bound) {
//            data = d;
//            boundary = bound;
//        }
//    }

}