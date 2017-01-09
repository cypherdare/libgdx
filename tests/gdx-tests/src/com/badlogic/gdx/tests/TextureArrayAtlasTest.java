/* Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests;

import java.util.Random;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureArrayAtlas;
import com.badlogic.gdx.graphics.g2d.TextureArrayRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import static com.badlogic.gdx.graphics.glutils.ShaderProgram.*;
import com.badlogic.gdx.math.Affine2;
import static com.badlogic.gdx.math.MathUtils.*;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** @author cypherdare */
public class TextureArrayAtlasTest extends GdxTest {
	TextureAtlas atlas;
	Array<AtlasRegion> regions;
	float time = 0;
	final Affine2 tmp = new Affine2();
	Viewport viewport;
	SpriteBatch spriteBatch;
	BitmapFont font;
	ShaderProgram customShader;

	public void create () {
		FileHandle packFile = Gdx.files.internal("data/pack3page");
		
		// Fall back to standard TextureAtlas if not using gl30
		atlas = Gdx.gl30 == null ? new TextureAtlas(packFile) : new TextureArrayAtlas(packFile);
		
		viewport = new ExtendViewport(640, 480);

		Gdx.gl.glClearColor(0.2f, 0.2f, 0.3f, 1);
		
		// Same random shuffle every time for comparison between gl20 and gl30
		regions = new Array<AtlasRegion>(48);
		Random rand = new Random(0);
		Array<AtlasRegion> atlasRegions = atlas.getRegions();
		for (int i=0; i<48; i++){
			regions.add(atlasRegions.get(rand.nextInt(atlasRegions.size)));
		}
		spriteBatch = new SpriteBatch();
		customShader = Gdx.gl30 == null ? spriteBatch.getShader() : createCustomShader();
		font = new BitmapFont();

	}
	
	public void resize (int width, int height){
		viewport.update(width, height, true);
	}

	public void render () {
		viewport.apply();
		time += Gdx.graphics.getDeltaTime();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
		spriteBatch.setShader(customShader);
		spriteBatch.begin();
		random.setSeed(0);
		for (int i=0; i<8; i++){
			for (int j=0; j<6; j++){
				AtlasRegion region = regions.get(j * 8 + i);
				float w = 60f;
				float h = 60f / (float)region.getRegionWidth() * (float)region.getRegionHeight();
				tmp.idt().translate(w/2 + i * 100, h/2 + j * 75).rotate(time * 60f + i * j);
				spriteBatch.setColor(random(1f), random(1f), random(1f), random(0.5f, 1f));
				spriteBatch.draw(region, w, h, tmp);
			}
		}
		spriteBatch.end();
		
		spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
		spriteBatch.setShader(null);
		spriteBatch.begin();
		font.draw(spriteBatch, (Gdx.gl30 != null ? "gl30" : "gl20") + " flushes: " + spriteBatch.totalRenderCalls, 20f, viewport.getCamera().viewportHeight - 20f);
		spriteBatch.end();
		spriteBatch.totalRenderCalls = 0;
	}

	public void dispose () {
		atlas.dispose();
		customShader.dispose();
		spriteBatch.dispose();
		font.dispose();
	}
	
	static ShaderProgram createCustomShader () {
		ShaderProgram.prependFragmentCode = ShaderProgram.prependVertexCode = 
			Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 140\n #extension GL_EXT_texture_array : enable\n" : "#version 300 es\n";
		
		ShaderProgram shader = new ShaderProgram(Gdx.files.internal("data/shaders/batchTextureArray.vert"), Gdx.files.internal("data/shaders/batchTextureArray.frag"));
		if (shader.isCompiled() == false) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
		
		ShaderProgram.prependFragmentCode = ShaderProgram.prependVertexCode = null;
		return shader;
	}
}