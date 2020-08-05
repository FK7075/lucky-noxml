package com.lucky.jacklamb.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SSHClient {

    private static final Logger log= LogManager.getLogger(SSHClient.class);

    private String user="root";

    private String host="localhost";

    private int port=22;

    private String password="";

    private final static int CONNECT_TIMEOUT = 30000;

    private final static Integer SESSION_TIMEOUT = 30000;

    private Session session;

    public SSHClient setUser(String user){
        this.user=user;
        return this;
    }

    public SSHClient setPassword(String password){
        this.password=password;
        return this;
    }

    public SSHClient setHost(String host){
        this.host=host;
        return this;
    }

    public SSHClient setPort(int port){
        this.port=port;
        return this;
    }

    public SSHClient login() throws JSchException {
        JSch jSch = new JSch();
        session = jSch.getSession(user, host,port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(SESSION_TIMEOUT);
        System.out.println("Host("+host+") connected.");
        return this;
    }

    public SSHClient(String user, String host, int port, String password) {
        this.user = user;
        this.host = host;
        this.port = port;
        this.password = password;
    }

    public SSHClient( String host, String user,String password) {
        this.user = user;
        this.host = host;
        this.password = password;
    }

    /**
     * 发送命令
     * @param command
     * @return
     * @throws JSchException
     */
    public String sendCmd(String command) throws JSchException {
        if(session.isConnected()){
            login();
        }
        System.out.println(">> "+command);
        String result=null;
        ChannelExec channel = null;
        try{
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            InputStream input = channel.getInputStream();
            channel.connect(CONNECT_TIMEOUT);
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(input));
                String inputLine = null;
                while((inputLine = inputReader.readLine()) != null) {
                    System.out.println("   "+inputLine);
                    result+=inputLine+"\r\n";
                }
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (Exception e) {
                        log.error("JSch inputStream close error:", e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("IOcxecption:", e);
        } finally {
            if (channel != null) {
                try {
                    channel.disconnect();
                } catch (Exception e) {
                    log.error("JSch channel disconnect error:", e);
                }
            }
        }
        return result;
    }

    public void logOut(){
        this.session.disconnect();
    }

    /**
     * 将文件上传到远程服务器
     * @param source 本地文件路径
     * @param destination 远程服务器的目标文件夹的绝对路径
     * @return
     */
    public long scpTo(String source, String destination) {
        FileInputStream fileInputStream = null;
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();
            boolean ptimestamp = false;
            String command = "scp";
            if (ptimestamp) {
                command += " -p";
            }
            command += " -t " + destination;
            channel.setCommand(command);
            channel.connect(CONNECT_TIMEOUT);
            if (checkAck(in) != 0) {
                return -1;
            }
            File _lfile = new File(source);
            if (ptimestamp) {
                command = "T " + (_lfile.lastModified() / 1000) + " 0";
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
                out.write(command.getBytes());
                out.flush();
                if (checkAck(in) != 0) {
                    return -1;
                }
            }
            //send "C0644 filesize filename", where filename should not include '/'
            long fileSize = _lfile.length();
            command = "C0644 " + fileSize + " ";
            if (source.lastIndexOf(File.separator) >= 0) {
                command += source.substring(source.lastIndexOf(File.separator) + 1);
            } else {
                command += source;
            }
            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                return -1;
            }
            //send content of file
            fileInputStream = new FileInputStream(_lfile);
            byte[] buf = new byte[1024];
            long sum = 0;
            while (true) {
                int len = fileInputStream.read(buf, 0, buf.length);
                if (len <= 0) {
                    break;
                }
                out.write(buf, 0, len);
                sum += len;
            }
            //send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                return -1;
            }
            return sum;
        } catch(JSchException e) {
            log.error("scp to catched jsch exception, ", e);
        } catch(IOException e) {
            log.error("scp to catched io exception, ", e);
        } catch(Exception e) {
            log.error("scp to error, ", e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Exception e) {
                    log.error("File input stream close error, ", e);
                }
            }
        }
        return -1;
    }

    /**
     * 从远程服务器下载文件
     * @param source 服务器文件的绝对路径
     * @param destination 本地保存的目录路径
     * @return
     */
    public long scpFrom(String source, String destination) {
        FileOutputStream fileOutputStream = null;
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("scp -f " + source);
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();
            channel.connect();
            byte[] buf = new byte[1024];
            //send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            while(true) {
                if (checkAck(in) != 'C') {
                    break;
                }
            }
            //read '644 '
            in.read(buf, 0, 4);
            long fileSize = 0;
            while (true) {
                if (in.read(buf, 0, 1) < 0) {
                    break;
                }
                if (buf[0] == ' ') {
                    break;
                }
                fileSize = fileSize * 10L + (long)(buf[0] - '0');
            }
            String file = null;
            for (int i = 0; ; i++) {
                in.read(buf, i, 1);
                if (buf[i] == (byte) 0x0a) {
                    file = new String(buf, 0, i);
                    break;
                }
            }
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            // read a content of lfile
            if (Files.isDirectory(Paths.get(destination))) {
                fileOutputStream = new FileOutputStream(destination + File.separator +file);
            } else {
                fileOutputStream = new FileOutputStream(destination);
            }
            long sum = 0;
            while (true) {
                int len = in.read(buf, 0 , buf.length);
                if (len <= 0) {
                    break;
                }
                sum += len;
                if (len >= fileSize) {
                    fileOutputStream.write(buf, 0, (int)fileSize);
                    break;
                }
                fileOutputStream.write(buf, 0, len);
                fileSize -= len;
            }
            return sum;
        } catch(JSchException e) {
            log.error("scp to catched jsch exception, ", e);
        } catch(IOException e) {
            log.error("scp to catched io exception, ", e);
        } catch(Exception e) {
            log.error("scp to error, ", e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                    log.error("File output stream close error, ", e);
                }
            }
        }
        return -1;
    }

    private static int checkAck(InputStream in) throws IOException {
        int b=in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if(b==0) return b;
        if(b==-1) return b;
        if(b==1 || b==2){
            StringBuffer sb=new StringBuffer();
            int c;
            do {
                c=in.read();
                sb.append((char)c);
            }
            while(c!='\n');
            if(b==1){ // error
                System.out.println(sb.toString());
            }
            if(b==2){ // fatal error
                System.out.println(sb.toString());
            }
        }
        return b;
    }

    public boolean remoteEdit(String source, Function<List<String>, List<String>> process) {
        InputStream in = null;
        OutputStream out = null;
        try {
            String fileName = source;
            int index = source.lastIndexOf('/');
            if (index >= 0) {
                fileName = source.substring(index + 1);
            }
            //backup source
            sendCmd(String.format("cp %s %s", source, source + ".bak." +System.currentTimeMillis()));
            //scp from remote
            String tmpSource = System.getProperty("java.io.tmpdir") + session.getHost() +"-" + fileName;
            scpFrom(source, tmpSource);
            in = new FileInputStream(tmpSource);
            //edit file according function process
            String tmpDestination = tmpSource + ".des";
            out = new FileOutputStream(tmpDestination);
            List<String> inputLines = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String inputLine = null;
            while ((inputLine = reader.readLine()) != null) {
                inputLines.add(inputLine);
            }
            List<String> outputLines = process.apply(inputLines);
            for (String outputLine : outputLines) {
                out.write((outputLine + "\n").getBytes());
                out.flush();
            }
            //scp to remote
            scpTo(tmpDestination,source);
            return true;
        } catch (Exception e) {
            log.error("remote edit error, ", e);
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    log.error("input stream close error", e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    log.error("output stream close error", e);
                }
            }
        }
    }

    /*

    remoteExecute(session, "pwd");
    remoteExecute(session, "mkdir /root/jsch-demo");
    remoteExecute(session, "ls /root/jsch-demo");
    remoteExecute(session, "touch /root/jsch-demo/test1; touch /root/jsch-demo/test2");
    remoteExecute(session, "echo 'It a test file.' > /root/jsch-demo/test-file");
    remoteExecute(session, "ls -all /root/jsch-demo");
    remoteExecute(session, "ls -all /root/jsch-demo | grep test");
    remoteExecute(session, "cat /root/jsch-demo/test-file");
     */
    public static void main(String[] args) throws JSchException {
        SSHClient ssh=new SSHClient("192.168.0.167","root","123456").login();
        ssh.remoteEdit("/mytest/test.txt",(inLs)->{
            return inLs.stream().map(l->l.toUpperCase()).collect(Collectors.toList());
        });
        ssh.sendCmd("cat /mytest/test.txt");
    }
}
