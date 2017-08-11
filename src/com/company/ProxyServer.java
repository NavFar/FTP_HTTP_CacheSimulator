package com.company;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by navidfarahmand on 5/22/17.
 */
public class ProxyServer implements Runnable{
    private JSONObject cachedObjects;
    private ServerSocket controllSocket;
    private ServerSocket dataSocket;
    private static String basePath="/~94131090/CN1_Project_Files/";
    private HttpWrapper serverConnection;
    private JSONObject userpasses;
    private Integer ftpLogLock;
    private Integer httpLogLock;

    public static void main(String[] args) {
        ProxyServer proxyServer = new ProxyServer();
    }
    public  ProxyServer()
    {
        Runnable refresher=new Runnable() {
            @Override
            public void run() {
                ProxyServer.this.refreshAll();
            }
        };
        boolean clear=true;
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(refresher, 0, 1, TimeUnit.HOURS);
        this.ftpLogLock=new Integer(0);
        this.httpLogLock=new Integer(0);
        try {
            File httpLogFile = new File("log/httpLog.txt");
            if(httpLogFile.exists())
            {
                httpLogFile.delete();
            }
            File FTPLogFile = new File("log/ftpLog.txt");
            if(FTPLogFile.exists())
            {
                FTPLogFile.delete();
            }
            File userpassFile = new File("configs/passwords.json");
            String content = new Scanner(userpassFile).useDelimiter("\\Z").next();
            this.userpasses = new JSONObject(content);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.out.println("Cant'find User and passwords file (exit panic mode)");
            System.exit(1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        serverConnection = new HttpWrapper("ceit.aut.ac.ir");
        File file =new File("cache/");
        if(!file.exists()){
            file.mkdir();
        }
        this.cachedObjects=new JSONObject();
        if(clear){
            String [] list = file.list();
            for(String s : list){
                File temp_file=new File(file.getPath(),s);
                temp_file.delete();
            }
            file.mkdir();
        }
        else{

            String [] list = file.list();
            for(String s : list){
//                File temp_file=new File(file.getPath(),s);
                try {
                    this.cachedObjects.put(s,"1");
                } catch (JSONException e) {
                    //e.printStackTrace();
                    System.out.println("Can't load previous files");
                    this.cachedObjects=new JSONObject();
                }
            }
        }
            try {
                this.controllSocket=new ServerSocket(2525);
                this.dataSocket=new ServerSocket(2526);
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Can'n Listen to port 2525 or 2526 (Exit panic mode)");
                System.exit(1);
            }
            Thread thread = new Thread(this,"Proxy Server Main Thread");
            thread.start();

    }
    public void refreshAll(){
        Iterator<String> iter = this.cachedObjects.keys();
        while(iter.hasNext()){
            String key =iter.next();
            HttpResult headResult=this.serverConnection.doHttpMethod("HEAD",this.basePath+key,null);
            HttpResult curRes = new HttpResult(key+".cache");

            if(curRes.getLastModified().before(headResult.getLastModified()))
            {
                curRes = this.serverConnection.doHttpMethod("GET",this.basePath+key,null);
                this.httpLog("Refresh "+ key+" From Server");
                curRes.saveToFile(key+".cache");
            }
        }
    }
    public HttpResult retriveFile(String name) {
        HttpResult existFile=null;
        synchronized (this.cachedObjects) {
            try {
                boolean result = this.cachedObjects.has(name);
                if (result) {
                    // log
                    existFile = new HttpResult(name+".cache");
//                    HttpResult serverFile = this.serverConnection.doHttpMethod("Head", this.basePath + name, null);
//                    if (existFile.getLastModified().before(serverFile.getLastModified())) {
//                        // log
//                        existFile = this.serverConnection.doHttpMethod("GET", this.basePath + name, null);
//                        existFile.saveToFile(name);
//                    }
//                    System.out.println(new String(existFile.getcontent()));
//                    System.out.println("Was here");
                    return existFile;
                } else {
                    // log
//                    existFile = this.serverConnection.doHttpMethod("GET", this.basePath + URLEncoder.encode(name,"UTF-8").replace("+","%20") , null);
                    existFile = this.serverConnection.doHttpMethod("GET", this.basePath + name , null);
                    this.httpLog("get "+name+" from http Server");
                    if(existFile.getstatusCode()==200) {
                        existFile.saveToFile(name + ".cache");
                        this.cachedObjects.put(name, 1);
//                    System.out.println(new String(existFile.getcontent()));
//                        System.out.println("Came here");
                    }
                    return existFile;

                }
            } catch (JSONException e) {
                //e.printStackTrace();
                System.out.println("Can't access to cached file (Exit panic mode)");
                System.exit(1);
            }
// catch (UnsupportedEncodingException e) {
//                //e.printStackTrace();
//                System.out.println("Can't encode to url mode (exit panic mode)");
//            }

            return existFile;
        }
    }
    public boolean removeFromCache(String name,boolean priv) {
        if(name.indexOf("secret_")==0&&!priv)
            return false;
        synchronized (this.cachedObjects) {
            if (this.cachedObjects.has(name)) {
                this.cachedObjects.remove(name);
                File file = new File("cache/" + name+".cache");
                if (file.exists()) {
                    return file.delete();
                }
                return true;
            } else
                return false;
        }
    }
    public boolean removeAll(boolean priv){
        synchronized (this.cachedObjects) {
            boolean answer = true;
            Iterator<String> iter = this.cachedObjects.keys();
            //System.out.println(this.cachedObjects.length());
            while (iter.hasNext()) {
                String key = iter.next();
                if(key.indexOf("secret_")==0&&!priv){
                    continue;
                }
                else {
                    iter.remove();
                    File file = new File("cache/" + key + ".cache");
//                    System.out.println(key);
                    if (file.exists()) {
                        answer = answer && file.delete();
                    }
                }
            }
            return answer;
        }
    }
    public boolean login(String userName,String password){
            if(this.userpasses.has(userName)){
                try {
                    if(password.compareTo((String)((JSONObject)this.userpasses.get(userName)).get("pass"))==0)
                    {
                        return (Boolean)((JSONObject)this.userpasses.get(userName)).get("priv");
                    }
                    else
                        return false;
                } catch (JSONException e) {
                    //e.printStackTrace();
                    System.out.println("Can't Read user Pass file exit panic mode");
                    System.exit(1);
                }
            }
            else
                return false;
            return false;
    }
    @Override
    public void run() {
        while (true) {
            try {
                Socket contorllInputSocket = this.controllSocket.accept();
                Socket dataInputSocket = this.dataSocket.accept();
                //Add new Machine for user??????????????????????
                this.ftpLog("new User","New client machine has created");
                new ClientMachine(this,contorllInputSocket,dataInputSocket);
                // log
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Can'n Accept inputSocket on 2525 or 2526 (Exit panic mode)");
                System.exit(1);
            }
        }
    }
    public String listServer(boolean hasPrivilage){
        String answer="\n";
        HttpResult httpResult= this.serverConnection.doHttpMethod("GET",this.basePath,null);
        this.httpLog("Got a list from server");
        String html = new String (httpResult.getcontent());
//        System.out.println(html);
        int index=html.indexOf("<a ");
        for(int i=0;i<5 & index!=-1;i++){
            index=html.indexOf("<a ",index+1);
        }
        int count=0;
        while(index!=-1){
            count++;
            int start=html.indexOf(">",index)+1;
            int end=html.indexOf("<",start);
            String temp =html.substring(start,end).trim();
            int secretIndex=temp.indexOf("secret_");
            if(secretIndex==0){
                if(hasPrivilage){
                    answer+=count+"- "+temp+"\n";
                }
                else{
                    count--;
                }
            }
            else
            answer+=count+"- "+temp+"\n";
            index=html.indexOf("<a ",index+1);
        }
        return "There are "+count+" files: "+answer;
    }
    public void ftpLog(String username,String message){
        synchronized (this.ftpLogLock) {
            try {
                File ftpLogFile = new File("log/ftpLog.txt");
                String content=new String();
                if(ftpLogFile.exists())
                content = new Scanner(ftpLogFile).useDelimiter("\\Z").next();
                Date curDate = new Date(System.currentTimeMillis());

                content = content + "\n" + curDate + " " + username + " " + message;
                FileOutputStream fileOutputStream = new FileOutputStream("log/ftpLog.txt");
                fileOutputStream.write(content.getBytes());
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
                System.out.println("Can't log user activity (exit panic mode)");
                System.exit(1);
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Can't log user activity(exit panic mode)");
                System.exit(1);
            }
        }
    }
    private void httpLog(String message){
        synchronized (this.httpLogLock) {
            try {
                File ftpLogFile = new File("log/httpLog.txt");
                String content=new String ();
                if(ftpLogFile.exists())
                content = new Scanner(ftpLogFile).useDelimiter("\\Z").next();
                Date curDate = new Date(System.currentTimeMillis());

                content = content + "\n" + curDate + " " + message;
                FileOutputStream fileOutputStream = new FileOutputStream("log/httpLog.txt");
                fileOutputStream.write(content.getBytes());
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
                System.out.println("Can't log http activity (exit panic mode)");
                System.exit(1);
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Can't log http activity (exit panic mode)");
                System.exit(1);
            }
        }
    }
}
