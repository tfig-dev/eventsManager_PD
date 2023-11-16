import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        InetAddress serverAddr;
        int serverPort;
        String userInput;

        if (args.length != 2) {
            System.out.println("Syntax: java Client serverAddress serverPort");
            return;
        }

        try {
            serverAddr = InetAddress.getByName(args[0]);
            serverPort = Integer.parseInt(args[1]);

            try (Socket socket = new Socket(serverAddr, serverPort);
                 Scanner scanner = new Scanner(System.in);
                 PrintStream pout = new PrintStream(socket.getOutputStream())) {

                Thread responseThread = new Thread(new ResponseHandler(socket));
                responseThread.start();

                while (true) {
                    try {
                        System.out.print("Enter your input: ");
                        userInput = scanner.nextLine();
                    } catch (Exception e) {
                        System.out.println("Error reading user input. Please try again.");
                        continue;
                    }

                    pout.println(userInput);
                    pout.flush();

                    if (userInput.equals("3")) {
                        break;
                    }
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
            System.out.println("Server port must be a positive integer.");
        }
    }

    static class ResponseHandler implements Runnable {
        private Socket socket;

        public ResponseHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader bin = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String response;

                while ((response = bin.readLine()) != null) {
                    System.out.println(response);
                }
            } catch (IOException e) {
                System.out.println("Error handling server response: " + e.getMessage());
            }
        }
    }
}