package com.aumbishoyi.cindy;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static co.intentservice.chatui.models.ChatMessage.Type.RECEIVED;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Enumeration;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;

public class MainActivity extends AppCompatActivity {
    Thread thread;
    Thread uiThread;
    Boolean flag;
    ChatView chatView;
    String message;
    StringBuilder response;


    public void updateUIThread(){
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String message=response.toString();
                ChatMessage chatMessage = new ChatMessage(message,System.currentTimeMillis() ,RECEIVED);
                chatView.addMessage(chatMessage);

            }
        });
    }

    public void networkThread(){
        new Thread(new Runnable(){

            @Override
            public void run() {
                URL url =null;

                try{
                    url = new URL("http://192.168.228.38:3000/");
                }catch(Exception e){
                    Log.i("msg0", String.valueOf(e));
                }
                HttpURLConnection conn =null;
                try{
                    conn = (HttpURLConnection) url.openConnection();
                }catch(Exception e){
                    Log.i("msg0", String.valueOf(e));
                }
                try{
                    conn.setRequestMethod("POST");
                }catch(Exception e){
                    Log.i("msg0", String.valueOf(e));
                }
                conn.setRequestProperty("Content-Type","application/json; utf-8");
                conn.setRequestProperty("Accept","application/json");
                conn.setDoOutput(true);
                String jsonInputString = String.format("{\"message\":\"%s\"}",message);
                try(OutputStream os = conn.getOutputStream()){
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input,0,input.length);
                }catch(Exception e){
                    Log.i("msg0", String.valueOf(e));
                }
                try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(),"utf-8"))){
                    response = new StringBuilder();
                    String responseLine = null;
                    while((responseLine=br.readLine())!=null){
                        response.append(responseLine.trim());
                    }
                    Log.i("msg0",response.toString());
                    updateUIThread();


                }catch(Exception e){
                    Log.i("msg0",e.toString());
                }

            }
        }).start();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flag=false;
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
        chatView = (ChatView) findViewById(R.id.chat_view);
        chatView.setOnSentMessageListener(new ChatView.OnSentMessageListener(){
            @Override
            public boolean sendMessage(ChatMessage chatMessage){
                // perform actual message sending
                if(!flag) {
                    Log.i("msg0", "Thread Started");
                    message=chatMessage.getMessage();
                    networkThread();
                    flag=true;
                }else{
                    Log.i("msg0", "Thread Run");
                    message=chatMessage.getMessage();
                    networkThread();
                }
                return true;
            }
        });



    }
}