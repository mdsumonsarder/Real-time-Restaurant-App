package verzac.tastebuddy.courier_activity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import verzac.tastebuddy.DeliveryStatusMapActivity;
import verzac.tastebuddy.R;
import verzac.tastebuddy.models.Order;

public class CourierDeliveryStatusMapActivity extends DeliveryStatusMapActivity {
    // this activity is for couriers
    public static final String LOG_TAG = "CustomerDeliveryStatus";

    @Override
    protected void updateStatusText(TextView statusView) {
        switch(getOrder().getStatus()){
            case Order.STATUS_PENDING:
                statusView.setText(getString(R.string.status_courier_pending_order));
                break;
            case Order.STATUS_PICKUP:
                statusView.setText(getString(R.string.status_courier_pickup_order));
                break;
            case Order.STATUS_DELIVERING:
                statusView.setText(getString(R.string.status_courier_delivery_order));
                break;
            case Order.STATUS_DELIVERED:
                statusView.setText(getString(R.string.status_courier_delivered_order));
                break;
            default:
                Log.e(LOG_TAG, "Unhandled order status detected: " + String.valueOf(getOrder().getStatus()));
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.deliveryStatusContextButton:
                Button button = getContextButton();
                button.setEnabled(false);
                switch (getOrder().getStatus()) {
                    case Order.STATUS_PENDING:
                        Log.e(LOG_TAG, "Unexpected behavior detected.");
                        break;
                    case Order.STATUS_PICKUP:
                        getOrder().setStatus(Order.STATUS_DELIVERING);
                        getActiveOrderRef().child(getOrder().getFbKey()).setValue(getOrder());
                        break;
                    case Order.STATUS_DELIVERING:
                        //courier has confirmed delivery
                        getOrder().setStatus(Order.STATUS_DELIVERED);
                        getActiveOrderRef().child(getOrder().getFbKey()).setValue(getOrder());
                        break;
                    case Order.STATUS_DELIVERED:
                        onDeliveryCompleted();
                        break;
                    default:
                        Log.e(LOG_TAG, "Unhandled order status detected: " + String.valueOf(getOrder().getStatus()));
                        break;
                }
        }
    }

    @Override
    protected void updateConfirmButton(Button button) {
        switch(getOrder().getStatus()){
            case Order.STATUS_PENDING:
                button.setEnabled(false);
                button.setVisibility(View.GONE);
                break;
            case Order.STATUS_PICKUP:
                button.setEnabled(true);
                button.setText(getString(R.string.button_pickup_delivery));
                button.setVisibility(View.VISIBLE);
                break;
            case Order.STATUS_DELIVERING:
                button.setEnabled(true);
                button.setText(R.string.button_finish_delivery);
                button.setVisibility(View.VISIBLE);
                break;
            case Order.STATUS_DELIVERED:
                button.setEnabled(false);
                button.setText(R.string.button_finish_delivery);
                button.setVisibility(View.VISIBLE);
                break;
            default:
                Log.e(LOG_TAG, "Unhandled order status detected: " + String.valueOf(getOrder().getStatus()));
        }
    }


}
