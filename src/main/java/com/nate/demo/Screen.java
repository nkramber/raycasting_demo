package com.nate.demo;

import java.awt.Color;
import java.awt.Graphics2D;

public class Screen {

    public static int TOP_HALF = 0;
    public static int BOTTOM_HALF = Main.SCREEN_HEIGHT;

    public int screenHalf = 0;

    private double xScroll, yScroll;

    private Graphics2D g;

    public Screen(Graphics2D g) {
        this.g = g;
    }

    public void drawTopDownPlayer(double posX, double posY, double dirX, double dirY) {
        double x1 = posX - dirX * 10;
        double y1 = posY - dirY * 10;

        double topX = posX + dirX * 10;
        double topY = posY + dirY * 10;

        double leftX = x1 + dirY * 6;
        double leftY = y1 - dirX * 6;

        double rightX = x1 - dirY * 6;
        double rightY = y1 + dirX * 6;

        drawLine(Color.WHITE, leftX, leftY, topX, topY);
        drawLine(Color.WHITE, rightX, rightY, topX, topY);
        drawLine(Color.WHITE, leftX, leftY, rightX, rightY);
    }

    public void drawTopDownWorld(int[][] worldMap) {
        for (int y = 0; y < worldMap.length; y++) {
            for (int x = 0; x < worldMap[1].length; x++) {
                if (worldMap[y][x] == 1) {
                    fillRect(Color.BLUE, x * 32, y * 32, 32, 32);
                }
            }
        }
    }

    public void fillRect(Color color, int x, int y, int width, int height) {
        g.setColor(color);
        g.fillRect(x + (int) xScroll, y + (int) yScroll + screenHalf, width, height);
    }

    private void drawLine(Color color, double x1, double y1, double x2, double y2) {
        g.setColor(color);
        g.drawLine((int) (x1 + xScroll), (int) (y1 + yScroll + screenHalf), (int) (x2 + xScroll), (int) (y2 + yScroll + screenHalf));
    }

    public void setScroll(double posX, double posY) {
        xScroll = -posX + Main.SCREEN_WIDTH / 2;
        yScroll = -posY + Main.SCREEN_HEIGHT / 2;
    }

    public Graphics2D getGraphics() { return g; }
    public void setScreenHalf(int screenHalf) { this.screenHalf = screenHalf; }
}
