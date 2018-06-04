/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voip_v2;

import CMPC3M06.AudioRecorder;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import uk.ac.uea.cmp.voip.DatagramSocket4;

/**
 *
 * @author
 */
public class Socket4Sender implements Runnable {

    static DatagramSocket4 sending_socket;
    public AudioRecorder recorder;

    public Socket4Sender() {
        try {
            this.recorder = new AudioRecorder();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(Socket1Sender.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Can't record");
        }
    }
//    // AUDIO END

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        //***************************************************
        //IP ADDRESS to send to
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName(GlobalVariables.iPAddress);  //CHANGE localhost to IP or NAME of client machine
        } catch (UnknownHostException e) {
            System.out.println("ERROR: TextSender: Could not find client IP");
            System.out.println(e);
            System.exit(0);
        }
        //***************************************************

        //***************************************************
        //Open a socket to send from
        //We dont need to know its port number as we never send anything to it.
        //We need the try and catch block to make sure no errors occur.
        //DatagramSocket sending_socket;
        try {
            sending_socket = new DatagramSocket4();
        } catch (SocketException e) {
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            System.out.println(e);
            System.exit(0);
        }

        String data = "A parity bit, or check bit, is a bit added to a string of binary code to ensure that the total number of 1-bits in the string is even or odd. Parity bits are used as the simplest form of error detecting code. There are two variants of parity bits: even parity bit and odd parity bit. In the case of even parity, for a given set of bits, the occurrences of bits whose value is 1 is counted. If that count is odd, the parity bit value is set to 1, making the total count of occurrences of 1s in the whole set inclu";
        byte[] dataByte = data.getBytes();
        // Header number
        int sequence = 1;

        //Main loop.
        while (GlobalVariables.Socket4SendRun) {

            try {

                byte[] audioBuffer = recorder.getBlock();

                //Creating header
                String headerString = "";
                if (sequence / 1000 < 1) {
                    headerString += "0";
                }
                if (sequence / 100 < 1) {
                    headerString += "0";
                }
                if (sequence / 10 < 1) {
                    headerString += "0";
                }
                headerString += String.valueOf(sequence);
                sequence++;
                if (sequence == 9999) {
                    sequence = 1;
                }
                byte[] headerNumber = headerString.getBytes();

                // Combaining header and data
//                byte[] wholeBuffer = new byte[audioBuffer.length + headerNumber.length];
//                System.arraycopy(headerNumber, 0, wholeBuffer, 0, headerNumber.length);
//                System.arraycopy(audioBuffer, 0, wholeBuffer, headerNumber.length, audioBuffer.length);
                
                byte[] wholeBuffer = new byte[dataByte.length + headerNumber.length];
                System.arraycopy(headerNumber, 0, wholeBuffer, 0, headerNumber.length);
                System.arraycopy(dataByte, 0, wholeBuffer, headerNumber.length, 512);


                byte[] checker = new byte[5];
                for (int i = 0; i < 5; i++){
                    checker[i] = (byte) (wholeBuffer[i*100]%10);
                }
                byte[] wholeBuffer2 = new byte[wholeBuffer.length + checker.length];
                System.arraycopy(wholeBuffer, 0, wholeBuffer2, 0, wholeBuffer.length);
                System.arraycopy(checker, 0, wholeBuffer2, wholeBuffer.length, checker.length);




                String test = new String(wholeBuffer2);
                UI.log.setText(test);

                //Make a DatagramPacket from it, with client address and port number
                DatagramPacket packet = new DatagramPacket(wholeBuffer2, wholeBuffer2.length, clientIP, GlobalVariables.port);

                //Send it
                //System.out.println("Sent Buffer: "+Arrays.toString(wholeBuffer2));
                //System.out.println(Arrays.toString(checker));
                sending_socket.send(packet);
                
                for (int i = 0; i < 512; i++){
                    if (i%2 == 0){
                        wholeBuffer[i+4] = (byte) (wholeBuffer[i+4] + 1);
                    }
                    else{
                        wholeBuffer[i+4] = (byte) (wholeBuffer[i+4] - 1);
                    }
                }
                packet = new DatagramPacket(wholeBuffer, wholeBuffer.length, clientIP, GlobalVariables.port);

                sending_socket.send(packet);
                

            } catch (IOException e) {
                System.out.println("ERROR: TextSender: Some random IO error occured!");
                e.printStackTrace();
            }

        }
        recorder.close();

        if (GlobalVariables.Socket4SendRun == false) {
            byte[] headerNumber = "0000".getBytes();
            DatagramPacket packet = new DatagramPacket(headerNumber, headerNumber.length, clientIP, GlobalVariables.port);
            try {
                for (int i = 0; i < 5; i++) {
                    sending_socket.send(packet);
                }
            } catch (IOException ex) {
                System.out.println("ERROR: TextSender: Some random IO error occured!");
            }
            UI.log.setText("Transmission was stopped");
        }

        //Close the socket
        sending_socket.close();
        //***************************************************
    }
}
