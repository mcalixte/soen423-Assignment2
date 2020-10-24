package implementation.entities.threads;

import implementation.StoreImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PurchaseItemThread extends Thread {
    private DatagramSocket serverSocket;
    private DatagramPacket receivePacket;
    private StoreImpl store;

    public PurchaseItemThread(DatagramSocket serverSocket, DatagramPacket receivePacket, StoreImpl store) {
        this.serverSocket = serverSocket;
        this.receivePacket = receivePacket;
        this.store = store;
    }

    @Override
    public void run() {

        while (true) {
            try {
                serverSocket.receive(receivePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String purchaseRequestString = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("\n RECEIVED item: " + purchaseRequestString + "Port: " + receivePacket.getPort());

            //Get the item that is attempted to be purchased and take it from this store
            String[] purchaseOrder = unpackPurchaseRequest(purchaseRequestString); // customerID and ItemID
            String customerID = purchaseOrder[0];
            String itemID = purchaseOrder[1];
            String dateOfPurchase = purchaseOrder[2];

            String purchaseOrderSuccess;

            try {
                InetAddress ip = InetAddress.getLocalHost();
                purchaseOrderSuccess = store.purchaseItem(customerID, itemID, dateOfPurchase);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(outputStream);
                os.writeObject(purchaseOrderSuccess);

                byte[] data = outputStream.toByteArray();
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, ip, receivePacket.getPort());
                serverSocket.send(sendPacket); //TODO It is now sending a boolean
                System.out.println("Item sent to the store that made the request ...");
            } catch (Exception e) {
                System.out.println("PurchaseItem Exception: " + e);
                e.printStackTrace();
            }


        }


    }

    private static String[] unpackPurchaseRequest(String purchaseRequestString) {
        String[] strParts = purchaseRequestString.split("\\r?\\n|\\r");
        System.out.println(purchaseRequestString);

        for (int i = 0; i < strParts.length - 1; i++) //TODO Erase after debuggin process
            System.out.println(strParts[i]);

        return strParts;
    }
}
