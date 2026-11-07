package io.jbnu.test.game.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Player {
    public Vector2 position;
    public Vector2 velocity;
    public Sprite sprite;
    public boolean isGrounded = false;

    public Player(Texture texture, float startX, float startY) {
        this.position = new Vector2(startX, startY);
        this.velocity = new Vector2(0, 0);
        this.sprite = new Sprite(texture);
        this.sprite.setSize(48, 48);
        this.sprite.setPosition(position.x, position.y);
    }

    public void jump(Sound sfx) {
        if (isGrounded) {
            velocity.y = 400f;
            isGrounded = false;
            sfx.play(1.0f);
        }
    }

    public void moveRight() {
        position.x += 200 * Gdx.graphics.getDeltaTime();
    }

    public void moveLeft() {
        position.x -= 200 * Gdx.graphics.getDeltaTime();
    }

    public void syncSpriteToPosition() {
        sprite.setPosition(position.x, position.y);
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }
}
