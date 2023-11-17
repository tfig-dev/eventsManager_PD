import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

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
        } finally {
            data.closeConnection();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private User loggedUser;
        List<Event> events = new ArrayList<>();

        public ClientHandler(Socket clientSocket) throws SocketException {
            this.clientSocket = clientSocket;
            this.loggedUser = null;
            //this.clientSocket.setSoTimeout(10000); POR ENQUANTO NAO METER ISTO
        }

        private void handleInput(String userInput, BufferedReader bin, PrintStream pout) throws IOException {
            if(loggedUser == null) notLoggedInUserInput(userInput, bin, pout);
            else if (!loggedUser.isAdmin()) userInput(userInput, bin, pout);
            else adminInput(userInput, bin, pout);
        }

        private void handleMenu(PrintStream pout) {
            if(loggedUser == null) notLoggedInMenu(pout);
            else if (loggedUser.isAdmin()) adminMenu(pout);
            else userMenu(pout);
        }

        @Override
        public void run() {
            try (BufferedReader bin = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintStream pout = new PrintStream(clientSocket.getOutputStream(), true)) {

                String receivedMsg;

                handleMenu(pout);

                while ((receivedMsg = bin.readLine()) != null) {
                    handleInput(receivedMsg, bin, pout);
                    handleMenu(pout);
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

        private void notLoggedInMenu(PrintStream pout) {
            pout.println("1 - Login");
            pout.println("2 - Register");
            pout.println("3 - Exit");
            pout.println("Choice: ");
        }

        private void userMenu(PrintStream pout) {
            pout.println("1 - Edit Account Details");
            pout.println("2 - Input Event Code");
            pout.println("3 - See past participations");
            pout.println("4 - Get past participations CSV file");
            pout.println("5 - Logout");
            pout.println("Choice: ");
        }

        private void adminMenu(PrintStream pout) {
            pout.println("1 - Create Event");
            pout.println("2 - Edit Event");
            pout.println("3 - Delete Event");
            pout.println("4 - Check Events");
            pout.println("5 - Generate Event Code");
            pout.println("6 - Check Participants");
            pout.println("7 - Get CSV File");
            pout.println("8 - Check events by user participation");
            pout.println("9 - Get CSV File");
            pout.println("10 - Delete Participant to event");
            pout.println("11 - Add Participant to event");
            pout.println("12 - Logout");
            pout.println("Choice: ");
        }

        private void notLoggedInUserInput(String userInput, BufferedReader bin, PrintStream pout) throws IOException {
            String email, password, name;
            int NIF;

            switch(userInput) {
                case "1":
                    pout.println("Email = ");
                    email = bin.readLine();

                    pout.println("Password = ");
                    password = bin.readLine();

                    loggedUser = data.authenticate(email, password);
                    if(loggedUser != null) pout.println("Login successful");
                    else pout.println("Login failed");
                    break;
                case "2":
                    pout.println("Name: ");
                    name = bin.readLine();

                    pout.println("Email: ");
                    email = bin.readLine();

                    pout.println("Password: ");
                    password = bin.readLine();

                    pout.println("NIF: ");
                    NIF = Integer.parseInt(bin.readLine());

                    User newUser = new User(name, NIF, email, password);
                    if(data.registerUser(newUser)) pout.println("Registration successful");
                    else pout.println("Registration failed");
                    break;
                case "3":
                    pout.println("exit");
                    break;
                default:
                    pout.println("Invalid option");
                    break;
            }
        }

        private void userInput(String userInput, BufferedReader bin, PrintStream pout) throws IOException {
            String choice;
            switch(userInput) {
                case "1":
                    pout.println("Which account detail do you want to edit?");
                    pout.println("1 - Email");
                    pout.println("2 - Name");
                    pout.println("3 - Password");
                    pout.println("4 - NIF");
                    pout.println("5 - Exit");
                    pout.println("Choice: ");

                    choice = bin.readLine();

                    switch (choice) {
                        case "1":
                            pout.println("Enter new email: ");
                            String newEmail = bin.readLine();
                            if(data.changeEmail(loggedUser, newEmail)) pout.println("Email changed successfully");
                            else pout.println("Email already in use");
                            break;
                        case "2":
                            pout.println("Enter new name: ");
                            String newName = bin.readLine();
                            if(data.changeName(loggedUser, newName)) pout.println("Name changed successfully");
                            else pout.println("There was an error changing your name");
                            break;
                        case "3":
                            pout.println("Enter new password: ");
                            String newPassword = bin.readLine();
                            if(data.changePassword(loggedUser, newPassword)) pout.println("Password changed successfully");
                            else pout.println("There was an error changing your password");
                            break;
                        case "4":
                            pout.println("Enter new NIF: ");
                            int newNIF = Integer.parseInt(bin.readLine());
                            if(data.changeNIF(loggedUser, newNIF)) pout.println("NIF changed successfully");
                            else pout.println("There was an error changing your NIF");
                            break;
                        case "5":
                            break;
                        default:
                            pout.println("Invalid edit choice");
                            break;
                    }
                    break;
                case "2":
                    pout.println("Enter event code: ");
                    String eventCode = bin.readLine();
                    String status = data.checkEvent(eventCode, loggedUser);
                    switch (status) {
                        case "used" -> pout.println("This code was already used");
                        case "success" -> pout.println("Presence registered successfully");
                        case "error" -> pout.println("Invalid Code");
                        default -> pout.println("Something went wrong");
                    }
                    break;
                case "3":
                    pout.println("Participations filter: ");
                    pout.println("1 - Name");
                    pout.println("2 - By date");
                    pout.println("3 - Between dates");
                    pout.println("4 - Exit");
                    pout.println("Choice: ");

                    choice = bin.readLine();
                    String parameter;

                    switch (choice) {
                        case "1":
                            pout.println("Enter event name: ");
                            parameter = bin.readLine();
                            events = data.getAttendanceRecords(parameter, null,null, null);
                            if(events != null && !events.isEmpty()) pout.println(events);
                            else pout.println("There are no events with this filter");
                            break;
                        case "2":
                            pout.println("Enter event date (yyyy-mm-dd): ");
                            parameter = bin.readLine();
                            events = data.getAttendanceRecords(null, parameter,null, null);
                            if(events != null && !events.isEmpty()) pout.println(events);
                            else pout.println("There are no events with this filter");
                            break;
                        case "3":
                            pout.println("Enter start date (yyyy-mm-dd): ");
                            parameter = bin.readLine();
                            pout.println("Enter end date (yyyy-mm-dd): ");
                            String secondParameter = bin.readLine();
                            events = data.getAttendanceRecords(null, null,parameter, secondParameter);
                            if(events != null && !events.isEmpty()) pout.println(events);
                            else pout.println("There are no events with this filter");
                            break;
                        case "4":
                            break;
                        default:
                            pout.println("Invalid choice");
                            break;
                    }
                    break;
                case "4":
                    if(data.saveAttendanceRecords(events, loggedUser)) pout.println("CSV file generated successfully");
                    else pout.println("You must first get an output from option 3");
                    break;
                case "5":
                    loggedUser = null;
                    break;
                default:
                    pout.println("Invalid option");
                    break;
            }
        }

        private void adminInput(String userInput, BufferedReader bin, PrintStream pout) throws IOException{
            String choice;
            int eventID;
            switch(userInput) {
                case "1":
                    pout.println("Event Name: ");
                    String eventName = bin.readLine();
                    pout.println("Local: ");
                    String local = bin.readLine();
                    pout.println("Date (yyyy-mm-dd): ");
                    String date = bin.readLine();
                    pout.println("Start Time (HOUR:MINUTE): ");
                    String startTime = bin.readLine();
                    pout.println("End Time (HOUR:MINUTE): ");
                    String endTime = bin.readLine();
                    if(data.createEvent(eventName, local, date, startTime, endTime)) pout.println("Event created successfully");
                    else pout.println("There was an error creating the event");
                    break;
                case "2":
                    pout.println("Enter event ID: ");
                    eventID = Integer.parseInt(bin.readLine());
                    if(!data.checkIfEventCanBeEdited(eventID)) {
                        pout.println("This event already has participants. Cannot be edited");
                        break;
                    }
                    pout.println("What do you want to edit?");
                    pout.println("1 - Name");
                    pout.println("2 - Local");
                    pout.println("3 - Date");
                    pout.println("4 - Start Time");
                    pout.println("5 - End Time");
                    pout.println("6 - Exit");
                    pout.println("Choice: ");
                    choice = bin.readLine();
                    String parameter;

                    switch (choice) {
                        case "1":
                            pout.println("Enter new name: ");
                            parameter = bin.readLine();
                            if(data.editEvent(eventID, parameter, null, null, null, null)) pout.println("Event edited successfully");
                            else pout.println("There was an error editing the event");
                            break;
                        case "2":
                            pout.println("Enter new local: ");
                            parameter = bin.readLine();
                            if(data.editEvent(eventID, null, parameter, null, null, null)) pout.println("Event edited successfully");
                            else pout.println("There was an error editing the event");
                            break;
                        case "3":
                            pout.println("Enter new date (yyyy-mm-dd): ");
                            parameter = bin.readLine();
                            if(data.editEvent(eventID, null, null, parameter, null, null)) pout.println("Event edited successfully");
                            else pout.println("There was an error editing the event");
                            break;
                        case "4":
                            pout.println("Enter new start time (HOUR:MINUTE): ");
                            parameter = bin.readLine();
                            if(data.editEvent(eventID, null, null, null, parameter, null)) pout.println("Event edited successfully");
                            else pout.println("There was an error editing the event");
                            break;
                        case "5":
                            pout.println("Enter new end time (HOUR:MINUTE): ");
                            parameter = bin.readLine();
                            if(data.editEvent(eventID, null, null, null, null, parameter)) pout.println("Event edited successfully");
                            else pout.println("There was an error editing the event");
                            break;
                        case "6":
                            break;
                        default:
                            pout.println("Invalid choice");
                            break;
                    }
                    break;
                case "3":
                    break;
                case "4":
                    break;
                case "5":
                    pout.println("Enter event ID: ");
                    eventID = Integer.parseInt(bin.readLine());
                    pout.println("Enter code duration (minutes): ");
                    int codeDuration = Integer.parseInt(bin.readLine());
                    if(data.updateCode(eventID, codeDuration)) pout.println("Code generated successfully");
                    else pout.println("Event does not exist");
                    break;
                case "6":
                    break;
                default:
                    pout.println("Invalid option");
                    break;
            }
        }
    }
}
