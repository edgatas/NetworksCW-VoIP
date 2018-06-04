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
import uk.ac.uea.cmp.voip.DatagramSocket4;

/**
 *
 * @author
 */
public class Socket4Receiver implements Runnable {

    static DatagramSocket4 receiving_socket;
    private AudioPlayer player;

    public Socket4Receiver() {
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
            receiving_socket = new DatagramSocket4(GlobalVariables.port);
            receiving_socket.setSoTimeout(5000);
        } catch (SocketException e) {
            System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }
        byte[] buffer2 = new byte [521];
        byte[] buffer[] = new byte[2][521];
        int bufferCount = 0;
        int play = 0;

        while (GlobalVariables.Socket4ReceiveRun) {
//            try {
                //while its running, creates bytes[]
//                // AUDIO START
//                byte[] audioBuffer = new byte[512];
//
//                DatagramPacket packet = new DatagramPacket(buffer[bufferCount], 0, 521);
                DatagramPacket packet = new DatagramPacket(buffer2, 0, 521);

                try {
                    receiving_socket.receive(packet);
                } catch (IOException ex) {
                    System.out.println("Time out");
                    UI.Socket1StartReceive.setEnabled(true);
                    UI.Socket2StartReceive.setEnabled(true);
                    UI.Socket3StartReceive.setEnabled(true);
                    UI.Socket4StartReceive.setEnabled(true);

                    UI.Socket4StopReceive.setEnabled(false);
                    UI.log2.setText("Timed Out");
                    GlobalVariables.Socket4ReceiveRun = false;
                    break;
                }

//                bufferCount++;
//
//                if (bufferCount == 2) {
//                    int playBuffer = 0;
//
//                    System.out.println("Buffer 0: " + Arrays.toString(buffer[0]));
//                    System.out.println("Buffer 1: " + Arrays.toString(buffer[1]));
//
//                    if (!checkBuffer(buffer[0])) {
//                        playBuffer = 1;
//                    }
//
//                    byte[] headerBuffer = new byte[4];
//                    System.arraycopy(buffer, 0, headerBuffer, 0, 4);
                    byte[] headerBuffer = new byte[4];
                    System.arraycopy(buffer2, 0, headerBuffer, 0, 4);
                    


//                    System.arraycopy(buffer[playBuffer], 0, headerBuffer, 0, 4);
                    String header1String = new String(headerBuffer);
                    if (header1String.endsWith("0000")) {
                        UI.Socket1StartReceive.setEnabled(true);
                        UI.Socket2StartReceive.setEnabled(true);
                        UI.Socket3StartReceive.setEnabled(true);
                        UI.Socket4StartReceive.setEnabled(true);

                        UI.Socket4StopReceive.setEnabled(false);
                        UI.log2.setText("Transmission stopped from another side");
                        GlobalVariables.Socket4ReceiveRun = false;
                        break;
                    }

//                    String test = new String(buffer[playBuffer]);
                    String test = new String(buffer2);
                    UI.log2.setText(test);

//                    System.out.println(Arrays.toString(buffer[playBuffer]));
                    //System.out.println(Arrays.toString(buffer2));

                        String test2 = new String(buffer2);
            System.out.println(test2);
//                    System.arraycopy(buffer[playBuffer], 4, audioBuffer, 0, 512);
//                    System.out.println("");
//                    // AUDIO END
//                    player.playBlock(audioBuffer);
//                    bufferCount = 0;
//                }
//            } catch (IOException e) {
//                System.out.println("ERROR: TextReceiver: Some random IO error occured!");
//                e.printStackTrace();
//            }
        }

        //Close the socket and player
        receiving_socket.close();
        player.close();
        System.out.println("Socket closed");
        System.out.println("Player closed");
        //***************************************************
    }

    boolean checkBuffer(byte[] buffer) {

        byte[] checker = new byte[5];
        for (int i = 0; i < 5; i++) {
            checker[i] = (byte) (buffer[i * 100] % 10);
        }

        boolean check = false;
        System.out.println(Arrays.toString(checker));
        for (int i = 0; i < 5; i++) {
            check = buffer[516 + + + i] == checker[i];
            if (check == false){
                break;
            }
        }
        System.out.println(check);
        return check;
    }
}
