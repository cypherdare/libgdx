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

import com.badlogic.gdx.graphics.Texture;

/** Defines a rectangular area of a texture. The coordinate system used has its origin in the upper left corner with the x-axis
 * pointing to the right and the y axis pointing downwards.
 * @author mzechner
 * @author Nathan Sweet */
public class TextureRegion extends GLTextureRegion<Texture> {

	/** Constructs a region with no texture and no coordinates defined. */
	public TextureRegion () {
		super();
	}

	/** Constructs a region the size of the specified texture. */
	public TextureRegion (Texture texture) {
		super(texture);
	}

	/** Constructs a region with the given width and height at the top left corner of the texture.
	 * @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn. */
	public TextureRegion (Texture texture, int width, int height) {
		super(texture, width, height);
	}

	/** Constructs a region with the same texture as the specified region and sets the coordinates relative to the specified
	 * region.
	 * @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn. */
	public TextureRegion (Texture texture, int x, int y, int width, int height) {
		super(texture, x, y, width, height);
	}

	/** Constructs a region with the given texture and coordinates. */
	public TextureRegion (Texture texture, float u, float v, float u2, float v2) {
		super(texture, u, v, u2, v2);
	}

	/** Constructs a region with the same texture and coordinates of the specified region. */
	public TextureRegion (TextureRegion region) {
		setRegion(region);
	}

	/** Constructs a region with the same texture as the specified region and sets the coordinates relative to the specified
	 * region.
	 * @param width The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param height The height of the texture region. May be negative to flip the sprite when drawn. */
	public TextureRegion (TextureRegion region, int x, int y, int width, int height) {
		setRegion(region, x, y, width, height);
	}

	/** Sets the texture and coordinates to the specified region. */
	public void setRegion (TextureRegion region) {
		texture = region.texture;
		setRegion(region.u, region.v, region.u2, region.v2);
	}

	/** Sets the texture to that of the specified region and sets the coordinates relative to the specified region. */
	public void setRegion (TextureRegion region, int x, int y, int width, int height) {
		texture = region.texture;
		setRegion(region.getRegionX() + x, region.getRegionY() + y, width, height);
	}

	/** Helper function to create tiles out of this TextureRegion starting from the top left corner going to the right and ending
	 * at the bottom right corner. Only complete tiles will be returned so if the region's width or height are not a multiple of
	 * the tile width and height not all of the region will be used. This will not work on texture regions returned form a
	 * TextureAtlas that either have whitespace removed or where flipped before the region is split.
	 * 
	 * @param tileWidth a tile's width in pixels
	 * @param tileHeight a tile's height in pixels
	 * @return a 2D array of TextureRegions indexed by [row][column]. */
	public TextureRegion[][] split (int tileWidth, int tileHeight) {
		int x = getRegionX();
		int y = getRegionY();
		int width = regionWidth;
		int height = regionHeight;

		int rows = height / tileHeight;
		int cols = width / tileWidth;

		int startX = x;
		TextureRegion[][] tiles = new TextureRegion[rows][cols];
		for (int row = 0; row < rows; row++, y += tileHeight) {
			x = startX;
			for (int col = 0; col < cols; col++, x += tileWidth) {
				tiles[row][col] = new TextureRegion(texture, x, y, tileWidth, tileHeight);
			}
		}

		return tiles;
	}

	/** Helper function to create tiles out of the given {@link Texture} starting from the top left corner going to the right and
	 * ending at the bottom right corner. Only complete tiles will be returned so if the texture's width or height are not a
	 * multiple of the tile width and height not all of the texture will be used.
	 * 
	 * @param texture the Texture
	 * @param tileWidth a tile's width in pixels
	 * @param tileHeight a tile's height in pixels
	 * @return a 2D array of TextureRegions indexed by [row][column]. */
	public static TextureRegion[][] split (Texture texture, int tileWidth, int tileHeight) {
		TextureRegion region = new TextureRegion(texture);
		return region.split(tileWidth, tileHeight);
	}
}
