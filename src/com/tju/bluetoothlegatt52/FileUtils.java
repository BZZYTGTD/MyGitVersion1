package com.tju.bluetoothlegatt52;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

public class FileUtils {
	
	 File file = null;
	 OutputStreamWriter osw = null ;
	 OutputStream os = null;
	 public static String[] ecgFileNames = {"ecgwifi1.txt","ecgwifi2.txt","ecgwifi3.txt","ecgwifi4.txt","ecgwifi5.txt",
		 "ecgwifi6.txt","ecgwifi7.txt","ecgwifi8.txt","ecgwifi9.txt","ecgwifi10.txt","ecgwifi11.txt","ecgwifi12.txt","ecgwifi13.txt",
		 "ecgwifi14.txt","ecgwifi15.txt","ecgwifi16.txt","ecgwifi17.txt","ecgwifi18.txt","ecgwifi19.txt","ecgwifi20.txt"};
	 int fileNum = 0; 
	 final int fileMaxNum = 20;
	 private boolean fileflag = false;   //为true是标志五个存储文件至少有一个存在且小于1M
	private String SDCardRoot;
    private boolean recordExist = false;

	public String getSDPath(){
		return SDCardRoot;
	}
	public FileUtils() {
		super();
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
		} else {
			Toast.makeText(ContextUtil.getInstance(), "请插入存储卡",
					Toast.LENGTH_SHORT).show();
		}
		System.out.println("---------xxxxx---->"+ SDCardRoot);
	}
	public File createFileInSdCard(String fileName,String dir)throws IOException{
		File file = new File(SDCardRoot + dir + File.separator + fileName);
		if(! file.exists()) {  
            makeDir(file.getParentFile());  
        }  
//        return file.createNewFile();  
		file.createNewFile();
		return file;
	}
	public static void makeDir(File dir) {  
        if(! dir.getParentFile().exists()) {  
            makeDir(dir.getParentFile());  
        }  
        dir.mkdir();  
    }  
	public File createSDDir(String dirName){
		File dirFile = new File(SDCardRoot + dirName + File.separator);
		dirFile.mkdirs();
		return dirFile;
	}
	public boolean isFileExist(String fileName){
		File file = new File(SDCardRoot + fileName);
		return file.exists();
	}
	public File writeToSDCardFromInput(String path,String fileName,InputStream input){
		File file = null;
		OutputStream output = null;
		try{
			createSDDir(path);
			file = createFileInSdCard(fileName, path);
			output = new FileOutputStream(file);
			byte buffer[] = new byte[1024*4];
			int temp;
			while((temp = input.read(buffer)) != -1){
				output.write(buffer, 0, temp);
			}
			output.flush();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				output.close();
			}catch (Exception e ){
				e.printStackTrace();
			}
		}
		return file;
	}
	public void writeFileSdcardFile(String fileName,String write_str) throws IOException{ 
		 try{ 

		       FileOutputStream fout = new FileOutputStream(fileName); 
		       byte [] bytes = write_str.getBytes(); 

		       fout.write(bytes); 
		       fout.close(); 
		     }

		      catch(Exception e){ 
		        e.printStackTrace(); 
		       } 
		   } 
	
	public boolean createLog(String fileName){
    	try{
    		if(isFileExist("/ECG/"+fileName))
    			file = new File(getSDPath() + "/ECG/"+fileName);
    		else
    			file = createFileInSdCard(fileName, "ECG");
//    		System.out.println(file.getName()+ "---------------"+file.getPath()+ "------file");
    		try{
    			os = new FileOutputStream(file,true);
    			osw = new OutputStreamWriter(os,"UTF-8");
    		}catch(FileNotFoundException e){
    			e.printStackTrace();
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return true;
    }
    public boolean createLog2(){
    	try{
    		for(int i=0;i<fileMaxNum;i++){
	    		if(isFileExist("/ECG/"+ecgFileNames[i])) {
	    			file = new File(getSDPath()+"/ECG/"+ecgFileNames[i]);
	    			if(file.length()<1024*1024*200){
	    				fileflag = true;
	    				fileNum = i;
	    				break;
	    			}
	    		}
//	    		else{
//	    			file = createFileInSdCard(ecgFileNames[i], "ECG");
//    				fileflag = true;
//	    			fileNum = i;
//	    			break;
//	    		}
    		}
    		if(!fileflag)
    			file =  new File(getSDPath()+"/ECG/"+ecgFileNames[0]);
    		else
    			file = new File(getSDPath()+"/ECG/"+ecgFileNames[fileNum]);

    		try{
    			os = new FileOutputStream(file,fileflag);
    			osw = new OutputStreamWriter(os,"UTF-8");
    			fileflag = false;
    		}catch(FileNotFoundException e){
    			e.printStackTrace();
    		}
    		File numFile = new File(getSDPath()+"/ECG/fileNum.txt");
			 try{
	    			OutputStream osNum = new FileOutputStream(numFile);
	    			OutputStreamWriter oswNum = new OutputStreamWriter(osNum,"UTF-8");
	    			oswNum.write(fileNum+"");
	    			oswNum.flush();
	    			oswNum.close();
	    			osNum.close();
	    		}catch(FileNotFoundException e){
	    			e.printStackTrace();
	    		}
    				
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return true;
    }
	
    boolean write(float fx,float fy){
    	try{
			 osw.write(fx + ":       " + fy + "\r\n");
			 osw.flush();
			 if(file.length()>=1024*1024*200){
				 osw.close();
				 os.close();
				 fileNum ++;
				 if(fileNum>=fileMaxNum)
					 fileNum = 0;
				 file = new File(getSDPath()+"/ECG/"+FileUtils.ecgFileNames[fileNum]);
				 try{
					 os = new FileOutputStream(file);
					 osw = new OutputStreamWriter(os,"UTF-8");
		    		}catch(FileNotFoundException e){
		    			e.printStackTrace();
		    		}
					File numFile = new File(getSDPath()+"/ECG/fileNum.txt");
					 try{
			    			OutputStream osNum = new FileOutputStream(numFile);
			    			OutputStreamWriter oswNum = new OutputStreamWriter(osNum,"UTF-8");
			    			oswNum.write(fileNum+"");
			    			oswNum.flush();
			    			oswNum.close();
			    			osNum.close();
			    		}catch(FileNotFoundException e){
			    			e.printStackTrace();
			    		}
			 }
				 
		 }catch(Exception e){
			 e.printStackTrace();
		 }
    	return true;
    }
    boolean write(float fx,double fy){
    	try{
			 osw.write(fx + ":       " + fy + "\r\n");
			 osw.flush();
			 if(file.length()>=1024*1024*200){
				 osw.close();
				 os.close();
				 fileNum ++;
				 if(fileNum>=fileMaxNum)
					 fileNum = 0;
				 file = new File(getSDPath()+"/ECG/"+FileUtils.ecgFileNames[fileNum]);
				 try{
					 os = new FileOutputStream(file);
					 osw = new OutputStreamWriter(os,"UTF-8");
		    		}catch(FileNotFoundException e){
		    			e.printStackTrace();
		    		}
					File numFile = new File(getSDPath()+"/ECG/fileNum.txt");
					 try{
			    			OutputStream osNum = new FileOutputStream(numFile);
			    			OutputStreamWriter oswNum = new OutputStreamWriter(osNum,"UTF-8");
			    			oswNum.write(fileNum+"");
			    			oswNum.flush();
			    			oswNum.close();
			    			osNum.close();
			    		}catch(FileNotFoundException e){
			    			e.printStackTrace();
			    		}
			 }
				 
		 }catch(Exception e){
			 e.printStackTrace();
		 }
    	return true;
    }


  
  ArrayList<String> readFile(String fileName){
	  ArrayList<String> strs = new ArrayList<String>();
	  try{
			if(isFileExist(fileName)){
				file = new File(getSDPath() + fileName);
				recordExist = true;
			}else {
				Toast.makeText(ContextUtil.getInstance(), "记录文件不存在", Toast.LENGTH_LONG).show();
				recordExist = false;
				return null;
			}
			if(file.length() == 0){
				Toast.makeText(ContextUtil.getInstance(), "记录文件不存在", Toast.LENGTH_LONG).show();
				recordExist = false;
				return null;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		if(recordExist == true){
			  try{
					@SuppressWarnings("resource")
					BufferedReader br = new BufferedReader(new FileReader(file));
					String line = "";
					while((line = br.readLine())!=null){
						strs.add(line);
					}
					br.close();
					}catch(Exception e){
						e.printStackTrace();
					}
		}
	  return strs;
  }
  ArrayList<String> readECGRecord(){
	  ArrayList<String> strs = new ArrayList<String>();
	  try{
  		if(isFileExist("/ECG/fileNum.txt")){
  			file = new File(getSDPath() + "/ECG/fileNum.txt");
//  			recordExist = true;
  		}else {
  			Toast.makeText(ContextUtil.getInstance(), "记录文件不存在", Toast.LENGTH_LONG).show();
  			recordExist = false;
  			return null;
  		}
  		if(file.length() == 0){
  			Toast.makeText(ContextUtil.getInstance(), "记录文件不存在", Toast.LENGTH_LONG).show();
  			recordExist = false;
  			return null;
  		}
  		BufferedReader br = new BufferedReader(new FileReader(file));
  		String num = br.readLine();
  		br.close();
  		int fileNum = Integer.parseInt(num);
  		if(isFileExist("/ECG/"+ecgFileNames[fileNum])){
      		file = new File(getSDPath()+"/ECG/"+ecgFileNames[fileNum]);
  			recordExist = true;
  		}else {
  			Toast.makeText(ContextUtil.getInstance(), "记录文件不存在", Toast.LENGTH_LONG).show();
  			recordExist = false;
  			return null;
  		}
  		if(file.length() == 0){
  			Toast.makeText(ContextUtil.getInstance(), "记录文件不存在", Toast.LENGTH_LONG).show();
  			recordExist = false;
  			return null;
  		}

  	}catch(Exception e){
  		e.printStackTrace();
  	}
	  if(recordExist == true){
		  try{
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = "";
				while((line = br.readLine())!=null){
					strs.add(line);
				}
				br.close();
				}catch(Exception e){
					e.printStackTrace();
				}
	}
  return strs;
  }
}

