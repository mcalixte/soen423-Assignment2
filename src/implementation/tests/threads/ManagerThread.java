package implementation.tests.threads;

import StoreApp.Store;

public class ManagerThread extends Thread {
    private Store quebecStore;
    private Store ontarioStore;
    private Store britishColumbiaStore;

    public ManagerThread(Store quebecStore, Store britishColumbiaStore, Store ontarioStore) {
        this.quebecStore = quebecStore;
        this.ontarioStore = ontarioStore;
        this.britishColumbiaStore = britishColumbiaStore;
    }

    @Override
    public void run() {
        britishColumbiaStore.addItem("BCM1111", "BC1111", "TEA", 2, 980.00);
        britishColumbiaStore.addItem("BCM1111", "BC1234", "LAPTOP", 2, 30.00);

        quebecStore.addItem("BCM1111", "QC1234", "TEA", 2, 30.00);
        quebecStore.addItem("BCM1111", "QC1111", "XYZ", 2, 40.00);
    }
}
