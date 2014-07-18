package jp.ac.uec.psd3.breakoutbase;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;

public class rankDialog extends JDialog {
int newScore;
ArrayList<String> ranking = new ArrayList();
ArrayList<Integer> scores = new ArrayList();
	public rankDialog(int score){
		super();
		this.setTitle("スコアランキング");
		newScore = score;
		this.setBounds(300, 300, 100, 300);
		this.getContentPane().setLayout(new GridLayout(10,1));
		this.readFile();
		scores.add(Integer.valueOf(newScore));
		this.writeFile();
		Collections.sort(scores);
		Collections.reverse(scores);
		for(int i=0;10>i;i++){
			System.out.println(scores.get(i));
			this.getContentPane().add(new JLabel(i+1+"位"+scores.get(i).toString()));
		}
	this.setVisible(true);
	}
	
	public void readFile(){
		
		try {
			File file = new File("score.txt");
			FileReader filereader = new FileReader(file);
			BufferedReader bf = new BufferedReader(filereader);
			String line = bf.readLine();
			while(line != null){
				scores.add(Integer.valueOf(line));
				line = bf.readLine();
			}
			bf.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void writeFile(){
		//スコアファイルを更新
		try {
			File file = new File("score.txt");
			file.delete();
			file.createNewFile();
			FileWriter filewriter = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(filewriter);
			for(int i=0;(scores.size())>i;i++){
				bw.write(scores.get(i).toString());
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
