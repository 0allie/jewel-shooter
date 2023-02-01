import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class graphics 
{
	public static void main(String arg[])
	{
		Game f = new Game();
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.setSize(800,800);		
		f.setVisible(true);	
		f.setup();
		f.draw();
	}
}

class Game extends JFrame
{
	private Image raster;
	private Graphics rGraphics;
	public static int windowX = 800;
	public static int windowY = 800;
	
	public void setup()
	{
		raster = this.createImage(windowX, windowY);
		rGraphics = raster.getGraphics();
	}

	public void draw()
	{
		Ball bob = new Ball(250,300,30); //customizable size of the player. the collision/drawing methods adjusted accordingly  
		ArrayList<Block> blocks = new ArrayList<>();
		this.addKeyListener(bob);                                            
		
		long startTime = System.currentTimeMillis();
		long blockCoolDown = startTime-1000;
		float difficulty = 1;
		float score = 0;
		boolean playerAlive=true;
		
		while(playerAlive)
		{
			drawBG(rGraphics);
			
			if (System.currentTimeMillis() - blockCoolDown  > 1000)
			{
				Color c = new Color((int)(Math.random()*155)+100,(int)(Math.random()*155)+100,(int)(Math.random()*155)+100); 
				int width = (int)(Math.random()*20)+40;
				int height = (int)(Math.random()*20)+40;
				blocks.add(new Block((int)(Math.random()*(Game.windowX-50)),-100,width,height,c));
				blockCoolDown = System.currentTimeMillis();
				difficulty+=0.5; 
			}
			
			for (int i=0;i<Bullet.bullets.size();i++)
			{
				Bullet temp = Bullet.bullets.get(i);
				temp.move();
				temp.draw(rGraphics);
			}
						
			bob.moveBall();
			bob.drawBall(rGraphics);	//bob drawn infront of bullet so bullet appears to come from the top of bob
					
			for (int i=0;i<blocks.size();i++)
			{	
				Block temp = blocks.get(i);
				if (!temp.isAlive)
				{
					blocks.remove(i);
					i--;
					score+=1000;  // shooting blocks gives more points than simply dodging them
				}
				temp.checkShotCollision();
				bob.checkCollide(temp);
				temp.move();
				temp.draw(rGraphics);
				if(!temp.playerAlive())
				{
					blocks.clear();
					endGame(rGraphics,score);
					playerAlive = false;
				}
			}
	
			rGraphics.setColor(Color.yellow);
			rGraphics.drawString("SCORE : "+(int)score, 5, 50);
			
			score += (0.05*difficulty);
			
			getGraphics().drawImage(raster,0,0,getWidth(),getHeight(),null);
											
			try{Thread.sleep(10);}catch(Exception e){}
		}
	}
	private void drawBG(Graphics g) 
	{
		g.setColor(new Color(0,0,0));
		g.fillRect(0,0,windowX,windowY);
	}	
	private void endGame(Graphics g,float score)
	{
		g.setColor(Color.black);
		g.fillRect(0,0,Game.windowX,Game.windowY);
		g.setColor(Color.green);
		g.drawString("GAME OVER", Game.windowX/2 -38, Game.windowY/2);
		g.drawString("SCORE: "+(int)score, Game.windowX/2 - 38, Game.windowY/2 + 25);
		
	}
}

class Block
{
	int X, Y, width, height;
	Color c;
	public boolean collidePlayer;
	public boolean isAlive = true;

	public Block(int x, int y, int width, int height, Color c) 
	{
		X = x;
		Y = y;
		this.width = width;
		this.height = height;
		this.c = c;
	}	
	public Rectangle getCollision()
	{
		return new Rectangle(X,Y,width,height);
	}
	public void move()
	{
		Y+=3;		
		if (Y > Game.windowY)
			Y=-100;
	}
	public void checkShotCollision()
	{
		for (int i=0;i<Bullet.bullets.size();i++)
		{
			Bullet temp = Bullet.bullets.get(i);
			if (temp.getCollision().intersects(this.getCollision()))
			{
				this.die();
			}	
		}
	}
	public void draw(Graphics g)
	{
		g.setColor(c.darker());
		g.fillRect(X, Y, width, height);
		g.setColor(c.brighter());
		g.fillRect(X, Y, width-5, height-5);
		g.setColor(c);
		g.fillRect(X+5, Y+5, width-10, height-10);
	}
	public void die()
	{
		isAlive = false;
	}
	public boolean playerAlive()
	{
		if (collidePlayer)
			return false;
		else return true;
	}

}

