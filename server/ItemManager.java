package server;

import common.Item;

import java.rmi.RemoteException;

public class ItemManager implements Runnable{
    private final Server server;

    public ItemManager(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            for(Item item : server.getItems()) {
                int time = item.getTimeRemaining();
                item.setTimeRemaining(time - 1);
                if(time - 1 == 0) {
                    server.endAuction(item.getItemName());
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}

