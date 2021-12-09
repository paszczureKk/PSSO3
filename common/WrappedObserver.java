package common;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("deprecation")
public class WrappedObserver implements Observer, Serializable {

    private static final long serialVersionUID = 1L;

    private final IAuctionListener al;

    public WrappedObserver(IAuctionListener al) {
        this.al = al;
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            al.update((Item)o);
        } catch (RemoteException e) {
            System.err.println("Remote exception removing observer:" + this);
            o.deleteObserver(this);
        }
    }

}