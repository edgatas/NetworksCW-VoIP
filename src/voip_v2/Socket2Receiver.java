/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voip_v2;

import CMPC3M06.AudioPlayer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import javax.sound.sampled.LineUnavailableException;

/**
 *
 * @author
 */
public class Socket2Receiver implements Runnable{
    static DatagramSocket receiving_socket;
    private AudioPlayer player;

    public Socket2Receiver(){
        try {
            this.player = new AudioPlayer();
        } catch (LineUnavailableException ex) {
            System.out.println("Audio player is unavailable");
            System.out.println(ex);
        }
    }
    public void start(){
        Thread thread = new Thread(this);
	thread.start();
    }
    
    @Override
    public void run (){
        
        //***************************************************
        //Open a socket to receive from on port PORT
        
        //DatagramSocket receiving_socket;
        try{
		receiving_socket = new DatagramSocket(GlobalVariables.port);
                receiving_socket.setSoTimeout(5000);
	} catch (SocketException e){
                System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
		e.printStackTrace();
                System.exit(0);
	}

        // SETTINGS HERE *************************************************************
        // Only odd numbers
        int bufferSize = 16;
        
        
        
        // All global variable for the class
        
//        // TEXT
//        byte[] storage[] = new byte[16][14];
        
        // SOUNDS
        byte[] storage[] = new byte[516][bufferSize];
        
        int header = 0;
        int headerDifference;
        int i = 0;
        
//        // TEXT
//        String dataString;

        //TESTING
        int times = 1;

        //***************************************************
        //Main loop.

        while (GlobalVariables.Socket2ReceiveRun){
         
            try{
                //while its running, creates bytes[]
                
//                // TEXT START
//                byte[] buffer = new byte[16];
//                DatagramPacket packet = new DatagramPacket(buffer, 0, 16);
//                // TEXT END
                
                // AUDIO START
                byte[] buffer = new byte[516];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 516);
                // AUDIO END
                
                try{
                receiving_socket.receive(packet);
                
                byte[] headerBuffer = new byte[4];
                System.arraycopy(buffer, 0, headerBuffer, 0, 4);
                String header1String = new String(headerBuffer);
                if (header1String.endsWith("0000")){
                    UI.Socket1StartReceive.setEnabled(true);
                    UI.Socket2StartReceive.setEnabled(true);
                    UI.Socket3StartReceive.setEnabled(true);
                    UI.Socket4StartReceive.setEnabled(true);
                    
                    UI.Socket2StopReceive.setEnabled(false);
                     UI.log2.setText("Transmission stopped from another side");
                    GlobalVariables.Socket2ReceiveRun = false;
                    break;
                }

                
                }
                catch(IOException ex){
                    System.out.println("Time out");
                    UI.Socket1StartReceive.setEnabled(true);
                    UI.Socket2StartReceive.setEnabled(true);
                    UI.Socket3StartReceive.setEnabled(true);
                    UI.Socket4StartReceive.setEnabled(true);
                    
                    UI.Socket2StopReceive.setEnabled(false);
                     UI.log2.setText("Timed Out");
                    GlobalVariables.Socket2ReceiveRun = false;
                    
                    break;
                }
                // Storing packets in 16 places buffer
                storage[i] = buffer;
                i++;
                
                // Sorting block by header in increaing order
                boolean sorted = false;
                if (i == bufferSize){
                    
                    while (sorted == false) {
                        sorted = true;
                        for (int cursor = 0; cursor < (bufferSize-1); cursor++) {

//                            System.out.println("Comparing: Storage[" + cursor + "] and Storage[" + (cursor + 1) + "]");
//                            System.out.println("Reading: " + Arrays.toString(storage[cursor]));
//                            System.out.println("Reading: " + Arrays.toString(storage[(cursor + 1)]));
                            if (compareTo(storage[cursor], storage[(cursor + 1)]) == 1) {
                                byte[] temp = storage[cursor];
                                storage[cursor] = storage[cursor + 1];
                                storage[cursor + 1] = temp;
                                sorted = false;
                            }
                        }
                    }
                }
                       
                
                
                if (sorted == true){

                    for (int cursor = 0; cursor < bufferSize/2; cursor++) {
                    
//                        // TEXT START
//                        byte[] data = new byte[10];
//                        byte[] dataHeader = new byte[4];
//
//                        System.arraycopy(storage[cursor], 4, data, 0, 10);
//                        System.arraycopy(storage[cursor], 0, dataHeader, 0, 4);
//
//                        String headerString2 = new String(dataHeader);
//                        dataString = new String(data);
//
//                        int header1Int = Integer.parseInt(headerString2);
//
//                        System.out.println("Sequence Number: " + headerString2 + " Data: " + dataString);
//                        
//                        if (header == 0) {
//                            header = header1Int;
//                        } else {
//                            headerDifference = header1Int - header;
//
//                            while (headerDifference > 1) {
//                                System.out.println("Sequence Number: " + headerString2 + " Data: " + dataString);
//                                headerDifference--;
//                            }
//                            header = header1Int;
//                        }
//                        
//                        // TEXT END

                        // AUDIO START
                        byte[] audioBuffer = new byte[512];
                        byte[] headerBuffer = new byte[4];
                        System.arraycopy(storage[cursor], 4, audioBuffer, 0, 512);
                        System.arraycopy(storage[cursor], 0, headerBuffer, 0, 4);
                        String header1String = new String(headerBuffer);
                        int header1Int = Integer.parseInt(header1String);

                        System.out.println("Header: "+header1Int);
                        System.out.println("Packets Played: "+times);
                        
                        String test = new String(storage[cursor]);
                        UI.log2.setText(test);
                        
                        player.playBlock(audioBuffer);
                        times++;

                        if (header == 0) {
                            header = header1Int;
                        } else {
                            headerDifference = header1Int - header;

                            if (times <= header1Int){
                                
                            player.playBlock(audioBuffer);
                            headerDifference -= 2;
                            times++;

                            header = header1Int;
                            }
                        }
                        // AUDIO END

                    }
                }
                
                if (sorted == true) {
                    for (int cursor = 0; cursor < bufferSize/2; cursor++) {
                        storage[cursor] = storage[cursor + bufferSize/2];
                    }
                    i = bufferSize/2;
                }
                
            } catch (IOException e){
                System.out.println("ERROR: TextReceiver: Some random IO error occured!");
                e.printStackTrace();
            } 
        }
        
        //Close the socket
        receiving_socket.close();
        player.close();
        System.out.println("Socket closed");
        System.out.println("Player closed");
        //***************************************************
    }
    
    private int compareTo(byte[] first, byte[] second){
        byte[] header1 = new byte[4];
        byte[] header2 = new byte[4];

        System.arraycopy(first, 0, header1, 0, 4);
        System.arraycopy(second, 0, header2, 0, 4);
        String header1String = new String(header1);
        String header2String = new String(header2);
        
        int header1Int = Integer.parseInt(header1String);
        int header2Int = Integer.parseInt(header2String);
        
        if (header1Int > header2Int){return 1;}
        else {return -1;}
        
    }
}
