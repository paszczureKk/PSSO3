package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAuctionServer extends Remote {
    void placeItemForBid(String ownerName, String itemName, String itemDesc, double startBid, int auctionTime)
            throws RemoteException;

    void bidOnItem(String bidderName, String itemName, double bid)
            throws RemoteException;

    Item[] getItems()
            throws RemoteException;

    void registerListener(IAuctionListener al, String itemName)
            throws  RemoteException;
}
