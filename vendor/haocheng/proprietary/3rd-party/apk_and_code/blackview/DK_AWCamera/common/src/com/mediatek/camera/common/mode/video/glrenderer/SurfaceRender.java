package com.mediatek.camera.common.mode.video.glrenderer;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class SurfaceRender {
    private static final String TAG = SurfaceRender.class.getSimpleName();
    private static final int SIZEOF_FLOAT = 4;

    // Orthographic projection matrix.
    private final float[] mIdentityMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private static final int[] mTempIntArray = new int[1];
    // Handles to the GL program and various components of it.
    private int mProgramHandle = -1;
    private int muMVPMatrixLoc = -1;
    private int maPositionLoc = -1;
    private int maTextureCoordinate = -1;
    private int maFabbyTextureCoordinate = -1;
    private int muTexMatrixLoc = -1;
    private int msTextureLoc = -1;
    private int msFabbyTextureLoc = -1;

    // private FloatBuffer mVertexArray;
    // private FloatBuffer mTexCoordArray;
    private int mVertexCount;
    private int mCoordsPerVertex;
    private int mVertexStride;

    private int mOrientention = 0;

    // Simple vertex shader, used for all programs.
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uTexMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
                    "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    private static final String VERTEX_SHADER_2 =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uTexMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "attribute vec4 aFabbyTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "varying vec2 vFabbyTextureCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
                    "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
                    "    vFabbyTextureCoord = (uTexMatrix * aFabbyTextureCoord).xy;\n" +
                    "}\n";

    // Simple fragment shader for use with "normal" 2D textures.
    private static final String FRAGMENT_SHADER_2D =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";
    private static final String FRAGMENT_SHADER_FABBY_2D =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "varying vec2 vFabbyTextureCoord;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "uniform sampler2D sFabbyTexture;\n" +
                    "void main() {\n" +
                    "    highp vec4 maskColor = texture2D(sFabbyTexture, vFabbyTextureCoord);\n" +
                    "    bool isMask = (maskColor.r + maskColor.g + maskColor.b) > 1.0;" +
                    "    gl_FragColor = isMask ? texture2D(sTexture, vTextureCoord) : vec4(1.0,1.0,1.0,1.0);\n" +
                    "}\n";
    private static final String FRAGMENT_SHADER_OES2D =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";
    /**
     * A "full" square, extending from -1 to +1 in both dimensions.  When the model/view/projection
     * matrix is identity, this will exactly cover the viewport.
     * <p>
     * The texture coordinates are Y-inverted relative to RECTANGLE.  (This seems to work out
     * right with external textures from SurfaceTexture.)
     */
    private static final float FULL_RECTANGLE_COORDS[] = {
            -1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f,
    };
    private static final float FULL_RECTANGLE_COORDS_FRONT[] = {
            1.0f, 1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            -1.0f, -1.0f,
    };
    private static final float FULL_RECTANGLE_COORDS_FACEU[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };
    private static final float FULL_RECTANGLE_TEX_COORDS[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    };
    private static final float FULL_RECTANGLE_TEX_COORDS_FABBY[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };
    private static final FloatBuffer FULL_RECTANGLE_BUF =
            createFloatBuffer(FULL_RECTANGLE_COORDS);
    private static final FloatBuffer FULL_RECTANGLE_BUF_FRON =
            createFloatBuffer(FULL_RECTANGLE_COORDS_FRONT);
    private static final FloatBuffer FULL_RECTANGLE_TEX_BUF =
            createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);
    private static final FloatBuffer FULL_RECTANGLE_TEX_BUF_FABBY =
            createFloatBuffer(FULL_RECTANGLE_TEX_COORDS_FABBY);
    private static final FloatBuffer FULL_RECTANGLE_BUF_FACEU =
            createFloatBuffer(FULL_RECTANGLE_COORDS_FACEU);

    public void onSurfaceCreate() {
        onSurfaceCreate(false);
    }

    public void onSurfaceCreate2() {
        onSurfaceCreate(true);
    }

    public void onSurfaceCreate(boolean usOES) {
        mProgramHandle = createProgram(usOES ? VERTEX_SHADER_2 : VERTEX_SHADER, usOES ? FRAGMENT_SHADER_FABBY_2D : FRAGMENT_SHADER_2D);
        if (mProgramHandle == 0) {
            Log.e(TAG, "Unable to create program!");
            return;
            // throw new RuntimeException("Unable to create program");
        }
        Matrix.setIdentityM(mIdentityMatrix, 0);
        // get locations of attributes and uniforms
        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        maTextureCoordinate = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        if (usOES)
            maFabbyTextureCoordinate = GLES20.glGetAttribLocation(mProgramHandle, "aFabbyTextureCoord");
        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
        msTextureLoc = GLES20.glGetUniformLocation(mProgramHandle, "sTexture");
        if (usOES)
            msFabbyTextureLoc = GLES20.glGetUniformLocation(mProgramHandle, "sFabbyTexture");

        //mVertexArray = FULL_RECTANGLE_BUF;
        //mTexCoordArray = FULL_RECTANGLE_TEX_BUF;
        mCoordsPerVertex = 2;
        mVertexStride = mCoordsPerVertex * SIZEOF_FLOAT;
        mVertexCount = FULL_RECTANGLE_COORDS.length / mCoordsPerVertex;

        // Set the background color.
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Disable depth testing -- we're 2D only.
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        // Don't need backface culling.  (If you're feeling pedantic, you can turn it on to
        // make sure we're defining our shapes correctly.)
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        GLES20.glDisable(GLES20.GL_BLEND);
    }

    public void draw(int textureId, boolean isFront) {
        GLES20.glUseProgram(mProgramHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mIdentityMatrix, 0);
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, mIdentityMatrix, 0);

        GLES20.glEnableVertexAttribArray(maPositionLoc);
        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, mCoordsPerVertex,
                GLES20.GL_FLOAT, false, mVertexStride, (isFront && mOrientention % 180 == 0) ? FULL_RECTANGLE_BUF_FRON : FULL_RECTANGLE_BUF);

        GLES20.glEnableVertexAttribArray(maTextureCoordinate);
        GLES20.glVertexAttribPointer(maTextureCoordinate, 2,
                GLES20.GL_FLOAT, false, mVertexStride, FULL_RECTANGLE_TEX_BUF);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);

        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordinate);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }

    public void drawFaceuTexture(int textureId, float[] surfaceTransform, int fabbyId) {
        GLES20.glUseProgram(mProgramHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        if (fabbyId != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + 1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fabbyId);
            GLES20.glUniform1i(msFabbyTextureLoc, 1);
        }

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mIdentityMatrix, 0);
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, surfaceTransform == null ? mIdentityMatrix : surfaceTransform, 0);

        GLES20.glEnableVertexAttribArray(maPositionLoc);
        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, mCoordsPerVertex,
                GLES20.GL_FLOAT, false, mVertexStride, FULL_RECTANGLE_BUF_FACEU);

        GLES20.glEnableVertexAttribArray(maTextureCoordinate);
        GLES20.glVertexAttribPointer(maTextureCoordinate, 2,
                GLES20.GL_FLOAT, false, mVertexStride, FULL_RECTANGLE_TEX_BUF);

        if (fabbyId != -1) {
            GLES20.glEnableVertexAttribArray(maFabbyTextureCoordinate);
            GLES20.glVertexAttribPointer(maFabbyTextureCoordinate, 2,
                    GLES20.GL_FLOAT, false, mVertexStride, FULL_RECTANGLE_TEX_BUF_FABBY);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);

        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordinate);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }

    public void drawFaceuTextureLogo(int logoTexture, boolean isFront) {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glUseProgram(mProgramHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, logoTexture);

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, mIdentityMatrix, 0);

        GLES20.glEnableVertexAttribArray(maPositionLoc);
        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, mCoordsPerVertex,
                GLES20.GL_FLOAT, false, mVertexStride, (isFront && mOrientention % 180 == 0) ? FULL_RECTANGLE_BUF_FRON : FULL_RECTANGLE_BUF);

        GLES20.glEnableVertexAttribArray(maTextureCoordinate);
        GLES20.glVertexAttribPointer(maTextureCoordinate, 2,
                GLES20.GL_FLOAT, false, mVertexStride, FULL_RECTANGLE_TEX_BUF);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);

        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordinate);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glUseProgram(0);
    }

    public void setLogoMatrix(float mX, float mY, boolean isFront) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.scaleM(mModelMatrix, 0, mX, mY, 1f);
        int zoom = 1;
        if (isFront && mOrientention % 180 == 0)
            zoom = -1;
        float tX = 1f / mX - 1;
        float tY = 1f / mY - 1;
        switch (mOrientention) {
            case 0:
                Matrix.translateM(mModelMatrix, 0, zoom * tX, -zoom * tY, 0);
                break;
            case 90:
                Matrix.translateM(mModelMatrix, 0, zoom * tX, zoom * tY, 0);
                break;
            case 180:
                Matrix.translateM(mModelMatrix, 0, -zoom * tX, zoom * tY, 0);
                break;
            case 270:
                Matrix.translateM(mModelMatrix, 0, -zoom * tX, -zoom * tY, 0);
                break;
        }
    }

    /**
     * Releases the program.
     */
    public void release() {
        GLES20.glDeleteProgram(mProgramHandle);
        mProgramHandle = -1;
    }

    /**
     * Checks to see if the location we obtained is valid.  GLES returns -1 if a label
     * could not be found, but does not set the GL error.
     * <p>
     * Throws a RuntimeException if the location is invalid.
     */
    public static void checkLocation(int location, String label) {
        if (location < 0) {
            throw new RuntimeException("Unable to locate '" + label + "' in program");
        }
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }
        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        if (program == 0) {
            Log.e(TAG, "Could not create program");
        }
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = mTempIntArray;
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: " + GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    /**
     * Compiles the provided shader source.
     *
     * @return A handle to the shader, or 0 on failure.
     */
    public static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shaderType + ":" + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    /**
     * Checks to see if a GLES error has been raised.
     */
    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e(TAG, msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * Allocates a direct float buffer, and populates it with the float array data.
     */
    public static FloatBuffer createFloatBuffer(float[] coords) {
        // Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * SIZEOF_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }

    public void setOrientation(int orientation) {
        // TODO Auto-generated method stub
        mOrientention = orientation;
    }
}
