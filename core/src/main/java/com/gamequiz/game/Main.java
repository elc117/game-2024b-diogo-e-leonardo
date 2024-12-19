package com.gamequiz.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import java.util.ArrayList;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.audio.Sound;

public class Main extends ApplicationAdapter {
    public enum GameState { JOGANDO, MENU, MENUFINAL }
    public GameState estadoAtual;
    private Menu menu;
    private MenuFinal menuFinal;
    private Music somDeFundo;
    private SpriteBatch batch;
    private Texture image, personagem, balaTexture;
    private Texture[] sequenciaImagens; // 10 perguntas
    private Texture[] inimigoTextures; // 20 imagens das respostas
    private int indiceImagem; // Índice da imagem atual das perguntas
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
    private Music somAcerto;
    private Music somTiro; //
    private int[] dificuldade;
    private int tempoInimigos = 12;

    @Override
    public void create() {
        batch = new SpriteBatch();
        estadoAtual = GameState.MENU;
        menu = new Menu(this);
        menuFinal = new MenuFinal(this);
        image = new Texture("bg.png");
        personagem = new Texture("personagem.png");
        personagemAtirando = new Texture("personagemAtirando.png"); // Carregue a textura do personagem atirando
        balaTexture = new Texture("bala.png");
        sequenciaImagens = new Texture[10];
        inimigoTextures = new Texture[20];
        somAcerto = Gdx.audio.newMusic(Gdx.files.internal("acerto.mp3"));
        somTiro = Gdx.audio.newMusic(Gdx.files.internal("tiro.mp3"));
        somDeFundo = Gdx.audio.newMusic(Gdx.files.internal("audiofundo.mp3"));
        dificuldade = new int[]{190, 240, 275};
        somDeFundo.setLooping(true);
        somDeFundo.setVolume(0.5f);

        somDeFundo.play();

        // Carregar imagens para os inimigos (20 diferentes)
        for (int i = 0; i < 20; i++) {
            inimigoTextures[i] = new Texture("inimigo" + (i + 1) + ".png"); // Pega a textura dos 20 inimigos
        }

        // Carregar imagens para as perguntas
        for (int i = 0; i < sequenciaImagens.length; i++) {
            sequenciaImagens[i] = new Texture("pergunta" + (i + 1) + ".png");
        }

        indiceImagem = 0;
        inimigos = new ArrayList<>();
        laser = new ArrayList<>();
        posX = 0;
        posY = 275;
        tempoPassado = 0;
        tempoUltimoTiro = -2; // Estava bugando quando clicava em jogar no menu
        estaAtirando = false;
        tempoAtirando = 0;
        acertos = 0;

        gerarInimigos();

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        tempoUltimaResposta = tempoInimigos; // Poder responder no início
        podeResponder = true;
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        batch.begin();

        if (estadoAtual == GameState.JOGANDO) {
            atualizarJogo(deltaTime);
        }
        if (estadoAtual == GameState.MENU) {
            menu.render();
        }
        if (estadoAtual == GameState.MENUFINAL) {
            menuFinal.render();
        }

        batch.end();
    }

    public void startGame() {
        estadoAtual = GameState.JOGANDO;
        reiniciarJogo();
    }

    private void atualizarJogo(float deltaTime) {
        tempoPassado += deltaTime;
        tempoUltimoTiro += deltaTime;

        if (!podeResponder) {
            tempoUltimaResposta += deltaTime;
            if (tempoUltimaResposta >= tempoInimigos) {
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
        if (tempoPassado >= tempoInimigos) {
            gerarInimigos();
            tempoPassado = 0;
        }

        // Mover o personagem
        posX = Gdx.input.getX() - personagem.getWidth() / 2;
        posY = Gdx.graphics.getHeight() - Gdx.input.getY() - personagem.getHeight() / 2;

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && tempoUltimoTiro >= 0) {
            atirar();
            estaAtirando = true;
            tempoUltimoTiro = 0; // Pra resolver o bug do menu
        }

        verificarColisao();

        // Desenha o fundo
        batch.draw(image, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (indiceImagem >= sequenciaImagens.length) {
            estadoAtual = GameState.MENUFINAL;
            return;
        }

        batch.draw(estaAtirando ? personagemAtirando : personagem, posX, posY);

        if(acertos < 3) {
            for (Sprite inimigo : inimigos) {
                inimigo.setX(inimigo.getX() - dificuldade[0] * deltaTime);
                batch.draw(inimigo, inimigo.getX(), inimigo.getY());
            }
        }

        if(acertos >= 3 && acertos < 5) {
            for (Sprite inimigo : inimigos) {
                inimigo.setX(inimigo.getX() - dificuldade[1] * deltaTime);
                batch.draw(inimigo, inimigo.getX(), inimigo.getY());
            }
        }

        if(acertos >= 5) { // aumenta velocidade dos inimigos conforme maior é o número de acertos
            for (Sprite inimigo : inimigos) {
                inimigo.setX(inimigo.getX() - (dificuldade[2] + (acertos - 5) * 5) * deltaTime);
                batch.draw(inimigo, inimigo.getX(), inimigo.getY());
            }
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
            batch.draw(imagemAtual, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        font.draw(batch, "Acertos: " + acertos, 10, Gdx.graphics.getHeight() - 10);
    }

    public void reiniciarJogo() {
        tempoPassado = 0;
        tempoUltimaResposta = tempoInimigos;
        tempoUltimoTiro = -2;
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
        somTiro.setVolume(0.1f);
        somTiro.play();
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
                            somAcerto.play(); // Era pra estar tocando o som de acerto
                            acertos++;
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

    public int getAcertos() {
        return acertos; // armazena o numero de acertos
    }
}
