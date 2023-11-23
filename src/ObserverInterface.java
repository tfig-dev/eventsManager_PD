import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ObserverInterface extends Remote {

    void updateNewUser(User newUser) throws RemoteException;
}