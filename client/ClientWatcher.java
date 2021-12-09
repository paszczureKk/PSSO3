package client;

import common.Item;

import lombok.Setter;

public class ClientWatcher implements Runnable{
    private Client client;
    private int timer;
    @Setter private boolean stopped = true;

    public ClientWatcher(Client client, int timer) {
        this.client = client;
        this.timer = timer;
    }

    @Override
    public void run() {
        if(stopped) {
            return;
        }

        for(Item i : client.getWatchedItems()) {
            int time = i.getTimeRemaining();
            if(this.timer == time) {
                String name = i.getItemName();
                client.removeWatch(name);
                client.bidOnItem(name, i.getCurrentBid() * 2);
            }
            else {
                i.setTimeRemaining(time - 1);
            }
        }
    }
}
