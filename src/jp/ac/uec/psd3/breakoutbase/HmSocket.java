/*
	BreakoutBase.jar : Simple Game "Breakout".
	Copyright (C) 2013 The University of Electro-Communications
	 (Chofu, Tokyo, Japan 182-8585)

	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package jp.ac.uec.psd3.breakoutbase;

import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;


/**
 * HOTMOCK通信（Soket)スレッド
 * @author K.Lucky
 *
 */
public class HmSocket extends Thread
{
	static String	server = "localhost";
	static int	port = 8888;
	static Socket sock;
	BreakoutBase par;
	static public  boolean exit;

	/**
	 * コンストラクタ
	 *
	 * @param frm 制御・描画画面オブジェクト
	 */
	HmSocket( BreakoutBase frm )
	{
		par = frm;
		exit = false;
		try
		{
			// ソケットを作成してサーバに接続する。
			sock = new Socket();
			sock.connect( new InetSocketAddress( server, port ) );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (非 Javadoc)
	 * @see java.lang.Thread#run()
	 * 受信スレッド
	 */
	public void run()
	{
		// サーバから送信された文字列を得るStream
		try
		{
			// サーバから送信された文字列を得るStream
			DataInputStream in = new DataInputStream( sock.getInputStream() );

			// サーバにデータ要求する Threadを作成し，開始する
			SendGsThread sendth = new SendGsThread( par, sock );
			sendth.start();

			int len; 		// 受信バイト数
			byte[] buffer = new byte[1024]; // 受信バッファ

			// データ受信待ち
			while ( 0 <= (len = in.read( buffer )))
			{
				// データ受信時の処理
				String msg = new String( buffer, 0, len );
				//System.out.println( "<" + msg + ">" );	// 文字列を表示してみる。

				// 終了リクエストのチェック
				if ( exit )
					break;

				// 受信データが加速度センサー関係なら表示クラスにイベント送信
				if ( msg.substring( 0, 4 ).equals( "GS01" ))
				{
					ActionEvent e = new ActionEvent( this, 3, msg );
					par.actionPerformed( e );
				}
				// 受信データがDI01のON->OFF変化で表示クラスにイベント送信
				else if ( msg.equals( "DI01,2&" ))
				{
					ActionEvent e = new ActionEvent( this, 2, "DI01" );
					par.actionPerformed( e );
				}
			}
			// Thread 終了前に，Socket送信側Threadも終了させる
			System.out.println( "Loop End" );
			sendth.exit = true;
			try
			{
				sendth.join();		// 送信Thread終了待ち
			} catch( InterruptedException e) {
				e.printStackTrace();
			}

			// Socketをクローズ
			sock.close();
			// このHmSocket関連のThreadが終了したことをイベント通知
			ActionEvent e = new ActionEvent( this, 2, "SockEnd" );
			par.actionPerformed( e );

		} catch( IOException e) {
			e.printStackTrace();
		}
		System.out.println( "---HmSocket End" );
	}
}

/**
 * 送信（データ要求）Thread
 * 重力加速度の要求コマンドを定周期で送信
 * @author K.Lucky
 */
class SendGsThread extends Thread
{
	private Socket sendSocket = null;
	public  boolean exit;
	BreakoutBase par;

	/**
	 * スレッド コンストラクタ
	 *
	 * @param frm 制御・描画画面オブジェクト
	 * @param socket 通信ソケット
	 */
	SendGsThread( BreakoutBase frm, Socket socket )
	{
		sendSocket = socket;
		par = frm;
		exit = false;
	}

	/* (非 Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		DataOutputStream out = null;
		try {
			// Socket送信Stremの作成
			out = new DataOutputStream( sendSocket.getOutputStream() );

			// 終了要求があるまで定周期で繰り返す
			while ( !exit )
			{
				//Hotmockに加速度センサーデータの送信依頼を行う
				String gs = "REQUEST,GS01,0";
				byte[] buffer = gs.getBytes(); // 受信バッファ
				int len = buffer.length;
				//System.out.println( "len:" + len );
				out.write( buffer, 0, len );
				out.flush();
				//System.out.println( "Send:" + gs );
				Thread.sleep( 100 );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 終了時,Streamのクローズ処理
		try {
			if ( out != null )
				out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

