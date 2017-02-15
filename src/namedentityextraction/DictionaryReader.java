package namedentityextraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class DictionaryReader {
	private File dictionary;
	private ArrayList<String> wordArr = new ArrayList<String>();
	public DictionaryReader(String dictionaryName){
		this.dictionary = new File(dictionaryName);
		read();
	}
	public void read(){
		try {
			Scanner sc = new Scanner(dictionary);
			while(sc.hasNext()){
				wordArr.add(sc.next());
			}
			sc.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public ArrayList<String> getWords(){
		return wordArr;
	}
}
