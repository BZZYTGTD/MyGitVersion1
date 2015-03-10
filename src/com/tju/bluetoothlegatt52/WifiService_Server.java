package com.tju.bluetoothlegatt52;

import java.io.BufferedReader;

import javax.servlet.http.HttpServletRequest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


public class WifiService_Server  extends Service  {
	public final static String ACTION_DATA_WIFI_ECG = "com.example.bluetooth.wifi.ECG_DATA";
	public final static String DATA_WIFI = "data_from_wifi";
	private static final int SERVER_PORT1 = 50000;//0xc350
	
	private static final int MAX = 100;//回馈字节的最大数
	private final static int BUFFERLEN = 250;//接收缓冲区长度
	private static final int SERVER_PORT2 = 53000;
	private static final int SERVER_PORT3 = 55000;
	public static byte[] buffer = new byte[BUFFERLEN*4+1];//1000字节
	public static int[] dataArray = new int[BUFFERLEN*2];
	public static byte[] buffer3 = new byte[BUFFERLEN];
	public static int[] dataArray2 = new int [BUFFERLEN/2];
	public static byte[] buffer2;
	public static int length;
	private static boolean  getDataRunning = false;  
	
	public   static InetAddress ClientIP; 
	private static boolean connected = false;
	public static DatagramSocket ds;
	public static DatagramSocket dss;
    public static DatagramPacket packet;
    public static DatagramPacket packet2;
    public static DatagramPacket send;
    public static DatagramSocket ds2;
     private byte[] desBuff;
 	
   public static boolean Senser = false;
   public  String hRate ;
    public static  String localIp  ;
   
//     public static HttpServletRequest request;
    
    public class LocalBinder extends Binder {
		public WifiService_Server getService() {
			return WifiService_Server.this;
		}
	}

	private IBinder mBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
    	// TODO Auto-generated method stub
    	new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.println("当前线程：" + Thread.currentThread().getName());
				
