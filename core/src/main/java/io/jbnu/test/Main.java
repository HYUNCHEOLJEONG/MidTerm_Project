package io.jbnu.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private BitmapFont font;
    GameWorld world;

    public enum GameState { MENU, PLAYING, GAME_OVER, CLEAR }
    public GameState gameState = GameState.MENU;

    private Array<Texture> idleFrames;
    private Array<Texture> runFrames;
    private Array<Texture> attackFrames;

    private Texture monsterMoveTex;
    private Texture flyTex;
    private Texture sawTex;
    private Texture coinTex;
    private Texture starTex;
    private Texture laserTex;
    private Texture heartTex;
    private Texture knifeTex;
    private Texture bg1Tex, bg2Tex, bg3Tex;
    private Texture flagUpTex, flagDownTex;

    private Music bgmMusic;
    private Music finishMusic;
    private Sound jumpSound;
    private float menuAnimTimer = 0f;

    // create() : 텍스처, 사운드 등 그래픽/오디오 자원 1회 초기화 및 메모리 로드
    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, 800, 480);

        idleFrames = new Array<>();
        for (int i = 1; i <= 2; i++) {
            if (Gdx.files.internal("idle/i" + i + ".png").exists()) {
                idleFrames.add(new Texture("idle/i" + i + ".png"));
            }
        }
        if (idleFrames.size == 0) idleFrames.add(new Texture("run/k1.png"));

        runFrames = new Array<>();
        for (int i = 1; i <= 8; i++) {
            if (Gdx.files.internal("run/k" + i + ".png").exists()) {
                runFrames.add(new Texture("run/k" + i + ".png"));
            }
        }

        attackFrames = new Array<>();
        for (int i = 1; i <= 13; i++) {
            if (Gdx.files.internal("attack/a" + i + ".png").exists()) {
                attackFrames.add(new Texture("attack/a" + i + ".png"));
            }
        }

        monsterMoveTex = new Texture("Monster_move.png");
        flyTex = new Texture("Fly.png");
        sawTex = new Texture("Saw.png");
        coinTex = new Texture("coinGold.png");
        starTex = new Texture("star.png");
        laserTex = new Texture("laser.png");

        try {
            bgmMusic = Gdx.audio.newMusic(Gdx.files.internal("bgm.ogg"));
            bgmMusic.setLooping(true);
            bgmMusic.setVolume(0.5f);
            bgmMusic.play();
        } catch (Exception e) {
            Gdx.app.log("Audio", "bgm.ogg 파일을 찾을 수 없습니다.");
        }

        try {
            finishMusic = Gdx.audio.newMusic(Gdx.files.internal("finish.ogg"));
            finishMusic.setLooping(false);
        } catch (Exception e) {
            Gdx.app.log("Audio", "finish.ogg 파일을 찾을 수 없습니다.");
        }

        try {
            jumpSound = Gdx.audio.newSound(Gdx.files.internal("jump_sound.wav"));
        } catch (Exception e) {
            Gdx.app.log("Audio", "jump_sound.wav 파일을 찾을 수 없습니다.");
        }

        Pixmap rawHeart = new Pixmap(Gdx.files.internal("heart.png"));
        Pixmap cleanHeart = new Pixmap(rawHeart.getWidth(), rawHeart.getHeight(), Pixmap.Format.RGBA8888);
        cleanHeart.setBlending(Pixmap.Blending.None);
        for (int x = 0; x < rawHeart.getWidth(); x++) {
            for (int y = 0; y < rawHeart.getHeight(); y++) {
                int c = rawHeart.getPixel(x, y);
                Color color = new Color(c);
                if (color.r > 0.85f && color.g > 0.85f && color.b > 0.85f) {
                    cleanHeart.setColor(0, 0, 0, 0);
                    cleanHeart.drawPixel(x, y);
                } else {
                    cleanHeart.setColor(color);
                    cleanHeart.drawPixel(x, y);
                }
            }
        }
        heartTex = new Texture(cleanHeart);
        rawHeart.dispose();
        cleanHeart.dispose();

        Pixmap rawKnife = new Pixmap(Gdx.files.internal("knife.png"));
        Pixmap cleanKnife = new Pixmap(rawKnife.getWidth(), rawKnife.getHeight(), Pixmap.Format.RGBA8888);
        cleanKnife.setBlending(Pixmap.Blending.None);
        for (int x = 0; x < rawKnife.getWidth(); x++) {
            for (int y = 0; y < rawKnife.getHeight(); y++) {
                int c = rawKnife.getPixel(x, y);
                Color color = new Color(c);
                if (color.r < 0.15f && color.g < 0.15f && color.b < 0.15f) {
                    cleanKnife.setColor(0, 0, 0, 0);
                    cleanKnife.drawPixel(x, y);
                } else {
                    cleanKnife.setColor(color);
                    cleanKnife.drawPixel(x, y);
                }
            }
        }
        knifeTex = new Texture(cleanKnife);
        rawKnife.dispose();
        cleanKnife.dispose();

        bg1Tex = new Texture("Round1_Background.png");
        bg2Tex = new Texture("Round2_Background.png");
        bg3Tex = new Texture("Round3_Background.png");
        flagUpTex = new Texture("flagRed1.png");
        flagDownTex = new Texture("flagRed_down.png");

        world = new GameWorld();
    }
    //3단 분리 메인 루프
    // Input -> Logic -> Draw
    @Override
    public void render() {
        ScreenUtils.clear(0.05f, 0.05f, 0.1f, 1);
        menuAnimTimer += Gdx.graphics.getDeltaTime();

        input(); // 1. 사용자 입력
        logic();  // 2. Delta Time 기반 물리 연산 및 충돌 보정


        //OrthographicCamera 수평 스크롤 추적 연출
        //플레이어 X좌표 진행에 맞춰 카메라 중심점 실시간 갱신
        if (gameState == GameState.PLAYING) {
            camera.position.x = Math.max(400, world.getPlayer().position.x + 36);
            camera.position.y = 240;
            camera.update();

            batch.setProjectionMatrix(camera.combined);
            shapeRenderer.setProjectionMatrix(camera.combined);

            drawGame();
        } else {
            batch.setProjectionMatrix(hudCamera.combined);
            drawUIStates();
        }
    }
    //2.DeltaTime 기반 물리 연산 및 충돌 보정
    private void logic(){
        if (gameState == GameState.PLAYING && !world.isPaused) {
            world.update(Gdx.graphics.getDeltaTime());
            if (world.isGameOver) {
                gameState = GameState.GAME_OVER;
            } else if (world.isGameCleared) {
                gameState = GameState.CLEAR;
            }
        }
    }

    // 3. SpriteBatch & ShapeRenderer 그래픽 출력
    private void drawGame() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        Texture currentBg = bg1Tex;
        if (world.currentRound == 2) currentBg = bg2Tex;
        else if (world.currentRound == 3) currentBg = bg3Tex;

        for (int i = 0; i < 7; i++) {
            batch.draw(currentBg, i * 800, 0, 800, 480);
        }
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        world.drawPlatforms(shapeRenderer);
        shapeRenderer.end();

        batch.begin();
        world.drawSprites(batch);
        world.drawAttacks(batch);
        batch.end();

        if (world.laserTimer > 0) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            world.drawGearSecondAura(shapeRenderer);
            shapeRenderer.end();
        }

        batch.setProjectionMatrix(hudCamera.combined);

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.05f, 0.12f, 0.85f);
        shapeRenderer.rect(10, 395, 330, 75);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.2f, 0.8f, 0.6f, 0.9f);
        shapeRenderer.rect(10, 395, 330, 75);
        shapeRenderer.end();

        batch.begin();
        font.getData().setScale(1.3f);
        font.draw(batch, "Stage " + world.currentRound, 20, 455);

        for (int i = 0; i < world.lives; i++) {
            batch.draw(heartTex, 115 + i * 28, 432, 24, 24);
        }

        batch.draw(coinTex, 20, 410, 24, 24);
        font.draw(batch, ": " + world.collectedCoins + " / " + world.totalCoins, 48, 430);

        if (world.showCoinWarning) {
            font.getData().setScale(2.0f);
            font.draw(batch, "Collect all coins first!", 260, 340);
            font.getData().setScale(1.5f);
        }

        if (world.laserTimer > 0) {
            font.draw(batch, "GEAR 2nd BLUE AURA! (" + (int)Math.ceil(world.laserTimer) + "s)", 20, 370);
        }

        float barX = 420;
        float barY = 430;
        float barWidth = 350;
        float barHeight = 16;

        batch.end();
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.9f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        float progress = Math.min(1.0f, world.getPlayer().position.x / world.mapLength);
        shapeRenderer.setColor(0.1f, 0.8f, 0.5f, 1.0f);
        shapeRenderer.rect(barX, barY, barWidth * progress, barHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.9f, 0.7f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.end();

        batch.begin();
        font.getData().setScale(1.0f);
        font.draw(batch, "MISSION PROGRESS", barX + 90, barY + 33);
        font.getData().setScale(1.5f);

        if (world.roundStartTimer < 2.0f && !world.isPaused) {
            font.getData().setScale(3.0f);
            font.draw(batch, "- STAGE " + world.currentRound + " -", 270, 270);
        }

        if (world.isPaused) {
            font.getData().setScale(2.5f);
            font.draw(batch, "PAUSED", 350, 260);
            font.draw(batch, "Press P or ESC to Resume", 260, 210);
        }

        batch.end();
    }

    private void drawUIStates() {
        batch.begin();
        for (int i = 0; i < 2; i++) {
            batch.draw(bg1Tex, i * 800, 0, 800, 480);
        }
        batch.end();

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.05f, 0.1f, 0.75f);
        shapeRenderer.rect(120, 60, 560, 360);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.2f, 0.7f, 0.9f, 0.8f);
        shapeRenderer.rect(120, 60, 560, 360);
        shapeRenderer.end();

        batch.begin();
        if (gameState == GameState.MENU) {
            font.getData().setScale(3.2f);
            font.draw(batch, "LUMEN ADVENTURE", 175, 360);

            font.getData().setScale(1.6f);
            font.draw(batch, "- Modern Platformer Odyssey -", 260, 315);

            if (idleFrames.size > 0) {
                batch.draw(idleFrames.get(0), 364, 180, 72, 72);
            }
            batch.draw(coinTex, 290, 195, 32, 32);
            batch.draw(starTex, 470, 195, 32, 32);
            batch.draw(heartTex, 384, 260, 32, 32);

            if ((int)(menuAnimTimer * 3) % 2 == 0) {
                font.getData().setScale(2.2f);
                font.draw(batch, "PRESS ENTER TO START", 235, 120);
            }
        } else if (gameState == GameState.GAME_OVER) {
            font.getData().setScale(3.0f);
            font.draw(batch, "GAME OVER", 290, 280);

            font.getData().setScale(2.0f);
            font.draw(batch, "Press ENTER to Restart", 260, 210);
        } else if (gameState == GameState.CLEAR) {
            font.getData().setScale(3.0f);
            font.draw(batch, "GAME CLEAR!", 270, 290);

            font.getData().setScale(2.0f);
            font.draw(batch, "Congratulations! You saved the world.", 180, 220);
            font.draw(batch, "Press ENTER to Play Again", 250, 150);
        }
        batch.end();
    }

    private void input() {
        if (gameState == GameState.MENU) {
            if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                gameState = GameState.PLAYING;
                world.restartGame();
            }
            return;
        }

        if (gameState == GameState.GAME_OVER || gameState == GameState.CLEAR) {
            if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                gameState = GameState.MENU;
            }
            return;
        }
        //  P 또는 ESC 키 일시정지 시스템
        if (Gdx.input.isKeyJustPressed(Keys.P) || Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            if (gameState == GameState.PLAYING) {
                world.isPaused = !world.isPaused;
            }
        }

        if (world.isPaused) return;

        boolean isMoving = false;
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)){
            world.onPlayerRight();
            isMoving = true;
        } else if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) {
            world.onPlayerLeft();
            isMoving = true;
        }

        world.getPlayer().isMoving = isMoving;

        if (Gdx.input.isKeyJustPressed(Keys.F)) {
            world.toggleFacingDirection();
        }

        if (Gdx.input.isKeyJustPressed(Keys.SPACE) || Gdx.input.isKeyJustPressed(Keys.UP)){
            world.onPlayerJump();
            if (jumpSound != null) {
                jumpSound.play(0.7f);
            }
        }

        if (Gdx.input.isKeyJustPressed(Keys.T)) {
            world.fireBasicAttack();
        }

        if (Gdx.input.isKeyJustPressed(Keys.X)) {
            world.fireLaser();
        }
    }

    //사용이 끝난 텍스처, 사운드, 배치 객체 명시적 메모리 해제
    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();

        for (Texture tex : idleFrames) tex.dispose();
        for (Texture tex : runFrames) tex.dispose();
        for (Texture tex : attackFrames) tex.dispose();

        monsterMoveTex.dispose();
        flyTex.dispose();
        sawTex.dispose();
        coinTex.dispose();
        starTex.dispose();
        laserTex.dispose();
        heartTex.dispose();
        knifeTex.dispose();
        bg1Tex.dispose();
        bg2Tex.dispose();
        bg3Tex.dispose();
        flagUpTex.dispose();
        flagDownTex.dispose();

        if (bgmMusic != null) bgmMusic.dispose();
        if (finishMusic != null) finishMusic.dispose();
        if (jumpSound != null) jumpSound.dispose();
    }

    public class gameCharacter {
        public Vector2 position;
        public Vector2 velocity;
        public Sprite sprite;
        public boolean isGrounded = false;
        public boolean facingRight = true;
        public boolean isMoving = false;
        public boolean isAttacking = false;
        private final float moveSpeed = 220f;

        private float animTimer = 0f;
        private int currentFrame = 0;
        private float attackAnimTimer = 0f;
        private final float charSize = 72f;

        public gameCharacter(float startX, float startY) {
            this.position = new Vector2(startX, startY);
            this.velocity = new Vector2(0, 0);
            Texture initialTex = (idleFrames.size > 0) ? idleFrames.get(0) : runFrames.get(0);
            this.sprite = new Sprite(initialTex);
            this.sprite.setSize(charSize, charSize);
            this.sprite.setPosition(position.x, position.y);
        }

        public void triggerAttack() {
            isAttacking = true;
            attackAnimTimer = 0.5f;
            currentFrame = 0;
        }

        public void update(float delta) {
            animTimer += delta;

            if (isAttacking) {
                attackAnimTimer -= delta;
                if (attackFrames.size > 0) {
                    if (animTimer > 0.04f) {
                        animTimer = 0f;
                        currentFrame = (currentFrame + 1) % attackFrames.size;
                        sprite.setRegion(attackFrames.get(currentFrame));
                    }
                }
                if (attackAnimTimer <= 0) {
                    isAttacking = false;
                    currentFrame = 0;
                }
            } else if (isMoving && runFrames.size > 0) {
                if (animTimer > 0.08f) {
                    animTimer = 0f;
                    currentFrame = (currentFrame + 1) % runFrames.size;
                    sprite.setRegion(runFrames.get(currentFrame));
                }
            } else {
                if (idleFrames.size > 0) {
                    if (animTimer > 0.3f) {
                        animTimer = 0f;
                        currentFrame = (currentFrame + 1) % idleFrames.size;
                        sprite.setRegion(idleFrames.get(currentFrame));
                    }
                }
            }
            sprite.setFlip(!facingRight, false);
        }

        public void jump() {
            if (isGrounded) {
                velocity.y = 480f;
                isGrounded = false;
            }
        }

        public void moveRight(float delta) {
            position.x += moveSpeed * delta;
            facingRight = true;
        }

        public void moveLeft(float delta) {
            position.x -= moveSpeed * delta;
            facingRight = false;
            if(position.x < 0) position.x = 0;
        }

        public void toggleDirection() {
            facingRight = !facingRight;
        }

        public void syncSpriteToPosition() { sprite.setPosition(position.x, position.y); }
        public void draw(SpriteBatch batch) { sprite.draw(batch); }
        public Rectangle getBoundingBox() { return new Rectangle(position.x, position.y, charSize, charSize); }
    }

    public interface Enemy {
        void update(float delta, float playerX, float floorLevel);
        void draw(SpriteBatch batch);
        Rectangle getBoundingBox();
        boolean isAlive();
        boolean hitByAttack(boolean isLaser);
    }

    public class Monster implements Enemy {
        public Vector2 position;
        public Vector2 velocity;
        public Sprite sprite;
        public boolean isAlive = true;
        public boolean isGrounded = false;
        private float jumpTimer = 0;
        private float size = 48f;

        public Monster(float startX, float startY, boolean isGiant) {
            this.position = new Vector2(startX, startY);
            this.velocity = new Vector2(0, 0);
            this.sprite = new Sprite(monsterMoveTex);
            if (isGiant) size = 75f;
            this.sprite.setSize(size, size);
            this.sprite.setPosition(position.x, position.y);
        }

        public void update(float delta, float playerX, float floorLevel) {
            if (!isAlive) return;
            velocity.y += -800f * delta;
            position.y += velocity.y * delta;
            if (position.y <= floorLevel) {
                position.y = floorLevel;
                velocity.y = 0;
                isGrounded = true;
            }
            float distance = playerX - position.x;
            if (Math.abs(distance) < 600) {
                if (distance > 0) position.x += 125f * delta;
                else position.x -= 125f * delta;

                jumpTimer += delta;
                if (jumpTimer > 0.9f && isGrounded) {
                    velocity.y = 420f;
                    isGrounded = false;
                    jumpTimer = 0;
                }
            }
            sprite.setPosition(position.x, position.y);
        }

        public void draw(SpriteBatch batch) { if (isAlive) sprite.draw(batch); }
        public Rectangle getBoundingBox() { return new Rectangle(position.x, position.y, size, size); }
        public boolean isAlive() { return isAlive; }
        public boolean hitByAttack(boolean isLaser) {
            isAlive = false;
            return true;
        }
    }

    public class Fly implements Enemy {
        public Vector2 position;
        public Sprite sprite;
        public boolean isAlive = true;
        private float angle = 0;

        public Fly(float startX, float startY) {
            this.position = new Vector2(startX, startY);
            this.sprite = new Sprite(flyTex);
            this.sprite.setSize(40, 40);
            this.sprite.setPosition(position.x, position.y);
        }

        public void update(float delta, float playerX, float floorLevel) {
            if (!isAlive) return;
            angle += delta * 4f;
            position.y += (float)Math.sin(angle) * 1.5f;

            float distance = playerX - position.x;
            if (Math.abs(distance) < 500) {
                if (distance > 0) position.x += 90f * delta;
                else position.x -= 90f * delta;
            }
            sprite.setPosition(position.x, position.y);
        }

        public void draw(SpriteBatch batch) { if (isAlive) sprite.draw(batch); }
        public Rectangle getBoundingBox() { return new Rectangle(position.x, position.y, 40, 40); }
        public boolean isAlive() { return isAlive; }
        public boolean hitByAttack(boolean isLaser) {
            isAlive = false;
            return true;
        }
    }

    public class Saw implements Enemy {
        public Vector2 position;
        public Vector2 velocity;
        public Sprite sprite;
        public boolean isAlive = true;
        public int hp = 3; // 보스 체력 3 (일반 칼질 3번, 레이저 1번이면 격파)
        private float dashTimer = 0f;
        private boolean isDashing = false;
        private float dashCooldown = 0f;

        public Saw(float startX, float startY) {
            this.position = new Vector2(startX, startY);
            this.velocity = new Vector2(0, 0);
            this.sprite = new Sprite(sawTex);
            this.sprite.setSize(200, 200);
            this.sprite.setPosition(position.x, position.y);
        }

        public void update(float delta, float playerX, float floorLevel) {
            if (!isAlive) return;
            float distance = playerX - position.x;

            dashCooldown += delta;
            if (Math.abs(distance) < 400 && dashCooldown > 2.5f) {
                isDashing = true;
                dashTimer = 0.6f;
                dashCooldown = 0f;
            }

            if (isDashing) {
                dashTimer -= delta;
                float dashSpeed = (distance > 0) ? 500f : -500f;
                position.x += dashSpeed * delta;
                if (dashTimer <= 0) isDashing = false;
            } else {
                if (Math.abs(distance) < 700) {
                    if (distance > 0) position.x += 100f * delta;
                    else position.x -= 100f * delta;
                }
            }

            sprite.rotate(300f * delta);
            sprite.setPosition(position.x, position.y);
        }

        public void draw(SpriteBatch batch) { if (isAlive) sprite.draw(batch); }
        public Rectangle getBoundingBox() { return new Rectangle(position.x, position.y, 200, 200); }
        public boolean isAlive() { return isAlive; }

        public boolean hitByAttack(boolean isLaser) {
            if (isLaser) {
                hp -= 3; // 레이저는 데미지 3이므로 맞으면 즉시 격파
            } else {
                hp -= 1; // 일반 칼질은 1씩 깎여서 총 3번 타격 필요
            }
            if (hp <= 0) {
                isAlive = false;
            }
            return true; // 공격이 적중했음을 알림
        }
    }

    public static class Platform {
        public Rectangle bounds;
        public Platform(float x, float y, float width, float height) {
            bounds = new Rectangle(x, y, width, height);
        }
    }

    public class Coin {
        public Rectangle bounds;
        public boolean collected = false;
        public Coin(float x, float y) {
            bounds = new Rectangle(x, y, 32, 32);
        }
        public void draw(SpriteBatch batch) {
            if (!collected) batch.draw(coinTex, bounds.x, bounds.y, 32, 32);
        }
    }

    public class Star {
        public Rectangle bounds;
        public boolean collected = false;
        public Star(float x, float y) {
            bounds = new Rectangle(x, y, 36, 36);
        }
        public void draw(SpriteBatch batch) {
            if (!collected) batch.draw(starTex, bounds.x, bounds.y, 36, 36);
        }
    }

    public class AttackProjectile {
        public Rectangle bounds;
        public float activeTime = 0.3f;
        public boolean facingRight;
        public boolean isLaser;
        public float rotationAngle = 0f;
        public boolean hasHitEnemy = false; // 공격 당 한 번만 적중하도록 중복 방지 플래그

        public AttackProjectile(float playerX, float playerY, boolean facingRight, boolean isLaser) {
            this.facingRight = facingRight;
            this.isLaser = isLaser;

            float width = isLaser ? 600 : 55;
            float height = isLaser ? 32 : 55;
            float attackX = facingRight ? playerX + 40 : playerX - width - 10;

            bounds = new Rectangle(attackX, playerY + 10, width, height);
        }

        public void update(float delta, float currentPlayerX, float currentPlayerY, boolean isFacingRight) {
            activeTime -= delta;
            rotationAngle += delta * 500f;

            if (!isLaser) {
                float offsetX = isFacingRight ? 40f : -45f;
                bounds.x = currentPlayerX + offsetX;
                bounds.y = currentPlayerY + 10f;
            }
        }

        public void draw(SpriteBatch batch) {
            if (activeTime > 0) {
                if (isLaser) {
                    if (facingRight) {
                        batch.draw(laserTex, bounds.x, bounds.y, bounds.width, bounds.height);
                    } else {
                        batch.draw(laserTex, bounds.x + bounds.width, bounds.y, -bounds.width, bounds.height);
                    }
                } else {
                    float originX = facingRight ? 5 : 50;
                    float originY = 5;
                    float rot = facingRight ? rotationAngle : -rotationAngle;

                    batch.draw(knifeTex,
                        bounds.x,
                        bounds.y,
                        originX, originY,
                        bounds.width, bounds.height,
                        1.1f, 1.1f,
                        rot,
                        0, 0, knifeTex.getWidth(), knifeTex.getHeight(),
                        !facingRight, false);
                }
            }
        }
    }

    public class Flag {
        public Vector2 position;
        public boolean isReached = false;
        private float currentHeight;
        private final float targetHeight;
        private final float flagHeight = 90f;

        public Flag(float blockX, float blockY, float blockHeight) {
            this.position = new Vector2(blockX, blockY + blockHeight);
            this.currentHeight = blockY + blockHeight;
            this.targetHeight = blockY + blockHeight;
        }

        public void update(float delta, boolean canOpen) {
            if (canOpen && isReached && currentHeight > targetHeight - flagHeight) {
                currentHeight -= 100f * delta;
                if (currentHeight < targetHeight - flagHeight) currentHeight = targetHeight - flagHeight;
            }
        }

        public void draw(SpriteBatch batch) {
            Texture tex = isReached ? flagDownTex : flagUpTex;
            batch.draw(tex, position.x, targetHeight, 48, flagHeight);
        }

        public Rectangle getBoundingBox() {
            return new Rectangle(position.x, targetHeight, 48, flagHeight);
        }
    }

    public class GameWorld {
        public final float WORLD_GRAVITY = -800f;
        public final float FLOOR_LEVEL = 50f;
        public int currentRound = 1;
        public boolean isGameOver = false;
        public boolean isPaused = false;
        public boolean isGameCleared = false;
        public boolean showCoinWarning = false;
        private float warningTimer = 0f;

        public int lives = 3;
        public int totalCoins = 3;
        public int collectedCoins = 0;
        public float laserTimer = 0f;
        public float roundStartTimer = 0f;
        private float auraAnimTime = 0f;

        private gameCharacter player;
        Array<Enemy> enemies;
        Array<Platform> platforms;
        Array<Coin> coins;
        Array<Star> stars;
        Array<AttackProjectile> attacks;
        private Flag flag;
        public float mapLength = 4500f;

        public GameWorld() {
            lives = 3;
            initLevel();
        }

        public void initLevel() {
            player = new gameCharacter(100, 100);
            enemies = new Array<>();
            platforms = new Array<>();
            coins = new Array<>();
            stars = new Array<>();
            attacks = new Array<>();
            isGameOver = false;
            isPaused = false;
            isGameCleared = false;
            showCoinWarning = false;
            warningTimer = 0f;
            laserTimer = 0f;
            roundStartTimer = 0f;
            collectedCoins = 0;

            int monsterCount = 5;
            if (currentRound == 1) { totalCoins = 3; monsterCount = 5; }
            else if (currentRound == 2) { totalCoins = 5; monsterCount = 10; }
            else if (currentRound == 3) { totalCoins = 7; monsterCount = 15; }

            if (currentRound == 3) {
                coins.add(new Coin(450, 180));
                coins.add(new Coin(950, 240));
                coins.add(new Coin(1450, 300));
                coins.add(new Coin(1950, 240));
                coins.add(new Coin(2450, 200));
                coins.add(new Coin(2950, 280));
                coins.add(new Coin(3350, 370));
            } else {
                for (int i = 0; i < totalCoins; i++) {
                    coins.add(new Coin(500 + i * 450, 180 + (i % 2) * 110));
                }
            }

            if (currentRound == 1) {
                stars.add(new Star(1500, 250));
            } else if (currentRound == 2) {
                stars.add(new Star(160, 120));
                stars.add(new Star(2200, 280));
            } else if (currentRound == 3) {
                stars.add(new Star(160, 120));
                stars.add(new Star(1400, 220));
                stars.add(new Star(2800, 300));
            }

            platforms.add(new Platform(350, 130, 130, 20));
            platforms.add(new Platform(550, 210, 130, 20));
            platforms.add(new Platform(800, 280, 130, 20));
            platforms.add(new Platform(1100, 180, 130, 20));
            platforms.add(new Platform(1350, 260, 130, 20));
            platforms.add(new Platform(1650, 320, 130, 20));
            platforms.add(new Platform(1950, 200, 130, 20));
            platforms.add(new Platform(2250, 270, 130, 20));
            platforms.add(new Platform(2550, 160, 130, 20));
            platforms.add(new Platform(2850, 240, 130, 20));
            platforms.add(new Platform(3150, 310, 130, 20));
            if (currentRound >= 2) {
                platforms.add(new Platform(1500, 150, 120, 20));
                platforms.add(new Platform(2400, 210, 120, 20));
            }
            if (currentRound >= 3) {
                platforms.add(new Platform(1850, 280, 110, 20));
                platforms.add(new Platform(2700, 190, 110, 20));
                platforms.add(new Platform(3050, 250, 110, 20));
            }

            Platform topPlatform = new Platform(3500, 330, 180, 25);
            platforms.add(topPlatform);
            mapLength = 3500f + 65f;

            flag = new Flag(topPlatform.bounds.x, topPlatform.bounds.y, topPlatform.bounds.height);

            for (int i = 1; i <= monsterCount; i++) {
                float monsterX = 350 + i * 200;

                if (currentRound == 1) {
                    enemies.add(new Monster(monsterX, FLOOR_LEVEL, false));
                } else if (currentRound == 2) {
                    if (i % 3 == 0) enemies.add(new Fly(monsterX, 220f));
                    else enemies.add(new Monster(monsterX, FLOOR_LEVEL, true));
                } else {
                    if (i == 4 || i == 11) enemies.add(new Saw(monsterX, FLOOR_LEVEL));
                    else if (i % 3 == 0) enemies.add(new Fly(monsterX, 220f));
                    else enemies.add(new Monster(monsterX, FLOOR_LEVEL, true));
                }
            }
        }

        public void restartGame() {
            currentRound = 1;
            lives = 3;
            initLevel();
        }

        public void fireBasicAttack() {
            player.triggerAttack();
            attacks.add(new AttackProjectile(player.position.x, player.position.y, player.facingRight, false));
        }

        public void fireLaser() {
            if (laserTimer > 0) {
                attacks.add(new AttackProjectile(player.position.x, player.position.y, player.facingRight, true));
            }
        }

        public void toggleFacingDirection() {
            player.toggleDirection();
        }

        public void update(float delta) {
            roundStartTimer += delta;
            auraAnimTime += delta;

            if (laserTimer > 0) {
                laserTimer -= delta;
                if (laserTimer < 0) laserTimer = 0;
            }

            if (showCoinWarning) {
                warningTimer += delta;
                if (warningTimer > 2.0f) {
                    showCoinWarning = false;
                    warningTimer = 0f;
                }
            }

            player.velocity.y += WORLD_GRAVITY * delta;

            float newX = player.position.x;
            float newY = player.position.y + player.velocity.y * delta;

            boolean grounded = false;

            if (newY <= FLOOR_LEVEL) {
                if (!((newX > 900 && newX < 1300) || (newX > 1800 && newX < 2200) || (newX > 2700 && newX < 3000))) {
                    newY = FLOOR_LEVEL;
                    player.velocity.y = 0;
                    grounded = true;
                }
            }

            for (Platform p : platforms) {
                if (player.velocity.y <= 0) {
                    Rectangle pBox = p.bounds;
                    if (player.position.y >= pBox.y + pBox.height - 15 &&
                        newY <= pBox.y + pBox.height &&
                        newX + 72 > pBox.x && newX < pBox.x + pBox.width) {
                        newY = pBox.y + pBox.height;
                        player.velocity.y = 0;
                        grounded = true;
                    }
                }
            }

            if (newY < -50) {
                isGameOver = true;
            }

            player.isGrounded = grounded;
            player.position.y = newY;
            player.position.x = newX;
            player.update(delta);
            player.syncSpriteToPosition();

            Rectangle playerBox = player.getBoundingBox();
            for (Coin c : coins) {
                if (!c.collected && playerBox.overlaps(c.bounds)) {
                    c.collected = true;
                    collectedCoins++;
                }
            }

            for (Star s : stars) {
                if (!s.collected && playerBox.overlaps(s.bounds)) {
                    s.collected = true;
                    laserTimer = 15.0f;
                }
            }

            // 공격과 적 충돌 처리 (공격당 딱 1번만 데미지가 들어가도록 수정)
            for (int i = attacks.size - 1; i >= 0; i--) {
                AttackProjectile atk = attacks.get(i);
                atk.update(delta, player.position.x, player.position.y, player.facingRight);
                if (atk.activeTime <= 0) {
                    attacks.removeIndex(i);
                } else {
                    if (!atk.hasHitEnemy) {
                        for (Enemy e : enemies) {
                            if (e.isAlive() && atk.bounds.overlaps(e.getBoundingBox())) {
                                boolean hit = e.hitByAttack(atk.isLaser);
                                if (hit) {
                                    atk.hasHitEnemy = true; // 이번 공격은 적중했으므로 추가 중복 타격 방지
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            for (Enemy e : enemies) {
                e.update(delta, player.position.x, FLOOR_LEVEL);
                if (e.isAlive() && playerBox.overlaps(e.getBoundingBox())) {
                    if (player.velocity.y < 0 && player.position.y >= e.getBoundingBox().y + e.getBoundingBox().height - 15) {
                        e.hitByAttack(false);
                        player.velocity.y = 420f;
                    } else {
                        if (laserTimer <= 0) {
                            lives--;
                            if (lives > 0) {
                                player.position.x -= 50;
                                player.velocity.y = 300f;
                            } else {
                                isGameOver = true;
                            }
                        }
                    }
                }
            }

            boolean canOpenFlag = (collectedCoins >= totalCoins);

            if (playerBox.overlaps(flag.getBoundingBox())) {
                if (canOpenFlag) {
                    flag.isReached = true;
                } else {
                    showCoinWarning = true;
                    warningTimer = 0f;
                }
            }

            flag.update(delta, canOpenFlag);

            if (canOpenFlag && flag.isReached) {
                if (currentRound < 3) {
                    currentRound++;
                    initLevel();
                } else {
                    isGameCleared = true;
                    if (bgmMusic != null) bgmMusic.stop();
                    if (finishMusic != null) finishMusic.play();
                }
            }
        }

        public void onPlayerJump() { player.jump(); }
        public void onPlayerLeft() { player.moveLeft(Gdx.graphics.getDeltaTime()); }
        public void onPlayerRight() { player.moveRight(Gdx.graphics.getDeltaTime()); }

        public gameCharacter getPlayer() { return player; }

        public void drawPlatforms(ShapeRenderer shape) {
            drawModernFloor(shape, 0, 900);
            drawModernFloor(shape, 1300, 500);
            drawModernFloor(shape, 2200, 500);
            drawModernFloor(shape, 3000, 1000);

            for (Platform p : platforms) {
                shape.setColor(0.08f, 0.15f, 0.1f, 0.6f);
                shape.rect(p.bounds.x, p.bounds.y - 6, p.bounds.width, 6);
                shape.setColor(0.35f, 0.55f, 0.4f, 1f);
                shape.rect(p.bounds.x, p.bounds.y, p.bounds.width, p.bounds.height);
                shape.setColor(0.5f, 0.9f, 0.6f, 1f);
                shape.rect(p.bounds.x, p.bounds.y + p.bounds.height - 5, p.bounds.width, 5);
            }
        }

        private void drawModernFloor(ShapeRenderer shape, float startX, float width) {
            shape.setColor(0.05f, 0.12f, 0.08f, 0.8f);
            shape.rect(startX, 0, width, FLOOR_LEVEL);
            shape.setColor(0.18f, 0.42f, 0.28f, 1f);
            shape.rect(startX, 4, width, FLOOR_LEVEL - 4);
            shape.setColor(0.25f, 0.85f, 0.5f, 1f);
            shape.rect(startX, FLOOR_LEVEL - 6, width, 6);
        }

        public void drawGearSecondAura(ShapeRenderer shape) {
            float cx = player.position.x + 36;
            float cy = player.position.y + 36;

            for (int i = 0; i < 6; i++) {
                float angle = auraAnimTime * 4f + (i * 60f);
                float radius = 40f + (float)Math.sin(auraAnimTime * 5f + i) * 8f;
                float px = cx + (float)Math.cos(angle) * radius;
                float py = cy + (float)Math.sin(angle) * radius;

                shape.setColor(0.1f, 0.6f + (i * 0.05f), 1f, 0.7f);
                shape.circle(px, py, 10f + (i % 3));
            }
        }

        public void drawSprites(SpriteBatch batch) {
            flag.draw(batch);
            player.draw(batch);
            for (Coin c : coins) c.draw(batch);
            for (Star s : stars) s.draw(batch);
            for (Enemy e : enemies) e.draw(batch);
        }

        public void drawAttacks(SpriteBatch batch) {
            for (AttackProjectile atk : attacks) {
                atk.draw(batch);
            }
        }
    }
}
