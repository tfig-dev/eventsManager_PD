package pt.isec.brago.eventsManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.io.FileOutputStream;
import java.rmi.server.UnicastRemoteObject;

public class Observer extends UnicastRemoteObject implements ObserverInterface {
    private Data data;
    public Observer() throws java.rmi.RemoteException {}

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
        if(data.registerUser(newUser)) System.out.println("Database updated successfully");
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateEmailChange(User loggedUser, String newEmail) throws RemoteException {
        if(data.changeEmail(loggedUser, newEmail)) System.out.println("Database updated successfully");
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateNameChange(User loggedUser, String newName) throws RemoteException {
        if(data.changeName(loggedUser, newName)) System.out.println("Database updated successfully");
        else System.out.println("Error updating the database");
    }

    @Override
    public void updatePasswordChange(User loggedUser, String newPassword) throws RemoteException {
        if(data.changePassword(loggedUser, newPassword)) System.out.println("Database updated successfully");
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateNIFChange(User loggedUser, int newNIF) throws RemoteException {
        if(data.changeNIF(loggedUser, newNIF)) System.out.println("Database updated successfully");
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateNewAttendance(User loggedUser, String eventCode) throws RemoteException {
        String status = data.checkEvent(eventCode, loggedUser);
        switch (status) {
            case "success":
                System.out.println("Database updated successfully");
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
        if(data.createEvent(newEvent)) System.out.println("Database updated successfully");
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateEventNameChange(int eventID, String newName) throws RemoteException {
        if(data.editEvent(eventID, newName, null, null, null, null)) System.out.println("Database updated successfully");
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateEventLocalChange(int eventID, String newLocal) throws RemoteException {
        if(data.editEvent(eventID, null, newLocal, null, null, null)) System.out.println("Database updated successfully");
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateEventDateChange(int eventID, String newDate) throws RemoteException {
        if(data.editEvent(eventID, null, null, newDate, null, null)) System.out.println("Database updated successfully");
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateEventStartTimeChange(int eventID, String newStartTime) throws RemoteException {
        if(data.editEvent(eventID, null, null, null, newStartTime, null)) System.out.println("Database updated successfully");
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateEventEndTimeChange(int eventID, String newEndTime) throws RemoteException {
        if(data.editEvent(eventID, null, null, null, null, newEndTime)) System.out.println("Database updated successfully");
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateEventDeletion(int eventID) throws RemoteException {
        if(data.deleteEvent(eventID)) System.out.println("Database updated successfully");
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateCodeGeneration(int eventID, int codeDuration) throws RemoteException {
        if(data.updateCode(eventID, codeDuration)) System.out.println("Database updated successfully");
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateParticipantDeletion(int eventID, String email) throws RemoteException {
        if(data.deleteParticipant(eventID, email)) System.out.println("Database updated successfully");
        else System.out.println("Error updating the database");
    }

    @Override
    public void updateParticipantAddition(int eventID, String email) throws RemoteException {
        if(data.addParticipant(eventID, email)) System.out.println("Database updated successfully");
        else System.out.println("Error updating the database");
    }

    public static void main(String[] args) throws IOException, NotBoundException {
        if(args.length != 2) {
            System.out.println("Deve passar 1 argumento na linha de comandos: ");
            System.out.println("1 - diretoria onde a base de dados sera guardada (tem que estar vazia)");
            System.out.println("2 - nome da base de dados");
            System.exit(1);
        }

        String databaseDirectory = args[0];
        String databaseName = args[1];

        // Create the new folder
        try {
            Files.createDirectories(Paths.get(databaseDirectory));
            System.out.println("Folder created successfully: " + Paths.get(databaseDirectory));
        } catch (Exception e) {
            System.err.println("Error creating folder: " + e.getMessage());
        }

        /* UNCOMMENT THIS AFTER
        String[] files = directory.list();
        if (files != null && files.length > 0) {
            System.out.println(databaseDirectory + " is not empty.");
            return;
        }

         */

        String objectUrl = "rmi://localhost/eventsManager_PD";
        ServerInterface mainServer = (ServerInterface) Naming.lookup(objectUrl);

        String pathName = databaseDirectory + "/" + databaseName;

        byte[] databaseContent = mainServer.getCompleteDatabase();

        ObserverInterface observer = new Observer();
        observer.saveDatabaseLocally(databaseContent, pathName);
        observer.createData(pathName);

        System.out.println("Servico pt.isec.brago.eventsManager.Observer criado e em execucao");

        mainServer.addObserver(observer);
        System.out.println("pt.isec.brago.eventsManager.Observer registado no servidor");

        /*
        mainServer.removeObserver(observer);
        UnicastRemoteObject.unexportObject(observer, true);
        */
    }
}
