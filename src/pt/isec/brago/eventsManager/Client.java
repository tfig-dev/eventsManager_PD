package pt.isec.brago.eventsManager;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    private static boolean exit = false;

    public static void main(String[] args) {
        InetAddress serverAddr;
        int serverPort;
        String userInput;

        if (args.length != 2) {
            System.out.println("Syntax: java pt.isec.brago.eventsManager.Client serverAddress serverPort");
            return;
        }

        try {
            TerminalData.clearScreen();
            serverAddr = InetAddress.getByName(args[0]);
            serverPort = Integer.parseInt(args[1]);

            try (Socket socket = new Socket(serverAddr, serverPort);
                 Scanner scanner = new Scanner(System.in);
                 PrintStream pout = new PrintStream(socket.getOutputStream(), true)) {

                Thread responseThread = new Thread(new ResponseHandler(socket));
                responseThread.start();

                while (!exit) {
                    try {
                        userInput = scanner.nextLine();
                    } catch (Exception e) {
                        System.out.println("Error reading user input. Please try again.");
                        continue;
                    }
                    TerminalData.clearScreen();
                    pout.println(userInput);
                }
                responseThread.join();
            } catch (IOException e) {
                System.out.println("An error occurred: " + e.getMessage());
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted: " + e.getMessage());
            }
        } catch (UnknownHostException e) {
            System.out.println("Unknown destination: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Port must be a positive integer.");
        }
    }

    static class ResponseHandler implements Runnable {
        private final Socket socket;

        public ResponseHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader bin = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String response;

                while ((response = bin.readLine()) != null) {
                    if(response.equals("exit")) {
                        exit = true;
                        System.out.println("Connection closed. Press enter to exit.");
                        break;
                    }
                    System.out.println(response);
                }
            } catch (IOException e) {
                System.out.println("Error handling server response: " + e.getMessage());
            }
        }
    }
}