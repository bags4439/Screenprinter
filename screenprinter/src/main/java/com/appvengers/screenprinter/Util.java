package com.appvengers.screenprinter;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

public class Util {

    public static String createFolderIfNotExist(String folderPath){
        if(folderPath==null)
             return folderPath="";

            ArrayList<File> directories=new ArrayList<>();

            String[] folders=folderPath.split("\\/");
            StringBuilder builder=new StringBuilder();
            builder.append(Environment.getExternalStorageDirectory());
            for(int a=0;a<folders.length;a++){
                if(!folders[a].trim().isEmpty()) {
                    builder.append("/").append(folders[a]);
                    directories.add(new File(builder.toString()));
                }
            }

            directories.add(new File(builder.toString()));


            for(File file:directories){
                if(!file.exists()){
                    file.mkdir();
                }
            }
            String path=directories.size()>0?directories.get(directories.size()-1).getAbsolutePath():"";
            return path;
    }
}
