package jp.techacademy.hiroshi.tanooka.jumpactiongame;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class ResultScreen extends ScreenAdapter {
    static final float GUI_WIDTH = 320;
    static final float GUI_HEIGHT = 480;

    private JumpActionGame mGame;
    Sprite mBg;
    OrthographicCamera mGuiCamera;
    FitViewport mGuiViewPort;
    BitmapFont mFont;

    int mScore;

    // コンティニュー時の効果音
    Sound continuePlay = Gdx.audio.newSound(Gdx.files.internal("continue01.mp3"));

    public ResultScreen(JumpActionGame game, int score) {

        mGame = game;

        if (mGame.mRequestHandler != null) {
            mGame.mRequestHandler.showAds(true);
            Gdx.app.log("mGame.mRequestHandler", "OK");
        }

        mScore = score;

        // 背景の準備
        Texture bgTexture = new Texture("resultback.png");
        mBg = new Sprite(new TextureRegion(bgTexture, 0, 0, 540, 810));
        mBg.setSize(GUI_WIDTH, GUI_HEIGHT);
        mBg.setPosition(0, 0);

        // GUI用のカメラを設定する
        mGuiCamera = new OrthographicCamera();
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT);
        mGuiViewPort = new FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera);

        // フォント
        mFont = new BitmapFont(Gdx.files.internal("font.fnt"), Gdx.files.internal("font.png"), false);
    }

    @Override
    public void render(float delta) {
        // 描画する
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // カメラの座標をアップデート(計算)し、スプライトの表示に反映させる
        mGuiCamera.update();
        mGame.batch.setProjectionMatrix(mGuiCamera.combined);

        mGame.batch.begin();
        mBg.draw(mGame.batch);
        mFont.draw(mGame.batch, "Score: " + mScore, 0, GUI_HEIGHT / 2 + 40, GUI_WIDTH, Align.center, false);
        // UFOまで到達している場合
        if (mScore >= 1000000) {
            mFont.draw(mGame.batch, "Congratulations!", 0, GUI_HEIGHT / 2 - 20, GUI_WIDTH, Align.center, false);
            mFont.draw(mGame.batch, "Thank you", 0, GUI_HEIGHT / 2 - 60, GUI_WIDTH, Align.center, false);
            mFont.draw(mGame.batch, "for playing!", 0, GUI_HEIGHT / 2 - 100, GUI_WIDTH, Align.center, false);
        } else {    // 途中でGAMEOVERになった場合
            mFont.draw(mGame.batch, "Retry?", 0, GUI_HEIGHT / 2 - 40, GUI_WIDTH, Align.center, false);
        }
        mGame.batch.end();

        if (Gdx.input.justTouched()) {
            if (mGame.mRequestHandler != null) {
                mGame.mRequestHandler.showAds(false);
            }
            continuePlay.play(0.6f);
            mGame.setScreen(new GameScreen(mGame));
        }
    }
}
