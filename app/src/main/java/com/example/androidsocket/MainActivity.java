package com.example.androidsocket;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.*;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private Socket clientSocket;    //客戶端的socket
    private PrintWriter out;        //取得網路輸出串流
    private BufferedReader in;      //取得網路輸入串流
    private String msgFromServer;   //要接收的訊息
    private String msgToServer;     //要發出的訊息

    private static String SERVER_IP = "0";// = "192.168.43.125";
    private static int SERVER_PORT = 0;// = 9090;

    TextView t; // 顯示來自伺服器的訊息
    Context context; // 預存的context
    EditText editText; // 文字框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 一些初始化
        t = (TextView) findViewById(R.id.msg);
        context = this;
        editText = findViewById(R.id.Edit);

        // 彈出視窗
        openDialog();

        // 開始執行緒並連線
        // ( 在輸入完 IP 與 PORT 並按下確定後開始 )
    }

    // 接收訊息的執行緒
    class StartASocket implements Runnable{
        @Override
        public void run(){
            // 嘗試開一個接收器
            try{
                clientSocket = new Socket(SERVER_IP, SERVER_PORT);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e){} // 就算拋出例外我也沒辦法處裡
            // 嘗試從接收器擷取訊息
            try{
                while(true){
                    msgFromServer = in.readLine();
                    // 執行 UI Thread 印出SERVER的訊息
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            t.setText(msgFromServer);
                        }
                    });
                }
            }catch (IOException e){} // 就算拋出例外我也沒辦法處裡
        }
    }

    // 送出訊息的執行緒
    class Sender implements Runnable {
        @Override
        public void run(){
            // 嘗試送出訊息
            try {
                out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
                out.println(msgToServer);
                editText.setText("");
            } catch (IOException e) {} // 就算有例外我也沒辦法處裡
        }
    }

    // 按鈕點擊事件
    public void ClickOnSend(View view) {
        EditText edit = (EditText) findViewById(R.id.Edit);
        msgToServer = edit.getText().toString();
        // 彈出吐司 (剛剛發出的訊息內容)
        Toast.makeText(this, "Sent : " + msgToServer, Toast.LENGTH_SHORT).show();
        // 建立Sender物件並啟用執行緒
        Sender sender = new Sender();
        new Thread(sender).start();
    }

    // 彈出視窗
    public void openDialog(){
        // 宣告inflater並找到XML
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.ip_and_port, null);
        // 從XML中找到按鈕
        final EditText ip = (EditText) v.findViewById(R.id.IP);
        final EditText port = (EditText) v.findViewById(R.id.PORT);
        // 宣告一個彈出視窗
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setView(v);
        // 設定一個「確定」按鈕與點擊事件
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                // 取值給 ip 跟 port
                SERVER_IP = ip.getText().toString();
                SERVER_PORT = Integer.parseInt(port.getText().toString());
                // 開始執行緒並連線
                StartASocket startASocket = new StartASocket();
                new Thread(startASocket).start();
            }
        });
        // 顯示
        AlertDialog dialog = alert.create();
        dialog.show();
    }
}
