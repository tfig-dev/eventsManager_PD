import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ObserverInterface extends Remote {
    void saveDatabaseLocally(byte[] content, String filepath) throws RemoteException;
    void createData(String pathName) throws RemoteException;
    void updateNewUser(User newUser) throws RemoteException;
    void updateEmailChange(User loggedUser, String newEmail) throws RemoteException;
    void updateNameChange(User loggedUser, String newName) throws RemoteException;
    void updatePasswordChange(User loggedUser, String newPassword) throws RemoteException;
    void updateNIFChange(User loggedUser, int newNIF) throws RemoteException;
    void updateNewAttendance(User loggedUser, String eventCode) throws RemoteException;
    void updateNewEvent(Event newEvent) throws RemoteException;
    void updateEventNameChange(int eventID, String newName) throws RemoteException;
    void updateEventLocalChange(int eventID, String newLocal) throws RemoteException;
    void updateEventDateChange(int eventID, String newDate) throws RemoteException;
    void updateEventStartTimeChange(int eventID, String newStartTime) throws RemoteException;
    void updateEventEndTimeChange(int eventID, String newEndTime) throws RemoteException;
    void updateEventDeletion(int eventID) throws RemoteException;
    void updateCodeGeneration(int eventID, int codeDuration) throws RemoteException;
    void updateParticipantDeletion(int eventID, String email) throws RemoteException;
    void updateParticipantAddition(int eventID, String email) throws RemoteException;
}