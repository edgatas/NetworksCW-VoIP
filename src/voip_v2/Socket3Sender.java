/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voip_v2;

import CMPC3M06.AudioRecorder;
import java.io.IOException;
import static java.lang.Math.sqrt;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import uk.ac.uea.cmp.voip.DatagramSocket3;


/**
 *
 * @author
 */
public class Socket3Sender implements Runnable{
    static DatagramSocket3 sending_socket3;
    
    
//    // AUDIO START
    public AudioRecorder recorder;

    public Socket3Sender(){
        try {
            this.recorder = new AudioRecorder();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(Socket2Sender.class.getName()).log(Level.SEVERE, null, ex);
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
            sending_socket3 = new DatagramSocket3();  
	}
        catch (SocketException e){
                System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
                System.out.println(e);
                System.exit(0);
	}

               
        // SETTINGS HERE *******************************************************
        // Only numbers that have square root as an int 4/9/16/25/36/49...
        int matrixSize = 4;
        
        // Automatic settings
        final int matrixRowLength = (int) sqrt(matrixSize);
        
        //SOCKET 2 2D ARRAY
        
        byte[] matrixBuffer[][] = new byte[matrixSize][matrixSize/matrixRowLength][matrixSize/matrixRowLength];
        int matrixXPosition = 0;
        int matrixYPosition = 0;
        
        int matrixXSend = 0;
        int matrixYSend = 0;
        
        boolean matrixFull = false;

        // Header number
        int sequence = 1;
        
        //Main loop.
        while (GlobalVariables.Socket3SendRun){

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
                
                // Writing to matrix buffer
                matrixBuffer[matrixXPosition][matrixYPosition] = wholeBuffer;
                
                // Updating matrix position to write
                matrixYPosition++;
                if (matrixYPosition == matrixRowLength){
                    matrixXPosition++;
                    matrixYPosition = 0;
                }
                if (matrixXPosition == matrixRowLength){matrixXPosition = 0;}
                
                
                // Checking if the matrix is full
                if (matrixXPosition == 0 && matrixYPosition == 0){matrixFull = true;}
                
                
                
                if(matrixFull){
                    
                    for (int c = 0;c<matrixSize;c++){
                    
                        //Make a DatagramPacket from it, with client address and port number
                        DatagramPacket packet = new DatagramPacket(matrixBuffer[matrixXSend][matrixYSend], matrixBuffer[matrixXSend][matrixYSend].length, clientIP, GlobalVariables.port);

                        String test = new String(matrixBuffer[matrixXSend][matrixYSend]);
                        UI.log.setText(test);

                        // Updating sending matrix
                        matrixXSend++;
                        if (matrixXSend == matrixRowLength){
                        matrixYSend++;
                        matrixXSend = 0;
                        }
                        if(matrixYSend == matrixRowLength){matrixYSend = 0;}

                        //Send it
                        sending_socket3.send(packet);


//                        if (sentence2[i].contains("Q")){
//                            running=false;
//                            System.out.println("Packets Sent: "+(i+1));
//                            break;
//                        }
                        if (c == matrixSize-1){matrixFull = false;}
                    }
                    

                }
            } catch (IOException e){
                System.out.println("ERROR: TextSender: Some random IO error occured!");
                e.printStackTrace();
            }
    
        }
        recorder.close();
        
        
        if (GlobalVariables.Socket1SendRun == false){
            byte[] headerNumber = "0000".getBytes();
            DatagramPacket packet = new DatagramPacket(headerNumber, headerNumber.length, clientIP, GlobalVariables.port);
            try{
                for (int i = 0; i < 5; i++){
                    sending_socket3.send(packet);
                }
            }
            catch(IOException ex){
                System.out.println("ERROR: TextSender: Some random IO error occured!");
            }
            UI.log.setText("Transmission was stopped");
        }

        //Close the socket
        sending_socket3.close();

        //***************************************************
    }
}
