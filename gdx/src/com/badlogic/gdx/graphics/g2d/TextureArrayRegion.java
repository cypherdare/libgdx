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

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.TextureArray;

/** A {@link TextureRegion} that also has a layer component, intended for use with a {@link TextureArray}. The layer can be optionally
 * baked into the texture coordinates for unpacking in a vertex shader. */
public class TextureArrayRegion extends TextureRegion {
	int layer;
	private boolean layerBaked;

	/** Constructs a region with no texture, coordinates, or layer defined. */
	public TextureArrayRegion () {
		super();
	}

	/** Constructs a region the size of the specified texture, on layer 0. */
	public TextureArrayRegion (GLTexture texture) {
		super(texture);
	}

	/** Constructs a region the size of the specified texture.
	 * @param layer The array layer. */
	public TextureArrayRegion (GLTexture texture, int layer) {
		super(texture);
		this.layer = layer;
	}

	/** @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn.
	 * @param layer The array layer. */
	public TextureArrayRegion (GLTexture texture, int layer, int width, int height) {
		super(texture, width, height);
		this.layer = layer;
	}

	/** Constructs a region with the same texture as the specified region and sets the coordinates relative to the specified
	 * region.
	 * @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn.
	 * @param layer The array layer. */
	public TextureArrayRegion (GLTexture texture, int layer, int x, int y, int width, int height) {
		super(texture, x, y, width, height);
		this.layer = layer;
	}

	/** Constructs a region with the given texture, layer, and coordinates, with optional baking of the layer into the texture
	 * coordinates' non-fractional part. */
	public TextureArrayRegion (GLTexture texture, int layer, float u, float v, float u2, float v2, boolean layerBaked) {
		super(texture, u, v, u2, v2);
		this.layer = layer;
		this.layerBaked = layerBaked;
	}

	/** Constructs a region with the same texture and coordinates of the specified region. */
	public TextureArrayRegion (TextureArrayRegion region) {
		super(region);
		this.layer = region.layer;
	}

	/** Constructs a region with the same texture as the specified region and sets the coordinates relative to the specified
	 * region.
	 * @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn. */
	public TextureArrayRegion (TextureArrayRegion region, int x, int y, int width, int height) {
		super(region, x, y, width, height);
		this.layer = region.layer;
	}

	/** @return The layer of the TextureArray on which the desired region resides. */
	public int getLayer () {
		return layer;
	}

	/** Sets the layer of the TextureArray on which the desired region resides. */
	public void setLayer (int layer) {
		this.layer = layer;
		if (layerBaked)
			setRegion(u, v, u2, v2);
	}
	
	public void setRegion (TextureArrayRegion region) {
		super.setRegion(region);
		this.layer = region.layer;
	}

	/** Constructs a region with the same texture as the specified region and sets the coordinates relative to the specified
	 * region.
	 * @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn. */
	public void setRegion (TextureArrayRegion region, int x, int y, int width, int height) {
		super.setRegion(region, x, y, width, height);
		this.layer = region.layer;
	}

	/** @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn. */
	public void setRegion (int x, int y, int width, int height, int layer) {
		super.setRegion(x, y, width, height);
		this.layer = layer;
	}

	public void setRegion (float u, float v, float u2, float v2, int layer) {
		super.setRegion(u, v, u2, v2);
		this.layer = layer;
	}

	@Override
	public void setRegion (float u, float v, float u2, float v2) {
		if (layerBaked)
			super.setRegion(u - (float)Math.floor(u) + layer, v - (float)Math.floor(v) + layer, u2 - (float)Math.floor(u2) + layer,
				v2 - (float)Math.floor(v2) + layer);
		else
			super.setRegion(u, v, u2, v2);
	}

	/** @return Whether the texture array layer is baked into the non-fractional part of the UV coordinates. */
	public boolean isLayerBaked () {
		return layerBaked;
	}

	public void setLayerBaked (boolean layerBaked) {
		if (this.layerBaked != layerBaked) {
			this.layerBaked = layerBaked;
			if (layerBaked)
				super.setRegion(u - (float)Math.floor(u) + layer, v - (float)Math.floor(v) + layer,
					u2 - (float)Math.floor(u2) + layer, v2 - (float)Math.floor(v2) + layer);
			else // strip baking
				super.setRegion(u - (float)Math.floor(u), v - (float)Math.floor(v), u2 - (float)Math.floor(u2),
					v2 - (float)Math.floor(v2));
		}
	}

}
