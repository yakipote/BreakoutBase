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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;



/**
 * ブロック崩し 制御・表示クラス
 * @author K.Lucky
 *
 */
public class BreakoutBase extends JPanel implements ActionListener , KeyListener
{
	private static final long serialVersionUID = 1L;

	private BufferedImage imgBar;				// Bar画像
	private BufferedImage imgBlock;				// Block画像
	private BufferedImage imgBall;				// Ball画像
	private javax.swing.Timer timer;
	static JFrame frame;

	int score = 0; 								// 得点
	int[][] blocks = new int[7][7];				// ブロック用配列

	int mouseX;									// マウスの動き監視用

	int gameStat;								// ボール(ゲーム)の状態 0:ラケット上 1:移動中 2:終了画面
	private Point ball = new Point( 200, 300 );	// Ball座標
	int ballSize;								// ボールのサイズ
	private Point racket = new Point( 300, 400 );	// ラケット座標
	int racketWidth;							// ラケット幅
	private Dimension block = new Dimension( 80, 30 );	// ブロック幅・高さ

	// ゲームフィールドの座標
	private Rectangle gameFace = new Rectangle( 0, 0, 640, 480 );

	int chk = 0;							// マウスのボタンを離すときの処理
	int ballDirectionX, ballDirectionY; 	// ボールの進行方向
	double movement = 3.0;					// ボールの移動量
	Point blockOrg = new Point( 40, 55 );	// ブロック群原点（左上）

	// ラケット移動のための移動平均情報
	int[] runningX = { 0, 0, 0, 0, 0 };
	int	runningPos = 0;

	// Hotmock入力Thread
	public Thread hmSocket;
	Thread readTh;

	/**
	 * BreakoutBaseクラス
	 * コンストラクタ
	 */
	BreakoutBase()
	{
		// 画面作成用イメージを読み込む
		imgBar = getImage( "block_bar_b.png" );
		racketWidth = imgBar.getWidth();

		imgBlock = getImage( "block_c.png" );

		imgBall = getImage( "block_ball_b.png" );
		ballSize = imgBall.getWidth();

		this.setBackground( new Color( 115, 99, 87 ) );
       frame.addKeyListener(this);
		// ゲーム情報の初期化
		startGame();

		// 一定時間毎にactionListenerへ
		timer = new javax.swing.Timer( 20, this );
		timer.start();

		// HotMockためのSocket通信 Thread を生成する
		hmSocket = new HmSocket( this );
		readTh = new Thread( hmSocket );
		readTh.start();

		// Window クローズ時の処理アダプタ
		frame.addWindowListener( new MyListener() );
	}

	/**
	 * リソースからイメージを読み込む
	 *
	 * @param imgFile：リソース内のイメージファイル名
	 * @return リソースから読み込んだイメージオブジェクト BufferedImage
	 */
	private BufferedImage getImage( String imgFile )
	{
		BufferedImage img;
		try
		{
			img = ImageIO.read(getClass().getResource(imgFile));
		} catch (IOException e) {
			img = null;
		}
		return img;
	}

	/**
	 * ゲーム情報の初期化
	 */
	private void startGame()
	{
		score = 0;

		// ボール進行量（右または下が正）
		ballDirectionX = 0;
		ballDirectionY = 0;

		// ボール座標
		ball.x = racket.x + racketWidth / 2 - ballSize / 2;
		ball.y = racket.y - ballSize;

		// ボールを発射前の状態に設定
		gameStat = 0;

		// ブロック初期化
		for ( int i = 0; i < blocks.length; i++ )
		{
			for ( int j = 0; j < blocks[i].length; j++ )
			{
				blocks[i][j] = 1;
			}
		}
	}

