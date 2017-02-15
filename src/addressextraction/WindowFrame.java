package addressextraction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class WindowFrame {
	static int frameWidth;
	static int windowWidth;
	static int windowLength;
	static int scanUnit = 10;
	static int[][] maskArray;
	static OutputStream os;
	static WritableSheet sheet;
	static WritableWorkbook workbook;
	static ArrayList<int[]> sizes = new ArrayList<int[]>();
	static ArrayList<String> words ;
	static ArrayList<Integer> x0List;
	static ArrayList<Integer> y0List ;
	static ArrayList<Integer> x1List ;
	static ArrayList<Integer> y1List;
	static ArrayList<File> filesArr = new ArrayList<File>();
	static int length ;
	static volatile  ArrayList<int[]> boxes;
	static volatile  ArrayList<ArrayList<String>> contents;
	static volatile  ArrayList<Double> ratios;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws InterruptedException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println("Started at"+dateFormat.format(cal.getTime()));
		// TODO Auto-generated method stub
		String folderPath = "data/demo_input/";
		String hocrPath = "data/demo_hocr/";
		int[] size1 = {700,200,20};
		int[] size2 = {800,200,20};
		sizes.add(size1);
		sizes.add(size2);
		getFiles(folderPath);
		try {
			os = new FileOutputStream("data/demo_stats.xls");
			workbook = Workbook.createWorkbook(os);
			sheet = workbook.createSheet("First Sheet",0);
			Label fileName = new Label(0,0,"FileName");
	        sheet.addCell(fileName);
	        Label boxesCaptured = new Label(1,0,"BoxesCaptured");
	        sheet.addCell(boxesCaptured);
	        Label targets = new Label(2,0,"ContextAddress");
	        sheet.addCell(targets);
	        Label targetContents = new Label(3,0,"Address");
	        sheet.addCell(targetContents);
	        Label targetWords = new Label(4,0,"TargetWords");
	        sheet.addCell(targetWords);
	        Label boxContents = new Label(5,0,"BoxContent");
	        sheet.addCell(boxContents);
	        Label wordsInBox = new Label(6,0,"WordsInBox");
	        sheet.addCell(wordsInBox);
	        Label accurateWords = new Label(7,0,"AccurateWords");
	        sheet.addCell(accurateWords);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int rowNumber = 1;
		for(File file:filesArr){
			String inputTiffName = file.getName();
			String inputTiff = file.getAbsolutePath();
			String outputHocrName = inputTiffName.replace(".tif", "");
			String hocrFile = hocrPath + outputHocrName;
			String command1 = "/usr/local/Cellar/tesseract/3.04.01_1/bin/tesseract " + inputTiff + " " + hocrFile+" hocr";
			Process p;
			try {
				p = Runtime.getRuntime().exec(command1);
				p.waitFor();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			MaskArray MA = new MaskArray(hocrFile+".hocr");
			MA.setUp();
			ArrayList<Map<String, Object>> pages = MA.getPages();
			ArrayList<ArrayList<String>> allContents = new ArrayList<ArrayList<String>>();
			for(Map<String, Object> page:pages){
				length = (int) page.get("Length");
				int width = (int) page.get("Width");
				int pageNumber = (int) page.get("PageNumber");
				words = (ArrayList<String>) page.get("Content");
				x0List = (ArrayList<Integer>) page.get("X0List");
				y0List = (ArrayList<Integer>) page.get("Y0List");
				x1List = (ArrayList<Integer>) page.get("X1List");
				y1List = (ArrayList<Integer>) page.get("Y1List");
				maskArray = MA.getMaskArray(x0List, y0List, x1List, y1List, words, width, length);
				boxes = new ArrayList<int[]>();
				contents = new ArrayList<ArrayList<String>>();
				ratios = new ArrayList<>();
				String outputPath = "data/demo_output/";
				String outputFileName = outputHocrName + ".txt";
				File outputFile = new File(outputPath+outputFileName);
				//		System.out.println(contents);
				if(!outputFile.exists()){
					try {
						outputFile.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				FileWriter writer = null;
				try {
					writer = new FileWriter(outputFile, true);
					writer.write("Page:"+pageNumber+"\n");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				for(int[] size:sizes){
					windowWidth = size[0];
					windowLength = size[1];
					frameWidth = size[2];
					for(int i=0;i<width+1-windowWidth-frameWidth*2;i=i+scanUnit){
						new WindowFrame().ForLine(i);
					}
				}
				for(int i=0;i<contents.size();i++){
					allContents.add(contents.get(i));
				}
				try {
					for(int i=0;i<boxes.size();i++){
						writer.write("Box: ["+boxes.get(i)[0]+", "+boxes.get(i)[1]+", "+boxes.get(i)[2]+", "+
								boxes.get(i)[3]+"] Words: "+contents.get(i).toString()+" Ratio: "+ratios.get(i)+"\n");
					}
					writer.close();
					cal = Calendar.getInstance();
					System.out.println("Ended at"+dateFormat.format(cal.getTime()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			String parent = file.getParent();
			String txtFile = parent + ".txt";
			Target target = new Target(txtFile);
			Map<String, ArrayList<String>> targetMap = target.getTargetMap();
			NumberFormat nf = new jxl.write.NumberFormat("#"); 
	        WritableCellFormat wcfN = new jxl.write.WritableCellFormat(nf); 
			for(String key:targetMap.keySet()){
				Label tempfileName = new Label(0,rowNumber, file.getName());
				jxl.write.Number boxCaptured = new jxl.write.Number(1, rowNumber, allContents.size(), wcfN);
				Label context = new Label(2, rowNumber, key);
				String addressWords = "";
				ArrayList<String> targetContent = targetMap.get(key);
				for(String word:targetContent){
					addressWords = addressWords+word+", ";
				}
				Label address = new Label(3, rowNumber, addressWords);
				jxl.write.Number wordsInTarget = new jxl.write.Number(4, rowNumber, targetContent.size(), wcfN); 
				ArrayList<String> selectedBox = new ArrayList<String>();
				for(int i=0;i<allContents.size();i++){
					ArrayList<String> boxContent = allContents.get(i);
					double count = 0;
					for(String word:targetContent){
						if(boxContent.contains(word)){
							count++;
						}
					}
					double ratio = count/targetContent.size();
					if(ratio>0.75){
						selectedBox = boxContent;
						break;
					}
				}
				String boxWords = "";
				for(String word:selectedBox){
					boxWords = boxWords+word+", ";
				}
				Label boxContent = new Label(5, rowNumber, boxWords);
				jxl.write.Number wordsInBox = new jxl.write.Number(6, rowNumber, selectedBox.size(), wcfN); 
				ArrayList<String> correctWords = new ArrayList<String>();
				for(String word:targetContent){
					if(selectedBox.contains(word)){
						correctWords.add(word);
					}
				}
				jxl.write.Number correctWordsInBox = new jxl.write.Number(7, rowNumber, correctWords.size(), wcfN); 
		        try {
					sheet.addCell(tempfileName);
					sheet.addCell(boxCaptured);
					sheet.addCell(context);
					sheet.addCell(address);
					sheet.addCell(wordsInTarget);
					sheet.addCell(boxContent);
					sheet.addCell(wordsInBox);
					sheet.addCell(correctWordsInBox);
				} catch (RowsExceededException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (WriteException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
		        rowNumber++;
			}
		}
		try {
			workbook.write();
			workbook.close();
	        os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
	}

	public Thread ForLine(int i) throws InterruptedException{
		Thread t1 = new Thread(new Runnable() {
		     public void run() {
		    	 for(int j=0;j<length+1-frameWidth*2-windowLength;j=j+scanUnit){
		 			boolean frameOK = true;
		 			boolean windowOK = false;
		 			for(int m=i;m<i+windowWidth+frameWidth*2+1;m++){
		 				for(int n=j;n<j+windowLength+frameWidth*2+1;n++){
		 					if(n-j>=frameWidth&&n-j<=windowLength+frameWidth
		 							&&m-i>=frameWidth&&m-i<=windowWidth+frameWidth){
		 						continue;
		 					}
		 					else{
		 						if(maskArray[m][n]==1){
		 							frameOK = false;
		 							break;
		 						}
		 					}
		 				}
		 			}
		 			double count = 0;
		 			for(int m=i+frameWidth;m<=i+frameWidth+windowWidth;m++){
		 				for(int n=j+frameWidth;n<=j+frameWidth+windowLength;n++){
		 					if(maskArray[m][n]==1){
		 						count++;
		 					}
		 				}
		 			}
		 			double ratio = count/(windowLength*windowWidth);
		 			if(ratio>0){
		 				windowOK = true;
		 			}
		 			if(frameOK&&windowOK){
		 				int[] box = {i, j, i+frameWidth*2+windowWidth, j+frameWidth*2+windowLength};
		 				//				boxes.add(box);
		 				//				ratios.add(ratio);
		 				ArrayList<String> content = new ArrayList<String>();
		 				for(int index=0;index<words.size();index++){
		 					int x0 = x0List.get(index);
		 					int y0 = y0List.get(index);
		 					int x1 = x1List.get(index);
		 					int y1 = y1List.get(index);
		 					if(x0>=i+frameWidth&&x1<=i+frameWidth+windowWidth
		 							&&y0>j+frameWidth&&y1<=j+frameWidth+windowLength){
		 						//						content.add(words.get(index)+" ["+x0+", "+y0+", "+x1+", "+y1+"]");
		 						content.add(words.get(index));
		 					}
		 				}
		 				if(!contents.contains(content)){
		 					contents.add(content);
		 					boxes.add(box);
		 					ratios.add(ratio);
		 				}
		 			}
		 		}
		     }
		});  
		t1.start();
		t1.join();
		return t1;
		
	}
	public static void getFiles(String dirPath){
		File dir = new File(dirPath); 
        File[] files = dir.listFiles(); 
        if (files == null) 
            return; 
        for (int i = 0; i < files.length; i++) { 
            if (files[i].isDirectory()) { 
                getFiles(files[i].getAbsolutePath()); 
            } 
            else if(files[i].getName().endsWith(".tif")){ 
//                String strFileName = files[i].getAbsolutePath(); 
//                System.out.println("---"+strFileName); 
                filesArr.add(files[i]);                    
            } 
            else{
            	continue;
            }
        } 
    } 
	
	

}
