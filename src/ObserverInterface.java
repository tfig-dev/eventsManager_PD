import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ObserverInterface extends Remote {

    void updateNewUser(User newUser) throws RemoteException;
    void saveDatabaseLocally(byte[] content, String filepath) throws RemoteException;
    void createData(String pathName) throws RemoteException;
}