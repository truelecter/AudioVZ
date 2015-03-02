package truelecter.iig.screen.visual;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CloudManager {

	private static final String CLOUD_TEXTURE_PATH = "res/gui/menu/";
	private static final int CLOUD_NUMBER = 10;
	private final Texture background;
	private final List<Cloud> clouds = new LinkedList<Cloud>();
	private final Random gen;
	private Texture[] cloudTextures;

	public CloudManager() {
		this.gen = new Random();
		this.background = new Texture(CLOUD_TEXTURE_PATH + "background.png");
		initCloudTextures();
		initCloudPositions();
	}

	private void initCloudTextures() {
		cloudTextures = new Texture[CLOUD_NUMBER];
		for (int i = 0; i < CLOUD_NUMBER; i++) {
			cloudTextures[i] = new Texture(CLOUD_TEXTURE_PATH + "cloud-" + (i + 1) + ".png");
		}
	}

	private void initCloudPositions() {
		for (int i = 0; i < CLOUD_NUMBER; i++) {
			clouds.add(randomCloud());
		}
	}

	private Cloud randomCloud() {
		Cloud cloud = new Cloud();
		cloud.x = -500 + gen.nextInt(ConfigHandler.width + 500);
		cloud.y = gen.nextInt(ConfigHandler.height);
		cloud.speedDif = gen.nextFloat() / 200;
		if (gen.nextBoolean()) {
			cloud.speedDif = -cloud.speedDif;
		}
		cloud.img = getRandomCloudTexture();
		return cloud;
	}

	public void render(SpriteBatch g) {
		g.draw(background, 0, 0);
		for (Cloud cloud : clouds) {
			g.draw(cloud.img, cloud.x, cloud.y);
		}
	}

	public void update(float delta) {
		double cloudStep = 0.01;
		for (Cloud cloud : clouds) {
			cloud.x += (cloudStep + cloud.speedDif) * delta * 1000;
			if (cloud.x > ConfigHandler.width) {
				cloud.x = -200 - gen.nextInt(500);
				cloud.img = getRandomCloudTexture();
			}
		}
	}

	private Texture getRandomCloudTexture() {
		return cloudTextures[gen.nextInt(CLOUD_NUMBER)];
	}

	private class Cloud {
		public float x;
		public float y;
		public Texture img;
		public float speedDif;

	}
}