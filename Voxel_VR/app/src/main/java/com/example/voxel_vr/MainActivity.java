package com.example.voxel_vr;

import static javax.microedition.khronos.opengles.GL10.GL_DEPTH_TEST;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLES32;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.voxel_vr.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends Activity implements GLSurfaceView.Renderer, View.OnTouchListener, GestureDetector.OnGestureListener {

    enum InputMode { // Pour les contrôles de la caméra
        MOVE, ROTATE, UP_DOWN
    }
    private InputMode mCurrInputMode = InputMode.MOVE;

    ActivityMainBinding mBinding;

    private GestureDetector mGestureDetector;

    private Chunk mainChunk = new Chunk();

    private float[] model;
    private float[] view;
    private float[] projection;
    private float[] identity;
    private int glModel, glView, glProjection;
    private float mWidth;
    private float mHeight;

    final static int BytesPerFloat = 4;
    final static int FloatsPerPosition = 3;
    private static String TAG = MainActivity.class.getSimpleName();

    private String readAssetFile(String fileName){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader( new InputStreamReader(getAssets().open(fileName)) );
            StringBuilder sb = new StringBuilder();
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                sb.append(mLine);
                sb.append("\n");
            }
            return  sb.toString();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
        return null;
    }

    private int compileShader(String name, int type){
        String shaderCode;
        if(type == GLES32.GL_VERTEX_SHADER){
            shaderCode = readAssetFile(name+".vert");
        }else{
            shaderCode = readAssetFile(name+".frag");
        }
        int shaderId = GLES32.glCreateShader(type);
        GLES32.glShaderSource(shaderId, shaderCode);
        GLES32.glCompileShader(shaderId);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES32.glGetShaderiv(shaderId, GLES32.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            String str = GLES32.glGetShaderInfoLog(shaderId);
            Log.e(TAG, "Error compiling shader: " + str);
            GLES32.glDeleteShader(shaderId);
            return -1;
        }
        return shaderId;
    }

    private int createProgram(String name){
        int vertexShaderId =  compileShader(name, GLES32.GL_VERTEX_SHADER);

        int fragmentShaderId = compileShader(name, GLES32.GL_FRAGMENT_SHADER);

        int programId = GLES32.glCreateProgram();
        GLES32.glAttachShader(programId, vertexShaderId);
        GLES32.glAttachShader(programId, fragmentShaderId);
        GLES32.glLinkProgram(programId);
        checkErr(0);
        int[] success = new int[1];
        GLES32.glGetProgramiv(programId, GLES32.GL_LINK_STATUS, success, 0);
        // error
        if(success[0] == 0){
            String str = GLES32.glGetProgramInfoLog(programId);
            Log.e(TAG, str);
        }
        return programId;
    }

    private int gl_vArray;
    private int[] glTexBuffers;
    private int gl_program;

    private int sendTextureToGl(final int resourceId) {
        final int[] textureHandle = new int[1];
        GLES32.glGenTextures(1, textureHandle, 0);
        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error generating texture name.");
        }
        final int tex = textureHandle[0];

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId, options);

        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, tex);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_NEAREST);
        GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES32.glGenerateMipmap(GLES32.GL_TEXTURE_2D);
        bitmap.recycle();

        return tex;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mBinding.surfaceView.setEGLContextClientVersion(3);
        //mBinding.surfaceView.setEGLConfigChooser(8,8,8,8,16,0);
        mBinding.surfaceView.setRenderer(this);
        mGestureDetector = new GestureDetector(this, this);
        mBinding.surfaceView.setOnTouchListener(this);
        mBinding.moveImageButton.setOnClickListener(v -> {setCurrentInputMode(InputMode.MOVE);});
        mBinding.rotateImageButton.setOnClickListener(v -> {setCurrentInputMode(InputMode.ROTATE);});
        mBinding.upDownImageButton.setOnClickListener(v -> {setCurrentInputMode(InputMode.UP_DOWN);});
        mBinding.resetImageButton.setOnClickListener(v -> {resetCameraPosition();});
        mBinding.breakImageButton.setOnClickListener(v -> {tryBreakBlock();});

        setCurrentInputMode(mCurrInputMode);
    }

    private void tryBreakBlock() {
        // A faire
    }

    private float[] camPos = {0, 0, -5}; // Position Vec3 de la caméra
    private float camXAngle = 0; // Pitch
    private float camYAngle = 0; // Yaw
    private void resetCameraPosition() {
        camPos[0] = 0; camPos[1] = 0; camPos[2] = -5;
        camXAngle = 0;
        camYAngle = 0;

        // Matrix.setRotateEulerM(view, 0, camXAngle, camYAngle, 0); // Deprecated, sinon il y a setRotateEulerM2 mais je n'arrive pas à la faire fonctionner (problème de version apparemment)
        Matrix.setRotateM(view, 0, camXAngle, 1, 0, 0);
        Matrix.setRotateM(view, 0, camYAngle, 0, 1, 0);
        //Matrix.setRotateM(view, 0, 0, 0, 0, 1);
        Matrix.translateM(view, 0, camPos[0], camPos[1], camPos[2]);
    }

    private void setCurrentInputMode(InputMode inputMode) {
        int selectedColor = Color.argb(255, 255, 200, 0);
        int notSelectedColor = Color.argb(255, 200, 200, 200);
        mCurrInputMode = inputMode;
        if(mCurrInputMode == InputMode.MOVE){
            mBinding.moveImageButton.setBackgroundColor(selectedColor);
        }else{
            mBinding.moveImageButton.setBackgroundColor(notSelectedColor);
        }
        if(mCurrInputMode == InputMode.ROTATE){
            mBinding.rotateImageButton.setBackgroundColor(selectedColor);
        }else{
            mBinding.rotateImageButton.setBackgroundColor(notSelectedColor);
        }
        if(mCurrInputMode == InputMode.UP_DOWN){
            mBinding.upDownImageButton.setBackgroundColor(selectedColor);
        }else{
            mBinding.upDownImageButton.setBackgroundColor(notSelectedColor);
        }
    }

    private void checkErr(int loop){
        int err = GLES32.glGetError();
        if (err != 0){
            Log.d("Err("," " + err + ") in loop (" + loop + ")");
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES32.glClearColor(0.28F,0.56F,1.0F,1.0F);
        GLES32.glEnable(GL_DEPTH_TEST);
        //GLES32.glDepthFunc(GLES32.GL_LESS);
        this.glTexBuffers = new int[5];
        glTexBuffers[0] = sendTextureToGl(R.drawable.pumpkin);
        glTexBuffers[1] = sendTextureToGl(R.drawable.wood);
        glTexBuffers[2] = sendTextureToGl(R.drawable.cobble);
        glTexBuffers[3] = sendTextureToGl(R.drawable.obsi);
        glTexBuffers[4] = sendTextureToGl(R.drawable.gravel);

        gl_program = createProgram("modelviewprojection");

        GLES32.glUseProgram(gl_program);
        int gl_position = GLES32.glGetAttribLocation(gl_program,"a_Position");
        glModel = GLES32.glGetUniformLocation(gl_program,"a_Model");
        glView = GLES32.glGetUniformLocation(gl_program,"a_View");
        glProjection = GLES32.glGetUniformLocation(gl_program,"a_Projection");

        int[] tmp = sendVertexDataToGL(mainChunk.getVertices(), gl_position);
        gl_vArray = tmp[0];

        model = new float[16];
        view = new float[16];
        projection = new float[16];
        identity = new float[16];
        Matrix.setIdentityM(model, 0);
        Matrix.setIdentityM(view, 0);
        Matrix.setIdentityM(projection, 0);
        Matrix.setIdentityM(identity, 0);

        Matrix.translateM(view, 0, 0.0F, 0.0F, -3.0F); // Placement initial de la caméra
    }

    private int[] sendVertexDataToGL(float[] data, int gl_position){
        ByteBuffer vertices_data_bytes = ByteBuffer.allocateDirect(data.length*BytesPerFloat)
                .order(ByteOrder.nativeOrder());
        FloatBuffer vertices_data = vertices_data_bytes.asFloatBuffer();
        vertices_data.put(data).position(0);

        int[] tmp = new int[1];
        GLES32.glGenVertexArrays(1, tmp, 0);
        int gl_array_id = tmp[0];

        GLES32.glGenBuffers(1, tmp, 0);
        int gl_buffer_id = tmp[0];

        GLES32.glBindVertexArray(gl_array_id);

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, gl_buffer_id);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, data.length*4, vertices_data, GLES32.GL_STATIC_DRAW);

        GLES32.glEnableVertexAttribArray(gl_position);
        GLES32.glVertexAttribPointer(gl_position, FloatsPerPosition, GLES32.GL_FLOAT, false, FloatsPerPosition*BytesPerFloat, 0);

        return new int[]{gl_array_id, gl_buffer_id};
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;

        GLES32.glViewport(0,0,width,height);
        float aspect = mWidth/mHeight;
        Matrix.perspectiveM(projection, 0, 45.0F, aspect, 0.1F, 100.0F);
    }

    int loop = 0;
    @Override
    public void onDrawFrame(GL10 gl) {
        checkErr(++loop);
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

        // Matrix.rotateM(model, 0, 1F, 0,1,0);
        GLES32.glUseProgram(gl_program);
        GLES32.glUniformMatrix4fv(glModel, 1, false, model, 0);
        GLES32.glUniformMatrix4fv(glView, 1, false, view, 0);
        GLES32.glUniformMatrix4fv(glProjection, 1, false, projection, 0);

        GLES32.glBindVertexArray(gl_vArray);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, glTexBuffers[2]);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLES,0,mainChunk.getNumberOfFloat()/FloatsPerPosition);
    }

    // GestureDetector.OnGestureListener
    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        switch (mCurrInputMode){
            case MOVE:
                camPos[0] += 5*distanceX/mWidth;
                camPos[2] += 5*distanceY/mHeight;
                break;
            case ROTATE:
                camXAngle += 45*distanceY/mHeight;
                camYAngle += 45*distanceX/mWidth;
                break;
            case UP_DOWN:
                camPos[1] -= 10*distanceY/mHeight;
                break;
        }

        // Matrix.setRotateEulerM(view, 0, camXAngle, camYAngle, 0);
        float[] rotationMatrixX = new float[16];
        float[] rotationMatrixY = new float[16];
        Matrix.setRotateM(rotationMatrixX, 0, camXAngle, 1, 0, 0);
        Matrix.setRotateM(rotationMatrixY, 0, camYAngle, 0, 1, 0);
        Matrix.multiplyMM(view, 0, rotationMatrixY, 0, rotationMatrixX, 0);

        Matrix.translateM(view, 0, camPos[0], camPos[1], camPos[2]);

        return false;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    // View.OnTouchListener
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }
}
