package com.company;

import com.sun.istack.internal.Nullable;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Vector;

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
            System.out.println("Error while Try to convert name to address(Exit panic mode)");
            System.exit(1);
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error while Try to connect to address on port 80(Exit panic mode)");
            System.exit(1);
        }
    }
    public HttpResult doHttpMethod(String method,String address, @Nullable String data){
        HttpResult httpResult=new HttpResult(400,"Mon, 00 Jan 0000 00:00:00 GMT"," ".getBytes());
        try {
//            address=address.replace(" ","%20");
//            address= URLEncoder.encode(address,"UTF-8").replace("+","%20");
            DataOutputStream dataOutputStream = new DataOutputStream(this.outputStream);
            dataOutputStream.writeBytes((method+" "+address+" HTTP/1.1\r\n"));
            dataOutputStream.writeBytes(("Host: "+this.address+":80\r\n"));
            dataOutputStream.writeBytes(("\r\n"));
            dataOutputStream.flush();
            byte[] b = new byte[1];
            int index=0;
            byte[] tempByte=new byte[4096];
            Vector<Byte> allData=new Vector<Byte>();
            int hasAny=this.inputStream.read(b);
            while(hasAny!=-1){
                allData.add(b[0]);
                hasAny=this.inputStream.read(b);
            }
            byte[] allDataInByte=new byte[allData.size()];
            for(int i=0;i<allData.size();i++){
                allDataInByte[i]=allData.elementAt(i);
            }
            for(int i=0;i<tempByte.length;i++){
                if(i+3<tempByte.length&&tempByte[i]==10&&tempByte[i+1]==13&&tempByte[i+2]==10&&tempByte[i+3]==13)
                {
                    index=i+4;
                }
//                byte[] content= Arrays.copyOfRange(tempByte,index,tempByte.length);
//                byte[] header = Arrays.copyOfRange(tempByte,0,index-4);

//            System.out.println(new String (b));
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
//            boolean loop = true;
//            StringBuilder stringBuilder = new StringBuilder();
//            while (loop) {
//                if (bufferedReader.ready()) {
//                    int i = 0;
//                    while (i != -1) {
//                        i = bufferedReader.read();
//                        stringBuilder.append((char) i);
//                    }
//                    loop = false;
//                }
            }
//            //System.out.println(stringBuilder.toString());

            return new HttpResult(allDataInByte,2);

        } catch (IOException e) {
            System.out.println("Error while Try to use method on connection(Exit panic mode)");
            System.exit(1);
        }
        return httpResult;
    }
    @Override
    protected void finalize() throws Throwable {
        this.connectionSocket.close();
        super.finalize();
    }
}
