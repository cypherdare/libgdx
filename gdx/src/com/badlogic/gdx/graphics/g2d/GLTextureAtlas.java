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

import static com.badlogic.gdx.graphics.Texture.TextureWrap.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.GLTextureAtlas.TextureAtlasData.Page;
import com.badlogic.gdx.graphics.g2d.GLTextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.StreamUtils;

/** Loads images from texture atlases created by TexturePacker.
 * <p>
 * A TextureAtlas must be disposed to free up the resources consumed by the backing textures.
 *
 * @param <T> The type of GLTexture this atlas loads.
 * @param <S> The type of GLTextureRegion this atlas produces, typically a subclass of {@link TextureRegion} or {@link TextureArrayRegion}
 *           that includes additional data.
 * 
 * @author Nathan Sweet */
public abstract class GLTextureAtlas<T extends GLTexture, S extends GLTextureRegion<T>> implements Disposable {
	static final String[] tuple = new String[4];

	protected final ObjectSet<T> textures = new ObjectSet(4);
	protected final Array<S> regions = new Array();

	public static class TextureAtlasData {
		public static class Page {
			public final FileHandle textureFile;
			public GLTexture texture;
			public int layer; // The layer within the texture, if a TextureArray
			public final float width, height;
			public final boolean useMipMaps;
			public final Format format;
			public final TextureFilter minFilter;
			public final TextureFilter magFilter;
			public final TextureWrap uWrap;
			public final TextureWrap vWrap;

			public Page (FileHandle handle, float width, float height, boolean useMipMaps, Format format, TextureFilter minFilter,
				TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap) {
				this.width = width;
				this.height = height;
				this.textureFile = handle;
				this.useMipMaps = useMipMaps;
				this.format = format;
				this.minFilter = minFilter;
				this.magFilter = magFilter;
				this.uWrap = uWrap;
				this.vWrap = vWrap;
			}

			public boolean canShareTextureAtlas (Page page) {
				return texture == null && page.texture == null && width == page.width && height == page.height
					&& useMipMaps == page.useMipMaps && minFilter == page.minFilter && magFilter == page.magFilter
					&& uWrap == page.uWrap && vWrap == page.vWrap;
			}
		}

		public static class Region {
			public Page page;
			public int index;
			public String name;
			public float offsetX;
			public float offsetY;
			public int originalWidth;
			public int originalHeight;
			public boolean rotate;
			public int left;
			public int top;
			public int width;
			public int height;
			public boolean flip;
			public int[] splits;
			public int[] pads;
			public int layer;
		}

		final Array<Page> pages = new Array();
		final Array<Region> regions = new Array();