				System.out.println("Server:Wait  Connecting...");  
//				startConnect();
				 try {
						ds = new DatagramSocket(SERVER_PORT1);
						
						//				ServerIP = InetAddress.getLocalHost();//获取主机IP为127.0.0.1还回地址
						
						localIp = getLocalIpAddress();
							System.out.println("localIp :"+ localIp);
					} catch (SocketException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					String [] ipb=localIp.split("\\.");
					byte[] stringArr = new byte[4];
					stringArr[0]=(byte)(Integer.parseInt(ipb[0]));
					stringArr[1]=(byte)(Integer.parseInt(ipb[1]));
					stringArr[2]=(byte)(Integer.parseInt(ipb[2]));
					stringArr[3]=(byte)(Integer.parseInt(ipb[3]));
			    
					byte[] instr = new byte[MAX];
				    //用来接收客户端消息
				       packet = new DatagramPacket(instr,instr.length);
				     
							try {
								ds.receive(packet);
								
								String str = new String(packet.getData());  //提取数据
//								System.out.println("the reveived packet is : "+ str.trim()); //打印客户机消息
//						      	System.out.println("client address: " + packet.getAddress().getHostAddress()
//						      			+" on Port is : "+ packet.getPort());
						      	 try {
									 ClientIP = InetAddress.getByName(packet.getAddress().getHostAddress());
//									 System.out.println("ClientIP " + ClientIP);
								} catch (UnknownHostException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							
						      	desBuff = new byte[packet.getLength()];  
					            System.arraycopy(packet.getData(), 0, desBuff, 0, packet.getLength()); 
//						        //for循环是为了验证接收正确与否
//						        for(int i = 0; i < desBuff.length;i++) {
//						        	System.out.printf("desBuff[i]" + (desBuff[i] ));
//						        }
						       
						         
						        dss = new DatagramSocket(SERVER_PORT2);
						        
						        if((desBuff[0]) ==127 && (desBuff[1])== -2
						        		   && (desBuff[2]) ==7 && (desBuff[3]&0xff)== 1
						        	&& (desBuff[10]) ==-17){
						        	   byte[] buf = new byte[] {0x7f,  (byte) (0xef&0xff ),0x07,
									        	0x02,(byte) stringArr[0],(byte) stringArr[1],(byte) stringArr[2],
									        		(byte) stringArr[3],  (byte) (0xd6),(byte) 0xd8,(byte) (0xfe)}; 
									   int rPort = ((desBuff[8]<<8)&0xff00)+(desBuff[9]&0xff);
									   
						        	   send = new DatagramPacket(buf,buf.length, 
									        		ClientIP,rPort);//向客户机50000端口发送数据
						        	   
//						        	   send = new DatagramPacket(buf,buf.length,InetAddress.getByName("192.168.1.28"),50000);
									        dss.send(send); //执行发送
									        dss.send(send); //执行发送
									        dss.send(send); //执行发送
									        dss.send(send); //执行
									        dss.send(send); //执行发送
									        dss.send(send); //执行发 
									        dss.send(send); //执行发送
									        dss.send(send);   
									        connected = true;
//									        dss.close();
						        }else{
						        	ds.close();
						        	 dss.close();
						        	System.out.println("重新发送");
						        	desBuff = null;
//						        	startConnect();
						        }
						        
						       
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				       
					  if(connected){
						  System.out.println("Server: Connected !");
						  startReceive();
				
					  }
					dss.close();
					

			}

			
			
    		
    	}).start();
		return mBinder;
    }
   
   

    public String getLocalIpAddress() {  
        try {  
            for (Enumeration<NetworkInterface> en = NetworkInterface  
                            .getNetworkInterfaces(); en.hasMoreElements();) {  
                        NetworkInterface intf = en.nextElement();  
                       for (Enumeration<InetAddress> enumIpAddr = intf  
                                .getInetAddresses(); enumIpAddr.hasMoreElements();) {  
                            InetAddress inetAddress = enumIpAddr.nextElement();  
                            if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {  
                            return inetAddress.getHostAddress().toString();  
                            }  
                       }  
                    }  
                } catch (SocketException ex) {  
                    Log.e("WifiPreference IpAddress", ex.toString());  
                }  
        
        
             return null;  
} 
	 
    public boolean stopReceive() {
    	System.out.println("stopReceive");
		getDataRunning = false;
		ds2.close();
//		ds2 = null;
		return true;
	}
    
	public void startReceive() {
	
		new Thread(new Runnable() {
			public DatagramPacket packet2;
//			private byte[] desBuff2;
			@Override
			public void run() {
				// TODO Auto-generated method stub

				System.out.println("Server:Transfering...");   
				
				try {
//					ds2 = new DatagramSocket(SERVER_PORT3,WifiService_Server.ClientIP);
					ds2 = new DatagramSocket(SERVER_PORT3);
					//				ServerIP = InetAddress.getLocalHost();//获取主机IP为127.0.0.1还回地址
					
				} catch (SocketException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				
				
				 buffer2 = new byte[BUFFERLEN*4+1];//1001字节,对应500个点
				 while(ds2 != null){
			    //用来接收客户端消息
			       packet2 = new DatagramPacket(buffer2,buffer2.length);
			     
						try { 
							getDataRunning = true;
						
							ds2.receive(packet2);
							
							String str = new String(packet2.getData());  //提取数据
//							System.out.println("the reveived packet is : "+ str.trim()); //打印客户机消息
					      	System.out.println("client address: " + packet2.getAddress().getHostAddress()
					      			+" on Port is : "+ packet2.getPort());
						
					      	 buffer = new byte[BUFFERLEN*4+1]; 
					      	 length  = packet2.getLength();
				            System.arraycopy(packet2.getData(), 0, buffer, 0, packet2.getLength()); 
				           
					     final  Intent intent = new Intent(ACTION_DATA_WIFI_ECG);
					     if(buffer[0] == -128){
					    	 System.out.println("-----Raw Data ");
								while(length  > 0  && getDataRunning) {					
									for(int i = 0; i < length/2; i++) {
										{
//											dataArray[i] = (buffer[2*i]<<8)+(buffer[2*i+1]&0xff);
											dataArray[i] = (buffer[2*i+1]<<8)+(buffer[2*(i+1)]&0xff);
//											System.out.println("dataArray[i] "+ dataArray[i]);
										}
									
									}
									//      !***the stream of data does not have a sequence***!
									length = 0;
									buffer2 = new byte[BUFFERLEN*4];
									intent.putExtra(DATA_WIFI, dataArray);
									sendBroadcast(intent);
							}
						}else if(buffer[0] == 2){
//							System.out.println("-----signal quality");
							if(buffer[1] == 0){
								System.out.println("Senser off!");
								Senser = false;
							}else {
								System.out.println("Senser on");
								Senser = true;
							}
							if(buffer[2] == 3){
//								System.out.println("-----Real time HeartRate");
								System.out.println("buffer[3] :" + (buffer[3]&0xff));
								hRate = Integer.toString((buffer[3]&0xff));
							}
						}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//						ds2.close();
//						ds2 = null;
				} 
			}
			
		}).start();
	}		
	
	
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	
}		
