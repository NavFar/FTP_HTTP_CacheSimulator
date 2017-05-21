package com.company;

import com.sun.istack.internal.Nullable;
import jdk.internal.util.xml.impl.Input;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Created by navidfarahmand on 5/21/17.
 */
public class HttpWrapper {
    private String address;
    private Socket connectionSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private InetAddress inetAddress;
    public HttpWrapper(String address){
        try {
            this.address=address;
            this.inetAddress=InetAddress.getByName(address);
            this.connectionSocket=new Socket(this.inetAddress,80);
            this.inputStream= this.connectionSocket.getInputStream();
            this.outputStream=this.connectionSocket.getOutputStream();
        } catch (UnknownHostException e) {
            //e.printStackTrace();
            System.out.println("Error while Try to convert name to address");
            return;
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error while Try to connect to address on port 80");
            return;
        }
    }
    public HttpResult doHttpMethod(String method,String address, @Nullable String data){
        HttpResult httpResult=new HttpResult(400,"Mon, 00 Jan 0000 00:00:00 GMT"," ".getBytes());
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(this.outputStream);
            dataOutputStream.writeBytes((method+" "+address+" HTTP/1.1\r\n"));
            dataOutputStream.writeBytes(("Host: "+this.address+":80\r\n"));
            dataOutputStream.writeBytes(("\r\n"));
            dataOutputStream.flush();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
            boolean loop = true;
            StringBuilder stringBuilder = new StringBuilder();
            while (loop) {
                if (bufferedReader.ready()) {
                    int i = 0;
                    while (i != -1) {
                        i = bufferedReader.read();
                        stringBuilder.append((char) i);
                    }
                    loop = false;
                }
            }
            //System.out.println(stringBuilder.toString());
            return new HttpResult(stringBuilder);

        } catch (IOException e) {
            System.out.println("Error while Try to use method on connection");
            return httpResult;
        }

    }
    @Override
    protected void finalize() throws Throwable {
        this.connectionSocket.close();
        super.finalize();
    }
}
