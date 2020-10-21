package implementation.entities.threads;

import implementation.StoreImpl;
import implementation.entities.item.Item;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class ListItemThread extends Thread {
    private DatagramSocket serverSocket;
    private DatagramPacket receivePacket;
    private StoreImpl store;

    public ListItemThread(DatagramSocket serverSocket, DatagramPacket receivePacket, StoreImpl store ){
        this.serverSocket = serverSocket;
        this.receivePacket = receivePacket;
        this.store = store;
    }

    @Override
    public void run(){
        while (true) {
            try {
                InetAddress ip = InetAddress.getLocalHost();
                serverSocket.receive(receivePacket);
                String itemName = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("RECEIVED: Item name: " + itemName);

                //Get list from store
                List<Item> itemsFound = store.getClientHelper().getItemsByName(itemName, store.getInventory()); //TODO Does a list of serializable objects work

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(outputStream);
                os.writeObject(itemsFound);

                for(Item item : itemsFound)
                    System.out.println(item.toString()+"\n");

                byte[] data = outputStream.toByteArray();
                DatagramPacket sendPacket = new DatagramPacket(data, data.length , ip, receivePacket.getPort());
                serverSocket.send(sendPacket); //TODO
                System.out.println("Sent packet");
                // now send acknowledgement packet back to sender
            } catch (IOException e) {
               System.out.println("ListItemThread Exception: " + e);
            }

        }
    }
}
