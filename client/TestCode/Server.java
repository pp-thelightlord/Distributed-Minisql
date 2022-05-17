import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

/**
  *@���� Chat
  *@���� TODO Socket����??(����??)
  *@�汾 1.0
  *@����?? XuKang
  *@����ʱ�� 2020/9/24 16:17
  **/
  public class Server {

	private static CopyOnWriteArrayList<Channel> all =new CopyOnWriteArrayList<Channel>();

	public static void main(String[] args) throws IOException {
		System.out.println("-----Server-----");
		// 1��ָ����?? ʹ��ServerSocket��������??
		ServerSocket server =new ServerSocket(9999);
		// 2������ʽ�ȴ����� accept
		while(true) {
				Socket  client =server.accept(); 
				System.out.println("һ���ͻ��˽�������??");
				Channel c =new Channel(client);
				all.add(c); //�������еĳ�Ա
				new Thread(c).start();			
			}		
		}
		//һ���ͻ�����һ��Channel
		static class Channel implements Runnable{
			private BufferedReader cin;
    private BufferedWriter cout;
			
			private Socket  client;			
			private boolean isRunning;
			private String name;
			public Channel(Socket  client) {
				this.client = client;
				try {
					cout = new BufferedWriter(new java.io.OutputStreamWriter(client.getOutputStream()));
       	 cin = new BufferedReader(new java.io.InputStreamReader(client.getInputStream()));
        
					
					isRunning =true;
					//��ȡ����
					this.name =receive();//�˳�������??
					//��ӭ��ĵ���
					this.send("server:��ӭ��ĵ���");
					sendOthers(this.name+"��������??",true);//��ʱ�̶�Ϊ˽??
				} catch (IOException e) {
					System.out.println("---1------");
					release();					
				}			
			}
			//������Ϣ
			private String receive() {
				String msg ="";
				try {
					while( (msg = cin.readLine()) != null) {
						if (msg.equals("end")) {
							release();
							break;
						}
						
						
						
							send("<ip>:table:localhost");
						
						System.out.println(msg);
					}
	
					
				} catch (IOException e) {
					System.out.println("---2------");
					release();
				}
				return msg;
			}
			//������??
			private void send(String msg) {
				try {
					cout.write(msg);
					cout.newLine();
					cout.write("end");
					cout.newLine();
					cout.flush();
				} catch (IOException e) {
					System.out.println("---3------");
					release();
				}
			}
			/**
			 * @����?? sendOthers
			 * @���� TODO Ⱥ�ģ���ȡ�Լ�����Ϣ�����������ˣ���Ҫ����isSysΪfalse
			 * 		 TODO ˽��: Լ�����ݸ�ʽ: @xxx:msg
			 * @���� msg ������??
			 * @����??
			 * @����?? XuKang
			 * @����ʱ�� 2020/9/24 16:28
			 */
			private void sendOthers(String msg,boolean isSys) {
				boolean isPrivate = msg.startsWith("@");
				if(isPrivate) { //˽��
					int idx =msg.indexOf(":");
					//��ȡĿ�����??
					String targetName = msg.substring(1,idx);
					msg = msg.substring(idx+1);
					for(Channel other: all) {
						if(other.name.equals(targetName)) {//Ŀ��
							other.send(this.name +"���ĵض���˵:"+msg);
							break;
						}
					}
				}else {				
					for(Channel other: all) {
						if(other==this) { //�Լ�
							continue;
						}
						if(!isSys) {
							other.send(this.name +"��������??:"+msg);//Ⱥ����Ϣ
						}else {
							other.send(msg); //ϵͳ��Ϣ
						}
					}
				}
			}
			//�ͷ���Դ
			private void release() {
				this.isRunning = false;
				// CloseUtils.close(dis,dos,client);
				//��??
				all.remove(this);
				sendOthers(this.name+"�뿪���??...",true);
			}
			@Override
			public void run() {
				while(isRunning) {
					String msg = receive() ;
					if(!msg.equals("")) {
						//send(msg);
						sendOthers(msg,false);
					}
				}
			}
		}
}

