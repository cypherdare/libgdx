package com.badlogic.gdx.graphics.batch.batchable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.batch.utils.AttributeOffsets;
import com.badlogic.gdx.graphics.batch.utils.RenderContextAccumulator;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Poly2D extends Poly {
	public float rotation;

	@Override
	protected boolean isPosition3D () {
		return false;
	}

	@Override
	protected boolean isTextureCoordinate3D () {
		return false;
	}
	
	protected void prepareSharedContext (RenderContextAccumulator renderContext) {
		super.prepareSharedContext(renderContext);
		renderContext.setDepthMasking(false);
	}
	
	public void refresh () {
		super.refresh();
		rotation = 0;
	}

	/** Sets the position of the bottom left of the texture region in world space. 
	 * @return This object for chaining. */
	public Poly2D position (float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	/** Sets the position of the bottom left of the texture region in world space. 
	 * @return This object for chaining. */
	public Poly2D position (Vector2 position) {
		x = position.x;
		y = position.y;
		return this;
	}

	public Poly2D rotation (float rotation) {
		this.rotation = rotation;
		return this;
	}

	@Override
	protected int apply (float[] vertices, int vertexStartingIndex, AttributeOffsets offsets, int vertexSize) {
		super.apply(vertices, vertexStartingIndex, offsets, vertexSize);
		final PolygonRegion region = this.region;
		final TextureRegion tRegion = region.getRegion();
		
		final float originX = this.originX;
		final float originY = this.originY;
		final float scaleX = this.scaleX;
		final float scaleY = this.scaleY;
		final float[] regionVertices = region.getVertices();

		final float worldOriginX = x + originX;
		final float worldOriginY = y + originY;
		final float sX = width / tRegion.getRegionWidth();
		final float sY = height / tRegion.getRegionHeight();
		final float cos = MathUtils.cosDeg(rotation);
		final float sin = MathUtils.sinDeg(rotation);

		float fx, fy;
		for (int i = 0, v = vertexStartingIndex + offsets.position, n = regionVertices.length; i < n; i += 2, v += vertexSize) {
			fx = (regionVertices[i] * sX - originX) * scaleX;
			fy = (regionVertices[i + 1] * sY - originY) * scaleY;
			vertices[v] = cos * fx - sin * fy + worldOriginX;
			vertices[v + 1] = sin * fx + cos * fy + worldOriginY;
		}
		
		return numVertices;
	}

	// Chain methods must be overridden to allow return of subclass type.
	
	public Poly2D region (PolygonRegion region){
		super.region(region);
		return this;
	}
	
	public Poly2D size (float width, float height){
		super.size(width, height);
		return this;
	}
	
	public Poly2D origin (float originX, float originY){
		super.origin(originX, originY);
		return this;
	}
	
	public Poly2D color (Color color){
		super.color(color);
		return this;
	}
	
	public Poly2D color (float r, float g, float b, float a){
		super.color(r, g, b, a);
		return this;
	}
	
	public Poly2D color (float floatBits){
		super.color(floatBits);
		return this;
	}
	
	public Poly2D scale (float scaleX, float scaleY){
		super.scale(scaleX, scaleY);
		return this;
	}

}
