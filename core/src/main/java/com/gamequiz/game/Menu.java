package com.gamequiz.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class Menu {
    private Main game;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;
    private String botaoJogarText = "Jogar";
    private String botaoSairText = "Sair";
    private float botaoJogarX, botaoJogarY, botaoLargura, botaoAltura;
    private float botaoSairX, botaoSairY;
    private Texture background;

    private final float FONT_SCALE = 3f;

    public Menu(Main game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.layout = new GlyphLayout();
        this.background = new Texture("menubg.png");

        botaoLargura = Gdx.graphics.getWidth() * 0.3f;
        botaoAltura = Gdx.graphics.getHeight() * 0.1f;

        botaoJogarX = Gdx.graphics.getWidth() / 2f - botaoLargura / 2;
        botaoJogarY = Gdx.graphics.getHeight() / 2f + botaoAltura;

        botaoSairX = Gdx.graphics.getWidth() / 2f - botaoLargura / 2;
        botaoSairY = Gdx.graphics.getHeight() / 2f - botaoAltura;

        font.getData().setScale(FONT_SCALE);
    }

    public void render() {
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Calcula e desenha o texto do botao jogar
        layout.setText(font, botaoJogarText);
        float botaoJogarTextX = botaoJogarX + (botaoLargura - layout.width) / 2;
        float botaoJogarTextY = botaoJogarY + (botaoAltura + layout.height) / 2;
        font.draw(batch, botaoJogarText, botaoJogarTextX, botaoJogarTextY);

        // Calcula e desenha o texto do botao sair
        layout.setText(font, botaoSairText);
        float botaoSairTextX = botaoSairX + (botaoLargura - layout.width) / 2;
        float botaoSairTextY = botaoSairY + (botaoAltura + layout.height) / 2;
        font.draw(batch, botaoSairText, botaoSairTextX, botaoSairTextY);

        batch.end();

        // Detecta cliques nos botoes
        if (game.estadoAtual == Main.GameState.MENU) {
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                float mouseX = Gdx.input.getX();
                float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

                // Verifica o clique no botão jogar
                if (mouseX >= botaoJogarX && mouseX <= botaoJogarX + botaoLargura &&
                    mouseY >= botaoJogarY && mouseY <= botaoJogarY + botaoAltura) {
                    game.startGame();
                }

                // verifica o clique no botão sair
                if (mouseX >= botaoSairX && mouseX <= botaoSairX + botaoLargura &&
                    mouseY >= botaoSairY && mouseY <= botaoSairY + botaoAltura) {
                    Gdx.app.exit();
                }
            }
        }
    }

    // Libera os recursos
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
