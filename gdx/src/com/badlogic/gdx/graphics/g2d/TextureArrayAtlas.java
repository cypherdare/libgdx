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

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.graphics.g2d.GLTextureAtlas.TextureAtlasData;
import com.badlogic.gdx.utils.Array;

/** Loads images from texture atlases created by TexturePacker. Any pages that have the same size, mip mapping, filters, and wrapping
 * will share a single TextureArray, with each page's image on a different layer.
 * <p>
 * A TextureArrayAtlas must be disposed to free up the resources consumed by the backing textures.
 * <p>
 * TextureArrayAtlas can only be used in a GL 3.0 context.
 * @author Nathan Sweet, cypherdare */
public class TextureArrayAtlas extends GLTextureAtlas<TextureArray, com.badlogic.gdx.graphics.g2d.TextureArrayAtlas.ArrayAtlasRegion> {
	
	/** Creates an empty atlas to which regions can be added. */
	public TextureArrayAtlas () {
	}

	/** Loads the specified pack file using {@link FileType#Internal}, using the parent directory of the pack file to find the page
	 * images. */
	public TextureArrayAtlas (String internalPackFile) {
		this(Gdx.files.internal(internalPackFile));
	}

	/** Loads the specified pack file, using the parent directory of the pack file to find the page images. */
	public TextureArrayAtlas (FileHandle packFile) {
		this(packFile, packFile.parent());
	}

	/** @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner.
	 * @see #TextureArrayAtlas(FileHandle) */
	public TextureArrayAtlas (FileHandle packFile, boolean flip) {
		this(packFile, packFile.parent(), flip);
	}

	public TextureArrayAtlas (FileHandle packFile, FileHandle imagesDir) {
		this(packFile, imagesDir, false);
	}

	/** @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner. */
	public TextureArrayAtlas (FileHandle packFile, FileHandle imagesDir, boolean flip) {
		this(new TextureAtlasData(packFile, imagesDir, flip));
	}

	/** @param data May be null. */
	public TextureArrayAtlas (TextureAtlasData data) {
		super(data, true);
	}
	
	@Override
	protected TextureArray loadTexture (Pixmap.Format format, boolean useMipMaps, FileHandle... file){
		return new TextureArray(useMipMaps, format, file);
	}

	@Override
	protected ArrayAtlasRegion makeAtlasRegion (TextureArray texture, int layer, int x, int y, int width, int height, int index, String name,
		float offsetX, float offsetY, int packedWidth, int packedHeight, int originalWidth, int originalHeight, boolean rotate,
		int[] splits, int[] pads) {
		ArrayAtlasRegion atlasRegion = new ArrayAtlasRegion(texture, layer, x, y, width, height);
		atlasRegion.index = index;
		atlasRegion.name = name;
		atlasRegion.offsetX = offsetX;
		atlasRegion.offsetY = offsetY;
		atlasRegion.originalHeight = originalHeight;
		atlasRegion.originalWidth = originalWidth;
		atlasRegion.rotate = rotate;
		atlasRegion.splits = splits;
		atlasRegion.pads = pads;
		return atlasRegion;
	}

	@Override
	protected ArrayAtlasRegion copyAtlasRegion (ArrayAtlasRegion region) {
		return new ArrayAtlasRegion(region);
	}
	
	/** Adds a region to the atlas. The specified texture will be disposed when the atlas is disposed. */
	public ArrayAtlasRegion addRegion (String name, TextureArrayRegion textureRegion) {
		return addRegion(name, textureRegion.texture, textureRegion.getRegionX(), textureRegion.getRegionY(),
			textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
	}

	/** Describes the region of a packed image and provides information about the original image before it was packed. */
	static public class ArrayAtlasRegion extends TextureArrayRegion {
		/** The number at the end of the original image file name, or -1 if none.<br>
		 * <br>
		 * When sprites are packed, if the original file name ends with a number, it is stored as the index and is not considered as
		 * part of the sprite's name. This is useful for keeping animation frames in order.
		 * @see TextureArrayAtlas#findRegions(String) */
		public int index;

		/** The name of the original image file, up to the first underscore. Underscores denote special instructions to the texture
		 * packer. */
		public String name;

		/** The offset from the left of the original image to the left of the packed image, after whitespace was removed for packing. */
		public float offsetX;

		/** The offset from the bottom of the original image to the bottom of the packed image, after whitespace was removed for
		 * packing. */
		public float offsetY;

		/** The width of the image, after whitespace was removed for packing. */
		public int packedWidth;

		/** The height of the image, after whitespace was removed for packing. */
		public int packedHeight;

		/** The width of the image, before whitespace was removed and rotation was applied for packing. */
		public int originalWidth;

		/** The height of the image, before whitespace was removed for packing. */
		public int originalHeight;

		/** If true, the region has been rotated 90 degrees counter clockwise. */
		public boolean rotate;

		/** The ninepatch splits, or null if not a ninepatch. Has 4 elements: left, right, top, bottom. */
		public int[] splits;

		/** The ninepatch pads, or null if not a ninepatch or the has no padding. Has 4 elements: left, right, top, bottom. */
		public int[] pads;

		public ArrayAtlasRegion (TextureArray texture, int layer, int x, int y, int width, int height) {
			super(texture, layer, x, y, width, height);
			originalWidth = width;
			originalHeight = height;
			packedWidth = width;
			packedHeight = height;
		}

		public ArrayAtlasRegion (ArrayAtlasRegion region) {
			setRegion(region);
			index = region.index;
			name = region.name;
			offsetX = region.offsetX;
			offsetY = region.offsetY;
			packedWidth = region.packedWidth;
			packedHeight = region.packedHeight;
			originalWidth = region.originalWidth;
			originalHeight = region.originalHeight;
			rotate = region.rotate;
			splits = region.splits;
		}

		@Override
		/** Flips the region, adjusting the offset so the image appears to be flipped as if no whitespace has been removed for packing. */
		public void flip (boolean x, boolean y) {
			super.flip(x, y);
			if (x) offsetX = originalWidth - offsetX - getRotatedPackedWidth();
			if (y) offsetY = originalHeight - offsetY - getRotatedPackedHeight();
		}

		/** Returns the packed width considering the rotate value, if it is true then it returns the packedHeight, otherwise it
		 * returns the packedWidth. */
		public float getRotatedPackedWidth () {
			return rotate ? packedHeight : packedWidth;
		}

		/** Returns the packed height considering the rotate value, if it is true then it returns the packedWidth, otherwise it
		 * returns the packedHeight. */
		public float getRotatedPackedHeight () {
			return rotate ? packedWidth : packedHeight;
		}

		/** @return The name of the region, usually the original file name of the source image for the region, with any trailing number removed. */
		public String toString () {
			return name;
		}

		@Override
		public int getIndex () {
			return index;
		}
	}

}
