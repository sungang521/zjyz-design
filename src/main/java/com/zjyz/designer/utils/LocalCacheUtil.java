package com.zjyz.designer.utils;

import java.io.*;
import java.util.Base64;

public class LocalCacheUtil {

    private static final String[] dirs={System.getenv("LOCALAPPDATA"),System.getenv("APPDATA"),System.getenv("TEMP")};
    private static  String localAppData=System.getenv("LOCALAPPDATA");
    private static  String appData=System.getenv("APPDATA");
    private static  String temp=System.getenv("TEMP");
    private static final String dirName="zjyzDesigner";
    private static final String AppfileName="cacha.zjyz";

    public static boolean  set(String data){

        byte[] bytes =Base64.getEncoder().encode(data.getBytes());
        boolean status=false;
        for(int i=0;i<dirs.length;i++){
            String baseDirName=dirs[i];
            String appDirName=baseDirName+"/"+dirName;
            String fileName=appDirName+"/"+AppfileName;
            File baseDir=new File(baseDirName);
            if(baseDir.exists()&&baseDir.isDirectory()&&baseDir.canRead()&&baseDir.canExecute()&&baseDir.canWrite()){

                File dir=new File(appDirName);
                if(!dir.exists()){
                    if(dir.mkdir()){
                    }else{
                        //写目录失败
                        continue;
                    }
                }
                File file=new File(fileName);
                System.out.println(file.getPath());
                try {
                    if(!file.exists()){
                        if(!file.createNewFile()){
                            //文件创建失败
                            continue;
                        }
                    }
                    OutputStream outputStream=new FileOutputStream(file);
                    outputStream.write(bytes);
                    outputStream.flush();
                    outputStream.close();
                    status=true;
                    break;

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else{
                //此处异常
                continue;
            }
        }
        return status;
    }
    public static  String  get(){
        String content="";
        for(int i=0;i<dirs.length;i++){
            String baseDirName=dirs[i];
            String appDirName=baseDirName+"/"+dirName;
            String fileName=appDirName+"/"+AppfileName;
            File file=new File(fileName);
            if(!file.exists()){
                continue;
            }
            try {
                InputStream inputStream=new FileInputStream(file);
                int byteread=1024;
                byte[] bytes=new byte[byteread];
                StringBuffer stringBuffer=new StringBuffer();
                while ((byteread = inputStream.read(bytes)) != -1) {
                    stringBuffer.append(new String(bytes,0,byteread));
                }
                inputStream.close();
                byte[] contentBytes=Base64.getDecoder().decode(stringBuffer.toString().getBytes());
                content=new String(contentBytes);
                break;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        return  content;
    }

    public static void main(String[] args) {
        LocalCacheUtil.get();

    }
}
