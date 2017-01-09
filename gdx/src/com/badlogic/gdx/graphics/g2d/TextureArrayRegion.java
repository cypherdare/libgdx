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
import com.badlogic.gdx.graphics.Texture;

/** A {@link TextureRegion} intended for use with a TextureArray, but this is not enforced. It has a layer component. The layer
 * can be optionally baked into the texture coordinates for unpacking in a vertex shader. 
 * @author cypherdare*/
public class TextureArrayRegion extends TextureRegion {
	int layer;
	private boolean layerBaked;

	/** Constructs a region with no texture, coordinates, or layer defined. */
	public TextureArrayRegion () {
		super();
	}

	/** Constructs a region the size of the specified texture, on layer 0. */
	public TextureArrayRegion (Texture texture) {
		super(texture);
	}

	/** Constructs a region the size of the specified texture.
	 * @param layer The array layer. */
	public TextureArrayRegion (Texture texture, int layer) {
		super(texture);
		this.layer = layer;
	}

	/** @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn.
	 * @param layer The array layer. */
	public TextureArrayRegion (Texture texture, int layer, int width, int height) {
		super(texture, width, height);
		this.layer = layer;
	}

	/** Constructs a region with the same texture as the specified region and sets the coordinates relative to the specified
	 * region.
	 * @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn.
	 * @param layer The array layer. */
	public TextureArrayRegion (Texture texture, int layer, int x, int y, int width, int height) {
		super(texture, x, y, width, height);
		this.layer = layer;
	}

	/** Constructs a region with the given texture, layer, and coordinates, with optional baking of the layer into the texture
	 * coordinates' non-fractional part. */
	public TextureArrayRegion (Texture texture, int layer, float u, float v, float u2, float v2, boolean layerBaked) {
		super(texture, u, v, u2, v2);
		this.layer = layer;
		this.layerBaked = layerBaked;
	}

	/** Constructs a region with the same texture and coordinates of the specified region. */
	public TextureArrayRegion (TextureArrayRegion region) {
		setRegion(region);
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
		if (layerBaked) setRegion(u, v, u2, v2);
	}

	@Override
	public void setRegion (TextureRegion region) {
		if (region instanceof TextureArrayRegion)
			setRegion((TextureArrayRegion)region);
		else
			super.setRegion(region);
	}

	public void setRegion (TextureArrayRegion region) {
		this.layer = region.layer;
		this.layerBaked = region.layerBaked;
		super.setRegion(region);
	}
	
	@Override
	public void setRegion (TextureRegion region, int x, int y, int width, int height) {
		if (region instanceof TextureArrayRegion)
			setRegion((TextureArrayRegion)region, x, y, width, height);
		else
			super.setRegion(region, x, y, width, height);
	}

	/** Constructs a region with the same texture and layer as the specified region and sets the coordinates relative to the
	 * specified region.
	 * @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn. */
	public void setRegion (TextureArrayRegion region, int x, int y, int width, int height) {
		this.layer = region.layer;
		super.setRegion(region, x, y, width, height);
	}

	/** @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn. */
	public void setRegion (int x, int y, int width, int height, int layer) {
		this.layer = layer;
		super.setRegion(x, y, width, height);
	}

	public void setRegion (float u, float v, float u2, float v2, int layer) {
		this.layer = layer;
		super.setRegion(u, v, u2, v2);
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

	/** Sets whether the layer of the texture array should be baked into the non-fractional part of the texture coordinates. The 
	 * three dimensional texture coordinate can be unpacked in the vertex shader by using the {@code fract()} and {@code floor()} 
	 * functions, i.e.:
	 * <pre> {@code texCoordsOut = vec3(frac(a_texCoords), floor(a_texCoords.x));} </pre>
	 * When layer baking is enabled, any existing non-fractional parts of the texture coordinates will be lost. When it is 
	 * disabled, the non-fractional parts of the texture coordinates will be set to 0.
	 */
	public void setLayerBaked (boolean layerBaked) {
		if (this.layerBaked != layerBaked) {
			this.layerBaked = layerBaked;
			if (layerBaked)
				super.setRegion(bake(u), bake(v), bake(u2), bake(v2));
			else // strip baking
				super.setRegion(u - (float)Math.floor(u), v - (float)Math.floor(v), u2 - (float)Math.floor(u2),
					v2 - (float)Math.floor(v2));
		}
	}
	
	private float bake (float coord){
		if (coord == 1f)
			coord = 0.9999f;
		return coord - (float)Math.floor(coord) + layer;
	}

}
