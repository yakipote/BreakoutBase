package jp.ac.uec.psd3.breakoutbase;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.swing.Icon;
import javax.swing.JOptionPane;

public class scoreDialog extends JOptionPane {
int newScore;
ArrayList scores = new ArrayList();
	public scoreDialog() {
		// TODO �����������ꂽ�R���X�g���N�^�[�E�X�^�u
	}

	public scoreDialog(Object arg0) {
		super(arg0);
		// TODO �����������ꂽ�R���X�g���N�^�[�E�X�^�u
	}

	public scoreDialog(Object arg0, int arg1) {
		super(arg0, arg1);
		// TODO �����������ꂽ�R���X�g���N�^�[�E�X�^�u
	}

	public scoreDialog(Object arg0, int arg1, int arg2) {
		super(arg0, arg1, arg2);
		// TODO �����������ꂽ�R���X�g���N�^�[�E�X�^�u
	}

	public scoreDialog(Object arg0, int arg1, int arg2, Icon arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO �����������ꂽ�R���X�g���N�^�[�E�X�^�u
	}

	public scoreDialog(Object arg0, int arg1, int arg2, Icon arg3, Object[] arg4) {
		super(arg0, arg1, arg2, arg3, arg4);
		
	}

	public scoreDialog(Object arg0, int arg1, int arg2, Icon arg3,
			Object[] arg4, Object arg5) {
		super(arg0, arg1, arg2, arg3, arg4, arg5);
		// TODO �����������ꂽ�R���X�g���N�^�[�E�X�^�u
	}
	public scoreDialog(int newScore){
		super();
		this.readFile();
		scores.add(newScore);
		this.writeFile();
		this.showMessageDialog(getRootPane(),scores);
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
