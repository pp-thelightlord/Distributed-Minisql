package Distributed_Minisql;

import java.io.*;
import java.net.Socket;

public class SocketThread implements Runnable {
    private boolean isRunning = false;
    public BufferedReader input = null;
    public BufferedWriter output = null;
    private TableManager tableManager;
    private Socket socket;


    // 构造函数，从socket中获取输入输出流
    public SocketThread(Socket socket, TableManager tableManager) throws IOException{
        this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.isRunning = true;
        this.tableManager = tableManager;
        this.socket = socket;
    }

    // 在线程的run函数中，循环地从socket读取一行指令并进行处理，否则阻塞
    @Override
    public void run() {
        String inputStr;
        try{
            while (isRunning){
                Thread.sleep(500);
                inputStr = input.readLine();
                if (inputStr.equals("end"))
                {
                    ;
                }
                else processCommand(inputStr);
            }
        }
        catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public boolean createTable(String tableName, String command) throws IOException{
        String bss = tableManager.getBestServer();
        String bssCopy = tableManager.getBestServer(bss);
        SocketThread bssIp = tableManager.getSocketThread(bss);
        SocketThread bssIpCopy = tableManager.getSocketThread(bssCopy);
        // 对选中的两个服务器各发送建表请求
        // 主机发一份
        bssIp.output.write("execute:" + command);
        bssIp.output.newLine();
        bssIp.output.write(":end");
        bssIp.output.newLine();
        bssIp.output.flush();
        // 副本机发一份
        bssIpCopy.output.write("execute:" + command);
        bssIpCopy.output.newLine();
        bssIpCopy.output.write(":end");
        bssIpCopy.output.newLine();
        bssIpCopy.output.flush();

        String input = bssIp.input.readLine();
        bssIp.input.readLine();
        if (input.contains("error"))
            return false;
        input = bssIpCopy.input.readLine();
        if (input.contains("error"))
            return false;
        // 此时备份成功，需要通知主机它的备份机在哪
        bssIp.output.write("region:" + tableName + ":" + bss + ":" + bssCopy);
        bssIpCopy.output.newLine();
        bssIpCopy.output.write(":end");
        bssIpCopy.output.newLine();
        bssIpCopy.output.flush();
        // 在表管理器中添加这张表
        tableManager.addTable(tableName, bss, bssCopy);
        return true;
    }

    // 处理命令，需要根据字符串判断来源socket的种类
    public void processCommand(String cmd) throws IOException{
        System.out.println(cmd);
        String sourceIp = socket.getInetAddress().getHostAddress();
        String result = "";
        String[] cmds = cmd.split(":");
        switch (cmds.length){
            // 来自客户端的sql语句，只有create table这一种
            case 1:
                String tableName = cmds[0].split(" ")[2];
                if (tableManager.tableToIp.containsKey(tableName) || tableManager.tableToCopyIp.containsKey(tableName)) {
                    output.write("duplicated");
                    output.newLine();
                    output.write("end");
                    output.newLine();
                    output.flush();
                }
                // 选择最佳Region服务器，发送
                else {
                    boolean f = createTable(tableName, cmd);
                    if (f){
                        System.out.println("创建表成功");
                    }
                    else {
                        System.out.println("创建表失败");
                    }
                }
                break;
            case 2:
                // 删除表指令
                if (cmds[0].equals("tableDrop")){
                    this.tableManager.deleteTable(cmds[1], tableManager.getIpAddressMain(cmds[1]), tableManager.getIpAddressCopy(cmds[1]));
                    System.out.println("删除表成功" + cmds[1]);
                }
                else if (cmds[0].equals("search")){
                    String tableToSearch = cmds[1];
                    if (tableManager.tableToIp.containsKey(tableToSearch)){
                        String gotIp = tableManager.tableToIp.get(tableToSearch);
                        output.write("<ip>:" + tableToSearch + ":" + gotIp);
                    }
                    else {
                        output.write("<ip>:" + tableToSearch + ":unreachable");
                    }
                    output.newLine();
                    output.write("end");
                    output.newLine();
                    output.flush();
                }
                break;
            case 3:

                break;
            case 4:

                break;
        }
    }

}

