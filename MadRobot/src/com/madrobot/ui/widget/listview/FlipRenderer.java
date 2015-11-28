package com.madrobot.ui.widget.listview;

import static javax.microedition.khronos.opengles.GL10.GL_AMBIENT;
import static javax.microedition.khronos.opengles.GL10.GL_COLOR_BUFFER_BIT;
import static javax.microedition.khronos.opengles.GL10.GL_DEPTH_BUFFER_BIT;
import static javax.microedition.khronos.opengles.GL10.GL_DEPTH_TEST;
import static javax.microedition.khronos.opengles.GL10.GL_LEQUAL;
import static javax.microedition.khronos.opengles.GL10.GL_LIGHT0;
import static javax.microedition.khronos.opengles.GL10.GL_LIGHTING;
import static javax.microedition.khronos.opengles.GL10.GL_MODELVIEW;
import static javax.microedition.khronos.opengles.GL10.GL_NICEST;
import static javax.microedition.khronos.opengles.GL10.GL_PERSPECTIVE_CORRECTION_HINT;
import static javax.microedition.khronos.opengles.GL10.GL_POSITION;
import static javax.microedition.khronos.opengles.GL10.GL_PROJECTION;
import static javax.microedition.khronos.opengles.GL10.GL_SMOOTH;

import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.view.View;

import com.madrobot.geom.AngleUtils;


 class FlipRenderer implements GLSurfaceView.Renderer {

	private FlipView flipViewController;

	private FlipCards cards;

	private boolean created = false;

	private final LinkedList<Texture> postDestroyTextures = new LinkedList<Texture>();

	 FlipRenderer(FlipView flipViewController, FlipCards cards) {
		this.flipViewController = flipViewController;
		this.cards = cards;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glShadeModel(GL_SMOOTH);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		created = true;

		cards.invalidateTexture();
		flipViewController.reloadTexture();
	}

	 static float[] light0Position = { 0, 0, 100f, 0f };

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);

		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();

		float fovy = 20f;
		float eyeZ = height / 2f / (float) Math.tan(AngleUtils.convertToRadians(fovy / 2));

		GLU.gluPerspective(gl, fovy, (float) width / (float) height, 0.5f,
				Math.max(2500.0f, eyeZ));

		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();

		GLU.gluLookAt(gl, width / 2.0f, height / 2f, eyeZ, width / 2.0f, height / 2.0f, 0.0f,
				0.0f, 1.0f, 0.0f);

		gl.glEnable(GL_LIGHTING);
		gl.glEnable(GL_LIGHT0);

		float lightAmbient[] = new float[] { 3.5f, 3.5f, 3.5f, 1f };
		gl.glLightfv(GL_LIGHT0, GL_AMBIENT, lightAmbient, 0);

		light0Position = new float[] { 0, 0, eyeZ, 0f };
		gl.glLightfv(GL_LIGHT0, GL_POSITION, light0Position, 0);

	}

	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		synchronized (postDestroyTextures) {
			for (Texture texture : postDestroyTextures)
				texture.destroy(gl);
			postDestroyTextures.clear();
		}

		cards.draw(this, gl);
	}

	 void postDestroyTexture(Texture texture) {
		synchronized (postDestroyTextures) {
			postDestroyTextures.add(texture);
		}
	}

	 void updateTexture(int frontIndex, View frontView, int backIndex, View backView) {
		if (created) {
			cards.reloadTexture(frontIndex, frontView, backIndex, backView);
			flipViewController.getSurfaceView().requestRender();
		}
	}

}