class Bullet
{
	public static ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	public float X, Y;
	public float XVelocity, YVelocity, speed = .2f;
	public int timeToLive = 1000;
	public long creationTime;
	public Bullet(float x, float y, float xVelocity, float yVelocity) {
		X = x;
		Y = y;
		XVelocity = xVelocity;
		YVelocity = yVelocity;
		creationTime = System.currentTimeMillis();
		bullets.add(this);
	}
	public Ellipse2D.Float getCollision() 
	{
		return new Ellipse2D.Float(X-3,Y-3,6,6);
	}
	public void draw(Graphics g) 
	{
		g.setColor(Color.white);
		g.fillOval((int)X-4,(int)Y-4,8,8);
	}
	public void move()
	{
		X += XVelocity;
		Y += YVelocity;
		if (System.currentTimeMillis() - creationTime > timeToLive)
			die();
	}
	public void die()
	{
		bullets.remove(this);
	}
}

class Ball implements KeyListener
{
	public float X, Y;
	public int R;
	final private int MOVEAMT = 5;
	private boolean UP, DOWN, LEFT, RIGHT, SPACE;
	private long timeSinceLastShot = 0;
	
	public Ball(int x, int y, int r)
	{		
		X=x;
		Y=y;		
		R = r;
	}
	public void checkCollide(Block r)
	{
		Ellipse2D.Float collision = new Ellipse2D.Float(X-R,Y-R,2*R,2*R); 
		if (collision.intersects(new Rectangle(r.X,r.Y,r.width,r.height)))
		{
			r.collidePlayer = true;
		}
		else
			r.collidePlayer = false;	
	}

	public void moveBall()
	{
		if (SPACE && System.currentTimeMillis() - timeSinceLastShot > 300)
		{
			new Bullet(X,Y,0,-5);
			timeSinceLastShot = System.currentTimeMillis();
		}

		if (UP&&Y>1.5*R)
			Y -= MOVEAMT;
		if (DOWN&&Y<Game.windowY-1.5*R)
			Y += MOVEAMT;
		if (RIGHT&&X<Game.windowX-1.5*R)
			X += MOVEAMT;
		if (LEFT&&X>1.5*R)
			X -= MOVEAMT;
	}
	public void drawBall(Graphics g)
	{

		g.setColor(Color.yellow);
		drawEllipse(g,(int)X,(int)Y, R,R);	
		//eyes
		g.setColor(Color.black);
		drawEllipse(g,(int)(X-(R/3)), (int)Y-(R/5), R/5,R/5);	
		drawEllipse(g,(int)(X+(R/3)), (int)Y-(R/5), R/5,R/5);	
		//mouth
		drawEllipse(g,(int)X, (int)Y+(R/5), R/4, R/4);	
		g.setColor(Color.yellow);
		g.fillRect((int)(X-(R/1.5)),(int)Y-(R/6),(int)(R*1.5),R/3);
		//blush
		g.setColor(Color.pink);
		drawEllipse(g,(int)(X-(R/1.5)), (int)Y+(R/15), R/5,R/7);	
		drawEllipse(g,(int)(X+(R/1.5)), (int)Y+(R/15), R/5,R/7);	
	}
	private void drawEllipse(Graphics cg, int xCenter, int yCenter, int xR, int yR) 
	{
		cg.fillOval(xCenter-xR, yCenter-yR, 2*xR, 2*yR);
	}
	public void keyPressed(KeyEvent e) 
    { 
    	if (e.getKeyCode() == KeyEvent.VK_UP)
    	{
    		UP=true;
    	}
    	if (e.getKeyCode() == KeyEvent.VK_DOWN)
    	{
    		DOWN=true;
    	}
    	if (e.getKeyCode() == KeyEvent.VK_RIGHT)
    	{
    		RIGHT=true;
    	}
    	if (e.getKeyCode() == KeyEvent.VK_LEFT)
    	{
    		LEFT=true;
    	}
    	if (e.getKeyCode() == KeyEvent.VK_SPACE)
    	{
    		SPACE=true;
    	}
    }
    public void keyReleased(KeyEvent e) 
    {
    	if (e.getKeyCode() == KeyEvent.VK_UP)
    	{
    		UP=false;
     	}
    	if (e.getKeyCode() == KeyEvent.VK_DOWN)
    	{
    		DOWN=false;
    	}
    	if (e.getKeyCode() == KeyEvent.VK_RIGHT)
    	{
    		RIGHT=false;
    	}
    	if (e.getKeyCode() == KeyEvent.VK_LEFT)
    	{
    		LEFT=false;
    	}  
    	if (e.getKeyCode() == KeyEvent.VK_SPACE)
    	{
    		SPACE=false;
    	}
    }
	
	public void keyTyped(KeyEvent e) {
		
		
	}
	
}