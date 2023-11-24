import java.io.File;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.io.FileOutputStream;
import java.rmi.server.UnicastRemoteObject;

public class Observer extends UnicastRemoteObject implements ObserverInterface {
    private Data data;
    public Observer() throws java.rmi.RemoteException {}


    @Override
    public void createData(String pathName) {
        try {
            data = new Data(pathName);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void updateNewUser(User newUser) throws RemoteException {
        if(data.registerUser(newUser)) System.out.println("A new user was registered");
        else System.out.println("A new user was not registered");
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

        File directory = new File(databaseDirectory);

        if (!directory.isDirectory()) {
            System.out.println(databaseDirectory + " is not a directory.");
            return;
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

        String pathName = databaseDirectory + "/" + databaseName + ".db";

        byte[] databaseContent = mainServer.getCompleteDatabase();

        ObserverInterface observer = new Observer();
        observer.saveDatabaseLocally(databaseContent, pathName);
        observer.createData(pathName);

        System.out.println("Servico Observer criado e em execucao");

        mainServer.addObserver(observer);
        System.out.println("Observer registado no servidor");

        /*
        mainServer.removeObserver(observer);
        UnicastRemoteObject.unexportObject(observer, true);
        */
    }
}
