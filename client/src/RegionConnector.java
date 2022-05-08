import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.BufferedWriter;


public class RegionConnector {
    
    private CacheManager cachemanager;
   private MasterConnector masterconnector;
    public RegionConnector(CacheManager cache,MasterConnector master){
        cachemanager=cache;
        masterconnector=master;
    }
    public boolean ConnectToRegion(String info,String sql){
        ToRegion toregion=new ToRegion(info,sql);
        if(toregion.ConnectThisRegion()==false) return false;
        new Thread(toregion).start();
        toregion.send(sql);
        return true;
    }
    
    


    class ToRegion implements Runnable {
       
        
        
        private Socket socket;
        // private DataInputStream cin;
        // private DataOutputStream cout;
        private BufferedReader cin;
        private BufferedWriter cout;
        private String ip;
        private int port;
        private boolean isRunning=true;
        private String sql;
        public ToRegion(String info,String sql) {
            String[] infos=info.split(" ");
            ip=infos[0];
            port=Integer.parseInt(infos[1]);
            this.sql=sql;
        }
        public boolean ConnectThisRegion(){
            try {
                socket=new Socket(ip,port);
                
            } catch (UnknownHostException e) {
                System.out.println("the region"+ip+" "+port+"can not connect");
                return false;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                cin = new BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
            cout = new BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream()));
                // cout=new DataOutputStream(socket.getOutputStream());
                // cin=new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return true;
        }
        public void send(String str){
            try {
                System.out.println("you will send: "+str);
                // String sendstr=;
                cout.write("excute:"+str);
                cout.newLine();
                cout.write("end");
                cout.newLine();
                // cout.writeUTF("excute:123");
                // cout.writeUTF("\n"+"end");
                cout.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            };
            
        }
        //接收消息
        private void receive() {
            String msg ="";
           
            try {
                // msg=cin.readUTF();
                while( (msg = cin.readLine()) != null) {
                    if (msg.equals("end")) {
                        release();
                        break;
                    }
                    System.out.println(msg);
                }
                // msg=cin.readLine();
                // msg=cin.read
                // System.out.println("from region: "+msg);
                // if(msg.isEmpty()==false){
                //     System.out.println(msg);    
                //     release();
                // }
                           
            } catch (IOException e) {
                 release();
                 
            }
           
        }
        
        @Override
        public void run() {	
            while(isRunning) {
                receive();
            }
            return ;
        }
        //释放资源
        public void release() {
            isRunning = false;
            System.out.println("break socket connected with region");
        }
    }
}

