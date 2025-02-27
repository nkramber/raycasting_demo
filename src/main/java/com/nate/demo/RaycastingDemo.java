package com.nate.demo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public class RaycastingDemo {

    int[][] worldMap = new int[][] {
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };

    double rotateSpeed = 0.03f;
    double moveSpeed = 2.5f;

    int mapHeight = worldMap.length;
    int mapWidth = worldMap[1].length;

    double posX, posY, dirX, dirY;

    public RaycastingDemo() {
        posX = 64;
        posY = 64;
        dirX = 1;
        dirY = 0;
    }

    public void tick(boolean[] keys) {
        double oldX = posX;
        double oldY = posY;

        if (keys[KeyEvent.VK_W]) {
            posX += dirX * moveSpeed;
            posY += dirY * moveSpeed;
        } if (keys[KeyEvent.VK_S]) {
            posX -= dirX * moveSpeed;
            posY -= dirY * moveSpeed;
        } if (keys[KeyEvent.VK_A]) {
            double oldDirX = dirX;
            dirX = dirX * Math.cos(-rotateSpeed) - dirY * Math.sin(-rotateSpeed);
            dirY = oldDirX * Math.sin(-rotateSpeed) + dirY * Math.cos(-rotateSpeed);
        } if (keys[KeyEvent.VK_D]) {
            double oldDirX = dirX;
            dirX = dirX * Math.cos(rotateSpeed) - dirY * Math.sin(rotateSpeed);
            dirY = oldDirX * Math.sin(rotateSpeed) + dirY * Math.cos(rotateSpeed);
        }

        if (insideWall(oldX, posY)) {
            posY = oldY;
        } if (insideWall(posX, oldY)) {
            posX = oldX;
        }
    }

    private boolean insideWall(double x, double y) {
        return (worldMap[(int) (y / 32)][(int) (x / 32)] == 1);
    }

    public void renderTopDown(Screen screen) {
        screen.setScreenHalf(Screen.BOTTOM_HALF);
        screen.setScroll(posX, posY);
        screen.drawTopDownWorld(worldMap);
        screen.drawTopDownPlayer(posX, posY, dirX, dirY);
    }

    public void renderRaycasting(Screen screen) {
        screen.setScreenHalf(Screen.TOP_HALF);
        
        // Draw floor and ceiling first
        Graphics2D g = screen.getGraphics();
        g.setColor(new Color(100, 100, 100)); // Floor color (darker gray)
        g.fillRect(0, Main.SCREEN_HEIGHT / 2, Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT / 2);
        
        g.setColor(new Color(135, 206, 235)); // Ceiling color (sky blue)
        g.fillRect(0, 0, Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT / 2);
        
        // Camera plane is perpendicular to direction vector
        double planeX = -dirY * 0.66;
        double planeY = dirX * 0.66;
        
        for (int x = 0; x < Main.SCREEN_WIDTH; x++) {
            // Calculate ray position and direction
            double cameraX = 2 * x / (double) Main.SCREEN_WIDTH - 1; // x-coordinate in camera space
            double rayDirX = dirX + planeX * cameraX;
            double rayDirY = dirY + planeY * cameraX;
            
            // Current map position (in tile coordinates)
            int mapX = (int) (posX / 32);
            int mapY = (int) (posY / 32);
            
            // Initial distance to next X or Y grid line
            double sideDistX, sideDistY;
            
            // Distance ray travels to go from one grid line to next
            double deltaDistX = Math.abs(rayDirX) < 0.00001 ? 1e30 : Math.abs(1 / rayDirX);
            double deltaDistY = Math.abs(rayDirY) < 0.00001 ? 1e30 : Math.abs(1 / rayDirY);
            
            // Direction to step in X and Y
            int stepX, stepY;
            
            // Calculate step and initial sideDist
            if (rayDirX < 0) {
                stepX = -1;
                sideDistX = ((posX / 32) - mapX) * deltaDistX;
            } else {
                stepX = 1;
                sideDistX = (mapX + 1.0 - (posX / 32)) * deltaDistX;
            }
            
            if (rayDirY < 0) {
                stepY = -1;
                sideDistY = ((posY / 32) - mapY) * deltaDistY;
            } else {
                stepY = 1;
                sideDistY = (mapY + 1.0 - (posY / 32)) * deltaDistY;
            }
            
            // DDA algorithm
            int hit = 0; // Was a wall hit?
            int side = 0; // Was a NS or EW wall hit?
            double perpWallDist = 0; // Distance to the wall
            
            while (hit == 0) {
                // Jump to next map square
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX;
                    mapX += stepX;
                    side = 0;
                } else {
                    sideDistY += deltaDistY;
                    mapY += stepY;
                    side = 1;
                }
                
                // Check if ray has hit a wall
                if (mapX >= 0 && mapX < mapWidth && mapY >= 0 && mapY < mapHeight) {
                    if (worldMap[mapY][mapX] > 0) {
                        hit = 1;
                    }
                } else {
                    // Ray hit map boundary
                    hit = 2; // Mark as boundary hit
                    break;
                }
            }
            
            // Only proceed if we hit an actual wall
            if (hit == 1) {
                // Calculate distance to the wall
                if (side == 0) {
                    perpWallDist = (mapX - posX / 32 + (1 - stepX) / 2) / rayDirX;
                } else {
                    perpWallDist = (mapY - posY / 32 + (1 - stepY) / 2) / rayDirY;
                }
                
                // Guard against zero or negative distance
                if (perpWallDist <= 0) perpWallDist = 0.1;
                
                // Calculate height of the wall slice
                int lineHeight = (int) (Main.SCREEN_HEIGHT / perpWallDist);
                
                // Calculate lowest and highest pixel
                int drawStart = -lineHeight / 2 + Main.SCREEN_HEIGHT / 2;
                if (drawStart < 0) drawStart = 0;
                int drawEnd = lineHeight / 2 + Main.SCREEN_HEIGHT / 2;
                if (drawEnd >= Main.SCREEN_HEIGHT) drawEnd = Main.SCREEN_HEIGHT - 1;
                
                // Choose wall color
                Color wallColor;
                if (side == 0) {
                    wallColor = Color.RED; // EW walls
                } else {
                    wallColor = new Color(0, 100, 0); // NS walls
                }
                
                // Draw the wall slice
                g.setColor(wallColor);
                g.drawLine(x, drawStart, x, drawEnd);
            }
        }
    }
}
