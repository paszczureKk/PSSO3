package server;

import common.Item;

import java.rmi.RemoteException;

public class ItemManager implements Runnable{
    private Server server;

    public ItemManager(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            for(Item item : server.getItems()) {
                int time = item.getTimeRemaining();

                if(time > 0) {
                    item.setTimeRemaining(time - 1);
                }
                else {
                    server.endAuction(item.getItemName());
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
