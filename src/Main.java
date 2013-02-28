import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.Random;

public class Main {
	
	private final int WIDTH = 1600;
	private final int HEIGHT = 1000;
	
	private Random rand = new Random();
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	
	private double delta = 1.0;
	private long lastTime = (Sys.getTime() * 1000) / Sys.getTimerResolution();
	
	private byte magic = 0; //0 = attract 1 = repell 2 = spinny 3 = gravity
	private double scale = 1f;
	private double mouseX, mouseY;
	private float currentX = 0f;
	private float currentY = 0f;
	private float mouseDistance = 500f;
	
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		initDisplay();
		gameLoop();
		destroy();
	}
	
	public void initDisplay() {
		try {
			Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
//			Display.setFullscreen(true);
			Display.setTitle("Wouter0100 LWJGL Testing");
			Display.setVSyncEnabled(true);
			Display.create();
			
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			glOrtho(0, WIDTH, 0, HEIGHT, -1, 1);
			glMatrixMode(GL_MODELVIEW);
			
			for(int i = 0; i < 10000; i++) {
				entities.add(new Entity(WIDTH/2, HEIGHT/2));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void gameLoop() {
		while(!Display.isCloseRequested()) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glLoadIdentity();
			updateInput();
			updateDelta();
			render();
			Display.update();
			Display.sync(60);
		}
	}
	
	public void updateDelta() {
		long time = (Sys.getTime() * 1000) / Sys.getTimerResolution();
		delta = ((double) time - (double) lastTime) / 16.0;
		lastTime = time;
		System.out.println(delta);
	}
	
	public void updateInput() {
		if(Keyboard.isKeyDown(Keyboard.KEY_EQUALS)) {
			mouseDistance++;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_MINUS)) {
			mouseDistance--;
		}
		
		mouseX = (Mouse.getX() - currentX) / scale;
		mouseY = (Mouse.getY() - currentY) / scale;
		if(Mouse.isButtonDown(1)) {
			currentX += Mouse.getDX();
			currentY += Mouse.getDY();
			
		}
		scale += Mouse.getDWheel()/500f;
		if(Keyboard.isKeyDown(Keyboard.KEY_1))
			magic = 0;
		if(Keyboard.isKeyDown(Keyboard.KEY_2))
			magic = 1;
		if(Keyboard.isKeyDown(Keyboard.KEY_3))
			magic = 2;
		if(Keyboard.isKeyDown(Keyboard.KEY_4))
			magic = 3;
	}
	
	public void render() {
		glPointSize((float) scale*2f);
		
		glPushMatrix();
		
		glTranslatef(currentX, currentY, 0);
		glScalef((float) scale, (float) scale, 1);
		
		for(Entity entity : entities) {
			entity.update();
			glColor3f(entity.cR, entity.cG, entity.cB);
			glBegin(GL_POINTS);
			glVertex2f(entity.x, entity.y);
			glEnd();
		}
		
		glPopMatrix();
	}
	
	public void destroy() {
		Display.destroy();
	}
	
	private class Entity {
		float cR, cG, cB, lastCR, lastCG, lastCB;
		
		float x;
		float y;
		float xSpeed;
		float ySpeed;
		
		boolean waiting = false;
		long lastTime;
		float waitingTime = rand.nextInt(3000)+ 1000;
		
		public Entity(float x, float y) {
			lastTime = System.currentTimeMillis();
			this.x = x;
			this.y = y;
			double direction = rand.nextFloat() * Math.PI * 2;
			float speed = rand.nextFloat();
			xSpeed = (float) (Math.sin(direction) * speed);
			ySpeed = (float) (Math.cos(direction) * speed);
			cR = rand.nextFloat();
			cG = rand.nextFloat();
			cB = rand.nextFloat();
			lastCR = cR;
			lastCG = cG;
			lastCB = cB;
		}
		
		public void update() {
			double direction;
			double distance = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));
			if(distance < mouseDistance && Mouse.isButtonDown(0)) {
				switch(magic) {
				case 0: {
					cR = lastCR;
					cG = lastCG;
					cB = 1f;
					direction = Math.atan2(mouseX - x, mouseY - y);
					xSpeed = (float) (Math.sin(direction) * 3);
					ySpeed = (float) (Math.cos(direction) * 3);
					break;
				}
				case 1: {
					cR = 1f;
					cG = lastCG;
					cB = lastCB;
					direction = Math.atan2(mouseX - x, mouseY - y);
					xSpeed = (float) (Math.sin((direction) - Math.PI) * 3);
					ySpeed = (float) (Math.cos((direction) - Math.PI) * 3);
					break;
				}
				case 2: {
					cR = lastCR;
					cG = 1f;
					cB = lastCB;
					direction = Math.atan2(mouseX - x, mouseY - y);
					xSpeed = (float) (Math.sin(direction-(Math.PI/2f)+0.05f) * 3);
					ySpeed = (float) (Math.cos(direction-(Math.PI/2f)+0.05f) * 3);
					break;
				}
				case 3: {
					cR = 1.0f;
					cG = 0.75f;
					cB = lastCB;
					direction = Math.atan2(mouseX - x, mouseY - y);
					xSpeed += (float) (Math.sin(direction)) * (1f / (float) (distance / 2.0));
					ySpeed += (float) (Math.cos(direction)) * (1f / (float) (distance / 2.0));
					break;
				}
				}
			} else {
				cR = lastCR;
				cG = lastCG;
				cB = lastCB;
				if(System.currentTimeMillis() - lastTime > waitingTime) {
					if(waiting){
						direction = rand.nextFloat() * Math.PI * 2;
						waiting = false;
						lastTime = System.currentTimeMillis();
						waitingTime = rand.nextInt(2000)+ 1000;
						xSpeed = (float) (Math.sin(direction));
						ySpeed = (float) (Math.cos(direction));
					}
					else {
						waiting = true;
						lastTime = System.currentTimeMillis();
						waitingTime = rand.nextInt(2000)+ 1000;
						xSpeed = 0f;
						ySpeed = 0f;
					}
				}
			}
			
			x += xSpeed * delta;
			y += ySpeed * delta;
		}
	}
}
