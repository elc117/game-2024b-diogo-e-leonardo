package com.gamequiz.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class MenuFinal {
    private Main game;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;
    private String botaoReiniciarText = "Reiniciar Jogo";
    private String botaoSairText = "Sair";
    private float botaoReiniciarX, botaoReiniciarY, botaoLargura, botaoAltura;
    private float botaoSairX, botaoSairY;
    private Texture background;

    private final float FONT_SCALE = 3f;

    public MenuFinal(Main game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.layout = new GlyphLayout();
        this.background = new Texture("menubg.png");

        botaoLargura = Gdx.graphics.getWidth() * 0.3f;
        botaoAltura = Gdx.graphics.getHeight() * 0.1f;

        botaoReiniciarX = Gdx.graphics.getWidth() / 2f - botaoLargura / 2;
        botaoReiniciarY = Gdx.graphics.getHeight() / 2f;

        botaoSairX = Gdx.graphics.getWidth() / 2f - botaoLargura / 2;
        botaoSairY = Gdx.graphics.getHeight() / 2f - botaoAltura;

        font.getData().setScale(FONT_SCALE);
    }

    public void render() {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Desenha o botão reiniciar
        layout.setText(font, botaoReiniciarText);
        float botaoReiniciarTextX = botaoReiniciarX + (botaoLargura - layout.width) / 2;
        float botaoReiniciarTextY = botaoReiniciarY + (botaoAltura + layout.height) / 2;
        font.draw(batch, botaoReiniciarText, botaoReiniciarTextX, botaoReiniciarTextY);

        // Desenha o botão sair
        layout.setText(font, botaoSairText);
        float botaoSairTextX = botaoSairX + (botaoLargura - layout.width) / 2;
        float botaoSairTextY = botaoSairY + (botaoAltura + layout.height) / 2;
        font.draw(batch, botaoSairText, botaoSairTextX, botaoSairTextY);

        // Desenha a pontuação
        String scoreText = "Respostas certas: " + game.getAcertos();
        layout.setText(font, scoreText);
        float scoreTextX = Gdx.graphics.getWidth() / 2f - layout.width / 2;
        float scoreTextY = Gdx.graphics.getHeight() / 2f + 100;
        font.draw(batch, scoreText, scoreTextX, scoreTextY);

        batch.end();

        // Detecta cliques nos botões
        if (game.estadoAtual == Main.GameState.MENUFINAL) {
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                float mouseX = Gdx.input.getX();
                float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

                // Verifica o clique no botão reiniciar
                if (mouseX >= botaoReiniciarX && mouseX <= botaoReiniciarX + botaoLargura &&
                    mouseY >= botaoReiniciarY && mouseY <= botaoReiniciarY + botaoAltura) {
                    game.startGame(); // Reinicia o jogo
                }

                // Verifica o clique no botão sair
                if (mouseX >= botaoSairX && mouseX <= botaoSairX + botaoLargura &&
                    mouseY >= botaoSairY && mouseY <= botaoSairY + botaoAltura) {
                    Gdx.app.exit(); // Fecha o jogo
                }
            }
        }
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
        background.dispose(); // Libera os recursos
    }
}
