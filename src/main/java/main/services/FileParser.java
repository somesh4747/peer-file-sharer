package main.services;

import main.utils.UploadUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class FileParser {

    private static HashMap<Integer, String> files;

    public FileParser() {
        files = new HashMap<>();

    }

    // TODO : add one timer, suppose the server cant generate a new port
    public int OfferFile(String filePath) {
        int port = 0;

        while (true) {
            port = UploadUtils.getPort();
            if (!files.containsKey(port)) {
                files.put(port, filePath);
                return port;
            }
        }
    }

    public void startFileSharer(int port) throws Exception {
        String filePath = files.getOrDefault(port, "NOT_FOUND");
        /*
         * checking if port number is valid or file is not present
         */
        if (filePath.contains("NOT_FOUND")) {
            System.out.println("Invalid port number or File is not present");
            throw new Exception("Invalid port number or File is not present");
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.print("Server socket started at : " + port);
            System.out.println(" file is ->> " + new File(filePath).getName());

            // TODO : keep a 10 min limit to transfer files
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // clientSocket.getInputStream()
                System.out.println("Client Socket connected at : " + port);
                System.out.println("Client Address : " + clientSocket.getInetAddress());
                /*
                 * new thread to handle every request
                 */
                new Thread(() -> {
                    // @Override
                    // public void run() {
                    try {
                        OutputStream clientOutput = clientSocket.getOutputStream();
                        File folder = new File(filePath);
                        File[] filesInFolder = folder.listFiles();
                        if (filesInFolder == null || filesInFolder.length == 0) {
                            clientOutput.write("No files found in the folder.\n".getBytes());
                        } else {
                            for (File file : filesInFolder) {
                                if (file.isFile()) {
                                    String fileName = file.getName();
                                    String header = "Filename: " + fileName + "\n";
                                    clientOutput.write(header.getBytes());

                                    try (FileInputStream fis = new FileInputStream(file)) {
                                        byte[] buffer = new byte[4096];
                                        int byteRead;
                                        while ((byteRead = fis.read(buffer)) != -1) {
                                            clientOutput.write(buffer, 0, byteRead);
                                        }
                                    }
                                    clientOutput.write("\n---END_OF_FILE---\n".getBytes());
                                    // clientOutput.flush(); // [ from GPT ] to clean outputStream after current
                                    // file sent
                                    System.out.println("File " + fileName + " sent to the address "
                                            + clientSocket.getInetAddress());
                                }
                            }
                        }
                    } catch (IOException e) {

                        System.out.println(
                                "file sending issue at " + port + " client address : " + clientSocket.getInetAddress());
                        throw new RuntimeException(e);

                    } finally {
                        try {
                            clientSocket.close();
                        } catch (Exception e) {
                            System.err.println("error at client socket closing " + e.getMessage());
                        }

                    }

                    // }
                }).start();
            }
        } catch (IOException e) {
            System.out.println(
                    e.getMessage());
            throw new IOException();

        }

    }

}
