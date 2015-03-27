package com.hatarakuma.yomite;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import koby.rotationcipher.Cipher;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Marker;
import com.qualcomm.vuforia.MarkerResult;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.SampleApplicationSession;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vec2I;
import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.utils.CubeShaders;
import com.qualcomm.vuforia.utils.SampleUtils;
import com.qualcomm.vuforia.utils.Texture;

//import com.qualcomm.vuforia.samples.VuforiaSamples.R;
//import com.google.zxing.integration.android.IntentIntegrator;
//import com.google.zxing.integration.android.IntentResult;

// The renderer class for the FrameMarkers sample. 
public class FrameMarkerRenderer implements GLSurfaceView.Renderer
{
    private static final String LOGTAG = "FrameMarkerRenderer";
    
    SampleApplicationSession vuforiaAppSession;
    FrameMarkers mActivity;

    //AsyncTask<String, Integer, String> mFindBarcode = null;
    //AsyncTask<int[], Integer, String> mbFindBarcode = null;
    AsyncTask<Bitmap, Integer, String> mbFindBarcode = null;
    
    public boolean mIsActive = false;
    public boolean mBarcodeDecoded = false;
    String strScanContent = "";
    
    private Vector<Texture> mTextures;
    
    // OpenGL ES 2.0 specific:
    private int shaderProgramID = 0;
    private int vertexHandle = 0;
    private int normalHandle = 0;
    private int textureCoordHandle = 0;
    private int mvpMatrixHandle = 0;
    private int texSampler2DHandle = 0;
    
    // Constants:
    static private float kLetterScale = 25.0f;
    static private float kLetterTranslate = 25.0f;
    
//    private QObject qObject = new QObject();
//    private CObject cObject = new CObject();
//    private AObject aObject = new AObject();
//   private RObject rObject = new RObject();
    //private DynamicObject dObject = new DynamicObject();
    
    private ArrayList<DynamicObject> dObjects = new ArrayList<DynamicObject>();
    
    boolean pic = false;
    // was the marker scanned for barcodes already?
    boolean mMarkerScanned = false;
    // was a marker found for the target yet?
    boolean mFoundBarcode = false;
    // was a bitmap/texture created yet?
    boolean mBitmapCreated = false;/////////////////////////////////
    
    Texture texDecodedImage;
    String contents = "";
    
    private int mViewWidth = 0;
    private int mViewHeight = 0;
    
    int xxx = 0;
    
    //public Cipher indexCipher = new Cipher(12);
    public Cipher indexCipher;
    
    Square left;
    Square right;
	
