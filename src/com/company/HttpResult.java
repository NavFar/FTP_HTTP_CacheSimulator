package com.company;
import java.util.Scanner;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * Created by navidfarahmand on 5/21/17.
 */
public class HttpResult {
    private Date lastModified;
    private byte content[];
    private int statusCode;
    public HttpResult(String fileName){
        try {
            SimpleDateFormat simpleDateFormat= new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            String content = new Scanner(new File("cache/"+fileName)).useDelimiter("\\Z").next();
            JSONObject jsonObject = new JSONObject(content);
            this.content = Base64.decode((String)jsonObject.get("content"));
            this.lastModified = simpleDateFormat.parse((String)jsonObject.get("last-modified"));
            this.statusCode = (Integer)jsonObject.get("statusCode");
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.out.println("Error while Read Previous Files(File Can't be opened)");
            return;
        } catch (JSONException e) {
            //e.printStackTrace();
            System.out.println("Error while Read Previous Files(Broken Json Object)");
            return;
        } catch (ParseException e) {
            //e.printStackTrace();
            System.out.println("Error while Read Previous Files(Date Parse Error)");
            return;
        }  catch (Base64DecodingException e) {
            //e.printStackTrace();
            System.out.println("Error while Read Previous Files(Content Parsing problem)");
            return;
        }
    }
    public HttpResult(int statusCode,String date,byte content[])
    {
        this.statusCode=statusCode;
        this.content = content;
        date=date.trim();
        SimpleDateFormat simpleDateFormat= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        try {
            this.lastModified=simpleDateFormat.parse(date);
        } catch (ParseException e) {
            //e.printStackTrace();
            System.out.println("Error while Convert last-modified header");
            System.exit(1);
        }
    }
    public HttpResult(StringBuilder allContent){
        String content=allContent.toString();
        int code=0;
        int index=content.indexOf(" ");
        content=content.substring(index+1);
        index=content.indexOf(" ");
        code =Integer.parseInt(content.substring(0,index).trim());
        index=content.indexOf("Last-Modified");
        String date="Mon, 00 Jan 0000 00:00:00 GMT";
        if(index!=-1)
        {
            date=content.substring(index+"Last-Modified".length()+1);
            date=date.substring(0,date.indexOf("\r"));
            date.trim();
        }
        index = content.indexOf("\r\n\r\n");
        if(index!=-1){
            content=content.substring(index+1).trim();
        }
        this.statusCode=code;
        this.content = content.getBytes();
        date=date.trim();
        SimpleDateFormat simpleDateFormat= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        try {
            this.lastModified=simpleDateFormat.parse(date);
        } catch (ParseException e) {
            //e.printStackTrace();
            System.out.println("Error while Convert last-modified header");
            System.exit(1);
        }
    }
    public boolean saveToFile(String fileName){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("cache/"+fileName);
            JSONObject temp = new JSONObject();
            temp.put("last-modified",this.lastModified.toString());
            temp.put("statusCode",this.statusCode);
            temp.put("content",Base64.encode(this.content));
            fileOutputStream.write(temp.toString().getBytes());
            fileOutputStream.flush();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Error while Create file to save data");
            return false;
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error while Writing to file ");
            return false;
        } catch (JSONException e) {
            //e.printStackTrace();
            System.out.println("Error while pasring to Json ");
            return false;
        }
        return true;
    }
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public byte[] getcontent() {
        return content;
    }

    public void setcontent(byte[] content) {
        this.content = content;
    }

    public int getstatusCode() {
        return statusCode;
    }

    public void setstatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
