import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ObserverInterface extends Remote {
    void notifyNewChanges(String description) throws RemoteException;
}