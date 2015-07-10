package com.lafarge.truckmix.service;

import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.communicator.Communicator;

public interface ITruckMixService {

    //
    // Bluetooth specific
    //

    /**
     * Return the state of the connection of the calculator.
     *
     * @return true if the device is connected to the calculator, false otherwise
     */
    boolean isConnected();

    //
    // Communicator specific
    //

    /**
     * Set Truck parameters to the service, will be send next time the calculator will request them.
     *
     * @param parameters The truck parameters
     * @throws IllegalArgumentException If parameters is null
     */
    void setTruckParameters(final TruckParameters parameters);

    /**
     * Set Delivery parameters to the service, will be send next time the calculator will request
     * them.
     *
     * @param parameters The delivery parameters
     * @throws IllegalArgumentException If parameters is null
     */
    void deliveryNoteReceived(final DeliveryParameters parameters);

    /**
     * Tell the calculator to pass in "delivery in progress" or not, you should not call this
     * method without having called <code>setTruckParameters</code> and
     * <code>deliveryNoteReceived</code> before.
     *
     * @param accepted Pass true to start a delivery or false, to reset the state of the calculator
     */
    void acceptDelivery(final boolean accepted);

    /**
     * Tell the calculator to end a delivery in progress.
     */
    void endDelivery();

    /**
     * Tell the calculator to allow or disallow a water addition request, you should have received
     * a request from the calculator before use this method.
     * Also, if you have <code>TruckMix#setWaterRequestAllowed(boolean)</code> to <code>true</code>,
     * this method will have no effect.
     *
     * @param allowWaterAddition true to accept the water addition request, otherwise false.
     */
    void allowWaterAddition(final boolean allowWaterAddition);

    /**
     * Change the external display state on the truck.
     * Note that if the external display state is for example currently activated, passing true will
     * have no effect on it.
     *
     * @param activated true to activate the external display, or false to shutdown it
     */
    void changeExternalDisplayState(final boolean activated);

    /**
     * Return the state of the external display
     * Note this method is here only of UI purpose, you can't trust this value because if you
     * change the external display and there is no connection the change will have no effect.
     *
     * @see ITruckMixService#changeExternalDisplayState(boolean)
     * @return true if the external is activated, otherwise false.
     */
    boolean isExternalDisplayActivated();

    /**
     * Return last information that was sent by the calculator.
     * If a value has expired, then it will be null.
     * The object is reset each time a new delivery is started.
     *
     * @return The Information.
     * @see com.lafarge.truckmix.communicator.Communicator.Information
     */
    Communicator.Information getLastInformation();

    //
    // Options
    //

    /**
     * Allow water actions, useful for countries that doesn't allow water addition in the concrete.
     * By default, water request is not allowed.
     *
     * @param waterRequestAllowed true if you want to interact with the water, otherwise false to disable it.
     */
    void setWaterRequestAllowed(final boolean waterRequestAllowed);

    /**
     * Return the state of the water request allowance
     */
    boolean isWaterRequestAllowed();

    /**
     * Activate the quality tracking, if true, events will be send to the EventListener passed
     * in constructor.
     * By default, quality tracking is not enabled.
     *
     * @param qualityTrackingEnabled true to activate the quality tracking, otherwise false to disable it.
     */
    void setQualityTrackingActivated(final boolean qualityTrackingEnabled);

    /**
     * Return the state of the quality tracking
     */
    boolean isQualityTrackingActivated();
}
