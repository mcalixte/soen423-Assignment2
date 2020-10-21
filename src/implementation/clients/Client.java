package implementation.clients;


import StoreApp.Store;
import StoreApp.StoreHelper;
import implementation.StoreImpl;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import java.io.*;
import java.util.*;

public class Client {
/**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
      try {
	    ORB orb = ORB.init(args, null);
	    org.omg.CORBA.Object objRef =   orb.resolve_initial_references("NameService");
	    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

	    Store quebecStore = StoreHelper.narrow(ncRef.resolve_str("quebecStore"));
	    Store britishColumbiaStore = StoreHelper.narrow(ncRef.resolve_str("britishColumbiaStore"));
	    Store ontarioStore = StoreHelper.narrow(ncRef.resolve_str("ontarioStore"));

	    Scanner c=new Scanner(System.in);
	    System.out.println("\t\t >>>>>>>>>> Welcome to the DSMS <<<<<<<<<<<<< \n\n");

	    System.out.println("Quebec Store Purchase:" + quebecStore.purchaseItem("QCC1111", "QC2222", "03/03/2020"));
	    System.out.println("British Columbia Store Purchase:" + britishColumbiaStore.purchaseItem("BCC1111", "BC2222", "03/03/2020"));
	    System.out.println("Ontario Store Purchase:" + ontarioStore.purchaseItem("ONC1111", "ON2222", "03/03/2020" ));

       }
       catch (Exception e) {
          System.out.println("Hello Client exception: " + e);
	  e.printStackTrace();
       }

    }
}
