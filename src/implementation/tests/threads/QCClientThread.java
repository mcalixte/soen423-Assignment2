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
        StringBuilder response = new StringBuilder();
        pause(1000);
        response.append(quebecStore.purchaseItem("QCC1111", "QC1234", "25/10/2020 20:00")+"\n");
        System.out.println(response.toString());
        pause(2000);
        response.append(quebecStore.exchange("QCC1111", "QC1111", "QC1234","25/10/2020 20:00")+"\n");
        System.out.println(response.toString());
    }

    private void pause(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
           // e.printStackTrace();
        }
    }
}
