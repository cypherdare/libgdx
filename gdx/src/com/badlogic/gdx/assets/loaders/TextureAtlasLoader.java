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

package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.badlogic.gdx.utils.Array;

/** {@link AssetLoader} to load {@link TextureAtlas} instances. Passing a {@link TextureAtlasParameter} to
 * {@link AssetManager#load(String, Class, AssetLoaderParameters)} allows to specify whether the atlas regions should be flipped
 * on the y-axis or not.
 * @author mzechner */
public class TextureAtlasLoader<T extends TextureAtlas, P extends TextureAtlasLoader.TextureAtlasParameter<T>> extends SynchronousAssetLoader<T, P> {
	public TextureAtlasLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	TextureAtlasData data;

	@Override
	public T load (AssetManager assetManager, String fileName, FileHandle file, TextureAtlasParameter parameter) {
		for (Page page : data.getPages()) {
			Texture texture = assetManager.get(page.textureFile.path().replaceAll("\\\\", "/"), getTextureType());
			page.texture = texture;
		}

		return (T)generateAtlas(data);
	}
	
	protected Class<? extends Texture> getTextureType (){
		return Texture.class;
	}
	
	protected TextureAtlas generateAtlas (TextureAtlasData data){
		return new TextureAtlas(data);
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle atlasFile, TextureAtlasParameter parameter) {
		FileHandle imgDir = atlasFile.parent();

		if (parameter != null)
			data = new TextureAtlasData(atlasFile, imgDir, parameter.flip);
		else {
			data = new TextureAtlasData(atlasFile, imgDir, false);
		}

		Array<AssetDescriptor> dependencies = new Array();
		for (Page page : data.getPages()) {
			TextureParameter params = new TextureParameter();
			params.format = page.format;
			params.genMipMaps = page.useMipMaps;
			params.minFilter = page.minFilter;
			params.magFilter = page.magFilter;
			dependencies.add(new AssetDescriptor(page.textureFile, getTextureType(), params));
		}
		return dependencies;
	}

	static public class TextureAtlasParameter<T extends TextureAtlas> extends AssetLoaderParameters<T> {
		/** whether to flip the texture atlas vertically **/
		public boolean flip = false;

		public TextureAtlasParameter () {
		}

		public TextureAtlasParameter (boolean flip) {
			this.flip = flip;
		}
	}
}
