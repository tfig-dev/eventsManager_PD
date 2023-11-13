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
        private User loggedUser;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.loggedUser = null;
        }
        private void printMenu(PrintStream pout) {
            if(loggedUser == null) {
                pout.println("1 - Login");
                pout.println("2 - Register");
                pout.println("3 - Exit");
            }
            else if(!loggedUser.isAdmin()) {
                pout.println("1 - Edit Account Details");
                pout.println("2 - Input Event Code");
                pout.println("3 - See past participations");
                pout.println("4 - Get CSV file");
                pout.println("4 - Logout");
            }
            else {
                pout.println("1 - Change Admin Account Details");
                pout.println("2 - Create Event");
                pout.println("3 - Edit Event");
                pout.println("4 - Delete Event");
                pout.println("5 - Check Events");
                pout.println("6 - Generate Event Code");
                pout.println("7 - Check Participants");
                pout.println("8 - Get CSV File");
                pout.println("9 - Check events by user participation");
                pout.println("10 - Get CSV File");
                pout.println("11 - Delete Participant to event");
                pout.println("12 - Add Participant to event");
                pout.println("13 - Logout");
            }

            pout.flush();
        }

        private void inputMenu(BufferedReader bin, String receivedMsg, PrintStream pout) throws IOException {
            if(loggedUser == null) {
                switch(receivedMsg) {
                    case "1":
                        pout.println("Email = ");
                        pout.flush();
                        String email = bin.readLine();
                        pout.println("Password = ");
                        pout.flush();
                        String password = bin.readLine();
                        loggedUser = data.authenticate(email, password);
                        if(loggedUser != null) {
                            pout.println("Login successful");
                        }
                        else pout.println("Login failed");
                        break;
                    case "2":
                        pout.println("Name = ");
                        pout.flush();
                        String name = bin.readLine();
                        pout.println("Email = ");
                        pout.flush();
                        email = bin.readLine();
                        pout.println("Password = ");
                        pout.flush();
                        password = bin.readLine();
                        pout.println("Identification Number = ");
                        pout.flush();
                        int identificationNumber = Integer.parseInt(bin.readLine());
                        User newUser = new User(name, identificationNumber, email, password);
                        data.registerUser(newUser);
                        break;
                    case "3":
                        break;
                    default:
                        pout.println("Invalid option");
                        break;
                }
            }
            pout.flush();
        }

        @Override
        public void run() {
            try (BufferedReader bin = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintStream pout = new PrintStream(clientSocket.getOutputStream())) {

                String receivedMsg;
                printMenu(pout);

                while ((receivedMsg = bin.readLine()) != null) {
                    inputMenu(bin, receivedMsg, pout);
                    printMenu(pout);
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
}
