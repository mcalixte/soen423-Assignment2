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
        StringBuilder response = new StringBuilder();
        response.append(quebecStore.purchaseItem("ONC1111", "QC1234", "25/10/2020 20:00")+"\n");
        System.out.println(response.toString());
        pause(1000);
        response.append(quebecStore.exchange("ONC1111", "QC1111", "QC1234", "25/10/2020 20:00")+"\n");
        System.out.println(response.toString());
        pause(1000);
        response.append(quebecStore.exchange("ONC1111", "BC1111", "QC1234", "25/10/2020 20:00")+"\n");
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
