import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ObserverInterface extends Remote {
    void updateDatabase(String operation) throws RemoteException;
}