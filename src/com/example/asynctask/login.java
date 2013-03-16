package com.example.asynctask;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Nitin
 * Date: 21/2/13
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class login extends Activity {

    EditText userName;
    EditText password;
    Button loginBtn;
    loginTask task;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        loginBtn= (Button) findViewById(R.id.btnLogin);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                v.setEnabled(false);
                userName= (EditText) findViewById(R.id.txtUsername);
                password = (EditText) findViewById(R.id.txtPassword);
                task=new loginTask();
                task.execute();
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }


    private class loginTask extends AsyncTask<Void,Void,String>
    {

        @Override
        protected void onPostExecute(String result)
        {
            if(result!="") {
                Intent mainact = new Intent(login.this,Main.class);
                mainact.putExtra("email",userName.getText().toString());
                startActivity(mainact);
                finish();
            }
             else
                loginBtn.setEnabled(true);
        }

        @Override
        protected String doInBackground(Void... params) {

            HttpResponse response;
            BufferedReader br=null;
            String result="";
            String token="";
            StringBuilder sb = new StringBuilder("");
            JSONObject Jobj = null;

            ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();
//            postParams.add(new BasicNameValuePair("email",userName.getText().toString()));
//            postParams.add(new BasicNameValuePair("password",password.getText().toString()));
            postParams.add(new BasicNameValuePair("email","collector1@example.com"));
            postParams.add(new BasicNameValuePair("password","password"));
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://personal-is.herokuapp.com/api/v1/tokens.json");
            UrlEncodedFormEntity formEntity = null;
            try {
                formEntity = new UrlEncodedFormEntity(postParams);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            post.setEntity(formEntity);

            try {
                response = client.execute(post);
                br= new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                String NL = System.getProperty("line.separator");
                while ((line = br.readLine()) != null) {
                    sb.append(line + NL);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            result= sb.toString();

            try {
                Jobj= new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            if (Jobj != null) {
                try {
                    token=(String)Jobj.get("token");
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }


            return token;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}