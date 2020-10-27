package implementation.tests.threads;

import StoreApp.Store;

public class ONClientThread extends Thread  {
    private Store quebecStore;
    private Store ontarioStore;
    private Store britishColumbiaStore;

    public ONClientThread(Store quebecStore, Store ontarioStore, Store britishColumbiaStore){
        this.quebecStore = quebecStore;
        this.ontarioStore = ontarioStore;
        this.britishColumbiaStore = britishColumbiaStore;
    }

    @Override
    public void run() {
        quebecStore.purchaseItem("ONC1111", "QC1234", "10/25/2020 20:00");
        pause(1000);
        quebecStore.exchange("ONC1111", "QC1111", "QC1234", "10/25/2020 20:00");
        pause(1000);
        quebecStore.exchange("ONC1111", "BC1111", "QC1234", "10/25/2020 20:00");
    }

    private void pause(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
           // e.printStackTrace();
        }
    }
}
