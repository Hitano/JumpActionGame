package jp.techacademy.hiroshi.tanooka.jumpactiongame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class JumpActionGame extends Game {
    public SpriteBatch batch;
    public ActivityRequestHandler mRequestHandler;

    public JumpActionGame(ActivityRequestHandler requestHandler) {
        super();
        mRequestHandler = requestHandler;
    }

    @Override
    public void create () {
        batch = new SpriteBatch();
        setScreen(new GameScreen(this));
    }
}
