package com.epo.report.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class HttpBasicAuth {

    public static void main(String[] args) {

        try {
            URL url = new URL ("http://erpdev01.mo-co.org:8000/zfssc/get_costcenter?sap-client=300");
            
            String encoding = Base64.getEncoder().encodeToString(("CNHQ_LYB:Moco@2018").getBytes("UTF-8"));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty  ("Authorization", "Basic " + encoding);
            InputStream content = (InputStream)connection.getInputStream();
            BufferedReader in   = 
                new BufferedReader (new InputStreamReader (content));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

}