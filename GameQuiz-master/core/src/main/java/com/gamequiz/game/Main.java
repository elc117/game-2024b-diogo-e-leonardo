package com.gamequiz.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import java.util.ArrayList;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.audio.Sound;

public class Main extends ApplicationAdapter {
    private enum GameState { JOGANDO, MENU }
    private GameState estadoAtual;

    private SpriteBatch batch;
    private Texture image, personagem, inimigoTexture, balaTexture;
    private Texture[] sequenciaImagens; // 10 perguntas
    private Texture[] inimigoTextures; // 20 imagens das respostas
    private int indiceImagem; // Índice da imagem atual das perguntas
    private Sprite perso;
    private ArrayList<Sprite> inimigos;
    private ArrayList<Sprite> laser;
    private float posX, posY;
    private float tempoPassado; // Controla o tempo para gerar as respostas
    private float tempoUltimoTiro; // Controla o tempo entre os lasers
    private int acertos;
    private BitmapFont font;
    private Texture personagemAtirando;
    private boolean estaAtirando;
    private float tempoAtirando; // Controla o tempo que a textura de tiro aparece
    private float tempoUltimaResposta; // Controla o tempo desde a última colisão
    private boolean podeResponder; // Indica se a colisão está permitida
    private Sound somAcerto;
    private Sound somTiro; // Nenhum som funcionou aqui

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("bg.png");
        personagem = new Texture("personagem.png");
        personagemAtirando = new Texture("personagemAtirando.png"); // Carregue a textura do personagem atirando
        balaTexture = new Texture("bala.png");
        sequenciaImagens = new Texture[10];
        inimigoTextures = new Texture[20];
        somAcerto = Gdx.audio.newSound(Gdx.files.internal("acerto.mp3"));
        somTiro = Gdx.audio.newSound(Gdx.files.internal("tiro.mp3"));


        // Carregar imagens para os inimigos (20 diferentes)
        for (int i = 0; i < 20; i++) {
            inimigoTextures[i] = new Texture("inimigo" + (i + 1) + ".png"); // Pega a textura dos 20 inimigos
        }

        // Carregar imagens para as perguntas
        for (int i = 0; i < sequenciaImagens.length; i++) {
            sequenciaImagens[i] = new Texture("pergunta" + (i + 1) + ".png");
        }

        indiceImagem = 0;
        perso = new Sprite(personagem);
        inimigos = new ArrayList<>();
        laser = new ArrayList<>();
        posX = 0;
        posY = 275;
        tempoPassado = 0;
        tempoUltimoTiro = 0;
        estaAtirando = false;
        tempoAtirando = 0;
        acertos = 0;

        gerarInimigos();

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        tempoUltimaResposta = 15; // Poder responder no início
        podeResponder = true;

        estadoAtual = GameState.JOGANDO; // Começa jogando
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();

        if (estadoAtual == GameState.JOGANDO) {
            atualizarJogo(deltaTime);
        } else if (estadoAtual == GameState.MENU) {
            exibirMenu();
        }

