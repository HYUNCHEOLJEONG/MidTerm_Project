package io.jbnu.test.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import io.jbnu.test.game.MainGame;

public class GameScreen extends ScreenAdapter {
    private final MainGame game;
    private SpriteBatch batch;

    // 배경 (라운드 1, 2)
    private Texture bg1, bg2;
    private int currentRound = 1;

    // 캐릭터 및 오브젝트 텍스처
    private Texture playerTex;
    private Texture monsterIdleTex, monsterMoveTex, monsterDeadTex;
    private Texture flagTex;
    private Texture blockTex; // 블록 텍스처 (없을 경우 임시 사각형으로 대체 가능하지만 변수 유지)

    // 플레이어 좌표 및 물리
    private float playerX = 100;
    private float playerY = 150;
    private float playerVelocityY = 0;
    private boolean isGrounded = false;
    private final float GRAVITY = -800f;
    private final float JUMP_FORCE = 400f;
    private final float MOVE_SPEED = 200f;
    private final float FLOOR_LEVEL = 100f;

    // 몬스터 상태 및 좌표
    private float monsterX = 600;
    private float monsterY = 100;
    private boolean isMonsterAlive = true;

    // 깃발 좌표 (클리어 지점)
    private float flagX = 800;
    private float flagY = 100;

    // 충돌 판정을 위한 Rectangle 객체들
    private Rectangle playerRect;
    private Rectangle monsterRect;
    private Rectangle flagRect;

    public GameScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        // 에셋 로드
        bg1 = new Texture("Round1_Background.png");
        bg2 = new Texture("Round2_Background.png");

        playerTex = new Texture("t.png");
        monsterIdleTex = new Texture("Monster.png");
        monsterMoveTex = new Texture("Monster_move.png");
        monsterDeadTex = new Texture("Monster_dead.png");
        flagTex = new Texture("flagRed1.png"); // 깃발 기본 이미지

        playerRect = new Rectangle();
        monsterRect = new Rectangle();
        flagRect = new Rectangle();
    }

    @Override
    public void render(float delta) {
        handleInput(delta);
        update(delta);

        ScreenUtils.clear(0, 0, 0, 1);

        batch.begin();

        // 배경 렌더링 (라운드에 따라 전환)
        Texture currentBg = (currentRound == 1) ? bg1 : bg2;
        batch.draw(currentBg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // 바닥 및 지형 블록 예시 렌더링 (코드 기반 연속 배치)
        // 화면 하단 바닥을 블록 형태로 반복해서 깔아줄 수 있습니다.
        for (int i = 0; i < Gdx.graphics.getWidth(); i += 64) {
            // 필요시 블록 이미지를 여기에 그릴 수 있습니다.
        }

        // 깃발 렌더링
        batch.draw(flagTex, flagX, flagY, 48, 64);

        // 몬스터 렌더링 (살아있으면 이동 텍스처, 죽으면 dead 텍스처)
        Texture currentMonsterTex = isMonsterAlive ? monsterMoveTex : monsterDeadTex;
        batch.draw(currentMonsterTex, monsterX, monsterY, 64, 64);

        // 플레이어 렌더링
        batch.draw(playerTex, playerX, playerY, 64, 64);

        batch.end();
    }

    private void handleInput(float delta) {
        // 좌우 이동
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            playerX -= MOVE_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            playerX += MOVE_SPEED * delta;
        }
        // 점프
        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) && isGrounded) {
            playerVelocityY = JUMP_FORCE;
            isGrounded = false;
        }
    }

    private void update(float delta) {
        // 중력 적용
        playerVelocityY += GRAVITY * delta;
        playerY += playerVelocityY * delta;

        // 바닥 충돌 처리
        if (playerY <= FLOOR_LEVEL) {
            playerY = FLOOR_LEVEL;
            playerVelocityY = 0;
            isGrounded = true;
        }

        // 몬스터가 플레이어를 향해 이동하는 로직 (AI)
        if (isMonsterAlive) {
            if (monsterX > playerX) {
                monsterX -= 50f * delta; // 왼쪽으로 이동하며 플레이어 추적
            } else if (monsterX < playerX) {
                monsterX += 50f * delta; // 오른쪽으로 이동하며 플레이어 추적
            }
        }

        // 바운딩 박스(Rect) 업데이트
        playerRect.set(playerX, playerY, 64, 64);
        monsterRect.set(monsterX, monsterY, 64, 64);
        flagRect.set(flagX, flagY, 48, 64);

        // 플레이어와 몬스터 충돌 체크
        if (isMonsterAlive && playerRect.overlaps(monsterRect)) {
            // 플레이어가 위에서 아래로 떨어지면서 몬스터를 밟았을 때 (밟기 성공)
            if (playerVelocityY < 0 && playerY > monsterY + 30) {
                isMonsterAlive = false; // 몬스터 사망 처리 (Monster_dead.png로 변경됨)
                playerVelocityY = 300f; // 밟고 다시 살짝 튀어오름
            } else {
                // 그냥 부딪힌 경우 피격 처리 (원하는 대로 구현 가능)
            }
        }

        // 깃발에 도달했을 때 (Background2로 전환)
        if (playerRect.overlaps(flagRect)) {
            currentRound = 2; // 라운드 2 배경으로 전환
            // 다음 라운드 시작 위치로 리셋
            playerX = 100;
            playerY = FLOOR_LEVEL;
            flagX = -1000; // 깃발 안 보이게 치우기
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        bg1.dispose();
        bg2.dispose();
        playerTex.dispose();
        monsterIdleTex.dispose();
        monsterMoveTex.dispose();
        monsterDeadTex.dispose();
        flagTex.dispose();
    }
}
