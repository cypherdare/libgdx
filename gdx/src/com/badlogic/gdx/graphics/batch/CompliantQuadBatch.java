package com.badlogic.gdx.graphics.batch;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.batch.batchable.Quad2D;
import com.badlogic.gdx.graphics.batch.utils.BatchablePreparation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.NumberUtils;

import static com.badlogic.gdx.graphics.g2d.Batch.*;

/** A batch for single-texture {@linkplain Quad2D Quad2Ds} that creates its own default ShaderProgram. The
 * default ShaderProgram is owned and is disposed automatically with the CompliantQuadBatch.
 * <p>
 * If you want to use multi-texture Quad2Ds, use {@link FlexBatch} directly with a subclass type of Quad2D
 * that uses multiple textures.
 * <p>
 * This implementation of FlexBatch implements the {@link Batch} interface, so it is compatible with {@link Stage}/{@link Actor}, 
 * {@link BitmapFont}, {@link ParticleEffect}, and {@link Sprite}. It cannot support multi-texturing or
 * extra vertex attributes due to its compliance with Batch. */
public class CompliantQuadBatch<T extends Quad2D> extends FlexBatch<T> implements Batch {
	private final T tmp;
	private final ShaderProgram defaultShader;
	private float color = Color.WHITE.toFloatBits();
	private Color tempColor = new Color(1, 1, 1, 1);
	private final float[] tempVertices = new float[20];
	
	/** Constructs a CompliantQuadBatch with a default shader and a capacity of 1000 quads that can be
	 * drawn per flush. The default shader is owned by the CompliantQuadBatch, so it is disposed when the 
	 * CompliantQuadBatch is disposed. If an alternate shader has been applied with {@link #setShader(ShaderProgram)}, 
	 * the default can be used again by setting the shader to null. */
	public CompliantQuadBatch (Class<T> batchableType) {
		this(batchableType, 1000);
	}
	
	/** Constructs a CompliantQuadBatch with a default shader. The default shader is owned by the CompliantQuadBatch, so it is 
	 * disposed when the CompliantQuadBatch is disposed. If an alternate shader has been applied with 
	 * {@link #setShader(ShaderProgram)}, the default can be used again by setting the shader to null. 
	 * @param maxQuads The capacity of quads that can be drawn per batch flush. Must be no greater than 8191.*/
	public CompliantQuadBatch (Class<T> batchableType, int maxQuads) {
		this(batchableType, maxQuads, true);
	}

	/** Constructs a CompliantQuadBatch with a specified capacity and optional default shader.
	 * @param maxQuads The capacity of quads that can be drawn per batch flush. Must be no greater than 8191.
	 * @param generateDefaultShader Whether a default shader should be created. The default shader
	 * is owned by the CompliantQuadBatch, so it is disposed when the CompliantQuadBatch is disposed. If an
	 * alternate shader has been applied with {@link #setShader(ShaderProgram)}, the default can be
	 * used again by setting the shader to null. */
	public CompliantQuadBatch (Class<T> batchableType, int maxQuads, boolean generateDefaultShader) {
		super(batchableType, maxQuads * 4, maxQuads * 2);
		try {
			tmp = batchableType.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("Batchable classes must be public and have an empty constructor.", e);
		}
		if (generateDefaultShader){
			defaultShader = 
				new ShaderProgram(BatchablePreparation.generateGenericVertexShader(1), BatchablePreparation.generateGenericFragmentShader(1));
			if (defaultShader.isCompiled() == false) throw new IllegalArgumentException("Error compiling shader: " + defaultShader.getLog());
			setShader(defaultShader);
		} else {
			defaultShader = null;
		}
	}
	
	@Override
	public void setShader (ShaderProgram shader){
		if (shader == null) 
			shader = defaultShader;
		super.setShader(shader);
	}
	
	@Override
	public void dispose (){
		super.dispose();
		if (defaultShader != null)
			defaultShader.dispose();
	}

	/**
	 * Sets the color used to tint images when they are added to the Batch. Default is Color.WHITE. Does not
	 * affect the color of anything drawn with {@link #draw()}, {@link #draw(Batchable)}, or {@link #draw(Texture, float[], int, int)}.
	 */
	@Override
	public void setColor (Color tint) {
		color = tint.toFloatBits();
	}

	/**
	 * Sets the color used to tint images when they are added to the Batch. Default is equivalent to Color.WHITE. Does not
	 * affect the color of anything drawn with {@link #draw()}, {@link #draw(Batchable)}, or {@link #draw(Texture, float[], int, int)}.
	 */
	@Override
	public void setColor (float r, float g, float b, float a) {
		int intBits = (int)(255 * a) << 24 | (int)(255 * b) << 16 | (int)(255 * g) << 8 | (int)(255 * r);
		color = NumberUtils.intToFloatColor(intBits);
	}