        batch.end();
    }

    private void atualizarJogo(float deltaTime) {
        tempoPassado += deltaTime;
        tempoUltimoTiro += deltaTime;

        if (!podeResponder) {
            tempoUltimaResposta += deltaTime;
            if (tempoUltimaResposta >= 15) {
                podeResponder = true;
            }
        }

        if (estaAtirando) {
            tempoAtirando += deltaTime;
            if (tempoAtirando > 0) {
                estaAtirando = false;
                tempoAtirando = 0;
            }
        }

        // Timer pra atualizar as perguntas
        if (tempoPassado >= 15) {
            gerarInimigos();
            tempoPassado = 0;
        }

        // Mover o personagem
        posX = Gdx.input.getX() - personagem.getWidth() / 2;
        posY = Gdx.graphics.getHeight() - Gdx.input.getY() - personagem.getHeight() / 2;

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && tempoUltimoTiro >= 0) {
            atirar();
            estaAtirando = true;
            tempoUltimoTiro = 0;
        }

        verificarColisao();

        // Desenha o fundo
        batch.draw(image, 0, 0);

        if (indiceImagem >= sequenciaImagens.length) {
            estadoAtual = GameState.MENU;
            return;
        }

        if (estaAtirando) {
            batch.draw(personagemAtirando, posX, posY);
        } else {
            batch.draw(personagem, posX, posY);
        }

        for (Sprite inimigo : inimigos) {
            inimigo.setX(inimigo.getX() - 150 * deltaTime);
            batch.draw(inimigo, inimigo.getX(), inimigo.getY());
        }

        for (int i = 0; i < laser.size(); i++) {
            Sprite bala = laser.get(i);
            bala.setX(bala.getX() + 500 * deltaTime);
            if (bala.getX() > Gdx.graphics.getWidth()) {
                laser.remove(i);
                i--;
            } else {
                batch.draw(bala, bala.getX(), bala.getY());
            }
        }

        if (indiceImagem > 0 && indiceImagem <= sequenciaImagens.length) {
            Texture imagemAtual = sequenciaImagens[indiceImagem - 1];
            batch.draw(imagemAtual, 0, 0);
        }

        font.draw(batch, "Acertos: " + acertos, 10, Gdx.graphics.getHeight() - 10);
    }

    private void exibirMenu() {
        // Fundo para o menu
        batch.draw(image, 0, 0);

        font.getData().setScale(2);
        font.draw(batch, "Jogo Finalizado!", Gdx.graphics.getWidth() / 2 - 100, Gdx.graphics.getHeight() / 2 + 100);
        font.draw(batch, "Acertos: " + acertos, Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() / 2 + 50);
        font.draw(batch, "Pressione R para reiniciar", Gdx.graphics.getWidth() / 2 - 120, Gdx.graphics.getHeight() / 2 - 50);
        font.draw(batch, "Pressione Q para sair", Gdx.graphics.getWidth() / 2 - 100, Gdx.graphics.getHeight() / 2 - 100);
        font.setColor(1, 0, 0, 1);


        // Verifica se o jogador pressionou R ou Q
        if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            reiniciarJogo();
        } else if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            Gdx.app.exit();
        }
    }


    private void reiniciarJogo() {
        indiceImagem = 0;
        acertos = 0;
        inimigos.clear();
        laser.clear();
        gerarInimigos();
        estadoAtual = GameState.JOGANDO;
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
        personagem.dispose();
        personagemAtirando.dispose();
        balaTexture.dispose();
        for (Texture textura : sequenciaImagens) {
            textura.dispose();
        }
        for (Texture textura : inimigoTextures) {
            textura.dispose();
        }
        font.dispose();
    }

    private void moverPersonagem() {
        if (Gdx.input.isKeyPressed(Input.Keys.D) && posX < Gdx.graphics.getWidth() - personagem.getWidth()) {
            posX += 200 * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) && posX > 0) {
            posX -= 200 * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W) && posY < Gdx.graphics.getHeight() - personagem.getHeight()) {
            posY += 200 * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) && posY > 0) {
            posY -= 200 * Gdx.graphics.getDeltaTime();
        }
    }

    private void gerarInimigos() {
        float posXInimigo1 = 1400;
        float posYInimigo1 = 200;
        float posXInimigo2 = 1400;
        float posYInimigo2 = 500;

        for (int i = 0; i < 2; i++) {
            Texture inimigoTexture = inimigoTextures[indiceImagem * 2 + i];
            Sprite inimigo = new Sprite(inimigoTexture);

            if (i == 0) {
                inimigo.setPosition(posXInimigo1, posYInimigo1);
            } else {
                inimigo.setPosition(posXInimigo2, posYInimigo2);
            }

            inimigos.add(inimigo);
        }

        indiceImagem++;
        podeResponder = true;
    }

    private void atirar() {
        Sprite bala = new Sprite(balaTexture);
        bala.setPosition(posX + personagem.getWidth() - 75, posY + personagem.getHeight() - 9 - bala.getHeight());
        laser.add(bala);
        somTiro.play(1.0f);
    }

    private boolean verificarColisao(Sprite inimigo, Sprite bala) {
        return inimigo.getBoundingRectangle().overlaps(bala.getBoundingRectangle());
    }

    private void verificarColisao() {
        for (int i = 0; i < inimigos.size(); i++) {
            Sprite inimigo = inimigos.get(i);

            for (int j = 0; j < laser.size(); j++) {
                Sprite bala = laser.get(j);

                if (verificarColisao(inimigo, bala) && podeResponder) {
                    Texture texturaInimigo = inimigo.getTexture();

                    // Verifica se a textura corresponde é a resposta certa
                    for (int k : new int[]{0, 3, 4, 7, 8, 10, 12, 15, 16}) { // Os indices começam em zero
                        if (texturaInimigo == inimigoTextures[k]) {
                            acertos++;
                            somAcerto.play(1.0f); // Era pra estar tocando o som de acerto
                            break;
                        } // Essa parte do código foi feita com auxílio do ChatGPT
                    }

                    inimigos.remove(i);
                    laser.remove(j);
                    podeResponder = false;
                    tempoUltimaResposta = 0;
                    return;
                }
            }
        }
    }


}
