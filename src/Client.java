import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        InetAddress serverAddr;
        int serverPort;

        if (args.length != 2) {
            System.out.println("Sintaxe: java Cliente serverAddress serverPort");
            return;
        }

        try {
            serverAddr = InetAddress.getByName(args[0]);
            serverPort = Integer.parseInt(args[1]);

            try (Socket socket = new Socket(serverAddr, serverPort)) {
                Scanner scanner = new Scanner(System.in);

                Thread responseThread = new Thread(new ResponseHandler(socket));
                responseThread.start();

                while (true) {
                    String userInput = scanner.nextLine();

                    PrintStream pout = new PrintStream(socket.getOutputStream());
                    pout.println(userInput);
                    pout.flush();

                    if (userInput.equals("3")) {
                        break;
                    }
                }

                responseThread.join();
                scanner.close();
            }
        } catch (UnknownHostException e) {
            System.out.println("Destino desconhecido:\n\t" + e);
        } catch (NumberFormatException e) {
            System.out.println("O porto do servidor deve ser um inteiro positivo.");
        } catch (SocketTimeoutException e) {
            System.out.println("Nao foi recebida qualquer resposta:\n\t" + e);
        } catch (SocketException e) {
            System.out.println("Ocorreu um erro ao nível do socket TCP:\n\t" + e);
        } catch (IOException e) {
            System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted:\n\t" + e);
        }
    }

    static class ResponseHandler implements Runnable {
        private Socket socket;

        public ResponseHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader bin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response;

                while ((response = bin.readLine()) != null) {
                    System.out.println(response);
                }
            } catch (IOException e) {
                System.out.println("Error handling server response:\n\t" + e);
            }
        }
    }
}