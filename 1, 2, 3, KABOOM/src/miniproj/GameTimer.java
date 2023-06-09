package miniproj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GameTimer extends AnimationTimer{

	private GraphicsContext gc;
	private Scene theScene;
	private GameStage gameStage;
	private XWing xwing;
	private XWing player2;
	private XWing player3;
	private XWing player4;
	private ArrayList<PowerUps> powerups;
	private ArrayList<Obstacles> obstacles;
	private ArrayList<XWing> players;
	private double currentTime = System.nanoTime();
	private int previousSecond = -1;
	private int playerID = 0;

	public static final int PLAYERS = 3;
	public static final double SPAWN_TIME = 5.0d;
	public static final double EXPLOTION_TIME = 10.0d;
	public static final double POWERUP_SPAWN_TIME = 5.0d;

	// NETWORK
	private ReadFromServer rfsRunnable;
	private WriteToServer wtsRunnable;
	private Socket socket;

	public final Image p1_win = new Image("images/game_win.png",GameStage.WINDOW_WIDTH, GameStage.WINDOW_HEIGHT,false,false);
	public final Image bg = new Image("images/background.jpg",GameStage.WINDOW_WIDTH, GameStage.WINDOW_HEIGHT,false,false);

	//GameTimer(GraphicsContext gc, Scene theScene, GameStage gameStage, int playerID){
	GameTimer(GraphicsContext gc, Scene theScene, GameStage gameStage){
		this.gc = gc;
		this.theScene = theScene;
		this.gameStage = gameStage;
		this.powerups = new ArrayList<PowerUps>();
		this.obstacles = new ArrayList<Obstacles>();
		this.players = new ArrayList<XWing>();
		this.handleKeyPressEvent();

		this.connectToServer();
		// initialize xwing
		if(playerID == 1){
			this.xwing = new XWing("XWing",100,250,1); //initial position is at x=100, y=250
			this.xwing.setType(1);
			players.add(xwing);

			this.player2 = new XWing("Player 2",200,500,2);
			this.player2.setType(0);
			players.add(player2);

			this.player3 = new XWing("Player 3",200,250,3);
			this.player3.setType(0);
			players.add(player3);
			
			this.player4 = new XWing("Player 4",300,500,4);
			this.player4.setType(0);
			players.add(player4);
		} else if (playerID == 2){
			this.player2 = new XWing("Player 2",100,250,1); //initial position is at x=100, y=250
			this.player2.setType(1);
			players.add(player2);

			this.xwing = new XWing("XWing",200,500,2);
			this.xwing.setType(0);
			players.add(xwing);

			this.player3 = new XWing("Player 3",200,250,3);
			this.player3.setType(0);
			players.add(player3);
			
			this.player4 = new XWing("Player 4",300,500,4);
			this.player4.setType(0);
			players.add(player4);
		}else if(playerID == 3){
			this.player2 = new XWing("Player 2",100,250,1); //initial position is at x=100, y=250
			this.player2.setType(1);
			players.add(player2);

			this.player3 = new XWing("Player 3",200,500,2);
			this.player3.setType(0);
			players.add(player3);
			
			this.xwing = new XWing("XWing",200,250,3);
			this.xwing.setType(0);
			players.add(xwing);
			
			this.player4 = new XWing("Player 4",300,500,4);
			this.player4.setType(0);
			players.add(player4);
		}else{
			this.player2 = new XWing("Player 2",100,250,1); //initial position is at x=100, y=250
			this.player2.setType(1);
			players.add(player2);

			this.player4 = new XWing("Player 4",200,500,3);
			this.player4.setType(0);
			

			this.player3 = new XWing("Player 3",200,250,2);
			this.player3.setType(0);
			players.add(player3);
			players.add(player4);
			
			this.xwing = new XWing("XWing",300,500,4);
			this.xwing.setType(0);
			players.add(xwing);
		}
		// add players to players array list to remove them
		System.out.println(playerID);
	}

	@Override
	public void handle(long currentNanoTime) {
		double time = (currentNanoTime - currentTime)/1000000000.0;

		if(previousSecond != (int)time && (int)time != 0) {
			if((int)(time%GameTimer.EXPLOTION_TIME) == 0) {
				this.removePlayer();
				this.assignRandomBomb();
			}

			// if((int)time%GameTimer.POWERUP_SPAWN_TIME == 0) {
			// 	//this.spawnPowerUps();
			// 	this.spawnObstacles();
			// }
			// this.despawnPowerUps();
			// this.despawnObstacles();

			if(this.xwing.isInvincible()) {
				this.xwing.setInvincibilityElapsed();
			} else if (this.player2.isInvincible()) {
				this.player2.setInvincibilityElapsed();
			} else if (this.player3.isInvincible()) {
				this.player3.setInvincibilityElapsed();
			} else if (this.player4.isInvincible()) {
				this.player4.setInvincibilityElapsed();
			}

			// if(this.xwing.isSpeedUp()) {
			// 	this.xwing.setSpeedUpElapsed();
			// }

			// if(this.xwing.isSpeedDown()) {
			// 	this.xwing.setSpeedDownElapsed();
			// }

			// if(this.xwing.isStun()) {
			// 	this.xwing.setStunElapsed();
			// }
		}

		this.gc.clearRect(0, 0, GameStage.WINDOW_WIDTH,GameStage.WINDOW_HEIGHT);
		this.gc.drawImage(this.bg, 0, 0);

		this.checkPowerUpsCollision();
		this.checkPlayerCollision();
		this.checkObstaclesCollision();

		this.xwing.render(this.gc);
		this.player2.render(this.gc);
		this.player3.render(this.gc);
		this.player4.render(this.gc);

		this.renderPowerUps();
		this.renderObstacles();
		this.gameCheck(time);
		this.drawDetails(time);

		this.xwing.move();

		if(this.previousSecond != (int)time) {
			this.previousSecond = (int)time;
		}
	}

	// NETWORKING
	// connect to server
	private void connectToServer(){
		try{
			socket = new Socket("localhost",12345);
			DataInputStream in = new DataInputStream(socket.getInputStream());
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			playerID = in.readInt();
			System.out.println("You are player#"+playerID);
			// if(playerID == 1){
			// 	System.out.println("Waiting for Player #2 to connect");
			// }

			rfsRunnable = new ReadFromServer(in);
			wtsRunnable = new WriteToServer(out);

			rfsRunnable.waitForStartMsg();
		}catch(IOException ex){
			System.out.println("IOException from connectToServer()");
		}
	}

	private class ReadFromServer implements Runnable{
		private DataInputStream dataIn;

		public ReadFromServer(DataInputStream in){
			dataIn = in;
			System.out.println("RFS Runnable created");
		}

		public void run(){
			try{
                while(true){
					double player2X = dataIn.readDouble();
					double player2Y = dataIn.readDouble();
					double player3X = dataIn.readDouble();
					double player3Y = dataIn.readDouble();
					double player4X = dataIn.readDouble();
					double player4Y = dataIn.readDouble();

					if(player2 != null){
						player2.setX(player2X);
						player2.setY(player2Y);
					}

					if(player3 != null){
						player3.setX(player3X);
						player3.setY(player3Y);
					}

					if(player4 != null){
						player4.setX(player4X);
						player4.setY(player4Y);
					}

                }
            }catch(IOException ex){
                System.out.println("IOException from RFS run()");
            }
		}

		public void waitForStartMsg(){
			try{
				String startMsg = dataIn.readUTF();
				System.out.println("Message from server: " + startMsg);

				Thread readThread = new Thread(rfsRunnable);
				Thread writeThread = new Thread(wtsRunnable);

				readThread.start();
				writeThread.start();
			}catch(IOException ex){
				System.out.println("IOException from waitForStartMsg()");
			}
		}
	}

	private class WriteToServer implements Runnable{
		private DataOutputStream dataOut;

		public WriteToServer(DataOutputStream out){
			dataOut = out;
			System.out.println("WTS Runnable created");
		}

		public void run(){
			try{
				while(true){	// sends coordinates
					if(xwing != null){
						dataOut.writeDouble(xwing.getX());
						dataOut.writeDouble(xwing.getY());
						dataOut.flush();
					}
					try{
						Thread.sleep(25);
					}catch(InterruptedException ex){
						System.out.println("InterruptedException from WTS run()");
					}
				}
			}catch(IOException ex){
				System.out.print("IOException from WTS run()");
			}
		}
	}

	private void drawDetails(double t) {
		int time = (int) t; //typecasting a double to int

		String gameDetails;
		int strength = this.xwing.getStrength();
		gameDetails = "Time: 0" + (time>=60?(time/60):0) + (time%60<=9?":0":":") + time%60 + "	Score: " + this.xwing.getScore() + "	Strength: " + (strength<=0?0:strength);

		this.gc.fillText(gameDetails, 450, 50);
		this.gc.setFont(Font.font("Impact", FontWeight.NORMAL, 20));
		this.gc.setFill(Color.WHITE);
	}

	private void gameCheck(double t) {
		int time = (int) t; //typecasting a double to int

		if(this.players.size() == 1){
			this.gameStage.flashGameOver(0, this.xwing.getScore());
			this.stop();
		}

		// if(!this.xwing.isAlive()) { //the player loses the game (xwing died)
		// 	this.gameStage.flashGameOver(0, this.xwing.getScore());
		// 	this.stop();
		// } else if(time >= 60) { //the player wins the game
		// 	this.gameStage.flashGameOver(1, this.xwing.getScore());
		// 	this.stop();
		// }
	}

	private void renderPowerUps() {
		for(PowerUps p: this.powerups) {
			p.render(this.gc);
		}
	}

	private void renderObstacles() {
		for(Obstacles p: this.obstacles) {
			p.render(this.gc);
		}
	}

	private void spawnPowerUps() {
		PowerUps newPowerUp;
		Random r = new Random();
		int x = r.nextInt(GameStage.WINDOW_WIDTH/2); //location is at lesser half of screen
		int y = r.nextInt(GameStage.WINDOW_HEIGHT-PowerUps.POWERUP_HEIGHT); //it won't succeed window height

		int type = r.nextInt(2);

		newPowerUp = type==0?new Orb(x, y):new RebelAlliance(x, y);

		this.powerups.add(newPowerUp);
	}

	private void spawnObstacles() {
		Obstacles newObstacle;
		Random r = new Random();
		int x = r.nextInt(GameStage.WINDOW_WIDTH/2); //location is at lesser half of screen
		int y = r.nextInt(GameStage.WINDOW_HEIGHT-Obstacles.OBSTACLE_HEIGHT); //it won't succeed window height

		int type = r.nextInt(2);

		newObstacle = type==0?new Slow(x, y):new Stun(x, y);

		this.obstacles.add(newObstacle);
	}

	private void removePlayer(){
		for(int i=0;i<this.players.size();i++){
			XWing p = this.players.get(i);
			if(p.getType() == 1) {
				p.setVisible(false);
				p.Dead();
				this.players.remove(p);
			}
		}
	}

	private void assignRandomBomb(){
		Random r = new Random();
		int i = r.nextInt(this.players.size());
		XWing p = this.players.get(0);
		p.setType(1);
	}

	private void despawnPowerUps() {
		for(int i=0; i<this.powerups.size(); i++) {
			PowerUps p = this.powerups.get(i);
			p.setAvailabilityTimeElapsed();
			if(!p.isAvailable()) {
				this.powerups.remove(i);
			}
		}
	}

	private void despawnObstacles() {
		for(int i=0; i<this.obstacles.size(); i++) {
			Obstacles p = this.obstacles.get(i);
			p.setAvailabilityTimeElapsed();
			if(!p.isAvailable()) {
				this.obstacles.remove(i);
			}
		}
	}

	private void checkPowerUpsCollision() {
		for(int i=0; i<this.powerups.size(); i++) {
			PowerUps p = this.powerups.get(i);
			if(p.isAvailable()) {
				p.checkCollision(this.xwing);
			}else {
				this.powerups.remove(i);
			}
		}
	}

	private void checkObstaclesCollision() {
		for(int i=0; i<this.obstacles.size(); i++) {
			Obstacles p = this.obstacles.get(i);
			if(p.isAvailable()) {
				p.checkCollision(this.xwing);
			}else {
				this.obstacles.remove(i);
			}
		}
	}

	private void checkPlayerCollision() {
		// for(int i=0; i<this.players.size(); i++) {
		// 	XWing p = this.players.get(i);
		// 	if(p.isAvailable()) {
		// 		p.checkCollision(this.xwing, p);
		// 	}
		// }
		XWing p1,p2,p3,p4;
		int length = this.players.size();

		p1 = this.players.get(0);

		if(length == 2){
			p2 = this.players.get(1);
			if(p1.collidesWith(p2)) {
				if(p1.getType() == 0 && p2.getType() == 1){
					p1.setType(1);
					p2.setType(0);
					p2.makeInvincible(3);
				}
	
				else if(p1.getType() == 1 && p2.getType() == 0){
					p1.setType(0);
					p2.setType(1);
					p1.makeInvincible(3);
				}
			}
		}

		if(length == 3){
			p2 = this.players.get(1);
			p3 = this.players.get(2);
			if(p1.collidesWith(p3)) {
				if(p1.getType() == 0 && p3.getType() == 1){
					p1.setType(1);
					p3.setType(0);
					p3.makeInvincible(3);
				}
	
				else if(p1.getType() == 1 && p3.getType() == 0){
					p1.setType(0);
					p3.setType(1);
					p1.makeInvincible(3);
				}
			}
	
	
	
			if(p2.collidesWith(p3)) {
				if(p2.getType() == 0 && p3.getType() == 1){
					p2.setType(1);
					p3.setType(0);
					p3.makeInvincible(3);
				}
	
				else if(p2.getType() == 1 && p3.getType() == 0){
					p2.setType(0);
					p3.setType(1);
					p2.makeInvincible(3);
				}
			}
		}

		if(length == 4){
			p2 = this.players.get(1);
			p3 = this.players.get(2);
			p4 = this.players.get(3);
			if(p1.collidesWith(p4)) {
				if(p1.getType() == 0 && p4.getType() == 1){
					p1.setType(1);
					p4.setType(0);
					p4.makeInvincible(3);
				}
	
				else if(p1.getType() == 1 && p4.getType() == 0){
					p1.setType(0);
					p4.setType(1);
					p1.makeInvincible(3);
				}
			}

			if(p2.collidesWith(p4)) {
				if(p2.getType() == 0 && p4.getType() == 1){
					p2.setType(1);
					p4.setType(0);
					p2.makeInvincible(3);
				}
	
				else if(p2.getType() == 1 && p4.getType() == 0){
					p2.setType(0);
					p4.setType(1);
					p2.makeInvincible(3);
				}
			}
	
			if(p3.collidesWith(p4)) {
				if(p3.getType() == 0 && p4.getType() == 1){
					p3.setType(1);
					p4.setType(0);
					p4.makeInvincible(3);
				}
	
				else if(p3.getType() == 1 && p4.getType() == 0){
					p3.setType(0);
					p4.setType(1);
					p3.makeInvincible(3);
				}
			}
		}
		
	}

	//method that will listen and handle the key press events
	private void handleKeyPressEvent() {
		this.theScene.setOnKeyPressed(new EventHandler<KeyEvent>(){
			public void handle(KeyEvent e){
            	KeyCode code = e.getCode();
                moveXWing(code);
			}
		});

		this.theScene.setOnKeyReleased(new EventHandler<KeyEvent>(){
		            public void handle(KeyEvent e){
		            	KeyCode code = e.getCode();
		                stopXWing(code);
		            }
		        });
    }

	
	//method that will move the XWing depending on the key pressed
	private void moveXWing(KeyCode ke) { //should move 5 pixels
		if(ke==KeyCode.UP){
			if(playerID == 1){
				/*
				if(this.xwing.getType() == 1){
					
				} else {}
				*/
				this.xwing.setDY(-1*XWing.XWING_SPEED);
				this.xwing.faceUp();
			}
			else if (playerID == 2){
				this.xwing.setDY(-1*XWing.XWING_SPEED);
				this.xwing.faceUp2();
			}
			else if (playerID == 3){
				this.xwing.setDY(-1*XWing.XWING_SPEED);
				this.xwing.faceUp3();
			}
			else if (playerID == 4){
				this.xwing.setDY(-1*XWing.XWING_SPEED);
				this.xwing.faceUp4();
			}
		}

		if(ke==KeyCode.LEFT){
			if(playerID == 1){
				this.xwing.setDX(-1*XWing.XWING_SPEED);
				this.xwing.faceLeft();
			}
			else if (playerID == 2){
				this.xwing.setDX(-1*XWing.XWING_SPEED);
				this.xwing.faceLeft2();
			}
			else if (playerID == 3){
				this.xwing.setDX(-1*XWing.XWING_SPEED);
				this.xwing.faceLeft3();
			}
			else if (playerID == 4){
				this.xwing.setDX(-1*XWing.XWING_SPEED);
				this.xwing.faceLeft4();
			}

		}

		if(ke==KeyCode.DOWN){
			if(playerID == 1){
				this.xwing.setDY(XWing.XWING_SPEED);
				this.xwing.faceDown();
			}
			else if (playerID == 2){
				this.xwing.setDY(XWing.XWING_SPEED);
				this.xwing.faceDown2();
			}
			else if (playerID == 3){
				this.xwing.setDY(XWing.XWING_SPEED);
				this.xwing.faceDown3();
			}
			else if (playerID == 4){
				this.xwing.setDY(XWing.XWING_SPEED);
				this.xwing.faceDown4();
			}

		}

		if(ke==KeyCode.RIGHT){
			if(playerID == 1){
				this.xwing.setDX(XWing.XWING_SPEED);
				this.xwing.faceRight();
			}
			else if (playerID == 2){
				this.xwing.setDX(XWing.XWING_SPEED);
				this.xwing.faceRight2();
			}
			else if (playerID == 3){
				this.xwing.setDX(XWing.XWING_SPEED);
				this.xwing.faceRight3();
			}
			else if (playerID == 4){
				this.xwing.setDX(XWing.XWING_SPEED);
				this.xwing.faceRight4();
			}

		}

		//System.out.println(ke+" key pressed.");
   	}

	//method that will stop the ship's movement; set the ship's DX and DY to 0
	private void stopXWing(KeyCode ke){
		this.xwing.setDX(0);
		this.xwing.setDY(0);
	}
}
