package io.github.keep2iron.pejoy.listener;


/**
 *  when original is enabled , callback immediately when user check or uncheck original.
 */
public interface OnCheckedListener {
    void onCheck(boolean isChecked);
}