		public TextureAtlasData (FileHandle packFile, FileHandle imagesDir, boolean flip) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(packFile.read()), 64);
			try {
				Page pageImage = null;
				while (true) {
					String line = reader.readLine();
					if (line == null) break;
					if (line.trim().length() == 0)
						pageImage = null;
					else if (pageImage == null) {
						FileHandle file = imagesDir.child(line);

						float width = 0, height = 0;
						if (readTuple(reader) == 2) { // size is only optional for an atlas packed with an old TexturePacker.
							width = Integer.parseInt(tuple[0]);
							height = Integer.parseInt(tuple[1]);
							readTuple(reader);
						}
						Format format = Format.valueOf(tuple[0]);

						readTuple(reader);
						TextureFilter min = TextureFilter.valueOf(tuple[0]);
						TextureFilter max = TextureFilter.valueOf(tuple[1]);

						String direction = readValue(reader);
						TextureWrap repeatX = ClampToEdge;
						TextureWrap repeatY = ClampToEdge;
						if (direction.equals("x"))
							repeatX = Repeat;
						else if (direction.equals("y"))
							repeatY = Repeat;
						else if (direction.equals("xy")) {
							repeatX = Repeat;
							repeatY = Repeat;
						}

						pageImage = new Page(file, width, height, min.isMipMap(), format, min, max, repeatX, repeatY);
						pages.add(pageImage);
					} else {
						boolean rotate = Boolean.valueOf(readValue(reader));

						readTuple(reader);
						int left = Integer.parseInt(tuple[0]);
						int top = Integer.parseInt(tuple[1]);

						readTuple(reader);
						int width = Integer.parseInt(tuple[0]);
						int height = Integer.parseInt(tuple[1]);

						Region region = new Region();
						region.page = pageImage;
						region.left = left;
						region.top = top;
						region.width = width;
						region.height = height;
						region.name = line;
						region.rotate = rotate;

						if (readTuple(reader) == 4) { // split is optional
							region.splits = new int[] {Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]),
								Integer.parseInt(tuple[2]), Integer.parseInt(tuple[3])};

							if (readTuple(reader) == 4) { // pad is optional, but only present with splits
								region.pads = new int[] {Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]),
									Integer.parseInt(tuple[2]), Integer.parseInt(tuple[3])};

								readTuple(reader);
							}
						}

						region.originalWidth = Integer.parseInt(tuple[0]);
						region.originalHeight = Integer.parseInt(tuple[1]);

						readTuple(reader);
						region.offsetX = Integer.parseInt(tuple[0]);
						region.offsetY = Integer.parseInt(tuple[1]);

						region.index = Integer.parseInt(readValue(reader));

						if (flip) region.flip = true;

						regions.add(region);
					}
				}
			} catch (Exception ex) {
				throw new GdxRuntimeException("Error reading pack file: " + packFile, ex);
			} finally {
				StreamUtils.closeQuietly(reader);
			}

			regions.sort(indexComparator);
		}

		public Array<Page> getPages () {
			return pages;
		}

		public Array<Region> getRegions () {
			return regions;
		}
	}

	/** Creates an empty atlas to which regions can be added. */
	protected GLTextureAtlas () {
	}

	/** @param data May be null. */
	protected GLTextureAtlas (TextureAtlasData data, boolean useTextureArrays) {
		if (data != null) load(data, useTextureArrays);
	}

	private void load (TextureAtlasData data, boolean useTextureArrays) {
		ObjectMap<Page, GLTexture> pageToTexture = new ObjectMap<Page, GLTexture>(4);
		if (useTextureArrays) {
			ObjectSet<Array<Page>> pageArrays = new ObjectSet<Array<Page>>(); // arrays of pages that will share texture array
			for (Page page : data.pages) {
				Array<Page> pageArray = null;
				for (Array<Page> textureArray : pageArrays){
					if (page.canShareTextureAtlas(textureArray.first())){
						pageArray = textureArray;
						break;
					}
				}
				if (pageArray == null) {
					pageArray = new Array<Page>(4);
					pageArrays.add(pageArray);
				}
				pageArray.add(page);
			}
			for (Array<Page> pageArray : pageArrays) { // each page array loads one texture
				Array<FileHandle> files = new Array<FileHandle>(true, pageArray.size, FileHandle.class);
				for (int i=0; i<pageArray.size; i++){
					Page page = pageArray.get(i);
					files.add(page.textureFile);
					page.layer = i;
				}
				Page templatePage = pageArray.first();
				T texture = null;
				if (templatePage.texture == null) {
					texture = loadTexture(templatePage.format, templatePage.useMipMaps, files.toArray());
					texture.setFilter(templatePage.minFilter, templatePage.magFilter);
					texture.setWrap(templatePage.uWrap, templatePage.vWrap);
				} else {
					texture = (T)templatePage.texture;
					texture.setFilter(templatePage.minFilter, templatePage.magFilter);
					texture.setWrap(templatePage.uWrap, templatePage.vWrap);
				}
				textures.add((T)texture);
				for (Page page : pageArray)
					pageToTexture.put(page, texture);
			}
		} else {
			for (Page page : data.pages) {
				GLTexture texture = null;
				if (page.texture == null) {
					texture = loadTexture(page.format, page.useMipMaps, page.textureFile);
					texture.setFilter(page.minFilter, page.magFilter);
					texture.setWrap(page.uWrap, page.vWrap);
				} else {
					texture = page.texture;
					texture.setFilter(page.minFilter, page.magFilter);
					texture.setWrap(page.uWrap, page.vWrap);
				}
				textures.add((T)texture);
				pageToTexture.put(page, texture);
			}
		}

		for (Region region : data.regions) {
			int width = region.width;
			int height = region.height;
			S atlasRegion = makeAtlasRegion((T)pageToTexture.get(region.page), region.page.layer, region.left, region.top,
				region.rotate ? height : width, region.rotate ? width : height, region.index, region.name,
				region.offsetX, region.offsetY, region.originalWidth, region.originalHeight, region.originalWidth,
				region.originalHeight, region.rotate, region.splits, region.pads);
			if (region.flip) atlasRegion.flip(false, true);
			regions.add(atlasRegion);
		}
	}

	protected abstract T loadTexture (Pixmap.Format format, boolean useMipMaps, FileHandle... file);

	protected abstract S makeAtlasRegion (T texture, int layer, int x, int y, int width, int height, int index, String name,
		float offsetX, float offsetY, int packedWidth, int packedHeight, int originalWidth, int originalHeight, boolean rotate,
		int[] splits, int[] pads);

	protected abstract S copyAtlasRegion (S region);

	/** Adds a region to the atlas. The specified texture will be disposed when the atlas is disposed. */
	public S addRegion (String name, T texture, int x, int y, int width, int height) {
		return addRegion(name, texture, x, y, width, height, 0);
	}

	/** Adds a region to the atlas. The specified texture will be disposed when the atlas is disposed. */
	protected S addRegion (String name, T texture, int x, int y, int width, int height, int layer) {
		textures.add(texture);
		S region = makeAtlasRegion((T)texture, layer, x, y, width, height, -1, name, 0, 0, 0, 0, 0, 0, false, null, null);
		regions.add(region);
		return region;
	}

	/** Returns all regions in the atlas. */
	public Array<S> getRegions () {
		return regions;
	}

	/** Returns the first region found with the specified name. This method uses string comparison to find the region, so the
	 * result should be cached rather than calling this method multiple times.
	 * @return The region, or null. */
	public S findRegion (String name) {
		for (int i = 0, n = regions.size; i < n; i++)
			if (regions.get(i).toString().equals(name)) return regions.get(i);
		return null;
	}

	/** Returns the first region found with the specified name and index. This method uses string comparison to find the region, so
	 * the result should be cached rather than calling this method multiple times.
	 * @return The region, or null. */
	public S findRegion (String name, int index) {
		for (int i = 0, n = regions.size; i < n; i++) {
			S region = regions.get(i);
			if (!region.toString().equals(name)) continue;
			if (region.getIndex() != index) continue;
			return region;
		}
		return null;
	}

	/** Returns all regions with the specified name, ordered by smallest to largest {@linkplain GLTextureRegion#getIndex() index}.
	 * This method uses string comparison to find the regions, so the result should be cached rather than calling this method
	 * multiple times. */
	public Array<S> findRegions (String name) {
		Array<S> matched = new Array();
		for (int i = 0, n = regions.size; i < n; i++) {
			S region = regions.get(i);
			if (region.toString().equals(name)) matched.add(copyAtlasRegion(region));
		}
		return matched;
	}

	/** @return the textures of the pages, unordered */
	public ObjectSet<T> getTextures () {
		return textures;
	}

	/** Releases all resources associated with this TextureAtlas instance. This releases all the textures backing all
	 * TextureRegions and Sprites, which should no longer be used after calling dispose. */
	public void dispose () {
		for (T texture : textures)
			texture.dispose();
		textures.clear();
	}

	static final Comparator<Region> indexComparator = new Comparator<Region>() {
		public int compare (Region region1, Region region2) {
			int i1 = region1.index;
			if (i1 == -1) i1 = Integer.MAX_VALUE;
			int i2 = region2.index;
			if (i2 == -1) i2 = Integer.MAX_VALUE;
			return i1 - i2;
		}
	};

	static String readValue (BufferedReader reader) throws IOException {
		String line = reader.readLine();
		int colon = line.indexOf(':');
		if (colon == -1) throw new GdxRuntimeException("Invalid line: " + line);
		return line.substring(colon + 1).trim();
	}

	/** Returns the number of tuple values read (1, 2 or 4). */
	static int readTuple (BufferedReader reader) throws IOException {
		String line = reader.readLine();
		int colon = line.indexOf(':');
		if (colon == -1) throw new GdxRuntimeException("Invalid line: " + line);
		int i = 0, lastMatch = colon + 1;
		for (i = 0; i < 3; i++) {
			int comma = line.indexOf(',', lastMatch);
			if (comma == -1) break;
			tuple[i] = line.substring(lastMatch, comma).trim();
			lastMatch = comma + 1;
		}
		tuple[i] = line.substring(lastMatch).trim();
		return i + 1;
	}

}
