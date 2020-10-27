package implementation.tests.threads;

import StoreApp.Store;

public class BCClientThread extends Thread  {
    private Store quebecStore;
    private Store ontarioStore;
    private Store britishColumbiaStore;

    public BCClientThread(Store quebecStore, Store ontarioStore, Store britishColumbiaStore){
        this.quebecStore = quebecStore;
        this.ontarioStore = ontarioStore;
        this.britishColumbiaStore = britishColumbiaStore;
    }

    @Override
    public void run() {
        pause(2000);
        String result = britishColumbiaStore.purchaseItem("ONC1111", "QC1234", "25/10/2020 20:00");
        System.out.println(result);
    }


    private void pause(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
           // e.printStackTrace();
        }
    }
}