	/**
	 * Sets the color used to tint images when they are added to the Batch. Default is {@link Color#toFloatBits() Color.WHITE.toFloatBits()}. Does not
	 * affect the color of anything drawn with {@link #draw()}, {@link #draw(Batchable)}, or {@link #draw(Texture, float[], int, int)}.
	 */
	@Override
	public void setColor (float color) {
		this.color = color;
	}

	@Override
	public Color getColor () {
		int intBits = NumberUtils.floatToIntColor(color);
		Color color = tempColor;
		color.r = (intBits & 0xff) / 255f;
		color.g = ((intBits >>> 8) & 0xff) / 255f;
		color.b = ((intBits >>> 16) & 0xff) / 255f;
		color.a = ((intBits >>> 24) & 0xff) / 255f;
		return color;
	}

	@Override
	public float getPackedColor () {
		return color;
	}
	
	@Override
	public void draw (Texture texture, float[] spriteVertices, int offset, int count) {
		tmp.refresh();
		tmp.texture(texture);
		super.draw(tmp, spriteVertices, offset, count, 5);
	}

	@Override
	public void draw (Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX,
		float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
		float invTexWidth = 1.0f / texture.getWidth();
		float invTexHeight = 1.0f / texture.getHeight();
		float u = srcX * invTexWidth;
		float v = (srcY + srcHeight) * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = srcY * invTexHeight;
		draw().color(color).texture(texture).position(x, y).origin(originX, originY).size(width, height).scale(scaleX, scaleY)
			.rotation(rotation).region(u, v, u2, v2).flip(flipX, flipY);
	}

	@Override
	public void draw (Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
		int srcHeight, boolean flipX, boolean flipY) {
		float invTexWidth = 1.0f / texture.getWidth();
		float invTexHeight = 1.0f / texture.getHeight();
		float u = srcX * invTexWidth;
		float v = (srcY + srcHeight) * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = srcY * invTexHeight;
		draw().color(color).texture(texture).position(x, y).size(width, height).region(u, v, u2, v2).flip(flipX, flipY);
	}

	@Override
	public void draw (Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
		float invTexWidth = 1.0f / texture.getWidth();
		float invTexHeight = 1.0f / texture.getHeight();
		float u = srcX * invTexWidth;
		float v = (srcY + srcHeight) * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = srcY * invTexHeight;
		draw().color(color).texture(texture).position(x, y).region(u, v, u2, v2);
	}

	@Override
	public void draw (Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
		draw().color(color).texture(texture).position(x, y).size(width, height).region(u, v, u2, v2);
	}

	@Override
	public void draw (Texture texture, float x, float y) {
		draw().color(color).texture(texture).position(x, y);
	}

	@Override
	public void draw (Texture texture, float x, float y, float width, float height) {
		draw().color(color).texture(texture).position(x, y).size(width, height);
	}

	@Override
	public void draw (TextureRegion region, float x, float y) {
		draw().color(color).textureRegion(region).position(x, y);
	}

	@Override
	public void draw (TextureRegion region, float x, float y, float width, float height) {
		draw().color(color).textureRegion(region).position(x, y).size(width, height);
	}

	@Override
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation) {
		draw().color(color).textureRegion(region).position(x, y).origin(originX, originY).size(width, height).scale(scaleX, scaleY)
			.rotation(rotation);
	}

	@Override
	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation, boolean clockwise) {
		draw().color(color).textureRegion(region).position(x, y).origin(originX, originY).size(width, height).scale(scaleX, scaleY)
			.rotation(rotation).rotateCoordinates90(clockwise);
	}

	@Override
	public void draw (TextureRegion region, float width, float height, Affine2 transform) {
		float[] vertices = tempVertices;

		vertices[U1] = region.getU();
		vertices[V1] = region.getV2();
		vertices[U2] = region.getU();
		vertices[V2] = region.getV();
		vertices[U3] = region.getU2();
		vertices[V3] = region.getV();
		vertices[U4] = region.getU2();
		vertices[V4] = region.getV2();
		
		float color = this.color;
		vertices[C1] = color;
		vertices[C2] = color;
		vertices[C3] = color;
		vertices[C4] = color;
		
		// construct corner points
		vertices[X1] = transform.m02;
		vertices[Y1] = transform.m12;
		vertices[X2] = transform.m01 * height + transform.m02;
		vertices[Y2] = transform.m11 * height + transform.m12;
		vertices[X3] = transform.m00 * width + transform.m01 * height + transform.m02;
		vertices[Y3] = transform.m10 * width + transform.m11 * height + transform.m12;
		vertices[X4] = transform.m00 * width + transform.m02;
		vertices[Y4] = transform.m10 * width + transform.m12;
		
		draw(region.getTexture(), vertices, 0, 20);
	}
	
}
