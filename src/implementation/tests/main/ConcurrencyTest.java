package implementation.tests.main;

import StoreApp.Store;
import StoreApp.StoreHelper;
import implementation.tests.threads.QCClientThread;
import implementation.tests.threads.ONClientThread;
import implementation.tests.threads.BCClientThread;
import implementation.tests.threads.ManagerThread;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class ConcurrencyTest {
    private static QCClientThread qcClientThread;
    private static ONClientThread onClientThread;
    private static BCClientThread bcClientThread;
    private static ManagerThread genericManager;

    private static Store quebecStore;
    private static Store britishColumbiaStore;
    private static Store ontarioStore;

    public static void main(String[] args) {
        initializeStores(args);
        runTest();
    }

    private static void runTest() {
        genericManager.start();
        pause(2000);
        onClientThread.start();
        qcClientThread.start();
        bcClientThread.start();
    }

    private static void initializeStores(String[] args) {
        try {
            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            quebecStore = StoreHelper.narrow(ncRef.resolve_str("quebecStore"));
            britishColumbiaStore = StoreHelper.narrow(ncRef.resolve_str("britishColumbiaStore"));
            ontarioStore = StoreHelper.narrow(ncRef.resolve_str("ontarioStore"));

            initializeClientThreads(quebecStore, britishColumbiaStore, ontarioStore);
        }
        catch(Exception e) {
            System.out.println("Hello Client exception: " + e);
        }
    }

    private static void initializeClientThreads(Store quebecStore, Store britishColumbiaStore, Store ontarioStore) {
        genericManager = new ManagerThread(quebecStore, britishColumbiaStore, ontarioStore);
        qcClientThread = new QCClientThread(quebecStore, ontarioStore, britishColumbiaStore);
        onClientThread = new ONClientThread(quebecStore, ontarioStore, britishColumbiaStore);
        bcClientThread = new BCClientThread(quebecStore, ontarioStore, britishColumbiaStore);
    }

    private static void pause(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
           // e.printStackTrace();
        }
    }
}
