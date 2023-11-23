import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Observer extends UnicastRemoteObject implements ObserverInterface {
    public Observer() throws java.rmi.RemoteException {}

    @Override
    public void updateDatabase() throws RemoteException {
        System.out.println("I probably should update the database here....");
    }

    public static void main(String[] args) throws IOException, NotBoundException {
        /*
        if(args.length != 1) {
            System.out.println("Deve passar 1 argumento na linha de comandos: ");
            System.out.println("1 - diretoria onde a base de dados sera guardada (tem que estar vazia)");
            System.exit(1);
        }

        String databaseDirectory = args[0];
        */

        String databaseDirectory = "src/datafiles/backups/server1";


        String objectUrl = "rmi://localhost/eventsManager_PD";
        ServerInterface mainServer = (ServerInterface) Naming.lookup(objectUrl);
        ObserverInterface observer = new Observer();
        System.out.println("Servico Observer criado e em execucao");

        mainServer.addObserver(observer);
        System.out.println("Observer registado no servidor");

        /*
        System.out.println("<Enter> para terminar");
        System.out.println();
        System.in.read();
        mainServer.removeObserver(observer);
        UnicastRemoteObject.unexportObject(observer, true);
        */
    }
}
