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

import com.badlogic.gdx.graphics.TextureArray;

/** Defines a rectangular area of a texture array on one of its layers. The coordinate system used has its origin in the upper left corner with the x-axis
 * pointing to the right and the y axis pointing downwards.
 * @author mzechner
 * @author Nathan Sweet */
public class TextureArrayRegion extends GLTextureRegion<TextureArray> {
	int layer;

	/** Constructs a region with no texture, coordinates, or layer defined. */
	public TextureArrayRegion () {
		super();
	}

	/** Constructs a region the size of the specified texture, on layer 0. */
	public TextureArrayRegion (TextureArray texture) {
		super(texture);
	}

	/** Constructs a region the size of the specified texture.
	 * @param layer The array layer. */
	public TextureArrayRegion (TextureArray texture, int layer) {
		super(texture);
		this.layer = layer;
	}

	/** Constructs a region with the given width and height at the top left corner of the texture.  
	 * @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn.
	 * @param layer The array layer. */
	public TextureArrayRegion (TextureArray texture, int layer, int width, int height) {
		super(texture, width, height);
		this.layer = layer;
	}

	/** Constructs a region with the same texture as the specified region and sets the coordinates relative to the specified
	 * region.
	 * @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn.
	 * @param layer The array layer. */
	public TextureArrayRegion (TextureArray texture, int layer, int x, int y, int width, int height) {
		super(texture, x, y, width, height);
		this.layer = layer;
	}

	/** Constructs a region with the given texture, layer, and coordinates. */
	public TextureArrayRegion (TextureArray texture, int layer, float u, float v, float u2, float v2) {
		super(texture, u, v, u2, v2);
		this.layer = layer;
	}

	/** Constructs a region with the same texture, coordinates, and layer the specified region. */
	public TextureArrayRegion (TextureArrayRegion region) {
		setRegion(region);
		this.layer = region.layer;
	}

	/** Constructs a region with the same texture as the specified region and sets the coordinates relative to the specified
	 * region.
	 * @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn. */
	public TextureArrayRegion (TextureArrayRegion region, int x, int y, int width, int height) {
		setRegion(region, x, y, width, height);
		this.layer = region.layer;
	}

	/** @return The layer of the TextureArray on which the desired region resides. */
	public int getLayer () {
		return layer;
	}

	/** Sets the layer of the TextureArray on which the desired region resides. */
	public void setLayer (int layer) {
		this.layer = layer;
	}

	/** Sets the region to the same texture, coordinates, and layer of the specified region. */
	public void setRegion (TextureArrayRegion region) {
		texture = region.texture;
		setRegion(region.layer, region.getRegionX(), region.getRegionWidth(), region.getRegionWidth(), region.getRegionHeight());
	}
	
	/** Sets the region to use the same texture and layer as the specified region and sets the coordinates relative to the specified region.
	 * @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn. */
	public void setRegion (TextureArrayRegion region, int x, int y, int width, int height) {
		texture = region.texture;
		setRegion(region.layer, region.getRegionX() + x, region.getRegionY() + y, width, height);
	}
	
	/** @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn. */
	public void setRegion (int layer, int x, int y, int width, int height) {
		super.setRegion(x, y, width, height);
		this.layer = layer;
	}
	
	public void setRegion (int layer, float u, float v, float u2, float v2) {
		super.setRegion(u, v, u2, v2);
		this.layer = layer;
	}

}
