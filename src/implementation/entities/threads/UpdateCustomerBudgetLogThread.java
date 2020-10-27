package implementation.entities.threads;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

public class UpdateCustomerBudgetLogThread extends Thread{
    private DatagramSocket serverSocket;
    private DatagramPacket receivePacket;
    private HashMap<String, Double> customerBudgetLog;

    public UpdateCustomerBudgetLogThread(DatagramSocket serverSocket, DatagramPacket receivePacket, HashMap<String, Double> customerBudgetLog){
        this.serverSocket = serverSocket;
        this.receivePacket = receivePacket;
        this.customerBudgetLog = customerBudgetLog;
    }

    @Override
    public void run(){
        while (true) {
            try {
                byte[] incomingData = new byte[1024];
                serverSocket.receive(receivePacket); //TODO Could be receiving a null object, may need to refactor
                byte[] data = receivePacket.getData();
                ByteArrayInputStream in = new ByteArrayInputStream(data);
                ObjectInputStream is = new ObjectInputStream(in);

                HashMap<String, Double> customerIDandUpdatedBudget = (HashMap<String, Double>) is.readObject(); //This ALREADY HAS UPDATED BUDGET DOUBLE

                for(Map.Entry<String, Double> entry : customerIDandUpdatedBudget.entrySet()) {
                    if(entry.getValue() != null)
                        customerBudgetLog.put(entry.getKey(), entry.getValue());//Update the customer budget log
                    else
                        customerBudgetLog.put(entry.getKey(), 1000.00);//Update the customer budget log
                }
                System.out.println("Set and updated the customer budget log");
            } catch (IOException | ClassNotFoundException e) {
               // e.printStackTrace();
            }

        }
    }
}
