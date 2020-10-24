package implementation.tests.threads;

import StoreApp.Store;

public class QCClientThread extends Thread  {
    private Store quebecStore;
    private Store ontarioStore;
    private Store britishColumbiaStore;

    public QCClientThread(Store quebecStore, Store ontarioStore, Store britishColumbiaStore){
        this.quebecStore = quebecStore;
        this.ontarioStore = ontarioStore;
        this.britishColumbiaStore = britishColumbiaStore;
    }

    @Override
    public void run() {
        pause(1000);
        quebecStore.purchaseItem("QCC1111", "QC1234", "10/25/2020 20:00");
        pause(2000);
        quebecStore.exchange("QCC1111", "QC1111", "QC1234");
    }

    private void pause(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
