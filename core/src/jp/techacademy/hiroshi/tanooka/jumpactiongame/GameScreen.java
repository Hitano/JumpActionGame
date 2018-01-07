package jp.techacademy.hiroshi.tanooka.jumpactiongame;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameScreen extends ScreenAdapter {
    static final float CAMERA_WIDTH = 10;
    static final float CAMERA_HEIGHT = 15;
    static final float WORLD_WIDTH = 10;
    static final float WORLD_HEIGHT = 15 * 10;  // ***画面分登れば終了
    static final float GUI_WIDTH = 320;
    static final float GUI_HEIGHT = 480;

    static final int GAME_STATE_READY = 0;
    static final int GAME_STATE_PLAYING = 1;
    static final int GAME_STATE_GAMEOVER = 2;

    // 重力
    static final float GRAVITY = -13;

    private JumpActionGame mGame;

    Sprite mBg;
    OrthographicCamera mCamera;
    OrthographicCamera mGuiCamera;

    FitViewport mViewPort;
    FitViewport mGuiViewPort;

    Random mRandom;
    List<Step> mSteps;
    List<Star> mStars;
    List<Enemy> mEnemies;
    Ufo mUfo;
    Player mPlayer;

    float mHeightSoFar;
    int mGameState;
    Vector3 mTouchPoint;
    BitmapFont mFont;
    int mScore;
    int mHighScore;
    Preferences mPrefs;

    // BGMの準備
    Music bgm1 = Gdx.audio.newMusic(Gdx.files.internal("bgm01.mp3"));
    // 効果音の準備
    Sound collisionEnemy = Gdx.audio.newSound(Gdx.files.internal("collisionEnemy.mp3"));
    Sound hitStep = Gdx.audio.newSound(Gdx.files.internal("hitStep.mp3"));
    Sound getStar = Gdx.audio.newSound(Gdx.files.internal("getStar01.mp3"));
    Sound getUfo = Gdx.audio.newSound(Gdx.files.internal("getUfo.mp3"));
    Sound falling = Gdx.audio.newSound(Gdx.files.internal("falling.mp3"));

    public GameScreen(JumpActionGame game) {
        mGame = game;

        // 背景の準備
        Texture bgTexture = new Texture("back.png");
        // TextureRegionで切り出すときの原点は左上
        mBg = new Sprite(new TextureRegion(bgTexture, 0, 0, 540, 810));
        mBg.setSize(CAMERA_WIDTH, CAMERA_HEIGHT);
        mBg.setPosition(0,0);


        // カメラ、ViewPortを生成、設定する
        mCamera = new OrthographicCamera();
        mCamera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT);
        mViewPort = new FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT, mCamera);

        // GUI用のカメラを設定する
        mGuiCamera = new OrthographicCamera();
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT);
        mGuiViewPort = new FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera);

        // メンバ変数の初期化
        mRandom = new Random();
        mSteps = new ArrayList<Step>();
        mStars = new ArrayList<Star>();
        mEnemies = new ArrayList<Enemy>();
        mGameState = GAME_STATE_READY;
        mTouchPoint = new Vector3();
        mFont = new BitmapFont(Gdx.files.internal("font.fnt"), Gdx.files.internal("font.png"), false);
        mFont.getData().setScale(0.8f);
        mScore = 0;
        mHighScore = 0;

        // ハイスコアをPreferencesから取得する
        mPrefs = Gdx.app.getPreferences("jp.techacademy.hiroshi.tanooka.jumpactiongame");
        mHighScore = mPrefs.getInteger("HIGHSCORE", 0);

        createStage();
    }

    @Override
    public void render(float delta) {
        // それぞれの状態をアップデートする
        update(delta);

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // カメラの中心を超えたらカメラを上に移動させる　つまりキャラが画面の上半分に絶対行かない
        if (mPlayer.getY() > mCamera.position.y) {
            mCamera.position.y = mPlayer.getY();
        }

        // カメラの座標をアップデート(計算)し、スプライトの表示に反映させる
        mCamera.update();
        mGame.batch.setProjectionMatrix(mCamera.combined);

        mGame.batch.begin();

        // 原点は左下
        mBg.setPosition(mCamera.position.x - CAMERA_WIDTH / 2, mCamera.position.y - CAMERA_HEIGHT / 2);
        mBg.draw(mGame.batch);

        // Step
        for (int i = 0; i < mSteps.size(); i++) {
            mSteps.get(i).draw(mGame.batch);
        }

        // Star
        for (int i = 0; i < mStars.size(); i++) {
            mStars.get(i).draw(mGame.batch);
        }

        // Enemy
        for (int i = 0; i < mEnemies.size(); i++) {
            mEnemies.get(i).draw(mGame.batch);
        }

        // Ufo
        mUfo.draw(mGame.batch);

        // Player
        mPlayer.draw(mGame.batch);

        mGame.batch.end();

        // スコア表示
        mGuiCamera.update();
        mGame.batch.setProjectionMatrix(mGuiCamera.combined);
        mGame.batch.begin();
        mFont.draw(mGame.batch, "HighScore: " + mHighScore, 16, GUI_HEIGHT - 15);
        mFont.draw(mGame.batch, "Score: " + mScore, 16, GUI_HEIGHT - 35);
        mGame.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        mViewPort.update(width, height);
        mGuiViewPort.update(width, height);
    }

    // ステージを作成する
    private void createStage() {
        // テクスチャの準備
        Texture stepTexture = new Texture("step.png");
        Texture starTexture = new Texture("star.png");
        Texture playerTexture = new Texture("uma.png");
        Texture ufoTexture = new Texture("ufo.png");
        Texture enemyTexture = new Texture("enemy.png");

        // StepとStarとEnemyをゴールの高さまで配置していく
        // 高さに応じて難易度を調整
        float y = 0;

        float maxJumpHeight = Player.PLAYER_JUMP_VELOCIRY * Player.PLAYER_JUMP_VELOCIRY / (2 * -GRAVITY);
        while (y < WORLD_HEIGHT - 5) {
            int type;
            if (y < WORLD_HEIGHT / 4) {
                type = Step.STEP_TYPE_STATIC;
            } else if (y > WORLD_HEIGHT / 4 && y < WORLD_HEIGHT / 2) {
                type = mRandom.nextFloat() > 0.8f ? Step.STEP_TYPE_MOVING : Step.STEP_TYPE_STATIC;
            } else if (y > WORLD_HEIGHT / 2 && y < WORLD_HEIGHT * 3 / 4) {
                type = mRandom.nextFloat() > 0.7f ? Step.STEP_TYPE_MOVING : Step.STEP_TYPE_STATIC;
            } else {
                type = mRandom.nextFloat() > 0.65f ? Step.STEP_TYPE_MOVING : Step.STEP_TYPE_MOVING;
            }
            float x = mRandom.nextFloat() * (WORLD_WIDTH - Step.STEP_WIDTH);

            // Stepの配置
            Step step = new Step(type, stepTexture, 0, 0, 144, 36);
            step.setPosition(x, y);
            mSteps.add(step);

            // Starの配置
            Boolean starCreate;
            if (y < WORLD_HEIGHT / 4) {
                starCreate = mRandom.nextFloat() > 0.4f;
            } else if (y > WORLD_HEIGHT / 4 && y < WORLD_HEIGHT / 2) {
                starCreate = mRandom.nextFloat() > 0.3f;
            } else if (y > WORLD_HEIGHT / 2 && y < WORLD_HEIGHT * 3 / 4) {
                starCreate = mRandom.nextFloat() > 0.2f;
            } else {
                starCreate = mRandom.nextFloat() > 0.1f;
            }
            if (starCreate) {
                Star star = new Star(starTexture, 0, 0, 72, 72);
                star.setPosition(step.getX() + mRandom.nextFloat(), step.getY() + Star.STAR_HEIGHT + mRandom.nextFloat() * 3);
                mStars.add(star);
            }

            // Enemyの配置
            Boolean enemyCreate;
            if (y < WORLD_HEIGHT / 3) {
                enemyCreate = mRandom.nextFloat() > 0.9f;
            } else if (y > WORLD_HEIGHT / 3 && y < WORLD_HEIGHT * 2 / 3) {
                enemyCreate = mRandom.nextFloat() > 0.8f;
            } else {
                enemyCreate = mRandom.nextFloat() > 0.7f;
            }
            if (enemyCreate) {
                Enemy enemy = new Enemy(enemyTexture, 0, 0, 80, 80);
                enemy.setPosition(step.getX() + mRandom.nextFloat(), step.getY() + Enemy.ENEMY_HEIGHT + mRandom.nextFloat() * 3);
                mEnemies.add(enemy);
            }

            y += (maxJumpHeight - 0.4f);
            y -= mRandom.nextFloat() * (maxJumpHeight / 3);
        }

        // Playerを配置
        mPlayer = new Player(playerTexture, 0, 0, 68, 68);
        mPlayer.setPosition(WORLD_WIDTH / 2 - mPlayer.getWidth() / 2, Step.STEP_HEIGHT);

        // ゴールのUfoを配置
        mUfo = new Ufo(ufoTexture, 0, 0, 120, 74);
        mUfo.setPosition(WORLD_WIDTH / 2 - Ufo.UFO_WIDTH, y);

    }

    private void update(float delta) {
        switch (mGameState) {
            case GAME_STATE_READY:
                updateReady();
                break;
            case GAME_STATE_PLAYING:
                updatePlaying(delta);
                break;
            case GAME_STATE_GAMEOVER:
                updateGameOver();
                break;
        }
    }

    private void updateReady() {
        if (Gdx.input.justTouched()) {
            mGameState = GAME_STATE_PLAYING;
        }
    }

    private void updatePlaying(float delta) {
        // タップ処理
        float accel = 0;
        if (Gdx.input.isTouched()) {
            mGuiViewPort.unproject(mTouchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
            Rectangle left = new Rectangle(0, 0, GUI_WIDTH / 2, GUI_HEIGHT);
            Rectangle right = new Rectangle(GUI_WIDTH / 2, 0, GUI_WIDTH / 2, GUI_HEIGHT);
            if (left.contains(mTouchPoint.x, mTouchPoint.y)) {
                accel = 5.0f;
            }
            if (right.contains(mTouchPoint.x, mTouchPoint.y)) {
                accel = -5.0f;
            }
        }

        // Step
        for (int i = 0; i < mSteps.size(); i++) {
            mSteps.get(i).update(delta);
        }

        // Enemy
        for (int i = 0; i < mEnemies.size(); i++) {
            mEnemies.get(i).update(delta);
        }

        // Player
        if (mPlayer.getY() <= 0.5f) {
            mPlayer.hitStep();
        }
        mPlayer.update(delta, accel);
        mHeightSoFar = Math.max(mPlayer.getY(), mHeightSoFar);

        bgm1.setVolume(0.8f);
        bgm1.play();
        bgm1.setLooping(true);

        // 当たり判定を行う
        checkCollision();

        // ゲームオーバーの判定を行う
        checkGameOver();
    }

    private void updateGameOver() {
        bgm1.stop();
        if (Gdx.input.justTouched()) {
            mGame.setScreen(new ResultScreen(mGame, mScore));
        }
    }

    private void checkCollision() {
        // UFO(ゴールとの当たり判定)
        if (mPlayer.getBoundingRectangle().overlaps(mUfo.getBoundingRectangle())) {
            getUfo.play(0.7f);
            mScore += 1000000;
            if (mScore >= mHighScore) {
                mHighScore = mScore;
                mPrefs.putInteger("HIGHSCORE", mHighScore);
                mPrefs.flush();
            }
            mGameState = GAME_STATE_GAMEOVER;
            return;
        }

        // Enemyとの当たり判定
        for (int i = 0; i < mEnemies.size(); i++) {
            Enemy enemy = mEnemies.get(i);
            if (mPlayer.getBoundingRectangle().overlaps(enemy.getBoundingRectangle())) {
                collisionEnemy.play(0.7f);
                mGameState = GAME_STATE_GAMEOVER;
                return;
            }
        }

        // Starとの当たり判定
        for (int i = 0; i < mStars.size(); i++) {
            Star star = mStars.get(i);

            if (star.mState == Star.STAR_NONE) {
                continue;
            }

            if (mPlayer.getBoundingRectangle().overlaps(star.getBoundingRectangle())) {
                getStar.play(0.7f);
                star.get();
                mScore += 10;
                if (mScore > mHighScore) {
                    mHighScore = mScore;
                    // ハイスコアをPreferencesに保存する
                    mPrefs.putInteger("HIGHSCORE", mHighScore);
                    mPrefs.flush();
                }
                break;
            }
        }

        // Stepとの当たり判定
        // 上昇中はStepとの当たり判定を確認しない
        if (mPlayer.velocity.y > 0) {
            return;
        }

        for (int i = 0; i < mSteps.size(); i++) {
            Step step = mSteps.get(i);

            if (step.mState == Step.STEP_STATE_VANISH) {
                continue;
            }

            if (mPlayer.getY() > step.getY()) {
                if (mPlayer.getBoundingRectangle().overlaps(step.getBoundingRectangle())) {
                    hitStep.play(0.05f);
                    mPlayer.hitStep();
                    if (mRandom.nextFloat() > 0.9f) {   // ランダムでStepを消す
                        step.vanish();
                    }
                    break;
                }
            }
        }
    }

    private void checkGameOver() {
        if (mHeightSoFar - CAMERA_HEIGHT / 2 > mPlayer.getY()) {
            falling.play(0.7f);
            Gdx.app.log("JumpActionGame", "GAMEOVER");
            mGameState = GAME_STATE_GAMEOVER;
        }
    }
}
