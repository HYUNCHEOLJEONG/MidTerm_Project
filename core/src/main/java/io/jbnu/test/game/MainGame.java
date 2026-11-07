package io.jbnu.test.game;

import com.badlogic.gdx.Game;
import io.jbnu.test.game.screen.GameScreen;

public class MainGame extends Game {
    public MainGame() {
    }

    @Override
    public void create() {
        setScreen(new GameScreen(this));
    }
}
