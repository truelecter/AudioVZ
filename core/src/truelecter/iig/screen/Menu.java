package truelecter.iig.screen;

import truelecter.iig.Main;
import truelecter.iig.screen.visual.CloudManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Menu implements Screen {

	private SpriteBatch batch;
	private CloudManager cm;
	private Sprite b;
	private Sprite r;

	private float angle = 0;

	public Menu() {
		batch = new SpriteBatch();
		cm = new CloudManager();
		Texture t = new Texture(Gdx.files.internal("data/colors-borders.png"));
		b = new Sprite(t, 0, 0, 16, 5);
		b.setPosition(Main.width / 2, Main.height / 2 + 30);
		b.setSize(16, 5);
		r = new Sprite(t, 0, 5, 16, 5);
		r.setPosition(Main.width / 2, Main.height / 2 + 50);
		r.setSize(16, 5);
	}

	@Override
	public void show() {

	}

	@Override
	public void render(float delta) {
		angle += 50 * delta;
		if (angle > 360)
			angle -= 360;
		b.setOriginCenter();
		b.setRotation(angle);
		r.setOrigin(0, -50);
		r.setRotation(angle);
		cm.update(Gdx.graphics.getDeltaTime());
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		// cm.render(batch);
		b.draw(batch);
		r.draw(batch);
		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		Main.width = width;
		Main.height = height;
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {

	}

}
