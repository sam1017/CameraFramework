package com.mediatek.camera.feature.mode.aiworksfacebeauty.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 *
 * Accelerometer: Used to turn on the gravity sensor to get the current orientation of the mobile phone
 * 
 */
public class Accelerometer {
	private static final String TAG = "Accelerometer";

	/**
	 *
	 * @author MatrixCV
	 *
	 * After clockwise rotation, Deg0 is obtained, that is, the vertical screen of the mobile phone is up,
     * as shown in the following figure
	 *  ___________
	 * |           |
	 * |+---------+|
	 * ||         ||
	 * ||         ||
	 * ||         ||
	 * ||         ||
	 * ||         ||
	 * |+---------+|
	 * |_____O_____|
     *The Deg270 definition of the rotation angle of the mobile phone is shown in the figure below.
     *  ___________________
     * | +--------------+  |
     * | |              |  |
     * | |              |  |
     * | |              | O|
     * | |              |  |
     * | |______________|  |
     * ---------------------
	 */

	public static final int Deg0 = 0;
	public static final int Deg90 = 90;
	public static final int Deg180 = 180;
	public static final int Deg270 = 270;

	private SensorManager sensorManager = null;

	private boolean hasStarted = false;
	
	private int rotation;

	private IDirectionListener mListener;

	/**
	 * 
	 * @param ctx
	 * Initialization of Sensors
	 */
	public Accelerometer(Context ctx) {
		sensorManager = (SensorManager) ctx
				.getSystemService(Context.SENSOR_SERVICE);
		rotation = 0;
	}

	public void setDirectionListener(IDirectionListener listener) {
		mListener = listener;
	}

	/**
	 * start Sensor Monitoring
	 */
	public void start() {
		if (hasStarted) return;
		hasStarted = true;
		rotation = 0;
		sensorManager.registerListener(accListener,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	/**
	 * stop Sensor Monitoring
	 */
	public void stop() {
		if (!hasStarted) return;
		hasStarted = false;
		sensorManager.unregisterListener(accListener);
	}

	/**
	 * 
	 * @return
	 * Return to the current Mobile phone's rotation
	 */
	 public int getDirection() {
		return rotation;
	}

	/**
	 * Logical relationship between sensor and mobile phone's rotation
	 */
	private SensorEventListener accListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}

		@Override
		public void onSensorChanged(SensorEvent arg0) {
			//Slog.e(TAG, "arg0 " + arg0.accuracy);
			if (arg0.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				float x = arg0.values[0];
				float y = arg0.values[1];
				float z = arg0.values[2];
				if (Math.abs(x)>3 || Math.abs(y)>3) {
					if (Math.abs(x)>Math.abs(y)) {
						if (x > 0) {
							rotation = Deg270;
						} else {
							rotation = Deg90;
						}
					} else {
						if (y > 0) {
							rotation = Deg0;
						} else {
							rotation = Deg180;
						}
					}
					if (mListener != null)
						mListener.onDirectionChanged(rotation);
				}
			}
		}
	};
}
