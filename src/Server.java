import java.io.*;
import java.net.*;

public class Server {
    static Data data = new Data();
    public static void main(String[] args) {
        int listeningPort;

        if (args.length != 1) {
            System.out.println("Sintaxe: java Servidor listeningPort");
            return;
        }

        try {
            listeningPort = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(listeningPort)) {
            System.out.println("Servidor iniciado e á espera de conexões...");

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
                    Thread clientThread = new Thread(new ClientHandler(clientSocket));
                    clientThread.start();
                } catch (IOException e) {
                    System.out.println("Erro ao aceitar conexão do cliente: " + e);
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        } catch (IOException e) {
            System.out.println("Ocorreu um erro no servidor: " + e);
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader bin = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintStream pout = new PrintStream(clientSocket.getOutputStream())) {

                String receivedMsg;
                printMenu(pout);

                while ((receivedMsg = bin.readLine()) != null) {
                    System.out.println("Received: " + receivedMsg);

                    if (receivedMsg.equals("1")) {
                        pout.println("Email = ");
                        pout.flush();
                        String email = bin.readLine();
                        pout.println("Password = ");
                        pout.flush();
                        String password = bin.readLine();
                        if(data.authenticate(email, password)) pout.println("Login successful");
                        else pout.println("Login failed");
                    }
                    else if(receivedMsg.equals("3")) {
                        break;
                    }
                    else {
                        pout.println("Unknown command / Not Implemented");
                        pout.flush();
                    }
                }
            } catch (IOException e) {
                System.err.println("Communication error with the client: " + e);
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("Cliente desconectado: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
                } catch (IOException e) {
                    System.err.println("Error closing the client socket: " + e);
                }
            }
        }
    }

    private static void printMenu(PrintStream pout) {
        pout.println("1 - Login");
        pout.println("2 - Register");
        pout.println("3 - Exit");
        pout.flush();
        //TODO - Different menus for different type of user (Admin / User)
    }
}
