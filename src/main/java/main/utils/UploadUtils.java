package main.utils;

import java.util.Random;

public class UploadUtils {

    public static int getPort() {

//        has to generate port numbers
//        49152-65535

        /*
         *  the range for dynamic ports available in server to serve
         * */
        int STARTING_PORT = 49152;
        int ENDING_PORT = 65535;
        System.out.println("before return");
        Random number = new Random();
        
        return (number.nextInt(ENDING_PORT - STARTING_PORT)) + STARTING_PORT;


    }
}
