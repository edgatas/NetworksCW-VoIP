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
public class Socket1Receiver implements Runnable {

    static DatagramSocket receiving_socket;
    private AudioPlayer player;

    public Socket1Receiver() {
        try {
            this.player = new AudioPlayer();
        } catch (LineUnavailableException ex) {
            System.out.println("Audio player is unavailable");
            System.out.println(ex);
        }
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        //***************************************************
        //Open a socket to receive from on port PORT
        //DatagramSocket receiving_socket;
        try {
            receiving_socket = new DatagramSocket(GlobalVariables.port);
            receiving_socket.setSoTimeout(5000);
        } catch (SocketException e) {
            System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }

        while (GlobalVariables.Socket1ReceiveRun) {
            try {
                //while its running, creates bytes[]

                // AUDIO START
                byte[] wholeBuffer = new byte[516];
                byte[] audioBuffer = new byte[512];
                
                DatagramPacket packet = new DatagramPacket(wholeBuffer, 0, 516);
                

                try{
                receiving_socket.receive(packet);
                }
                catch(IOException ex){
                    System.out.println("Time out");
                    UI.Socket1StartReceive.setEnabled(true);
                    UI.Socket2StartReceive.setEnabled(true);
                    UI.Socket3StartReceive.setEnabled(true);
                    UI.Socket4StartReceive.setEnabled(true);
                    
                    UI.Socket1StopReceive.setEnabled(false);
                     UI.log2.setText("Timed Out");
                    GlobalVariables.Socket1ReceiveRun = false;
                    break;
                }

                byte[] headerBuffer = new byte[4];
                System.arraycopy(wholeBuffer, 0, headerBuffer, 0, 4);
                String header1String = new String(headerBuffer);
                if (header1String.endsWith("0000")){
                    UI.Socket1StartReceive.setEnabled(true);
                    UI.Socket2StartReceive.setEnabled(true);
                    UI.Socket3StartReceive.setEnabled(true);
                    UI.Socket4StartReceive.setEnabled(true);
                    
                    UI.Socket1StopReceive.setEnabled(false);
                     UI.log2.setText("Transmission stopped from another side");
                    GlobalVariables.Socket1ReceiveRun = false;
                    break;
                }

                System.arraycopy(wholeBuffer, 4, audioBuffer, 0, 512);

                // AUDIO END

                

                player.playBlock(audioBuffer);
                
                String test = new String(wholeBuffer);
                UI.log2.setText(test);


            } catch (IOException e) {
                System.out.println("ERROR: TextReceiver: Some random IO error occured!");
                e.printStackTrace();
            }
        }

        //Close the socket and player
        receiving_socket.close();
        player.close();
        System.out.println("Socket closed");
        System.out.println("Player closed");
        //***************************************************
    }
}
