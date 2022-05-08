import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MasterConnector {
    private Socket socket=null;
    private BufferedReader cin;
    private BufferedWriter cout;
    // private DataOutputStream cout;
    private boolean isRunning=true;
    private String Host="localhost";
    private int Port=9999;
    private CacheManager cachemanager;
   
    
    public MasterConnector(CacheManager cache)throws IOException{
        socket=new Socket(Host,Port);
        cout = new BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream()));
        cin = new BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));     
        isRunning=true;
        cachemanager=cache;
        new Thread(new Receive()).start();
    }
    public  MasterConnector(CacheManager cache,String host,int port) throws UnknownHostException, IOException{
        socket=new Socket(host,port);
        cout = new BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream()));
        cin = new BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
        isRunning=true;
        cachemanager=cache;
        new Thread(new Receive()).start();
    }
    public void send(String str){
        try {
           
            cout.write(str);
            cout.newLine();
            cout.write("end");
            cout.newLine();
            cout.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        };
        
    }
    public void release() {
        isRunning = false;
        
    }


    class Receive implements Runnable {
        
        public Receive() {
            
        
        }
        //接收消息
        private void receive() {
            String msg ="";
           
            try {
                while( (msg = cin.readLine()) != null) {
                    System.out.println("Test from server:"+msg);
                    if (msg.equals("end")) {
                        release();
                        break;
                    }
                    if(msg.startsWith("<ip>:")){// 
                        String [] ips=msg.split(":");
                        // System.out.println("i will add cache");
                        cachemanager.AddCache(ips[1], ips[2]);
                        ///<ip>:tablename:ip
                        //<ip>:tablename:unreachable

                    }
                    else  System.out.println(msg);
                }
               
               
            } catch (IOException e) {
                
                
                 release();
                
            }
           
        }
        
        @Override
        public void run() {		
            while(isRunning) {
                receive();
            }
        }
        //释放资源
        
    }
}

