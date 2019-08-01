package sample;

import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import sun.swing.JLightweightFrame;

import java.awt.*;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class SwingNode extends javafx.embed.swing.SwingNode {

    private Field lwFrame;

    public SwingNode() {
        super();

        Field[] fields = this.getClass().getSuperclass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("lwFrame")) {
                field.setAccessible(true);
                lwFrame = field;
                break;
            }
        }
        // Override key handler due to differences in JCEF's key handling
        this.setEventHandler(KeyEvent.ANY, event -> {
            JLightweightFrame frame;
            try {
                frame = (JLightweightFrame) lwFrame.get(this);
            } catch (Exception e) {
                return;
            }
            if (frame != null) {
                if (!event.getCharacter().isEmpty()) {
                    if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.TAB) {
                        event.consume();
                    }

                    int swingID = fxKeyEventTypeToKeyID(event);
                    if (swingID >= 0) {
                        int swingModifiers = fxKeyModsToKeyMods(event);
                        int swingKeyCode = event.getCode().impl_getCode();
                        char swingChar = event.getCharacter().charAt(0);
                        if (((short) swingChar) == 0) {
                            swingChar = (char) swingKeyCode;
                        }
                        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
                            String swingWhen = event.getText();
                            if (swingWhen.length() == 1) {
                                swingChar = swingWhen.charAt(0);
                            }
                        }

                        long swingWhen1 = System.currentTimeMillis();
                        java.awt.event.KeyEvent keyEvent = new java.awt.event.KeyEvent(frame, swingID, swingWhen1, swingModifiers, swingKeyCode, swingChar);
                        // set scan code, javafx.scene.input.KeyEvent has not scancode,but cef use,
                        // in master 8fec3be CefBrowser_N.cpp,
                        // cef_event.native_key_code = (scanCode << 16) |  // key scan code
                        //        1;  // key repeat count
                        long scanCode = 0;
                        switch (swingKeyCode) {
                            case 8: // backSpace
                                scanCode = 14;
                                break;
                            case 17:// ctrl
                                scanCode = 29;
                                break;
                            case 16: // shift
                                scanCode = 42;
                                break;
                            case 10: // enter
                                scanCode = 28;
                                break;
                            case 18: //alt
                                scanCode = 56;
                                break;
                            case 127: // del
                                scanCode = 83;
                                break;
                            case 37: // left
                                scanCode = 75;
                                break;
                            case 38: //up
                                scanCode = 72;
                                break;
                            case 39://right
                                scanCode = 77;
                                break;
                            case 40://down
                                scanCode = 80;
                                break;
                            case 86://v, used for ctrl + v
                                scanCode = 47;
                                break;
                            case 67://c, used for ctrl + c
                                scanCode = 46;
                                break;
                            case 65://a, used for ctrl + a
                                scanCode = 30;
                                break;
                            default:
                                break;
                        }
                        try {
                            Field scanCodeField = null;
                            scanCodeField = java.awt.event.KeyEvent.class.getDeclaredField("scancode");
                            scanCodeField.setAccessible(true);
                            scanCodeField.setLong(keyEvent, scanCode);
                            scanCodeField.setAccessible(false);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        AccessController.doPrivileged(new PostEventAction(keyEvent));
                    }
                }
            }
        });
    }

    private int fxKeyEventTypeToKeyID(KeyEvent event) {
        EventType eventType = event.getEventType();
        if (eventType == KeyEvent.KEY_PRESSED) {
            return 401;
        } else if (eventType == KeyEvent.KEY_RELEASED) {
            return 402;
        } else if (eventType == KeyEvent.KEY_TYPED) {
            return 400;
        } else {
            throw new RuntimeException("Unknown KeyEvent type: " + eventType);
        }
    }

    private int fxKeyModsToKeyMods(KeyEvent event) {
        int mods = 0;
        if (event.isAltDown()) {
            mods |= 512;
        }

        if (event.isControlDown()) {
            mods |= 128;
        }

        if (event.isMetaDown()) {
            mods |= 256;
        }

        if (event.isShiftDown()) {
            mods |= 64;
        }

        return mods;
    }

    private class PostEventAction implements PrivilegedAction<Void> {
        private AWTEvent event;

        public PostEventAction(AWTEvent event) {
            this.event = event;
        }

        public Void run() {
            EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
            eq.postEvent(this.event);
            return null;
        }
    }

}