package jp.techacademy.hiroshi.tanooka.jumpactiongame;

import com.badlogic.gdx.graphics.Texture;

import java.util.Random;

public class Enemy extends GameObject {
    Random mRandom;

    // 横幅、高さ
    public static final float ENEMY_WIDTH = 1.0f;
    public static final float ENEMY_HEIGHT = 1.0f;

    // 速度
    public static final float ENEMY_VELOCITY = 1.5f;

    public Enemy(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
        super(texture, srcX, srcY, srcWidth, srcHeight);
        setSize(ENEMY_WIDTH, ENEMY_HEIGHT);
        velocity.x = ENEMY_VELOCITY;
    }

    public void update(float deltaTime) {
        setX(getX() + velocity.x * deltaTime);

        if (getX() < ENEMY_WIDTH / 2) {
            velocity.x = -velocity.x;
            setX(ENEMY_WIDTH / 2);
        }
        if (getX() > GameScreen.WORLD_WIDTH - ENEMY_WIDTH / 2) {
            velocity.x = -velocity.x;
            setX(GameScreen.WORLD_WIDTH - ENEMY_WIDTH / 2);
        }
    }
}
