import java.util.*;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;
import java.io.BufferedReader;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CIS 457
Project 1 part 2
Java client server file transfer
Jonathan Powers, Kevin Anderson, Brett Greenman
 */
//Client class to connect to a server. 
class Client{
	//Main method that starts the client. 
	static DatagramSocket socket;
	static String message;
	static CopyOnWriteArrayList<Pair> packetList;
	static Integer packetCount;
	static boolean endOfFile;
	static OutputStream fileOut;
	static File file;
	static boolean suspend1;
	static boolean suspend2;
	static int maxPackets;
	public static void main(String args[]) throws Exception{
		//Prompt user for ip address
		String ip_address, port;
		Scanner input = new Scanner(System.in);
		System.out.println("Enter an IP address, loopback address is 127.0.0.1");
		ip_address = "127.0.0.1";//input.next();
		System.out.println("Enter a port, default port is 9875");
		port = "9999";//input.next();	

		if(checkIP(ip_address) == true && checkPort(port) == true){

		} else {
			System.out.print("Not a valid ip address or port.");
			System.exit(0);
		}
		
		System.out.println("Enter a file name. -1 to exit: ");
		message = input.next();
		
		byte[] sendData = message.getBytes();
		socket = new DatagramSocket();	
		socket.connect(InetAddress.getByName(ip_address), Integer.parseInt(port));
		
		try{
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
			socket.send(sendPacket);
		}catch(Exception e) {
			System.out.println("err1" + e.getStackTrace());
		}

		packetList = new CopyOnWriteArrayList<Pair>();
		packetCount = 0;
		endOfFile = false;
		suspend1 = false;
		suspend2 = true;
		maxPackets = 1000;
		file = new File ("Data/" + message);
		fileOut = new FileOutputStream(file);

		
		Thread receiveThread = new Thread(){
			public void run(){
				System.out.println("Receive Thread Running");
				try{
					while(socket.isConnected()){
						while(suspend1) {
							//System.out.println("Receive Thread Sleeping");
							Thread.sleep(300);
						}

						if(packetList.size() <= maxPackets) {
							//System.out.println("Receiving");
							byte[] receiveData = new byte[1024];
							DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
							socket.receive(receivePacket);
							
							int key = byteArrayToInt(Arrays.copyOfRange(receivePacket.getData(), 0, 4));
							byte[] val = Arrays.copyOfRange(receivePacket.getData(), 4, receivePacket.getData().length);
							
							if(new String(receivePacket.getData()).trim().equals("-EOF-")) {
								//System.out.println(new String(receivePacket.getData()));
								//System.out.println("End Of File");
								endOfFile = true;
								suspend1 = true;
								suspend2 = false;
							}else {
								packetList.add(new Pair(key, val));
							}
							
							
							byte[] sendData = new String("Packet Received").getBytes();
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
							socket.send(sendPacket);
							
						} else {
							suspend1 = true;
							suspend2 = false;
						}
					}
				} 
				catch (Exception e){
					System.out.println("err2" + e.getStackTrace());	    
				}
			}
		};

		Thread writeThread = new Thread(){
			public void run(){
				System.out.println("Write Thread Running");
				int currentPacket = 1;
				try{
					while(!endOfFile || packetList.size() > 0) {
						while(suspend2) {
							//System.out.println("Write Thread Sleeping");
							Thread.sleep(300);
						}
						if(endOfFile && packetList.size() == 0) {
							break;
						}
						//System.out.println(packetList.size() + ", " + currentPacket + ", " + packetList.get(0).getKey());
						int tempCount = currentPacket;
						if(packetList.size() > 700 || endOfFile) {
							for(int i = 0; i < packetList.size(); i++) {
								Pair temp = packetList.get(i);
								if(temp != null && temp.getKey() == currentPacket) {
									byte[] data = packetList.get(i).getValue();
									fileOut.write(data);

									packetList.remove(i);
									
									currentPacket++;
								}
							}
							if(tempCount == currentPacket && !endOfFile) {
								//System.out.println("Sleep 1, " + tempCount + ", " + packetCount);
								maxPackets += 1000;
								suspend2 = true;
								suspend1 = false;	
							}
							if(tempCount == currentPacket && endOfFile && packetList.size() == 0) {
								break;
							}
							
						} else {
							if(!endOfFile) {
								//System.out.println("Sleep 2");
								maxPackets = 1000;
								suspend2 = true;
								suspend1 = false;	
							}													
						}
					}	
				} 
				catch (Exception e){
					e.printStackTrace();	 
					System.exit(0);
				}
			}
		};
			
		receiveThread.start();
		writeThread.start();

		while(true) {
			if(endOfFile && packetList.size() == 0) {
				System.out.println("File closed");
				fileOut.close();
				System.exit(0);
				break;
			}
		}

	}

	void suspend1() {
		suspend1 = true;
	}
	synchronized void resume1() {
	  	suspend1 = false;
	   	notify();
	}

	void suspend2() {
		suspend2 = true;
	}
	synchronized void resume2() {
	  	suspend2 = false;
	   	notify();
	}
	
	/*
	Check to make sure the input is a valid ipv4 address. 
	Valid ip range is 0.0.0.1 to 255.255.255.254
	*/
	public static boolean checkIP(String ip){
		//Do work
	String[] tokens = ip.split("\\.");
		if (tokens.length > 4) return false;
		int token;
	try{
		for (int i = 0; i < 4; i++){
			try{token = Integer.parseInt(tokens[i]);}
			catch(NumberFormatException e){return false;}
			if (i != 3) {
				if(token < 0 || token > 255) return false;
			}
			else if (token <1 || token > 254) return false;
		}}catch(ArrayIndexOutOfBoundsException e){e.printStackTrace(System.out);}
		return true;
	}
	/*
	Check to make sure the input is a valid port. 
	*/
	public static boolean checkPort(String port){
		int input;
		try{input = Integer.parseInt(port);}
		catch(NumberFormatException e){return false;}
		if(input < 0 || input > 65535) return false;
		return true;
	}

	public static int byteArrayToInt(byte[] b) 
	{
	    int value = 0;
	    for (int i = 0; i < 4; i++) {
	        int shift = (4 - 1 - i) * 8;
	        value += (b[i] & 0x000000FF) << shift;
	    }
	    return value;
	}

	public static byte[] intToByteArray(int a)
	{
	    byte[] ret = new byte[4];
	    ret[3] = (byte) (a & 0xFF);   
	    ret[2] = (byte) ((a >> 8) & 0xFF);   
	    ret[1] = (byte) ((a >> 16) & 0xFF);   
	    ret[0] = (byte) ((a >> 24) & 0xFF);
	    return ret;
	}
}

