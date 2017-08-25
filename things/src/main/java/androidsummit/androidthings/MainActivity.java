package androidsummit.androidthings;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;

import java.io.IOException;

import android.util.Log;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.bmx280.Bmx280;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.cap12xx.Cap12xx;
import com.google.android.things.contrib.driver.cap12xx.Cap12xxInputDriver;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.ht16k33.Ht16k33;
import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String gpioButtonPinName = "BUS NAME";
    private static final String I2C_BUS = "BUS NAME";
    private Button mButton, buttonB, buttonC;
    private Cap12xxInputDriver mInputDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupCapacitiveTouchButtons();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyButton();
        destroyCapacitiveTouchButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupButton();
    }

    private void setupButton() {
        try {
            mButton = RainbowHat.openButtonA();
            buttonB = RainbowHat.openButtonB();
            buttonC = RainbowHat.openButtonC();

            final Speaker buzzer = RainbowHat.openPiezo();

            mButton.setOnButtonEventListener(new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button button, boolean pressed) {
                    try {
                        buzzer.play(31);
                        wait(500);
                        buzzer.stop();
                    } catch (Exception e) {

                    }
                }
            });

            buttonB.setOnButtonEventListener(new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button button, boolean pressed) {
                    for (int i = 0; i<500; i++) {
                        playFrequency(buzzer, i);
                    }
                }
            });

            buttonC.setOnButtonEventListener(new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button button, boolean pressed) {
                    try {
                        buzzer.stop();
                    } catch (Exception e) {

                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    private void playFrequency(final Speaker buzzLightYear, int freq) {
        try {
            buzzLightYear.play(freq);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void destroyButton() {
        if (mButton != null) {
            Log.i(TAG, "Closing button");
            try {
                mButton.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing button", e);
            } finally {
                mButton = null;
            }
        }
    }

    private void setupCapacitiveTouchButtons() {
        // Set input key codes
        int[] keyCodes = {
                KeyEvent.KEYCODE_1,
                KeyEvent.KEYCODE_2,
                KeyEvent.KEYCODE_3,
                KeyEvent.KEYCODE_4,
                KeyEvent.KEYCODE_5,
                KeyEvent.KEYCODE_6,
                KeyEvent.KEYCODE_7,
                KeyEvent.KEYCODE_8
        };

        try {
            mInputDriver = new Cap12xxInputDriver(this,
                    I2C_BUS,
                    null,
                    Cap12xx.Configuration.CAP1208,
                    keyCodes);

            // Disable repeated events
            mInputDriver.setRepeatRate(Cap12xx.REPEAT_DISABLE);
            // Block touches above 4 unique inputs
            mInputDriver.setMultitouchInputMax(4);

            mInputDriver.register();

        } catch (IOException e) {
            Log.w(TAG, "Unable to open driver connection", e);
        }
    }

    private void destroyCapacitiveTouchButtons() {
        if (mInputDriver != null) {
            mInputDriver.unregister();

            try {
                mInputDriver.close();
            } catch (IOException e) {
                Log.w(TAG, "Unable to close touch driver", e);
            } finally {
                mInputDriver = null;
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Handle key events from captouch inputs
        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_8:
                Log.d(TAG, "Captouch key released: " + event.getKeyCode());
                return true;
            default:
                Log.d(TAG, "Unknown key released: " + keyCode);
                return super.onKeyUp(keyCode, event);
        }
    }

}
