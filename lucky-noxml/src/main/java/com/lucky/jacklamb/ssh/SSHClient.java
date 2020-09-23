package com.lucky.jacklamb.ssh;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.lucky.jacklamb.rest.LSON;
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

    private Remote remote;
    
    private Session session;

    public SSHClient(){
        remote=new Remote();
    }

    public SSHClient setUser(String user){
        this.remote.setUser(user);;
        return this;
    }

    public SSHClient setPassword(String password){
        this.remote.setPassword(password);
        return this;
    }

    public SSHClient setIdentity(String identity) {
        this.remote.setIdentity(identity);
        return this;
    }

    public SSHClient setPassphrase(String passphrase) {
        this.remote.setPassphrase(passphrase);
        return this;
    }

    public SSHClient setHost(String host){
        this.remote.setHost(host);
        return this;
    }

    public SSHClient setPort(int port){
        this.remote.setPort(port);
        return this;
    }

    public void login() throws JSchException {
        JSch jSch = new JSch();
        if(Files.exists(Paths.get(remote.getIdentity()))){
            jSch.addIdentity(remote.getIdentity(),remote.getPassphrase());
        }
        session = jSch.getSession(remote.getUser(), remote.getHost(),remote.getPort());
        session.setPassword(remote.getPassword());
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(remote.getSessionTimeout());
        System.out.println("Host("+remote.getHost()+") connected.");
    }

    public SSHClient(Remote remote) {
        this.remote=remote;
    }

    /**
     * 发送命令
     * @param command
     * @return
     * @throws JSchException
     */
    public String sendCmd(String command) throws JSchException {
        if(!session.isConnected()){
            login();
        }
        System.out.println(">> "+command);
        String result=null;
        ChannelExec channel = null;
        try{
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            InputStream input = channel.getInputStream();
            channel.connect(remote.getConnectTimeout());
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
        System.out.println("Host("+remote.getHost()+") exits....");
    }

    /**
     * 将文件上传到远程服务器
     * @param source 本地文件路径
     * @param destination 远程服务器的目标文件夹的绝对路径
     * @return
     */
    public long scpTo(String source, String destination) {
        FileInputStream fileInputStream = null;
        ChannelExec channel=null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();
            boolean ptimestamp = false;
            String command = "scp";
            if (ptimestamp) {
                command += " -p";
            }
            command += " -t " + destination;
            channel.setCommand(command);
            channel.connect(remote.getConnectTimeout());
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
            channel.disconnect();
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
     * 将文件夹上传到远程服务器
     * @param folder
     * @param destination
     * @throws JSchException
     */
    public void scpFolderTo(String folder,String destination) throws JSchException {
        folder=folder.endsWith(File.separator)?folder:folder+File.separator;
        destination=destination.endsWith("/")?destination:destination+"/";
        File file=new File(folder);
        if(!file.exists())
            throw new RuntimeException("文件夹"+folder+"不存在...");
        String serverFolder=destination+file.getName();
        sendCmd("mkdir "+serverFolder);
        File[] files = file.listFiles();
        for (File f : files) {
            if(f.isDirectory()){
                scpFolderTo(folder+f.getName(),serverFolder);
            }else{
                scpTo(f.getAbsolutePath(),serverFolder);
            }
        }
    }

    /**
     * 从远程服务器下载文件
     * @param serverFilePath 服务器文件的绝对路径
     * @param locationFilePath 本地保存的目录路径
     * @return
     */
    public long scpFrom(String serverFilePath, String locationFilePath) {
        FileOutputStream fileOutputStream = null;
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("scp -f " + serverFilePath);
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
            if (Files.isDirectory(Paths.get(locationFilePath))) {
                fileOutputStream = new FileOutputStream(locationFilePath + File.separator +file);
            } else {
                fileOutputStream = new FileOutputStream(locationFilePath);
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

    /**
     * 远程编辑[逐行编辑]
     * @param source 源文件在远程服务中的绝对路径
     * @param process 逐行编辑的实现
     * @return
     */
    public boolean remoteEditLine(String source, Function<List<String>, List<String>> process) {
        InputStream in = null;
        OutputStream out = null;
        File oldFile=null;
        File newFile=null;
        try {
            String tmpSource =dataPull(source);
            oldFile=new File(tmpSource);
            in = new FileInputStream(oldFile);
            //edit file according function process
            String tmpDestination = tmpSource + ".des";

            newFile=new File(tmpDestination);
            out = new FileOutputStream(newFile);
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
            oldFile.delete();
            newFile.delete();
        }
    }

    /**
     * 远程编辑[流式编辑]
     * @param source 源文件在远程服务中的绝对路径
     * @param editing 流式编辑的实现
     * @return
     */
    public boolean remoteEdit(String source,RemoteEditing editing) {
        InputStream in = null;
        OutputStream out = null;
        File oldFile=null;
        File newFile=null;
        try {
            String tmpSource = dataPull(source);
            oldFile=new File(tmpSource);
            in = new FileInputStream(oldFile);
            //edit file according function process
            String tmpDestination = tmpSource + ".des";
            newFile=new File(tmpDestination);
            out = new FileOutputStream(newFile);
            editing.editor(in,out);
            scpTo(tmpDestination,source);
            return true;
        } catch (Exception e) {
            log.error("remote edit error, ", e);
            return false;
        } finally {
            oldFile.delete();
            newFile.delete();
        }
    }

    private String dataPull(String source) throws JSchException {
        String fileName = source;
        String currFolder="/";
        int index = source.lastIndexOf('/');
        if (index >= 0) {
            fileName = source.substring(index + 1);
            currFolder=source.substring(0,index+1);
        }
        String backupFolder = currFolder + fileName + "_BACKUP/";
        //原数据数据备份
        sendCmd("mkdir "+backupFolder);
        sendCmd(String.format("cp %s %s", source, backupFolder+ fileName+ ".bak." +System.currentTimeMillis()));
        //scp from remote
        String tmpSource = System.getProperty("java.io.tmpdir") + session.getHost() +"-" + fileName;
        scpFrom(source, tmpSource);
        return tmpSource;
    }


    public static void main(String[] args) throws JSchException {
        System.out.println(System.getProperty("java.io.tmpdir"));
        SSHClient ssh=new SSHClient().setHost("192.168.0.170").setPassword("123456");
        ssh.login();
//        ssh.scpTo("D:\\GitHub-Project\\lucky-noxml\\src\\main\\resources\\lucky-config\\config\\ContentType.json","/home/mytest/");
        ssh.remoteEdit("/home/mytest/ContentType.json",(in,out)->{
            BufferedReader br=new BufferedReader(new InputStreamReader(in));
            Gson gson=new Gson();
            List<String[]> list = gson.fromJson(br, new TypeToken<List<String[]>>() {
            }.getType());
            for (String[] array : list) {
                array[0]="HELLO";
                array[1]="SB";
            }
            BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(out));
            bw.write(new LSON().toFormatJsonByGson(list));
            bw.close();
            br.close();
        });
        ssh.sendCmd("cat /home/mytest/ContentType.json");
        ssh.logOut();
    }
}
