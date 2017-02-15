package addressextraction;

import java.io.File;
import java.util.ArrayList;

public class GetFiles {
	private String dir;
	private String type;
	private ArrayList<File> filesArr = new ArrayList<File>();
	public GetFiles(String dir, String type){
		this.dir = dir;
		this.type = type;
		getFiles(dir);
	}
	public void getFiles(String dirPath){
		File dir = new File(dirPath); 
        File[] files = dir.listFiles(); 
        if (files == null) 
            return; 
        for (int i = 0; i < files.length; i++) { 
            if (files[i].isDirectory()) { 
                getFiles(files[i].getAbsolutePath()); 
            } 
            else if(files[i].getName().endsWith(type)){ 
//                String strFileName = files[i].getAbsolutePath(); 
//                System.out.println("---"+strFileName); 
                filesArr.add(files[i]);                    
            } 
            else{
            	continue;
            }
        } 
	}
	public ArrayList<File> getFilesArr(){
		return filesArr;
	}
}
