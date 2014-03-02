import java.io.IOException;
import java.net.*;

public class CatServer {
	public static ServerSocket CatServerPort;
	public static void main(String[] args) 
	{
		try
		{
			System.out.println("INFO: Listening on port 3012.");
			CatServerPort = new ServerSocket(3012);
			while(true)
			{
				Socket newclient = CatServerPort.accept();
				System.out.print("CONNECTION: " + newclient.getRemoteSocketAddress());
				ClientHandler t = new ClientHandler(newclient);
				t.start();
			}
		}
		catch(IOException e){e.printStackTrace();}
	}
}

class ClientHandler extends Thread
{
	//Constants
	public static final int REQ_BUFFER_SIZE = 2048;
	public static final int RES_BUFFER_SIZE = 8192;
	
	private Socket clientSocket;
	Boolean isSecure;
	ClientHandler(Socket sck)
	{
		clientSocket = sck;
		isSecure=false;
	}
	@Override
	public void run()
	{
		try
		{
			byte[] recbuf = new byte[REQ_BUFFER_SIZE];
			String httpRequest = "";
			int i = 0;
			while(true)
			{
				i=clientSocket.getInputStream().read(recbuf,0,REQ_BUFFER_SIZE);
				//Decrypt(recbuf,i); // you can call your cryptography function
				if(i<0)break;
				String strtmp = new String(recbuf,0,i);
				httpRequest += strtmp;
				if(httpRequest.indexOf(".CATCLIENTREQUESTEND.")>=0) //Detecting end of a HTTP request
				{
					httpRequest = httpRequest.substring(0,httpRequest.length()-21);
					break;
				}
			}
			if(httpRequest.length() < 20)return; //incorrect HTTP request
			System.out.println("INFO: Request Recieved.");
			ProcessRequest(httpRequest);
			clientSocket.close();
		}
		catch(IOException e){e.printStackTrace();}
	}
	private int ProcessRequest(String request)
	{
		String [] reqlines = request.split(System.getProperty("line.separator"));
		String hostaddr="",tmpreq = "";
		int i = 0;
		byte [] internetBuffer = new byte[RES_BUFFER_SIZE];
		
		try
		{
			//removing Accept-Encoding from HTTP Request and also detecting GET/POST request
			for(i=0;i<reqlines.length;i++)
			{
				if(reqlines[i].indexOf("POST:")>=0)isSecure=true;   //recognize http or https header
				if(reqlines[i].indexOf("Accept-Encoding")>=0) continue;
				if(reqlines[i].indexOf("Host:")>=0)hostaddr = reqlines[i].substring(6,reqlines[i].length()-1);
				tmpreq = tmpreq + reqlines[i] + System.getProperty("line.separator");
			}
			
			if(hostaddr=="")//incorrect hostname
			{ 
				System.out.println("ERROR: connot detect host name from request.");
				return -1;
			}
			InetAddress hostip = InetAddress.getByName(hostaddr);
			System.out.print("HOST:" + hostaddr + " IP:" + hostip.getHostAddress() + " PORT:80");
			if(!isSecure) //HTTP
			{
				Socket internetSocket = new Socket(hostip,80);
				if(internetSocket.isConnected())System.out.println(" CONNECTED.");
				else
				{
					internetSocket.close();
					return -1;
				}
				internetSocket.getOutputStream().write(tmpreq.getBytes());
				
				while((i=internetSocket.getInputStream().read(internetBuffer,0,RES_BUFFER_SIZE))>0)
				{
					//Encryptor(internetBuffer, i, (byte)55);
					clientSocket.getOutputStream().write(internetBuffer,0,i);
				}
				clientSocket.getOutputStream().write((new String(".CATSERVERRESPONSEEND.")).getBytes(),0,22);//determine end of response
				clientSocket.getOutputStream().flush();
				internetSocket.close();
				System.out.println("Response Sent!");
			}
			return 1;
		}
		catch(IOException e){e.printStackTrace(); return -1;}
	}
}


//you can write your own cryptoghraphy functions
/*	private void Encryptor(byte[]data,int n,byte key)
	{
		byte t = 0;
		for(int i=0;i<n;i++)
		{
			t = data[i];
			t = (byte)(t*(-1));
			data[i] = t;
		}
	}
	private void Decryptor(byte[]data,int n,byte key)
	{
		byte t = 0;
		for(int i=0;i<n;i++)
		{
			t = data[i];
			//t = (t - key) % 128;
			t = (byte)(t * (-1));
			data[i] = t;
		}
	}
}*/
