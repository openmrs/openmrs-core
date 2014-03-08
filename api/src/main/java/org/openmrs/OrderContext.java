package org.openmrs;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Ak
 * Date: 3/8/14
 * Time: 10:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class OrderContext {

    private OrderType orderType;
    private CareSetting careSetting;
    private Map<String, Object> contextAttributes;

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }


    public CareSetting getCareSetting() {
        return careSetting;
    }

    public void setCareSetting(CareSetting careSetting) {
        this.careSetting = careSetting;
    }

    public Map<String, Object> getContextAttributes() {
        if(contextAttributes!=null)
            return contextAttributes;
        else
        {
            return new HashMap<String, Object>();
        }
    }

    public void setContextAttributes(Map<String, Object> contextAttributes) {
        this.contextAttributes = contextAttributes;
    }

}
