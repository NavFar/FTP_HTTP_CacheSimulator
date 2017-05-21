package com.company;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by navidfarahmand on 5/22/17.
 */
public class ProxyServer implements Runnable{
    private JSONObject cachedObjects;
    private ServerSocket liveSocket;
    private static String basePath="~94131090/CN1_Project_Files/";
    private HttpWrapper serverConnection;
    public  ProxyServer(boolean clear)
    {
        serverConnection = new HttpWrapper("ceit.aut.ac.ir");
        File file =new File("cache");
        if(!file.exists()){
            file.mkdir();
        }
        if(clear){
            String [] list = file.list();
            for(String s : list){
                File temp_file=new File(file.getPath(),s);
                temp_file.delete();
            }
            file.mkdir();
        }
        try {
            File listFile =new File("cache/list.cacheList");
            if(listFile.exists()&&listFile.canRead()) {
                String content = new Scanner(listFile).useDelimiter("\\Z").next();
                cachedObjects = new JSONObject(content);
            }
            else
                cachedObjects = new JSONObject();

        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.out.println("Can't find previous file list -Start normally");

        } catch (JSONException e) {
            //e.printStackTrace();
            System.out.println("Can't decode previous file list - Start normally");
        }
        finally {
            try {
                this.liveSocket=new ServerSocket(2525);
                this.liveSocket.setSoTimeout(50000);
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Can'n Listen to port 2525 (Exit panic mode)");
                System.exit(1);
            }
            Thread thread = new Thread(this,"Proxy Server Main Thread");
            thread.start();
        }
    }
    public HttpResult retriveFile(String name) {
        synchronized (this.cachedObjects) {
            HttpResult existFile = null;
            try {
                boolean result = this.cachedObjects.has(name);
                if (result) {
                    // log
                    existFile = new HttpResult(name);
                    HttpResult serverFile = this.serverConnection.doHttpMethod("Head", this.basePath + name, null);
                    if (existFile.getLastModified().before(serverFile.getLastModified())) {
                        // log
                        existFile = this.serverConnection.doHttpMethod("GET", this.basePath + name, null);
                        existFile.saveToFile(name);
                    }
                    return existFile;
                } else {
                    // log
                    existFile = this.serverConnection.doHttpMethod("GET", this.basePath + name, null);
                    this.cachedObjects.put(name, 1);
                    return existFile;

                }
            } catch (JSONException e) {
                //e.printStackTrace();
                System.out.println("Can't access to cached file (Exit panic mode)");
                System.exit(1);
            }
            return existFile;
        }
    }
    public boolean removeFromCache(String name) {
        synchronized (this.cachedObjects) {
            if (this.cachedObjects.has(name)) {
                this.cachedObjects.remove(name);
                File file = new File("cache/" + name);
                if (file.exists()) {
                    return file.delete();
                }
                return true;
            } else
                return true;
        }
    }
    @Override
    public void run() {
        while (true) {
            try {
                Socket inputSocket = this.liveSocket.accept();
                //Add new Machine for user??????????????????????
                // log
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Can'n Accept inputSocket on 2525 (Exit panic mode)");
                System.exit(1);
            }
        }
    }
}
