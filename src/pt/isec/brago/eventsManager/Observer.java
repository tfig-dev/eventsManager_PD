package pt.isec.brago.eventsManager;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;
import java.util.TimerTask;

public class Observer extends UnicastRemoteObject implements ObserverInterface {
    private Data data;
    private ServerInterface mainServer;
    private final InetAddress group;
    private final MulticastSocket socket;

    public Observer() throws IOException {
        //MULTICAST
        group = InetAddress.getByName("230.44.44.44");
        int port = 4444;
        socket = new MulticastSocket(port);
        socket.setSoTimeout(30000);
        socket.joinGroup(group);
    }

    @Override
    public void saveDatabaseLocally(byte[] content, String filepath) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(filepath)) {
            fileOutputStream.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createData(String pathName) {
        try {
            data = new Data(pathName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateNewUser(User newUser) throws RemoteException {
        if(data.registerUser(newUser)) {
            System.out.println("Database updated successfully");
            data.updateVersion();
        }
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateEmailChange(User loggedUser, String newEmail) throws RemoteException {
        if(data.changeEmail(loggedUser, newEmail)) {
            System.out.println("Database updated successfully");
            data.updateVersion();
        }
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateNameChange(User loggedUser, String newName) throws RemoteException {
        if(data.changeName(loggedUser, newName)) {
            System.out.println("Database updated successfully");
            data.updateVersion();
        }
        else System.out.println("Error updating the database");
    }

    @Override
    public void updatePasswordChange(User loggedUser, String newPassword) throws RemoteException {
        if(data.changePassword(loggedUser, newPassword)) {
            System.out.println("Database updated successfully");
            data.updateVersion();
        }
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateNIFChange(User loggedUser, int newNIF) throws RemoteException {
        if(data.changeNIF(loggedUser, newNIF)) {
            System.out.println("Database updated successfully");
            data.updateVersion();
        }
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateNewAttendance(User loggedUser, String eventCode) throws RemoteException {
        String status = data.checkEvent(eventCode, loggedUser);
        switch (status) {
            case "success":
                System.out.println("Database updated successfully");
                data.updateVersion();
                break;
            case "used":
            case "error":
            default:
                System.out.println("Error updating the database");
                break;
        }
    }

    @Override
    public void updateNewEvent(Event newEvent) throws RemoteException {
        if(data.createEvent(newEvent)) {
            System.out.println("Database updated successfully");
            data.updateVersion();
        }
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateEventNameChange(int eventID, String newName) throws RemoteException {
        if(data.editEvent(eventID, newName, null, null, null, null)) {
            System.out.println("Database updated successfully");
            data.updateVersion();
        }
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateEventLocalChange(int eventID, String newLocal) throws RemoteException {
        if(data.editEvent(eventID, null, newLocal, null, null, null)) {
            System.out.println("Database updated successfully");
            data.updateVersion();
        }
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateEventDateChange(int eventID, String newDate) throws RemoteException {
        if(data.editEvent(eventID, null, null, newDate, null, null)) {
            System.out.println("Database updated successfully");
            data.updateVersion();
        }
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateEventStartTimeChange(int eventID, String newStartTime) throws RemoteException {
        if(data.editEvent(eventID, null, null, null, newStartTime, null)) {
            System.out.println("Database updated successfully");
            data.updateVersion();
        }
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateEventEndTimeChange(int eventID, String newEndTime) throws RemoteException {
        if(data.editEvent(eventID, null, null, null, null, newEndTime)) {
            System.out.println("Database updated successfully");
            data.updateVersion();
        }
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateEventDeletion(int eventID) throws RemoteException {
        if(data.deleteEvent(eventID)) {
            System.out.println("Database updated successfully");
            data.updateVersion();
        }
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateCodeGeneration(int eventID, int codeDuration, String generatedCode) throws RemoteException {
        if(data.updateCode(eventID, codeDuration, generatedCode)) {
            System.out.println("Database updated successfully");
            data.updateVersion();
        }
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateParticipantDeletion(int eventID, String email) throws RemoteException {
        if(data.deleteParticipant(eventID, email)) {
            System.out.println("Database updated successfully");
            data.updateVersion();
        }
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateParticipantAddition(int eventID, String email) throws RemoteException {
        if(data.addParticipant(eventID, email)) {
            System.out.println("Database updated successfully");
            data.updateVersion();
        }
        else System.out.println("Error updating the database");
    }

    @Override
    public int getVersion() throws RemoteException {
        return data.getVersion();
    }

    @Override
    public void endObserver() throws RemoteException {
        try {
            socket.leaveGroup(group);
            socket.close();
            mainServer.removeObserver(this);
            UnicastRemoteObject.unexportObject(this, true);
            System.out.println("Press enter to continue...");
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, NotBoundException {
        if(args.length != 5) {
            System.out.println("Deve passar 1 argumento na linha de comandos: ");
            System.out.println("1 - diretoria onde a base de dados sera guardada (tem que estar vazia)");
            System.out.println("2 - nome da base de dados");
            System.out.println("3 - IP");
            System.out.println("4 - RMI service name");
            System.out.println("5 - RMI port");
            System.exit(1);
        }

        TerminalData.clearScreen();
        System.out.println("Observer starting...");

        String databaseDirectory = args[0];
        String databaseName = args[1];

        try {
            Path dir = Paths.get(databaseDirectory);
            Files.createDirectories(dir);

            String[] files = dir.toFile().list();
            if (files != null && files.length > 0) {
                System.out.println(databaseDirectory + " is not empty.");
                System.in.read();
                return;
            }
        } catch (Exception e) {
            System.err.println("Error creating folder: " + e.getMessage());
        }

        Observer observer = new Observer();

        String objectUrl = "rmi://" + args[2] + ":" + args[4] + "/" + args[3];
        observer.mainServer = (ServerInterface) Naming.lookup(objectUrl);
        String pathName = databaseDirectory + "/" + databaseName;
        byte[] databaseContent = observer.mainServer.getCompleteDatabase();

        observer.saveDatabaseLocally(databaseContent, pathName);
        observer.createData(pathName);

        System.out.println("Observer criado e em execucao");

        observer.mainServer.addObserver(observer);

        heartbeatHandler heartbeatReceiver = new heartbeatHandler(observer);
        Thread receiverThread = new Thread(heartbeatReceiver);
        receiverThread.start();

        System.out.println("Observer registado no servidor");
    }

    static class heartbeatHandler implements Runnable {
        private final Observer observer;

        public heartbeatHandler(Observer observer) {
            this.observer = observer;
        }

        @Override
        public void run() {
            while (true) {
                byte[] buffer = new byte[200];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    observer.socket.receive(packet);
                    Heartbeat receivedHeartbeat = deserializeHeartbeat(packet.getData());
                    if(receivedHeartbeat != null && receivedHeartbeat.getVersion() != observer.data.getVersion()) {
                        System.out.println("Received heartbeat version does not match the local version. Terminating...");
                        observer.endObserver();
                        break;
                    }
                    System.out.println("Received heartbeat: " + receivedHeartbeat);
                } catch (SocketTimeoutException e) {
                    System.out.println("No heartbeat received. Terminating...");
                    try {
                        observer.endObserver();
                        break;
                    } catch (RemoteException re) {
                        throw new RuntimeException(re);
                    }
                } catch (IOException | RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }

        private Heartbeat deserializeHeartbeat(byte[] data) {
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
                return (Heartbeat) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
