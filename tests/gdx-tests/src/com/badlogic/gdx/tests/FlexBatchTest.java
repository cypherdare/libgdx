package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.batch.CompliantQuadBatch;
import com.badlogic.gdx.graphics.batch.FlexBatch;
import com.badlogic.gdx.graphics.batch.batchable.Quad2D;
import com.badlogic.gdx.graphics.batch.batchable.Quad3D;
import com.badlogic.gdx.graphics.batch.utils.BatchablePreparation;
import com.badlogic.gdx.graphics.batch.utils.BatchableSorter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import static com.badlogic.gdx.math.MathUtils.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class FlexBatchTest extends GdxTest {
	Texture texture, egg, wheel;
	SpriteBatch spriteBatch;
	CompliantQuadBatch<DoubleTexQuad> quad2dBatch;
	FlexBatch<SolidQuad>	solidQuadBatch;
	FlexBatch<Quad3D> quad3dBatch;
	BatchableSorter<Quad3D> quad3dSorter;
	PerspectiveCamera pCam;
	ShaderProgram solidShader, typicalShader;
	Stage stage;
	Skin skin;
	Viewport viewport;
	TextureAtlas atlas;
	Test test = Test.values()[0];
	Array<DoubleTexQuad> quad2ds = new Array<DoubleTexQuad>();
	Array<Quad3D> quad3ds = new Array<Quad3D>();
	Sprite testSprite;
	BitmapFont testFont;
	final ObjectSet<Disposable> disposables = new ObjectSet<Disposable>();
	private static final int W = 800, H = 480;
	float elapsed;
	
	private enum Test {
		Quad3D, CompliantQuadBatch, SolidQuads
	}
	
	public static class SolidQuad extends Quad2D {
		protected int getNumberOfTextures (){
			return 0;
		}
	}
	
	public static class DoubleTexQuad extends Quad2D {
		protected int getNumberOfTextures (){
			return 2;
		}
	}
	
	@Override
	public void create () {
		viewport = new ExtendViewport(W, H);
		
		texture = new Texture("data/badlogic.jpg");
		disposables.add(texture);
		atlas = new TextureAtlas(Gdx.files.internal("data/pack")); 
		disposables.add(atlas);
		
		testSprite = new Sprite(texture);
		testSprite.setPosition(50, 102);
		testSprite.setColor(0, 1, 0, 0.6f);
		
		testFont = new BitmapFont(Gdx.files.internal("data/arial-32-pad.fnt"), false);
		testFont.setColor(Color.CYAN);
		
		spriteBatch = new SpriteBatch(100);
		spriteBatch.enableBlending();
		disposables.add(spriteBatch);
		
		quad2dBatch = new CompliantQuadBatch<DoubleTexQuad>(DoubleTexQuad.class, 4000);
		quad2dBatch.enableBlending();
		disposables.add(quad2dBatch);
		for (int i=0; i<80; i++){
			DoubleTexQuad sprite = new DoubleTexQuad();
			sprite.texture(texture).color(random(), 1, 1, random(0.5f, 1f)).position(random(W), random(H)).rotation(random(360)).size(random(50, 100), random(50, 100));
			quad2ds.add(sprite);
		}
		
		solidQuadBatch = new FlexBatch<SolidQuad>(SolidQuad.class, 10, 20);
		disposables.add(solidQuadBatch);
		solidShader = new ShaderProgram(BatchablePreparation.generateGenericVertexShader(0), BatchablePreparation.generateGenericFragmentShader(0));
		disposables.add(solidShader);
		solidQuadBatch.setShader(solidShader);
		
		egg = new Texture(Gdx.files.internal("data/egg.png"));
		egg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		disposables.add(egg);
		wheel = new Texture(Gdx.files.internal("data/wheel.png"));
		wheel.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		disposables.add(wheel);
		
		typicalShader = new ShaderProgram(BatchablePreparation.generateGenericVertexShader(1), BatchablePreparation.generateGenericFragmentShader(1));
		disposables.add(typicalShader);
		pCam = new PerspectiveCamera();
		pCam.near = 0.1f;
		pCam.far = 10000f;
		pCam.position.set(0, 20, -20);
		pCam.lookAt(0, 0, 0);
		pCam.update();
		quad3dBatch = new FlexBatch<Quad3D>(Quad3D.class, 4000, 8000);
		quad3dBatch.setShader(typicalShader);
		disposables.add(quad3dBatch);
		quad3dSorter = new BatchableSorter<Quad3D>(pCam);
		for (int i = 0; i < 500; i++) {
			quad3ds.add(makeQuad3D(10, 40));
		}
		
		setupUI();
		
	}
	
	public void resize (int width, int height){
		viewport.update(width, height, true);
		stage.getViewport().update(width, height, true);
		pCam.viewportWidth = width;
		pCam.viewportHeight = height;
		pCam.update();
	}
	
	public void render (){
		elapsed += Gdx.graphics.getDeltaTime();
		
		Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		viewport.apply();
		
		switch (test){
		case Quad3D:
			pCam.position.rotate(Vector3.Z, Gdx.graphics.getDeltaTime() * 90f);
			pCam.lookAt(0, 0, 0);
			pCam.update();
			for (Quad3D quad : quad3ds){
				quad.billboard(pCam);
				quad3dSorter.add(quad);
			}
			quad3dBatch.setProjectionMatrix(pCam.combined);
			quad3dBatch.begin();
			//quad3dBatch.draw().texture(texture).size(10, 10).billboard(pCam);
			quad3dSorter.flush(quad3dBatch);
			quad3dBatch.end();
			
			break;
		case CompliantQuadBatch:{
			quad2dBatch.setProjectionMatrix(viewport.getCamera().combined);
			quad2dBatch.begin();
			quad2dBatch.draw().texture(wheel).color(0, 0.5f, 1, 1).size(100, 100).rotation(45);
			for (DoubleTexQuad sprite : quad2ds){
				sprite.rotation += Gdx.graphics.getDeltaTime() * 30;
				quad2dBatch.draw(sprite);
			}
			testFont.draw(quad2dBatch, "BitmapFont", 50, 100);
			testSprite.draw(quad2dBatch);
			quad2dBatch.end();
			
//			spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
//			spriteBatch.begin();
//			testFont.draw(spriteBatch, "Windows", 50, 100);
//			spriteBatch.end();
			
			break;
		}
		case SolidQuads:
			solidQuadBatch.setProjectionMatrix(viewport.getCamera().combined);
			solidQuadBatch.begin();
			solidQuadBatch.draw().color(0, 0.5f, 1, 1).size(100, 100).position(600, 100).rotation(45);
			solidQuadBatch.draw().size(50, 50).color(Color.BLUE).position(30, 30).rotation(30);
			solidQuadBatch.draw().size(20, 70).color(Color.MAGENTA).position(400, 430).origin(10, 35).rotation(-elapsed * 45);
			solidQuadBatch.end();
		}
		
		stage.act();
		stage.draw();
	}
	
	private void setupUI () {
		stage = new Stage(new ScreenViewport(), quad2dBatch);
		disposables.add(stage);
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		disposables.add(skin);

		final SelectBox<Test> selectBox = new SelectBox<Test>(skin);
		selectBox.setItems(Test.values());
		selectBox.addListener(new ChangeListener(){
			public void changed (ChangeEvent event, Actor actor) {
				test = selectBox.getSelected();
				elapsed = 0;
			}});
		Table table = new Table();
		table.setFillParent(true);
		table.defaults().padTop(5).left();
		table.top().left().padLeft(5);
		table.add(selectBox).row();
		table.add(new Label("", skin){
			int fps = -1;
			public void act (float delta){
				super.act(delta);
				if (Gdx.graphics.getFramesPerSecond() != fps){
					fps = Gdx.graphics.getFramesPerSecond();
					setText(""+fps);
				}
			}
		}).row();
		stage.addActor(table);
		Gdx.input.setInputProcessor(stage);
	}
	
	public void dispose (){
		for (Disposable disposable : disposables) disposable.dispose();
	}
	
	int idx; static Vector3 tmp = new Vector3();
	private Quad3D makeQuad3D (float radius, float height) {
		Quad3D quad = new Quad3D();
		if (idx % 2 == 0){
			quad.texture(egg).blend();
		} else {
			quad.texture(wheel).opaque();
		}
		tmp.set(radius * MathUtils.random(), 0, 0).rotate(Vector3.Y, MathUtils.random() * 360f).add(0, (MathUtils.random() - 0.5f) * height, 0);
		quad.position(tmp).size(1, 1);
		idx++;
		return quad;
	}
}
