package com.appvengers.screenprinter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScreenPrinter {
    private static ScreenPrinter _instance;
    public static int TYPE_PDF=100;
    private int type=TYPE_PDF;
    private PrinterListener printerListener;


    public static ScreenPrinter getInstance(){
        return getInstance(null,TYPE_PDF);
    }

    public static ScreenPrinter getInstance(PrinterListener printerListener){
        return getInstance(printerListener,TYPE_PDF);
    }


    private static ScreenPrinter getInstance(PrinterListener printerListener,int type){
        if(_instance==null)
            _instance=new ScreenPrinter(printerListener,type);

        return _instance;
    }

    private ScreenPrinter(PrinterListener printerListener,int type){
        this.type=type;
        this.printerListener=printerListener;
    }

    private boolean hasRequiredPdfOS(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public void printScreen(View view){
        printScreen(view,null);
    }


    public void printScreen(View view,String folderPath){
        printScreen(view,folderPath,null);
    }

    public void printScreen(View view,String folderPath,String name){
        new PrinterAsync(view,name,folderPath).execute();
    }


    private PrintResponse doPrintScreen(View view,String name,String folderName){
        Bitmap bitmap=screenShot(view);

        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        // write the document content
        String targetPath =(folderName!=null&&!folderName.trim().isEmpty()?folderName:"");

        if(name==null||name.isEmpty())
            name="file_"+System.currentTimeMillis();

//        else if(type==TYPE_IMAGE) {
//            return new PrintResponse(false,writePNG(bitmap,targetPath,name));
//        }

        if(hasRequiredPdfOS()&&type==TYPE_PDF)
            return writePDF(bitmap,targetPath,name);
        else
            return new PrintResponse(true,"");
    }

//    private String writePNG(Bitmap bitmap,String targetPath,String name) {
//        File f = new File( generatePath(targetPath) +"/"+name+".png");
//        try{
//                f.createNewFile();
//
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
//                byte[] bitmapdata = bos.toByteArray();
//
//                FileOutputStream fos = new FileOutputStream(f);
//                fos.write(bitmapdata);
//                fos.flush();
//                fos.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        return f.getAbsolutePath();
//    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private PrintResponse writePDF(Bitmap bitmap,String targetPath,String name){

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();


        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawPaint(paint);


        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap, 0, 0 , null);
        document.finishPage(page);


        File filePath = new File(generatePath(targetPath) +"/"+name+".pdf");

        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return new PrintResponse(true,"PDF generation failed: " + e.getMessage());
        }

        // close the document
        document.close();

        return new PrintResponse(false,filePath.getAbsolutePath());
    }

    public static String generateUniqueName(String prefix){
        return prefix+"_"+System.currentTimeMillis();
    }

    private Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public static String generatePath(String basePath) {
        return Util.createFolderIfNotExist(basePath);
    }

    private void publishError(String msg){
        if(printerListener!=null)
             printerListener.onFailed(msg);
    }

    private void publishOnComplete(String path){
        if(printerListener!=null)
             printerListener.onComplete(path);
    }


    private class PrinterAsync extends AsyncTask<String,String,PrintResponse>{
        View view;
        String name;
        String foldername;
        public PrinterAsync(View view,String name,String folderName){
            this.view=view;
            this.name=name;
            this.foldername=folderName;
        }
        @Override
        protected PrintResponse doInBackground(String... strings) {
           return doPrintScreen(view,name,foldername);
        }

        @Override
        protected void onPostExecute(PrintResponse printResponse) {
            super.onPostExecute(printResponse);
            if(!printResponse.error)
                 publishOnComplete(printResponse.msg);
            else
                publishError(printResponse.msg);
        }
    }

    public  interface PrinterListener{
        void onComplete(String path);
        void onFailed(String errorMsg);
    }
}