	/**
	 * 定周期で処理するゲームエンジン部
	 */
	public void running()
	{
		// ラケットの移動
		if ( gameStat != 2 ) // 終了画面以外でラケット移動可能
		{
			racket.x = mouseX - racketWidth / 2;
			//System.out.println( "RacketX=" + racketX + " mouseX=" + mouseX);
		}
		// ラケットの移動範囲の制限
		if ( racket.x < gameFace.x )
			racket.x = gameFace.x;
		else if ( racket.x + racketWidth > gameFace.width + gameFace.x )
			racket.x = gameFace.width + gameFace.x - racketWidth;

		// ゲーム再開判定
		if ( gameStat == 2 && chk == 1 )
		{
			chk = 0; 			// マウスボタンのクリック処理用
			startGame();		// ゲーム初期化
			gameStat = 0;
		}

		// ゲームオーバー画面注なら何もしない
		if ( gameStat == 2 && chk == 0 )
		{ 
			//scoreDialog sd = new scoreDialog(this.score);
			rankDialog rd = new rankDialog(this.score);
			gameStat=0;
			return;
		}

		// ボールの発射判定
		// chk はマウスのボタンを一度離してから押したことの確認用
		if ( gameStat == 0 && chk == 1 ) // ボールが発射前で、マウスボタンが押されたら発射
		{
			chk = 0;
			gameStat = 1;
			// ボールの移動方向設定
			double d = 45.0; // 角度の指定
			ballDirectionX = (int) (movement * Math.cos( d * Math.PI / 180 ));
			ballDirectionY = (int) (-movement * Math.sin( d * Math.PI / 180 ));
//			s0.play();
		}

		// ボールの移動
		if ( gameStat == 0 )				// ボールが停止状態
		{
			ball.x = racket.x + racketWidth / 2 - ballSize / 2;
			ball.y = racket.y - ballSize;
		}
		else if ( gameStat == 1 ) // ボールが発射状態
		{
			ball.x += ballDirectionX;
			ball.y += ballDirectionY;
		}

		// 壁とボールの衝突処理
		// 左右の壁
		if ( ball.x < gameFace.x || ball.x + ballSize > gameFace.x + gameFace.width )
		{
			ballDirectionX = -ballDirectionX; // 移動方向反転
			//s1.play();
		}
		// 上の壁
		if ( ball.y < gameFace.y )
		{
			ballDirectionY = -ballDirectionY; // 移動方向反転
			//s1.play();
		}
		// 下の壁 ＝＞ミス
		if ( ball.y + ballSize > gameFace.y + gameFace.height )
		{
			gameStat = 2;
			chk = 0;
			//s2.play();
		}

		// ラケットとの衝突処理
		// ラケットの部位を3つに分け、打ち返す角度に変化をつける
		if ( ball.y + ballSize > racket.y && ball.y + ballSize < racket.y + movement && ball.y > 0 )
		{
			// ラケットの縦方向の位置にボールが侵入
			int bc = ball.x + ballSize / 2; // ボールの中心
			//System.out.println( "bc=" + bc + " vx=" + ballDirectionX + " vy=" + ballDirectionY );
			if ( bc > racket.x && bc < racket.x + racketWidth / 4 )
			{
				// 左端で打った
				if ( ballDirectionX > 0 )
					hittingA( 1 );
				else
					hittingA( 0 );
			}
			else if ( bc > racket.x + racketWidth * 3 / 4 && bc < racket.x + racketWidth )
			{
				// 右端で打った
				if ( ballDirectionX < 0 )
					hittingA( 2 );
				else
					hittingA( 0 );
			}
			else if ( bc >= racket.x + racketWidth / 4 && bc <= racket.x + racketWidth * 3 / 4 )
			{
				// 中央で打った
				hittingA( 0 );
			}
		}

		// ブロックとの衝突判定
		int hit = 0;
		for ( int y = 0; y < blocks.length; y++ )
		{
			for ( int x = 0; x < blocks[y].length; x++ )
			{
				if ( blocks[y][x] == 1 )	// ブロックが存在していたら
				{
					// Ball中央座標
					Point ballRB = new Point( ball.x + ballSize, ball.y + ballSize );
					Point blockLT = new Point( blockOrg.x + block.width * x, blockOrg.y + block.height * y );
					Point blockRB = new Point( blockLT.x + block.width, blockLT.y + block.height );
					if ( blockLT.x <= ballRB.x && ball.x <= blockRB.x
							&& blockLT.y <= ballRB.y && ball.y <= blockRB.y )
					{
						blocks[y][x] = 0;
						score += 100;
						System.out.println( "Hit" + " ball=" + ball.x + "," +ball.y
								+ " block=" + blockLT.x + "," + blockLT.x);
						int direct = 0;
						int dist = 100;
						if ( ballRB.x - blockLT.x >= 0 && dist >= ballRB.x - blockLT.x )
						{
							dist = ballRB.x - blockLT.x;
							direct = 1;
						}
						if ( blockRB.x - ball.x >= 0 && dist >= blockRB.x - ball.x )
						{
							dist = blockRB.x - ball.x;
							direct = 2;
						}
						if ( ballRB.y - blockLT.y >= 0 && dist >= ballRB.y - blockLT.y )
						{
							dist = ballRB.y - blockLT.y;
							direct = 3;
						}
						if ( blockRB.y - ball.y >= 0 && dist >= blockRB.y - ball.y )
						{
							dist = blockRB.y - ball.y;
							direct = 4;
						}
						//System.out.println( "direct=" + direct );
						if ( direct == 1 || direct == 2 )
							ballDirectionX = -ballDirectionX;
						else if ( direct == 3 || direct == 4 )
							ballDirectionY = -ballDirectionY;
						hit = 1;
					}
				}
				if ( hit == 1 )
					break;
			}
			if ( hit == 1)
				break;
		}

		// クリア判定
		int sum = 0;
		for ( int x = 0; x < blocks[0].length; x++ )
		{
			for ( int y = 0; y < blocks.length; y++ )
			{
				sum += blocks[y][x];
			}
		}

		// ブロックが全て消去された場合=>ゲーム終了
		if ( sum == 0 )
		{
			gameStat = 2;
			chk = 0;
			//s2.play();
		}
	}