    public FrameMarkerRenderer(FrameMarkers activity,
        SampleApplicationSession session)
    {
        mActivity = activity;
        vuforiaAppSession = session;
        left = new Square();
        right = new Square();
    }
    
    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        
        // Call function to initialize rendering:
        initRendering();
        
        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }
    
    
    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
        
        mViewWidth = width;
        mViewHeight = height;
        
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }
    
    
    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;
        
        // Call our function to render content
        renderFrame();

        /////////////////////////////////////
        /*
        gl.glTranslatef(-1.25f, 0.0f, -4.0f);
        left.draw(gl);
        
        gl.glTranslatef(3.0f, 0.0f, 0.0f);
        right.draw(gl);
        */
        /////////////////////////////////////
        
        /*if ( (pic == false) && (isScan == true) ) {
            saveScreenShot(0, 0, mViewWidth, mViewHeight, "test.png");
            pic = true;
            Log.d(LOGTAG, "SCREENSHOT: " + Environment.getExternalStorageDirectory());
        }*/
    }
    
    
    void initRendering()
    {
        Log.d(LOGTAG, "initRendering");
        
        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
            : 1.0f);
        
        // Now generate the OpenGL texture objects and add settings
        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, t.mData);
        }
        
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
            CubeShaders.CUBE_MESH_VERTEX_SHADER,
            CubeShaders.CUBE_MESH_FRAGMENT_SHADER);
        
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "texSampler2D");
        
        if( dObjects.size() == 0 ) {
        	// if no objects, create them
        	for( int z=0; z < mActivity.dataSet.length; z++ ) {
        		dObjects.add(new DynamicObject());
        	}
        }
    }

    public Bitmap grabPixels(int x, int y, int w, int h, int trial, int ID) {
        int b[] = new int[w * (y + h)];
        int bt[] = new int[w * h];
        IntBuffer ib = IntBuffer.wrap(b);
        ib.position(0);

        //GLES20.glReadPixels(x, 0, w, y + h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
        //GLES20.glReadPixels(x, y, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
        GLES20.glReadPixels(x, y, w, y+h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
  
        for (int i = 0, k = 0; i < h; i++, k++) {
            for (int j = 0; j < w; j++) {
                int pix = b[i * w + j];
                int pb = (pix >> 16) & 0xff;
                int pr = (pix << 16) & 0x00ff0000;
                int pix1 = (pix & 0xff00ff00) | pr | pb;
                bt[(h - k - 1) * w + j] = pix1;
            }
        }
  
        Bitmap sb = Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
        
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/yomite_bc_scans");
        myDir.mkdirs();
        String fname = "grabpixels-" + trial + "-ID-" + ID + ".jpg";
        File file = new File(myDir, fname);
        Log.i(LOGTAG, "" + file + "coord: " + x + "," + y + "," + w + "," + (y+h));
        if (file.exists())
        file.delete();
        try {
        FileOutputStream out = new FileOutputStream(file);
        sb.compress(Bitmap.CompressFormat.JPEG, 90, out);
        out.flush();
        out.close();
        } catch (Exception e) {
        e.printStackTrace();
        }
        
        return sb;
    }
    
    public Vec2F camPointToScreenPoint(Vec2F camPoint) {
    	CameraCalibration camCal = CameraDevice.getInstance().getCameraCalibration();
    	//Vec2F camCalSize = camCal.getSize();
    	
    	int xOffset = ((int) (mViewWidth - camCal.getSize().getData()[0])) / ((int) (2.0f + camCal.getPrincipalPoint().getData()[0]));
    	int yOffset = ((int) (mViewHeight - camCal.getSize().getData()[1])) / ((int) (2.0f + camCal.getPrincipalPoint().getData()[1]));

    	if( mViewWidth < mViewHeight ) {
    		// camera is rotated
    		int rotatedX = (int) (mViewHeight - camPoint.getData()[1]);
    		int rotatedY = (int) (camPoint.getData()[0]);
    		
    		return new Vec2F((rotatedX * camCal.getSize().getData()[0]) / (float)(mViewHeight + xOffset),
    						 (rotatedY * camCal.getSize().getData()[1]) / (float)(mViewWidth + yOffset));
    	}
    	return new Vec2F((camPoint.getData()[0] * camCal.getSize().getData()[0]) / (float)(mViewWidth + xOffset),
				 		 (camPoint.getData()[1] * camCal.getSize().getData()[1]) / (float)(mViewHeight + yOffset));
    }
    
    void renderFrame()
    {
        // Clear color and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        // Get the state from Vuforia and mark the beginning of a rendering
        // section
        State state = Renderer.getInstance().begin();
        //Renderer.getInstance().getVideoBackgroundTextureInfo();
        /*VideoBackgroundConfig config = Renderer.getInstance().getVideoBackgroundConfig();
        config.setEnabled(true);
        config.setSynchronous(true);
        config.setPosition(new Vec2I(0,0));
        
        
        config.setSize(new Vec2I(0, 0));        
        Renderer.getInstance().setVideoBackgroundConfig(config);
        Renderer.getInstance().drawVideoBackground();*/
        
        // Explicitly render the Video Background
        Renderer.getInstance().drawVideoBackground();
        /////////////////////////////////////////////////////////////////////////////
        // don't draw background, draw on 2 squares
        // in sert new config
        
        /*VideoBackgroundConfig config = Renderer.getInstance().getVideoBackgroundConfig();
        config.setEnabled(true);
        config.setSynchronous(true);
        config.setPosition(new Vec2I(-175, -425));
        
        // right eye
        int xSize = 900, ySize = 900;
        config.setSize(new Vec2I(xSize, ySize));        
        Renderer.getInstance().setVideoBackgroundConfig(config);
        Renderer.getInstance().drawVideoBackground();

        /*
        // left eye
        config = null;
        config = Renderer.getInstance().getVideoBackgroundConfig();
        config.setEnabled(true);
        config.setSynchronous(true);
        config.setPosition(new Vec2I(-175, 500));
        
        xSize = 900; ySize = 900;
        config.setSize(new Vec2I(xSize, ySize));        
        Renderer.getInstance().setVideoBackgroundConfig(config);
        Renderer.getInstance().drawVideoBackground();*/

        
        
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        // We must detect if background reflection is active and adjust the
        // culling direction.
        // If the reflection is active, this means the post matrix has been
        // reflected as well,
        // therefore standard counter clockwise face culling will result in
        // "inside out" models.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW);  // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW);   // Back camera
            
        /*int numTrack = state.getNumTrackableResults();
        ArrayList<int[]> bmpBoundary = new ArrayList<int[]>();
        if( numTrack != 0) {
	        int intSpan = mViewHeight / numTrack;
	        
	        for (int yyy = 0; yyy < numTrack; yyy++) {
	        	// create boundary to pass into screengrab
	        	//int[] newBoundary = new int [] {0,0,mViewWidth,(mViewHeight/(yyy+1))};
	        	bmpBoundary.add(new int [] {0,
	        								(yyy*intSpan),
	        								mViewWidth,
	        								(mViewHeight/(yyy+1)+(yyy*intSpan))});
	        }
        }*/
        
        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
        	// GET PRELIMINARY INFORMATION
            // Get the trackable:
            TrackableResult trackableResult = state.getTrackableResult(tIdx);
            
            float[] modelViewMatrix = Tool.convertPose2GLMatrix(
                trackableResult.getPose()).getData();

            

            
            // Check the type of the trackable:
//            assert (trackableResult.getType() == MarkerTracker.getClassType());
            MarkerResult markerResult = (MarkerResult) (trackableResult);
            Marker marker = (Marker) markerResult.getTrackable();

        	// if a barcode hasn't been found for this specific marker, find it
        	if (dObjects.get(marker.getMarkerId()).bBCFound == false) {
        		
                Vec2F camCoord = Tool.projectPoint(CameraDevice.getInstance().getCameraCalibration(), trackableResult.getPose(), new Vec3F(0,0,0));
                //Vec2F scrCoord = camPointToScreenPoint(camCoord);
                dObjects.get(marker.getMarkerId()).srcCoord = camPointToScreenPoint(camCoord);
                
                Log.d(LOGTAG, "modelViewMatrix" + modelViewMatrix.toString() + " x:" + dObjects.get(marker.getMarkerId()).srcCoord.getData()[1] + ", y: " + dObjects.get(marker.getMarkerId()).srcCoord.getData()[0] + "  maxW+maxH: " + mViewWidth + "," + mViewHeight);
                
                int intX0 = 0;
                int intY0 = mViewHeight - ((int) dObjects.get(marker.getMarkerId()).srcCoord.getData()[0] - 105);
                int intX1 = mViewWidth;
                int intY1 = mViewHeight - ((int) dObjects.get(marker.getMarkerId()).srcCoord.getData()[0] + 220);
                if (intY0 < 0) {
                	intY0 = 0;
                	intY1 = 325;
                } else if (intY1 > mViewHeight) {
                	intY1 = mViewHeight;
                	intY0 = mViewHeight - 325;
                }
        		
        		//int[] bMap = null;
        		Bitmap bMap;
        		// if find barcode task isn't running, run it
        		// otherwise, do nothing
        		if( mbFindBarcode == null ) {
        			// hasn't run yet
        			//bMap = grabPixelate(0, 0, mViewWidth, mViewHeight);
        			bMap = grabPixels(0, 0, mViewWidth, mViewHeight,xxx,marker.getMarkerId());
        			//bMap = grabPixels(intX0,intY0,intX1,intY1,xxx);
        			//bMap = grabPixels(intX0,intY0,intX1,300,xxx,marker.getMarkerId());
        			mbFindBarcode = new bFindBarcode().execute(bMap);
        			xxx += 1;
        			Log.d(LOGTAG, "async task run #" + xxx + "marker.id=" + marker.getMarkerId());
        		} else if( mbFindBarcode.getStatus() == AsyncTask.Status.RUNNING ) {
        			// running already, do nothing for now
        			Log.d(LOGTAG, "async task running" + xxx + "marker.id=" + marker.getMarkerId());
        		} else if( mbFindBarcode.getStatus() == AsyncTask.Status.FINISHED ) {
        			// it ran, but didn't find anything, or you wouldn't be here reading this
        			// run it again
        			//bMap = grabPixelate(0, 0, mViewWidth, mViewHeight);
        			bMap = grabPixels(0, 0, mViewWidth, mViewHeight,xxx,marker.getMarkerId());
        			//bMap = grabPixels(intX0,intY0,intX1,300,xxx,marker.getMarkerId());
        			mbFindBarcode = new bFindBarcode().execute(bMap);
        			xxx += 1;
        			Log.d(LOGTAG, "async task run #" + xxx + "marker.id=" + marker.getMarkerId());
        		} else if( mbFindBarcode.getStatus() == AsyncTask.Status.PENDING ) {
        			Log.d(LOGTAG, "async task pending " + "marker.id=" + marker.getMarkerId());
        		}
        		
        		// success? set object accordingly
        		dObjects.get(marker.getMarkerId()).setBCFound(mFoundBarcode);
        		// reset found flag
        		mFoundBarcode = false;
        	} else {
        		// Log.d(LOGTAG, "BARCODE ALREADY FOUND " + contents);
        		// if bitmap not created, do it now
        		if (dObjects.get(marker.getMarkerId()).bBMPCreated == false) {
	        		// Create an empty, mutable bitmap
	        		Bitmap bitmap = Bitmap.createBitmap(512, 256, Bitmap.Config.ARGB_8888);
	        		// get a canvas to paint over the bitmap
	        		Canvas canvas = new Canvas(bitmap);
	        		//bitmap.eraseColor(0);
	
	        		// get a background image from resources
	        		// note the image format must match the bitmap format
	        		Drawable background = mActivity.getBaseContext().getResources().getDrawable(R.drawable.background);
	        		background.setBounds(0, 0, 512, 256);
	        		background.draw(canvas); // draw the background to our bitmap
	
	        		String decString = indexCipher.Decrypt(contents);
	        		
	        		// Draw the text
	        		Paint textPaint = new Paint();
	        		textPaint.setTextSize(32);
	        		textPaint.setAntiAlias(true);
	        		textPaint.setARGB(0xFF, 0x40, 0xFF, 0x40);
	        		// draw the text centered (30 char limit)
	        		int intCounter = 30;
	        		int intStart = 0;
	        		int intEnd = 30;
	        		//int intCharsLeft = contents.length();
	        		int intCharsLeft = decString.length();
	        		//int intStringLength = contents.length();
	        		int intStringLength = decString.length();
	        		int intYCoord = 50;
	        		int intMarkerID = marker.getMarkerId();
	        		
	        		while( intCharsLeft >= 0 ) {
	        			//canvas.drawText(contents.substring(intStart, intEnd), 40, intYCoord, textPaint);
	        			canvas.drawText(decString.substring(intStart, intEnd), 40, intYCoord, textPaint);
	        			intCharsLeft -= 30;
	        			intStart += 30;
	        			if( intCharsLeft < intCounter )
	        				{ intEnd = intStringLength; }
	        			else
	        				{ intEnd += 30; }
	        			intYCoord += 35;
	        			
	        		}
	        		
	        		try {
	                    String path = Environment.getExternalStorageDirectory() + "/CHECKME" + intMarkerID + ".PNG";
	                    Log.d(LOGTAG, path);
	                     
	                    File file = new File(path);
	                    file.createNewFile();
	                     
	                    FileOutputStream fos = new FileOutputStream(file);
	                    bitmap.compress(CompressFormat.PNG, 100, fos);

	                    fos.flush();
	                     
	                    fos.close();
	                     
	                } catch (Exception e) {
	                    Log.d(LOGTAG, e.getStackTrace().toString());
	                }
	        		
	        		mTextures.add(Texture.loadTextureFromSDCard("CHECKME" + intMarkerID + ".PNG"));
	        		initRendering();
	        		
	        		dObjects.get(marker.getMarkerId()).setTextureIndex(mTextures.size()-1);
	        		dObjects.get(marker.getMarkerId()).bBMPCreated = true;
	        		
        		}

        	}
//            assert (textureIndex < mTextures.size());
        	if (mTextures.size() <= 2) { 
        		Log.d(LOGTAG, "textures size: " + mTextures.size());
        	}

            if( xxx == 14 ) { 
            	Log.d(LOGTAG, "stop here: " + dObjects.get(marker.getMarkerId()).bBMPCreated);
            }
//            Texture thisTexture = mTextures.get(3);
            Texture thisTexture = mTextures.get(dObjects.get(marker.getMarkerId()).getTextureIndex());
            
            // Select which model to draw:
            Buffer vertices = null;
            Buffer normals = null;
            Buffer indices = null;
            Buffer texCoords = null;
            int numIndices = 0;
            
            vertices = dObjects.get(marker.getMarkerId()).getVertices();
            normals = dObjects.get(marker.getMarkerId()).getNormals();
            indices = dObjects.get(marker.getMarkerId()).getIndices();
            texCoords = dObjects.get(marker.getMarkerId()).getTexCoords();
            numIndices = dObjects.get(marker.getMarkerId()).getNumObjectIndex();
            
/*            switch (marker.getMarkerId())
            //switch (i)
            {
                case 0:
                	// if the new bitmap is created, use new object
                        vertices = dObjects.get(marker.getMarkerId()).getVertices();
                        normals = dObjects.get(marker.getMarkerId()).getNormals();
                        indices = dObjects.get(marker.getMarkerId()).getIndices();
                        texCoords = dObjects.get(marker.getMarkerId()).getTexCoords();
                        numIndices = dObjects.get(marker.getMarkerId()).getNumObjectIndex();
                    break;
/*                case 1:
                    vertices = cObject.getVertices();
                    normals = cObject.getNormals();
                    indices = cObject.getIndices();
                    texCoords = cObject.getTexCoords();
                    numIndices = cObject.getNumObjectIndex();
                    break;
                case 2:
                    vertices = aObject.getVertices();
                    normals = aObject.getNormals();
                    indices = aObject.getIndices();
                    texCoords = aObject.getTexCoords();
                    numIndices = aObject.getNumObjectIndex();
                    break;
                default:
                    vertices = rObject.getVertices();
                    normals = rObject.getNormals();
                    indices = rObject.getIndices();
                    texCoords = rObject.getTexCoords();
                    numIndices = rObject.getNumObjectIndex();
                    break;
            }*/
            
            float[] modelViewProjection = new float[16];
            
            if (mActivity.isFrontCameraActive())
                Matrix.rotateM(modelViewMatrix, 0, 180, 0.f, 1.0f, 0.f);

            Matrix.translateM(modelViewMatrix, 0, -kLetterTranslate,
                    -kLetterTranslate, 0.f);
            Matrix.scaleM(modelViewMatrix, 0, kLetterScale, kLetterScale,
                kLetterScale);
            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);
            
            GLES20.glUseProgram(shaderProgramID);
            
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                false, 0, vertices);
            GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                false, 0, normals);
            GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                GLES20.GL_FLOAT, false, 0, texCoords);
            
            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glEnableVertexAttribArray(normalHandle);
            GLES20.glEnableVertexAttribArray(textureCoordHandle);
            
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                thisTexture.mTextureID[0]);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices,
                GLES20.GL_UNSIGNED_SHORT, indices);
            
            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glDisableVertexAttribArray(normalHandle);
            GLES20.glDisableVertexAttribArray(textureCoordHandle);
            
            SampleUtils.checkGLError("FrameMarkers render frame");
            
        }
        
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        // left eye
        /*config = null;
        config = Renderer.getInstance().getVideoBackgroundConfig();
        config.setEnabled(true);
        config.setSynchronous(true);
        config.setPosition(new Vec2I(-175, 500));
        
        xSize = 900; ySize = 900;
        config.setSize(new Vec2I(xSize, ySize));        
        Renderer.getInstance().setVideoBackgroundConfig(config);
        Renderer.getInstance().drawVideoBackground();*/
        
        Renderer.getInstance().end();
    }
    
    public void setRotationCipher(int index) {
    	// set cipher index here
    	indexCipher = new Cipher(index);
    }

	public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;
        
    }
	
	public class bFindBarcode extends AsyncTask<Bitmap, Integer, String> {
		
		protected void onPreExecute(Bitmap b) {
			// gets called first
		}
		
		protected String doInBackground(Bitmap... params) {
			Bitmap bMap = params[0];
			int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
			bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        	//LuminanceSource source = new RGBLuminanceSource(mViewWidth, mViewHeight, intArray);
			LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
        	// create hints
            Hashtable<DecodeHintType, Object> hint = new Hashtable<DecodeHintType, Object>();
            hint.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
            Vector<BarcodeFormat> vector = new Vector<BarcodeFormat>(8);
            vector.addElement(BarcodeFormat.PDF_417);
            //vector.addElement(BarcodeFormat.UPC_A);
            //vector.addElement(BarcodeFormat.UPC_E);
            //vector.addElement(BarcodeFormat.EAN_13);
            //vector.addElement(BarcodeFormat.EAN_8);
            hint.put(DecodeHintType.POSSIBLE_FORMATS, vector);
            hint.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            
            // init reader
        	Reader bmpReader = new MultiFormatReader();
            //Reader bmpReader = new PDF417Reader();
        	Result readResult;

        	try {
				readResult = bmpReader.decode(bitmap, hint);
				//contents = readResult.getText();
				contents = readResult.toString();
				bMap.recycle();
				bitmap = null;
				mFoundBarcode = true;
				bmpReader.reset();
				intArray = null;
        	} catch (NotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				Log.d(LOGTAG, "task#: " + xxx);
			} catch (ChecksumException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (FormatException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
        	if (mFoundBarcode) Log.d(LOGTAG, "task#: " + xxx + " BARCODE FOUND " + contents);
        	return "";
			//return findaBarcode();
		}
		
		protected void onProgressUpdated(Integer...progress) {
			// progress bar
		}
		
		protected void onPostExecute(String result) {
			// post execute
		}
	}

}
