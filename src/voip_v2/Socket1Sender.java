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



/**
 *
 * @author
 */
public class Socket1Sender implements Runnable{
    static DatagramSocket sending_socket;
    public AudioRecorder recorder;

    public Socket1Sender(){
        try {
            this.recorder = new AudioRecorder();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(Socket1Sender.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Can't record");
        }
    }
//    // AUDIO END
    public void start(){
        Thread thread = new Thread(this);
	thread.start();
    }
    
    @Override
    public void run (){
     
        //***************************************************
        //IP ADDRESS to send to
        InetAddress clientIP = null;
	try {
            clientIP = InetAddress.getByName(GlobalVariables.iPAddress);  //CHANGE localhost to IP or NAME of client machine
	} 
        catch (UnknownHostException e) {
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
        try{
            sending_socket = new DatagramSocket();  
	}
        catch (SocketException e){
                System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
                System.out.println(e);
                System.exit(0);
	}

        // Header number
        int sequence = 1;
        
        //Main loop.
        while (GlobalVariables.Socket1SendRun){

            try{

                byte[] audioBuffer = recorder.getBlock();

                //Creating header
                String headerString = "";
                if(sequence/1000 < 1){headerString += "0";}
                if(sequence/100 < 1){headerString += "0";}
                if(sequence/10 < 1){headerString += "0";}
                headerString += String.valueOf(sequence);
                sequence++;
                if(sequence == 9999){sequence = 1;}
                byte[] headerNumber = headerString.getBytes();
                      
                // Combaining header and data

                byte[] wholeBuffer = new byte[audioBuffer.length + headerNumber.length];
                System.arraycopy(headerNumber, 0, wholeBuffer, 0, headerNumber.length);
                System.arraycopy(audioBuffer, 0, wholeBuffer, headerNumber.length, audioBuffer.length);
                
                //Make a DatagramPacket from it, with client address and port number
                DatagramPacket packet = new DatagramPacket(wholeBuffer, wholeBuffer.length, clientIP, GlobalVariables.port);

                //Send it
                sending_socket.send(packet);
                
                
                String test = new String(wholeBuffer);
                UI.log.setText(test);

            }
            catch (IOException e){
                System.out.println("ERROR: TextSender: Some random IO error occured!");
            }
    
        }
        //Close recording
        recorder.close();

        if (GlobalVariables.Socket1SendRun == false){
            byte[] headerNumber = "0000".getBytes();
            DatagramPacket packet = new DatagramPacket(headerNumber, headerNumber.length, clientIP, GlobalVariables.port);
            try{
                for (int i = 0; i < 5; i++){
                    sending_socket.send(packet);
                }
            }
            catch(IOException ex){
                System.out.println("ERROR: TextSender: Some random IO error occured!");
            }
            UI.log.setText("Transmission was stopped");
        }
        
        //Close the socket
        sending_socket.close();
        
        //***************************************************
    }
}
