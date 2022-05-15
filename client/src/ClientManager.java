import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientManager {
    MasterConnector masterconnector=null;
    CacheManager cachemanager=null;
    RegionConnector regionconnector=null;
    boolean flag=true;
    public ClientManager(){
        cachemanager=new CacheManager();
       
    }
    public String GetTableName(String sql){
        String [] tokens=sql.split(" ");
        if(tokens[0].equals("drop")||tokens[0].equals("insert")||tokens[0].equals("delete"))
            return tokens[2];

        if(tokens[0].equals("select")){
            for(int i=0;i<tokens.length;i++){
                if (tokens[i].equals("from") && i != tokens.length - 1) {
                    return tokens[i+1];
                }
            }
        }
        return "table";

    }
    public void run() throws UnknownHostException, IOException, InterruptedException{
        Scanner console=new Scanner(System.in);
        String allsql,str,sql;
     
        System.out.println("Please inter master ip:");
        String ip=console.nextLine();
        System.out.println("Please inter master port:");
        int port=console.nextInt();
        masterconnector=new MasterConnector(cachemanager,ip,port);
        regionconnector=new RegionConnector(cachemanager,masterconnector);
        while(flag){
            allsql="";
            str="";
            while(str.isEmpty()||str.endsWith(";")==false){
                str=console.nextLine();
                allsql=allsql+str+" ";
            }
            String []sqls= allsql.split(";");
            
            // System.out.println("---------------------");
            System.out.println("输入的语句是"+allsql);
            for(int i=0;i<sqls.length-1;i++){
                sql=(sqls[i]+";").trim();
                
                if(sql.equals("quit;")){
                    flag=false;
                    break;
                }
                if(sql.startsWith("create")||sql.startsWith("drop")){//send to master directly
                    masterconnector.send(sql);
                }
                
                else {
                    String table = GetTableName(sql);
                    table="table";///调试
                    String regioncache=cachemanager.GetCache(table);
                    if(regioncache!="null"&&regioncache!="unreachable"){
                        if(regionconnector.ConnectToRegion(regioncache, sql)==false){
                            cachemanager.DelCache(table);
                           
                        }
                    }
                    if(cachemanager.GetCache(table)=="null"){
                        while(cachemanager.GetCache(table)=="null"){
                            masterconnector.send("search:"+table);
                            Thread.sleep(100);
                        }
                        regioncache=cachemanager.GetCache(table);
                        if(regioncache.equals("unreachable")){
                            cachemanager.DelCache(table);
                            System.out.println("the tablename is wrong，we can not find it in master!");
                        }else{
                            regionconnector.ConnectToRegion(regioncache, sql);
                        }
                    }
                    
                }
            }
            
            // masterConnector.send(sql);

        }
        masterconnector.release();
        console.close();
        return ;
    }
}
