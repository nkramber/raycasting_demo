package com.nate.demo;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main extends Canvas implements Runnable, KeyListener {

    public static final int SCREEN_WIDTH = 640;
    public static final int SCREEN_HEIGHT = 480;
    private static final int SCREEN_SCALE = 1;
    private static final String GAME_TITLE = "Raycasting Demo";
    private static final double TARGET_FPS = 60.0;
    private static final double TIME_BETWEEN_FRAMES = 1000000000 / TARGET_FPS;

    private JFrame frame;
    private Random random;
    private BufferedImage display;
    private Screen screen;

    private RaycastingDemo demo;

    private boolean running = true;
    private boolean keys[] = new boolean[256];

    private void init() {
        random = new Random();
        display = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT * 2, BufferedImage.TYPE_INT_RGB);
        screen = new Screen((Graphics2D) display.getGraphics());
        addKeyListener(this);

        demo = new RaycastingDemo();
    }

    @Override
    public void run() {
        init();

        double lastUpdateTime = System.nanoTime();
        while (running) {
            double currentTime = System.nanoTime();
            while (currentTime - lastUpdateTime > TIME_BETWEEN_FRAMES) {
                measureFPS();
                tick();
                render();
                lastUpdateTime += TIME_BETWEEN_FRAMES;
            }
        }
        System.exit(0);
    }

    private long lastFPSCheck;
    private int currentFPS = 0;
    private int frameCount = 0;
    
    private void measureFPS() {
        frameCount++;
        if(System.currentTimeMillis() - lastFPSCheck >= 1000) {
            currentFPS = frameCount;
            if (currentFPS > 3) System.out.println("FPS: " + currentFPS);
            frameCount = 0;
            lastFPSCheck = System.currentTimeMillis();
        }
    }

    private void tick() {
        demo.tick(keys);
    }

    private void render() {
        Graphics2D g = screen.getGraphics();

        //Wipe the window by rendering a black rectangle
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT * 2);

        demo.renderTopDown(screen);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        demo.renderRaycasting(screen);

        g.setColor(Color.WHITE);
        g.drawLine(0, SCREEN_HEIGHT, SCREEN_WIDTH, SCREEN_HEIGHT);

        getGraphics().drawImage(display, 0, 0, SCREEN_WIDTH * SCREEN_SCALE, SCREEN_HEIGHT * SCREEN_SCALE * 2, this);
    }

    private void createFrame() {
        frame = new JFrame(GAME_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(this);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setIgnoreRepaint(true);
    }

    private void start() {
        createFrame();
        requestFocus();
        new Thread(this).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main main = new Main();
            Dimension dimension = new Dimension(SCREEN_WIDTH * SCREEN_SCALE, SCREEN_HEIGHT * SCREEN_SCALE * 2);
            main.setMinimumSize(dimension);
            main.setMaximumSize(dimension);
            main.setPreferredSize(dimension);

            main.start();
        });
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) { keys[e.getKeyCode()] = true; }
    @Override public void keyReleased(KeyEvent e) { keys[e.getKeyCode()] = false; }
}
