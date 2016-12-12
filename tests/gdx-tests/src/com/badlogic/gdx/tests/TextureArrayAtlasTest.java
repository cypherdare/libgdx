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
import com.badlogic.gdx.graphics.g2d.GLTextureAtlas;
import com.badlogic.gdx.graphics.g2d.GLTextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureArrayAtlas;
import com.badlogic.gdx.graphics.g2d.TextureArrayRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class TextureArrayAtlasTest extends GdxTest {
	Batch batch;
	GLTextureAtlas atlas;
	Array<GLTextureRegion> regions;
	float time = 0;
	final Affine2 tmp = new Affine2();
	Viewport viewport;
	SpriteBatch spriteBatch;
	BitmapFont font;

	public void create () {
		batch = new Batch(Gdx.gl30 != null);
		FileHandle packFile = Gdx.files.internal("data/pack3page");
		
		// Fall back to standard TextureAtlas if not using gl30
		atlas = Gdx.gl30 == null ? new TextureAtlas(packFile) : new TextureArrayAtlas(packFile);
		
		viewport = new ExtendViewport(640, 480);

		Gdx.gl.glClearColor(0.2f, 0.2f, 0.3f, 1);
		
		// Same random shuffle every time for comparison between gl20 and gl30
		regions = new Array<GLTextureRegion>(48);
		Random rand = new Random(0);
		Array<GLTextureRegion> atlasRegions = atlas.getRegions();
		for (int i=0; i<48; i++){
			regions.add(atlasRegions.get(rand.nextInt(atlasRegions.size)));
		}
		
		spriteBatch = new SpriteBatch();
		font = new BitmapFont();

	}
	
	public void resize (int width, int height){
		viewport.update(width, height, true);
	}

	public void render () {
		viewport.apply();
		time += Gdx.graphics.getDeltaTime();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.combinedMatrix.set(viewport.getCamera().combined);
		batch.begin();
		for (int i=0; i<8; i++){
			for (int j=0; j<6; j++){
				GLTextureRegion<?> region = regions.get(j * 8 + i);
				float w = 60f;
				float h = 60f / (float)region.getRegionWidth() * (float)region.getRegionHeight();
				tmp.idt().translate(w/2 + i * 100, h/2 + j * 75).rotate(time * 60f + i * j);
				batch.draw(region, w, h, tmp);
			}
		}
		batch.end();
		
		spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
		spriteBatch.begin();
		font.draw(spriteBatch, (Gdx.gl30 != null ? "gl30" : "gl20") + " flushes: " + batch.renderCalls, 20f, viewport.getCamera().viewportHeight - 20f);
		spriteBatch.end();
	}

	public void dispose () {
		atlas.dispose();
		batch.dispose();
		spriteBatch.dispose();
		font.dispose();
	}
	
	static class Batch implements Disposable {
		static final int CAPACITY = 100;
		final boolean usingTextureArray;
		private Mesh mesh;
		final float[] vertices;
		int idx = 0;
		GLTexture lastTexture = null;
		final Matrix4 combinedMatrix = new Matrix4();
		final ShaderProgram shader;
		public int renderCalls, totalRenderCalls;
		final int spriteSize;
		
		public Batch(boolean usingTextureArray){
			this.usingTextureArray = usingTextureArray;
			VertexDataType vertexDataType = (Gdx.gl30 != null) ? VertexDataType.VertexBufferObjectWithVAO : VertexDataType.VertexArray;
			
			mesh = new Mesh(vertexDataType, false, CAPACITY * 4, CAPACITY * 6,
				new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(Usage.TextureCoordinates, usingTextureArray ? 3 : 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
			spriteSize = mesh.getVertexAttributes().vertexSize * 4 / 4;
			vertices = new float[CAPACITY * spriteSize];
			int len = CAPACITY * 6;
			short[] indices = new short[len];
			short j = 0;
			for (int i = 0; i < len; i += 6, j += 4) {
				indices[i] = j;
				indices[i + 1] = (short)(j + 1);
				indices[i + 2] = (short)(j + 2);
				indices[i + 3] = (short)(j + 2);
				indices[i + 4] = (short)(j + 3);
				indices[i + 5] = j;
			}
			mesh.setIndices(indices);
			
			shader = createShader(usingTextureArray);
		}
		
		public void begin (){
			Gdx.gl.glDepthMask(false);
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			shader.begin();
			shader.setUniformMatrix("u_projTrans", combinedMatrix);
			shader.setUniformi("u_texture", 0);
			renderCalls = 0;
		}
		
		public void end (){
			if (idx > 0) flush();
			lastTexture = null;
			Gdx.gl.glDepthMask(true);
			Gdx.gl.glDisable(GL20.GL_BLEND);
			shader.end();
		}
		
		protected void switchTexture (GLTexture texture) {
			flush();
			lastTexture = texture;
		}
		
		public void flush () {
			if (idx == 0) return;

			renderCalls++;
			totalRenderCalls++;
			int spritesInBatch = idx / spriteSize;
			int count = spritesInBatch * 6;

			lastTexture.bind(0);
			Mesh mesh = this.mesh;
			mesh.setVertices(vertices, 0, idx);
			mesh.getIndicesBuffer().position(0);
			mesh.getIndicesBuffer().limit(count);

			mesh.render(shader, GL20.GL_TRIANGLES, 0, count);

			idx = 0;
		}
		
		public void draw (GLTextureRegion region, float width, float height, Affine2 transform) {
			float[] vertices = this.vertices;

			GLTexture texture = region.getTexture();
			if (texture != lastTexture) {
				switchTexture(texture);
			} else if (idx == vertices.length) {
				flush();
			}

			// construct corner points
			float x1 = transform.m02;
			float y1 = transform.m12;
			float x2 = transform.m01 * height + transform.m02;
			float y2 = transform.m11 * height + transform.m12;
			float x3 = transform.m00 * width + transform.m01 * height + transform.m02;
			float y3 = transform.m10 * width + transform.m11 * height + transform.m12;
			float x4 = transform.m00 * width + transform.m02;
			float y4 = transform.m10 * width + transform.m12;

			float u = region.getU();
			float v = region.getV2();
			float u2 = region.getU2();
			float v2 = region.getV();
			float layer = region.getLayer();

			int idx = this.idx;
			vertices[idx++] = x1;
			vertices[idx++] = y1;
			vertices[idx++] = u;
			vertices[idx++] = v;
			if (usingTextureArray) vertices[idx++] = layer;

			vertices[idx++] = x2;
			vertices[idx++] = y2;
			vertices[idx++] = u;
			vertices[idx++] = v2;
			if (usingTextureArray) vertices[idx++] = layer;

			vertices[idx++] = x3;
			vertices[idx++] = y3;
			vertices[idx++] = u2;
			vertices[idx++] = v2;
			if (usingTextureArray) vertices[idx++] = layer;

			vertices[idx++] = x4;
			vertices[idx++] = y4;
			vertices[idx++] = u2;
			vertices[idx++] = v;
			if (usingTextureArray) vertices[idx++] = layer;
			this.idx = idx;
		}

		@Override
		public void dispose () {
			mesh.dispose();
			shader.dispose();
		}
		
	}
	
	static ShaderProgram createShader (boolean usingTextureArray) {
		if (usingTextureArray){
			String platformPrefix = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 140\n #extension GL_EXT_texture_array : enable\n" : "#version 300 es\n";
			
			ShaderProgram.prependVertexCode = platformPrefix + "#define varying out\n#define attribute in\n";
			ShaderProgram.prependFragmentCode = platformPrefix + "#define varying in\n#define texture2D texture\n#define gl_FragColor fragColor\nout vec4 fragColor;\n";
		} else {
			ShaderProgram.prependVertexCode = ShaderProgram.prependFragmentCode = "";
		}
		String tcVec = usingTextureArray ? "vec3" : "vec2";
		
		String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "attribute " + tcVec + " " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "uniform mat4 u_projTrans;\n" //
			+ "varying " + tcVec + " v_texCoords;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "}\n";
		String fragmentShader = "#ifdef GL_ES\n" //
			+ "#define LOWP lowp\n" //
			+ "precision mediump float;\n" //
			+ (usingTextureArray ? "precision mediump sampler2DArray;\n" : "")
			+ "#else\n" //
			+ "#define LOWP \n" //
			+ "#endif\n" //
			+ "varying " + tcVec + " v_texCoords;\n" //
			+ (usingTextureArray ? "varying float v_layer;\n" : "")
			+ "uniform " + (usingTextureArray ? "sampler2DArray" : "sampler2D") + " u_texture;\n" //
			+ "void main()\n"//
			+ "{\n" //
			+ "  gl_FragColor = texture2D(u_texture, v_texCoords);\n" //
			+ "}";

		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
		if (shader.isCompiled() == false) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
		return shader;
	}
}