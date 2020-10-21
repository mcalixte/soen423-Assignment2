package implementation.servers.qc;

import StoreApp.Store;
import StoreApp.StoreHelper;
import implementation.StoreImpl;
import org.omg.CORBA.Object;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import java.util.Properties;

public class QuebecServer {

    public static void main(String args[]) {
        try{
            // create and initialize the ORB //// get reference to rootpoa &amp; activate the POAManager
            ORB orb = ORB.init(args, null);
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant and register it with the ORB
            StoreImpl quebecStore = new StoreImpl("QC");
            quebecStore.setORB(orb);

            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(quebecStore);
            Store quebecStoreRef = StoreHelper.narrow(ref);

            org.omg.CORBA.Object objRef =  orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // bind object reference to a naming
            NameComponent path[] = ncRef.to_name( "quebecStore" );
            ncRef.rebind(path, quebecStoreRef);

            System.out.println("Quebec Store Server ready and waiting ...");

            // wait for invocations from clients
            orb.run();

        }

        catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }

        System.out.println("Quebec Store Server Exiting ...");

    }
}
