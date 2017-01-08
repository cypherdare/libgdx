/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.HashMap;
import java.util.Map;

/**
 * Open GLES wrapper for TextureArray.
 * @author Tomski */
public class TextureArray extends Texture {

	private TextureArrayData arrayData;

	public TextureArray (String... internalPaths) {
		this(getInternalHandles(internalPaths));
	}

	public TextureArray (FileHandle... files) {
		this(false, files);
	}

	public TextureArray (boolean useMipMaps, FileHandle... files) {
		this(useMipMaps, Pixmap.Format.RGBA8888, files);
	}

	public TextureArray (boolean useMipMaps, Pixmap.Format format, FileHandle... files) {
		this(TextureArrayData.Factory.loadFromFiles(format, useMipMaps, files));
	}

	public TextureArray (TextureArrayData data) {
		super(GL30.GL_TEXTURE_2D_ARRAY, Gdx.gl.glGenTexture(), data);
		arrayData = (TextureArrayData)data;
	}

	private static FileHandle[] getInternalHandles (String... internalPaths) {
		FileHandle[] handles = new FileHandle[internalPaths.length];
		for (int i = 0; i < internalPaths.length; i++) {
			handles[i] = Gdx.files.internal(internalPaths[i]);
		}
		return handles;
	}

	@Override
	public void load (TextureData data) {
		if (!(data instanceof TextureArrayData))
			throw new GdxRuntimeException("TextureArray only supports TextureArrayData");
		load((TextureArrayData)data);
	}
	
	public void load (TextureArrayData data) {
		if (this.data != null && data.isManaged() != this.data.isManaged())
			throw new GdxRuntimeException("New data must have the same managed status as the old data");
		this.data = data;

		bind();
		Gdx.gl30.glTexImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, data.getInternalFormat(), data.getWidth(), data.getHeight(), data.getDepth(), 0, data.getInternalFormat(), data.getGLType(), null);

		if (!data.isPrepared()) data.prepare();

		data.consumeTextureArrayData();

		setFilter(minFilter, magFilter);
		setWrap(uWrap, vWrap);
		Gdx.gl.glBindTexture(glTarget, 0);
	}

	@Override
	public int getDepth () {
		return arrayData.getDepth();
	}
	
	@Override
	public void draw (Pixmap pixmap, int x, int y) {
		throw new GdxRuntimeException("TextureArray does not support drawing.");
	}

}
