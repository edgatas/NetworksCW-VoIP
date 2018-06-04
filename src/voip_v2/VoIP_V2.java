/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voip_v2;

/**
 *
 * @author Baokang
 */
public class VoIP_V2 {

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static UI window;
    
    public static void main(String[] args) throws Exception{
        
//        GlobalVariables.Socket1ReceiveRun = true;
//        GlobalVariables.Socket1SendRun = true;
//        GlobalVariables.port = 55555;
//        GlobalVariables.iPAddress = "localhost";
//        
//        Socket1Receiver socket1Receiver = new Socket1Receiver();
//        Socket1Sender socket1Sender = new Socket1Sender();
//        
//        socket1Receiver.start();
//        socket1Sender.start();
        
//        Socket2Receiver socket2Receiver = new Socket2Receiver();
//        Socket2Sender socket2Sender = new Socket2Sender();
//        
//        socket2Receiver.start();
//        socket2Sender.start();

        window = new UI();
        window.start();


//
//        Socket3Receiver socket3Receiver = new Socket3Receiver();
//        Socket3Sender socket3Sender = new Socket3Sender();
//        
//        socket3Receiver.start();
//        socket3Sender.start();
    }
    
}
