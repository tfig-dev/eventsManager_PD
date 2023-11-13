import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    public static final int TIMEOUT = 10;

    public static void main(String[] args) {

        InetAddress serverAddr;
        int serverPort;
        String response;

        if (args.length != 2) {
            System.out.println("Sintaxe: java Cliente serverAddress serverPort");
            return;
        }

        try {
            serverAddr = InetAddress.getByName(args[0]);
            serverPort = Integer.parseInt(args[1]);

            try (Socket socket = new Socket(serverAddr, serverPort)) {
                socket.setSoTimeout(TIMEOUT * 1000);
                Scanner scanner = new Scanner(System.in);

                while (true) {
                    System.out.print("Enter a command (type 'exit' to quit): ");
                    String userInput = scanner.nextLine();

                    if (userInput.equalsIgnoreCase("exit")) {
                        break;
                    }

                    PrintStream pout = new PrintStream(socket.getOutputStream());
                    pout.println(userInput);
                    pout.flush();

                    BufferedReader bin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    response = bin.readLine();
                    System.out.println("Server output: " + response);
                }

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
        }
    }
}
