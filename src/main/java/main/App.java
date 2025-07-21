package main;

import main.controllers.FileController;
import main.utils.UploadUtils;
import org.apache.commons.io.IOExceptionWithCause;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {
        try {
            FileController fc = new FileController(55555);
            fc.start();
            Runtime.getRuntime().addShutdownHook(new Thread(
                    () -> {
                        System.out.println("Shutting Down API server");
                        fc.stop();

                    }
            ));
            System.out.println("Type '333' to stop the server...");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String input;
                while ((input = reader.readLine()) != null) {
                    if (input.trim().equals("333")) {
                        System.exit(0); // This will trigger the shutdown hook
                    } else {
                        System.out.println("Invalid input. Type '333' to stop the server.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }


    }

}
