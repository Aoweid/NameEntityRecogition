package addressextraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class Stats {
	static OutputStream os;
	static WritableSheet sheet;
	static WritableWorkbook workbook;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String outputFilePath = "data/testoutput/";
		String type1 = ".txt";
		GetFiles getfiles1 = new GetFiles(outputFilePath, type1);
		ArrayList<File> txtFilesArr = new ArrayList<File>();
		txtFilesArr = getfiles1.getFilesArr();
		String inputFilePath = "data/testinput/";
		String type2 = ".tif";
		GetFiles getfiles2 = new GetFiles(inputFilePath, type2);
		ArrayList<File> tifFilesArr = new ArrayList<File>();
		tifFilesArr = getfiles2.getFilesArr();
		try {
			os = new FileOutputStream("data/stats_30_percent.xls");
			workbook = Workbook.createWorkbook(os);
			sheet = workbook.createSheet("First Sheet", 0);
			Label fileNames = new Label(0, 0, "FileName");
			sheet.addCell(fileNames);
			Label boxesCaptured = new Label(1, 0, "BoxesCaptured");
			sheet.addCell(boxesCaptured);
			Label targets = new Label(2, 0, "ContextAddress");
			sheet.addCell(targets);
			Label targetContents = new Label(3, 0, "Address");
			sheet.addCell(targetContents);
			Label targetWords = new Label(4, 0, "TargetWords");
			sheet.addCell(targetWords);
			Label boxContents = new Label(5, 0, "BoxContent");
			sheet.addCell(boxContents);
			Label wordsInBox = new Label(6, 0, "WordsInBox");
			sheet.addCell(wordsInBox);
			Label accurateWords = new Label(7, 0, "AccurateWords");
			sheet.addCell(accurateWords);
		} catch (RowsExceededException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (WriteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int rowNumber = 1;
		for (File file : txtFilesArr) {
			String fileName = file.getName().replace(".txt", "");
			ArrayList<ArrayList<String>> allContents = new ArrayList<ArrayList<String>>();
			Scanner sc;
			try {
				sc = new Scanner(file);
				while (sc.hasNextLine()) {
					String line = sc.nextLine();
					if (line.startsWith("Box")) {
						ArrayList<String> content = new ArrayList<String>();
						String subString1 = line.split(": ")[2];
						int index1 = subString1.lastIndexOf("[") + 1;
						int index2 = subString1.lastIndexOf("]");
						String subString2 = subString1.substring(index1, index2);
						String[] words = subString2.split(", ");
						for (String word : words) {
							content.add(word);
						}
						allContents.add(content);
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String targetFile = "";
			for (File tiffFile : tifFilesArr) {
				if (tiffFile.getName().contains(fileName)) {
					String parent = tiffFile.getParent();
					targetFile = parent + ".txt";
					break;
				}
			}
			Target target = new Target(targetFile);
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
					if(ratio>0.3){
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
}
