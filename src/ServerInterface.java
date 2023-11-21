import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    void addObserver(ObserverInterface backupServer) throws RemoteException;
    void removeObserver(ObserverInterface backupServer) throws RemoteException;
}
