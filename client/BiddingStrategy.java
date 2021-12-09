package client;

import common.IAuctionServer;
import common.Item;

import java.rmi.RemoteException;

public interface BiddingStrategy {
    void bid(IAuctionServer server, Client bidder, Item item);

    static BiddingStrategy Maximum() {
        return (server, bidder, item) -> {
            try {
                double bid = item.getCurrentBid() + 1.00;
                if(bid <= bidder.getMaximumBid()) {
                    server.bidOnItem(bidder.getBidderName(), item.getItemName(), bid);
                }
                else {
                    System.out.println("Bid exceeds maximum bidding limit");
                }
            } catch (RemoteException e) {
                System.err.printf("Bidding does not succeeded: %s%n", item.getItemName());
                e.printStackTrace();
            }
        };
    }

    static BiddingStrategy LastMinute() {
        return (server, bidder, item) -> {
            try {
                if(item.getTimeRemaining() == 60) {
                    server.bidOnItem(bidder.getBidderName(), item.getItemName(), item.getCurrentBid() * 2);
                }
            } catch (RemoteException e) {
                System.err.printf("Bidding does not succeeded: %s%n", item.getItemName());
                e.printStackTrace();
            }
        };
    }
}