	/**
	 * 引数にラケット位置を指定してボールの反射移動量を決定
	 * @param mode 0:ラケット中央，1:ラケット左，2:ラケット右
	 */
	private void hittingA( int mode )
	{
		if ( mode == 0 )
			ballDirectionY = -ballDirectionY;
		else
		{
			ballDirectionY = -ballDirectionY;
			ballDirectionX = -ballDirectionX;
		}
		//s0.play();
	}

	/* (非 Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent( Graphics g )
	{
		super.paintComponent( g );

		// ボール表示
		g.drawImage( imgBall, ball.x, ball.y, this );

		// ラケット表示
		int h = imgBar.getHeight();
		g.drawImage( imgBar, racket.x, racket.y, racketWidth, h, this );

		// ブロック表示
		int w = block.width;
		h = block.height;
		for ( int y = 0; y < blocks.length; y++ )
		{
			for ( int x = 0; x < blocks[y].length; x++ )
			{
				// ブロックが存在していたら表示
				if ( blocks[y][x] == 1 )
					g.drawImage( imgBlock, 40 + w * x, 55 + h * y, w, h, this );
			}
		}

        // スコア表示
		g.setColor( Color.white );
		g.drawString( "SCORE: " + score, 10, 20 );
	}

	/**
	 * ラケットの移動を計算するのに移動平均を計算する
	 *  過去の値：runningX，位置：runningPos
	 *  過去3回の平均
	 * @param val：今回値
	 * @return 移動平均値
	 */
	private int runningMean( int val )
	{
		int	ret = 0;

		runningX[runningPos++] = val;
		if ( runningPos >= 3 )
			runningPos = 0;
		for ( int u = 0; u < 3; u++ )
			ret += runningX[u];
		ret /= 3;

		return ret;
	}

	/* (非 Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed( ActionEvent e )
	{
		// Swing タイマーから＝＞定周期の処理
		if ( e.getSource() == timer )
		{
			running();		// 移動処理
			repaint();		// 画面描画
		}
		else
		{
			// Hotmockからのデータ
			if ( e.getSource() == hmSocket )
			{
				// 加速度センサー
				if ( e.getActionCommand().substring( 0, 4 ).equals( "GS01" ))
				{
					String[] cmd = e.getActionCommand().split( "," );
					double x = Double.parseDouble( cmd[1] );
					// X座標に
					int xPos = (int)((x + 0.5) * 640.);
					mouseX = runningMean( xPos );
					//System.out.println( "mouseX=" + mouseX );
				}
				// 押しボタンSWの１
				else if ( e.getActionCommand().equals( "DI01" ))
				{
					// ゲーム開始
					chk = 1;
				}
				// その他は無視（ありえない）
				else
				{
					System.out.println( "Other cmd"  + e.getActionCommand());
				}
			}
		}
	}


	/**
	 *  Window クローズ時に，HmSocketクラスのSocketをクローズ処理する
	 *  ためのアダプタクラスを定義しておく
	 * @author K.Lucky
	 */
	public class MyListener extends WindowAdapter
	{
		/* (非 Javadoc)
		 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowClosing(WindowEvent e)
		{
			System.out.println("Cloaing!!!" );
			HmSocket.exit = true;
			try
			{
				readTh.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * JFおｒｍオブジェクトの作成
	 */
	public static void createFrame()
	{
		BreakoutBase b3;
		frame = new JFrame( "BreakoutBase" );
		frame.setBounds( 100, 100, 650, 510 );
		frame.getContentPane().add( b3 = new BreakoutBase() );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setResizable( false );
		frame.setVisible( true );

		// クライアント領域が６４０X４８０になるように再調整
		Dimension s = b3.getSize();
		System.out.println( "X=" + s.width + " Y=" + s.height );
		s.width = 650 - (s.width - 640);
		s.height = 510 - (s.height - 480);
		frame.setSize( s );
	}

	/**
	 * @param args 起動時のパラメータ：このアプリでは未使用
	 */
	public static void main( String[] args )
	{
		javax.swing.SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				createFrame();
			}
		} );
	}
	@Override
	public void keyPressed(KeyEvent e){
		System.out.printf("keyPressed");
			if(KeyEvent.VK_RIGHT==e.getKeyCode()){
				this.racket.x+=1;
			}
			if(KeyEvent.VK_LEFT==e.getKeyCode()){
				this.racket.x-=1;
			}
			if(KeyEvent.VK_SPACE==e.getKeyCode()){
				System.out.printf("SpacePressed");
				if ( gameStat == 2  )
				{
					startGame();		// ゲーム初期化
					gameStat = 0;
				}
				if ( gameStat == 0) // ボールが発射前で、マウスボタンが押されたら発射
				{
					gameStat = 1;
					// ボールの移動方向設定
					double d = 45.0; // 角度の指定
					ballDirectionX = (int) (movement * Math.cos( d * Math.PI / 180 ));
					ballDirectionY = (int) (-movement * Math.sin( d * Math.PI / 180 ));
//					s0.play();
				}
			}
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
