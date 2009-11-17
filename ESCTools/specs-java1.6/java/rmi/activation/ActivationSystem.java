package java.rmi.activation;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.activation.UnknownGroupException;
import java.rmi.activation.UnknownObjectException;

public interface ActivationSystem extends Remote {
    public static final int SYSTEM_PORT = 1098;
    
    public ActivationID registerObject(ActivationDesc desc) throws ActivationException, UnknownGroupException, RemoteException;
    
    public void unregisterObject(ActivationID id) throws ActivationException, UnknownObjectException, RemoteException;
    
    public ActivationGroupID registerGroup(ActivationGroupDesc desc) throws ActivationException, RemoteException;
    
    public ActivationMonitor activeGroup(ActivationGroupID id, ActivationInstantiator group, long incarnation) throws UnknownGroupException, ActivationException, RemoteException;
    
    public void unregisterGroup(ActivationGroupID id) throws ActivationException, UnknownGroupException, RemoteException;
    
    public void shutdown() throws RemoteException;
    
    public ActivationDesc setActivationDesc(ActivationID id, ActivationDesc desc) throws ActivationException, UnknownObjectException, UnknownGroupException, RemoteException;
    
    public ActivationGroupDesc setActivationGroupDesc(ActivationGroupID id, ActivationGroupDesc desc) throws ActivationException, UnknownGroupException, RemoteException;
    
    public ActivationDesc getActivationDesc(ActivationID id) throws ActivationException, UnknownObjectException, RemoteException;
    
    public ActivationGroupDesc getActivationGroupDesc(ActivationGroupID id) throws ActivationException, UnknownGroupException, RemoteException;
}
