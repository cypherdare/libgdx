package com.badlogic.gdx.graphics.batch.utils;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.ObjectIntMap;

/** Provides fast and convenient access to VertexAttribute offsets, in float-size units. */
public class AttributeOffsets {

	public final VertexAttributes attributes;
	private final ObjectIntMap<String> byAlias;
	private final int[] byIndex;
	
	public AttributeOffsets (VertexAttributes attributes){
		this.attributes = attributes;
		byAlias = new ObjectIntMap<String>(attributes.size());
		byIndex = new int[attributes.size()];
		for (int i=0; i<byIndex.length; i++){
			VertexAttribute attribute = attributes.get(i);
			int offset = attribute.offset / 4;
			byAlias.put(attribute.alias, offset);
			byIndex[i] = offset;
		}
	}
	
	 /** Refreshes the mapping of aliases to offsets. Call if any {@link VertexAttribute#alias alias} has been changed. */
	public void udpate (){
		byAlias.clear();
		for (VertexAttribute attribute : attributes)
			byAlias.put(attribute.alias, attribute.offset / 4);
	}
	
	/** Get the VertexAttribute offset, in float-size units, looking it up by its {@link VertexAttribute#alias alias}.
	 * @return The offset, or -1 if the value is not found.*/
	public int get (String alias){
		return byAlias.get(alias, -1);
	}
	
	/** Get the VertexAttribute offset, in float-size units, looking it up by its index in the VertexAttributes.
	 * @return The offset.
	 * @throws IndexOutOfBoundsException If the given attribute index does not exist in the VertexAttributes. */
	public int get (int attributeIndex) {
		return byIndex[attributeIndex];
	}
}
