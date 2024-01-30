package pt.isec.brago.eventsManager;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    void addObserver(ObserverInterface backupServer) throws RemoteException;
    void removeObserver(ObserverInterface backupServer) throws RemoteException;
    byte[] getCompleteDatabase() throws RemoteException;
    void notifyNewUser(User newUser) throws RemoteException;
    void notifyEmailChange(User loggedUser, String newEmail) throws RemoteException;
    void notifyNameChange(User loggedUser, String newName) throws RemoteException;
    void notifyPasswordChange(User loggedUser, String newPassword) throws RemoteException;
    void notifyNIFChange(User loggedUser, int newNIF) throws RemoteException;
    void notifyNewAttendance(User loggedUser, String eventCode) throws RemoteException;
    void notifyNewEvent(Event newEvent) throws RemoteException;
    void notifyEventNameChange(int eventID, String newName) throws RemoteException;
    void notifyEventLocalChange(int eventID, String newLocal) throws RemoteException;
    void notifyEventDateChange(int eventID, String newDate) throws RemoteException;
    void notifyEventStartTimeChange(int eventID, String newStartTime) throws RemoteException;
    void notifyEventEndTimeChange(int eventID, String newEndTime) throws RemoteException;
    void notifyEventDeletion(int eventID) throws RemoteException;
    void notifyCodeGeneration(int eventID, int codeDuration, String generatedCode) throws RemoteException;
    void notifyParticipantDeletion(int eventID, String userEmail) throws RemoteException;
    void notifyParticipantAddition(int eventID, String userEmail) throws RemoteException;
}