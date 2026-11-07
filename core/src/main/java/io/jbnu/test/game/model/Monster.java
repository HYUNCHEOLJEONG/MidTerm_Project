package io.jbnu.test.game.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Monster {
    public Vector2 position;
    public Sprite sprite;
    private float moveTimer = 0;

    public Monster(Texture texture, float startX, float startY) {
        this.position = new Vector2(startX, startY);
        this.sprite = new Sprite(texture);
        this.sprite.setSize(40, 40);
        this.sprite.setPosition(position.x, position.y);
    }

    public void update(float delta) {
        moveTimer += delta;
        position.x += (float) Math.sin(moveTimer * 3) * 1.5f;
        sprite.setPosition(position.x, position.y);
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }
}
