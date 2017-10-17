import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Pong extends Application{
	//Game board width and height
	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
	private static final int fps = 30;
	//Game variables
	private boolean start = false;
	private int player1Score = 0;
	private int player1Special = 2;
	private int player2Special = 0;
	private int player2Score = 0;
	private int sum = 1;
	
	//board width and height
	private static final int BOARD_HEIGHT = 100;
	private static final int BOARD_WIDTH = 15;
	private static final int WALL_WIDTH = 10;
	private int wallWidth = 0;
	private boolean wallIsActive = false;
	private int wallHits = 0;
	private int wallX;
	//Ball variables
	//Ball speed
	private int ballSpeedY = -1;
	private int ballSpeedX = -1;
	//Ball position
	private int ballX = HEIGHT/2;
	private int ballY = HEIGHT/2;
	
	private boolean specialLine = false;
	private boolean turn = true;
	
	//Player 1 position
	private double player1XPos = 0;
	private double player1YPos = HEIGHT/2;
	//Player 2 position
	private double player2XPos = WIDTH-BOARD_WIDTH;
	private double player2YPos = HEIGHT/2;
	@Override
	public void start(Stage primaryStage) throws Exception {
		//Initialize game board
		Canvas c  = new Canvas(WIDTH,HEIGHT);
		GraphicsContext gc = c.getGraphicsContext2D();
		Timeline t = new Timeline(new KeyFrame(Duration.millis(10),e->run(gc)));
		t.setCycleCount(Timeline.INDEFINITE);
		//c.setOnMouseMoved(e ->  player1YPos  = e.getY());
		//Allows player to move their board
		c.setFocusTraversable(true);
		c.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case UP:    player1YPos-=20; break;
                    case DOWN:  player1YPos+=20; break;
                    case W: player2YPos -=20; break;
                    case S: player2YPos +=20; break;
                    case ENTER: start = true; break;
                    case SPACE: special(1); break;
                    case E: special(0);
                }
                if(player1YPos<WALL_WIDTH)
                	player1YPos=WALL_WIDTH;
                else if(player1YPos+BOARD_HEIGHT>HEIGHT-WALL_WIDTH)
                	player1YPos=HEIGHT-WALL_WIDTH-BOARD_HEIGHT;
                if(player2YPos<WALL_WIDTH)
                	player2YPos=WALL_WIDTH;
                else if(player2YPos+BOARD_HEIGHT>HEIGHT-WALL_WIDTH)
                	player2YPos=HEIGHT-WALL_WIDTH-BOARD_HEIGHT;
            }
        });
		//Set stage
		StackPane pane = new StackPane(c);
		Scene pongScene = new Scene(pane);
		primaryStage.setScene(pongScene);
		primaryStage.show();
		//Play game
		t.play();	
	}
	
	private void special(int player){
		int playerSpecial = player==1 ? player1Special: player2Special;
		switch(playerSpecial){
			case 0: return; 
			case 1: boomerangBall();
			break;
			case 2: initializeWall(player);
			break;
			case 3: speedBoost();
			break;
		}
		if (player==1)
			player1Special = 0;
		else
			player2Special = 0;
	}
	
	private void initializeWall(int player){
		if(player==1)
			wallX = (int)(WIDTH*.6);
		else
			wallX = (int)(WIDTH*.4);
		wallIsActive = true;
		wallHits = 3;
	}
	
	private void buildWall(GraphicsContext gc){
		if(wallIsActive&&ballX>=wallX-wallWidth&&ballX<=wallX+wallWidth){
			wallHits--;
			ballSpeedX *=-1;
		}
		if(wallHits==3)
			wallWidth = 20;
		else if(wallHits==2)
			wallWidth = 10;
		else if(wallHits==1)
			wallWidth = 5;
		else{
			wallWidth = 0;
			wallIsActive = false;
		}	
	}
	
	
	private void speedBoost(){
		ballX+=ballSpeedX;
		ballY+=ballSpeedY;
		sum++;
	}
	private void boomerangBall(){
		ballSpeedX *=-1;
	}
	
	//Every 10 ms, this function will run
	private void run(GraphicsContext gc) {

		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, WIDTH, HEIGHT);
		gc.setFill(Color.BLACK);
		gc.fillRect(0, WALL_WIDTH, WIDTH, HEIGHT-WALL_WIDTH*2);


		gc.setFill(Color.WHITE);
		gc.setFont(Font.font(STYLESHEET_CASPIAN, 25));
		gc.fillText(player1Score+"|"+player2Score, WIDTH/2, 100);
		if(!start){
			gc.fillText("Pres ENTER TO BEGIN", WIDTH/2, 200);
			return;
		}
		
		//Change ball position
		ballX+=ballSpeedX;
		ballY+=ballSpeedY;
		gc.setFill(Color.WHITE);
		gc.fillOval(ballX, ballY, 15, 15);
		
		//If hits a wall, change direction

		//If ball goes past player, then give opponent a point
		if(ifScore()) return;
		//What to do if it player's board
		//Player 1 Board
		hitDetection(gc);
		if(specialLine){
			gc.setFill(Color.PURPLE);
			gc.fillRect(WIDTH/2, WALL_WIDTH, 5, HEIGHT-WALL_WIDTH);
		}
		if(wallIsActive){
			gc.setFill(Color.YELLOW);
			gc.fillRect(wallX, WALL_WIDTH, wallWidth, HEIGHT-WALL_WIDTH);

		}

		//Display player boards
		gc.setFill(Color.WHITE);
		gc.fillRect(player1XPos, player1YPos, BOARD_WIDTH, BOARD_HEIGHT);
		gc.fillRect(player2XPos, player2YPos, BOARD_WIDTH, BOARD_HEIGHT);

	}
	
	
	private void hitDetection(GraphicsContext gc){
		//If hits wall
		if(ballY>=HEIGHT-WALL_WIDTH-15||ballY<=WALL_WIDTH)
			ballSpeedY *=-1;
		//If hits board
		ifHitsBoard(gc);
		ifHitsSpecialBoard(gc);
		buildWall(gc);
	}
	
	private void ifHitsSpecialBoard(GraphicsContext gc){
		if(specialLine&&ballX==WIDTH/2){
			Random rn = new Random();
			int rand = rn.nextInt(3) + 1;
			if(turn)
				player1Special = rand;
			else
				player2Special = rand;
			
			specialLine = false;
		}
			
	}
	
	private boolean ifScore(){
		if(ballX <= 0 ){
			player2Score+=sum;
			sum=1;
			//start = false;
			resetBall();
			return true;
		}
		else if(ballX>=WIDTH){
			player1Score+=sum;
			sum=1;
			//start = false;
			resetBall();
			return true;
		}
		return false;
	}
	
	private void ifHitsBoard(GraphicsContext gc){
		if((ballX < BOARD_WIDTH)&&(ballY>=player1YPos&&ballY<=player1YPos+BOARD_HEIGHT)){
			ballSpeedY += -1;
			ballSpeedX += -1;
			ballSpeedY *= -1;
			ballSpeedX *= -1;
			if(turn)
				sum++;
			turn = false;
			Random rn = new Random();
			int rand = rn.nextInt(5) + 1;
			if(rand == 1) 
				specialLine= true;
		}
		else if((ballX>WIDTH-BOARD_WIDTH-15)&&(ballY>=player2YPos&&ballY<=player2YPos+BOARD_HEIGHT)){
			ballSpeedY += 1;
			ballSpeedX += 1;
			ballSpeedY *= -1;
			ballSpeedX *= -1;
			if(!turn)
				sum++;
			turn = true;
			Random rn = new Random();
			int rand = rn.nextInt(5) + 1;
			if(rand == 1)
				specialLine= true;
		}

			
	}
	
	private void resetBall(){
		//Ball variables
		ballSpeedY = -1;
		ballSpeedX = -1;
		ballX = WIDTH/2;
		ballY = HEIGHT/2;
	}
	
	private void resetGame(){
		resetBall();
		start = false;
		player1Score = 0;
		player2Score = 0;
		player1XPos = 0;
		player1YPos = HEIGHT/2;
		player2XPos = WIDTH-BOARD_WIDTH;
		player2YPos = HEIGHT/2;
	}
	
	
	
	
	public static void main(String [] args){
		launch(args);
	}
	
	
	
	
}
