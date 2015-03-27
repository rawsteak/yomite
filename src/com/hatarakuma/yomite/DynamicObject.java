package com.hatarakuma.yomite;

import java.nio.Buffer;

import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.utils.MeshObject;

public class DynamicObject extends MeshObject {

	private float[] mVertices = new float[] {
	        3.0f, -2.0f, 0.0f, //bottom-left corner
	       11.0f, -2.0f, 0.0f, //bottom-right corner
	       11.0f,  3.5f, 0.0f, //top-right corner
	        3.0f,  3.5f, 0.0f  //top-left corner
	   };

/*	private static final float[] mVertices = new float[] {
	         3.0f, -2.0f, 0.0f, //bottom-left corner
	        11.0f, -2.0f, 0.0f, //bottom-right corner
	        11.0f,  3.5f, 0.0f, //top-right corner
	         3.0f,  3.5f, 0.0f  //top-left corner
	    };*/
	
    private static final float[] mNormals = new float[] {
            0.0f, 0.0f, 1.0f, //normal at bottom-left corner
            0.0f, 0.0f, 1.0f, //normal at bottom-right corner
            0.0f, 0.0f, 1.0f, //normal at top-right corner
            0.0f, 0.0f, 1.0f  //normal at top-left corner
       };
        
    private static final float[] mTexCoords = new float[] {
			0.0f, 0.0f, //tex-coords at bottom-left corner
			1.0f, 0.0f, //tex-coords at bottom-right corner
			1.0f, 1.0f, //tex-coords at top-right corner
			0.0f, 1.0f  //tex-coords at top-left corner
    };
    
    private final short[] mIndices = new short[] {
			0,1,2, //triangle 1
			2,3,0 // triangle 2
    };

    public void setVertices (float[] newVertices) {
    	mVertices = newVertices;
    	mVertBuff = fillBuffer(mVertices);
    }
    
    Buffer mVertBuff;
    Buffer mTexCoordBuff;
    Buffer mNormBuff;
    Buffer mIndBuff;
    boolean bBCFound = false;
    boolean bBMPCreated = false;
    int intTextureIndex = 0;
    
    public Vec2F srcCoord;

    public int getTextureIndex() {
    	return intTextureIndex;
    }
    
    public void setBMPCreated(boolean flag) {
    	bBMPCreated = flag;
    }
    
    public void setTextureIndex(int newIndex) {
    	intTextureIndex = newIndex;
    }
    
    public void setBCFound(boolean flag) {
    	bBCFound = flag;
    }

    public DynamicObject (float[] initVertices) {
    	mVertices = initVertices;
        mVertBuff = fillBuffer(mVertices);
        mTexCoordBuff = fillBuffer(mTexCoords);
        mNormBuff = fillBuffer(mNormals);
        mIndBuff = fillBuffer(mIndices);
    }
    
    public DynamicObject () {
           mVertBuff = fillBuffer(mVertices);
           mTexCoordBuff = fillBuffer(mTexCoords);
           mNormBuff = fillBuffer(mNormals);
           mIndBuff = fillBuffer(mIndices);
    }
    
    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = mVertBuff;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = mTexCoordBuff;
                break;
            case BUFFER_TYPE_INDICES:
                result = mIndBuff;
                break;
            case BUFFER_TYPE_NORMALS:
                result = mNormBuff;
            default:
                break;
        }
        return result;
    }
    
    
    @Override
    public int getNumObjectVertex()
    {
        return mVertices.length / 3;
    }
    
    
    @Override
    public int getNumObjectIndex()
    {
        return mIndices.length;
    }
}
