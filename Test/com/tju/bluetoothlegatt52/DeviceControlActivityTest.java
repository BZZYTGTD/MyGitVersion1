package com.tju.bluetoothlegatt52;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import android.os.Environment;
import android.widget.TextView;
import junit.framework.TestCase;

public class DeviceControlActivityTest extends TestCase {

	private TextView mDataField;
	private String n;
	private String data1,fileName,file;
	private float[] floatArray;
	public File files;
	private String SDCardRoot;
	@Before
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	//���췽��
	  public DeviceControlActivityTest(String n,String data1,float[] floatArray,
			  String fileName,String file)
	    {
	        // ���췽��
	        // JUnit��ʹ��׼���Ĳ������ݴ������캯��
	        this.n = n;
	        this.data1 = data1;
	        this.file = file;
	        this.fileName = fileName;
	        this.floatArray = floatArray;
	   }
	  public String getSDPath(){
		  SDCardRoot = Environment.
			getExternalStorageDirectory().getAbsolutePath() 
				+ File.separator;
			return SDCardRoot;
		}
	  
	 
	@Test
	public void testDisplayDataString() {
		
		System.out.println(n);
	}

	@Test
	public void testDisplayDataFloatArray() {
		//����һ������
		System.out.println("(RCount[0] - 1) * 250 * 60 / (RCount[2] - RCount[1]) ");
	}

	@Test
	public void testReadRecord() {
		fail("Not yet implemented");
	}

	@Test
	public void testReadRecord2() {
		fail("Not yet implemented");
	}

}
