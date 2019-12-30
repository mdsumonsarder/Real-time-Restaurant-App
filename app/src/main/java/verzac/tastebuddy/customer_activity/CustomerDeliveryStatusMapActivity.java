package verzac.tastebuddy.customer_activity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import verzac.tastebuddy.DeliveryStatusMapActivity;
import verzac.tastebuddy.R;
import verzac.tastebuddy.models.Order;

public class CustomerDeliveryStatusMapActivity extends DeliveryStatusMapActivity {
    //this activity is for customers
    public static final String LOG_TAG = "CustomerDeliveryStatus";
    private Marker m_CourierLocationMarker;
    private ValueEventListener m_CourierUserCEL;

    @Override
    protected void updateStatusText(TextView statusView) {
        switch(getOrder().getStatus()){
            case Order.STATUS_PENDING:
                statusView.setText(getString(R.string.status_customer_pending_order));
                break;
            case Order.STATUS_PICKUP:
                statusView.setText(getString(R.string.status_customer_pickup_order));
                break;
            case Order.STATUS_DELIVERING:
                statusView.setText(getString(R.string.status_customer_delivery_order));
                break;
            case Order.STATUS_DELIVERED:
                statusView.setText(getString(R.string.status_customer_delivered_order));
                break;
            default:
                Log.e(LOG_TAG, "Unhandled order status detected: " + String.valueOf(getOrder().getStatus()));
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        Button button = getContextButton();
        switch (v.getId()) {
            case R.id.deliveryStatusContextButton:
                button.setEnabled(false);
                switch (getOrder().getStatus()) {
                    case Order.STATUS_PENDING:
                        onDeliveryCompleted();
                        break;
                    case Order.STATUS_PICKUP:
                        onDeliveryCompleted();
                        break;
                    case Order.STATUS_DELIVERING:
                        //courier has confirmed delivery
                        getOrder().setStatus(Order.STATUS_DELIVERED);
                        getActiveOrderRef().child(getOrder().getFbKey()).setValue(getOrder());
                        break;
                    case Order.STATUS_DELIVERED:
                        button.setEnabled(false);
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
        button.setText(R.string.button_finish_delivery);
        button.setVisibility(View.VISIBLE);
        Drawable buttonDraw = DrawableCompat.wrap(button.getBackground());
        switch(getOrder().getStatus()){
            case Order.STATUS_PENDING:
                button.setEnabled(true);
                buttonDraw.setTint(Color.RED);
                button.setText(getString(R.string.button_cancel_delivery));
                break;
            case Order.STATUS_PICKUP:
                button.setEnabled(true);
                buttonDraw.setTint(Color.GREEN);
                button.setText(getString(R.string.button_cancel_delivery));
                break;
            case Order.STATUS_DELIVERING:
                button.setEnabled(false);
                buttonDraw.setTint(Color.GREEN);
                button.setText(getString(R.string.button_finish_delivery));
                break;
            case Order.STATUS_DELIVERED:
                button.setEnabled(true);
                buttonDraw.setTint(Color.GREEN);
                button.setText(getString(R.string.button_finish_delivery));
                break;
            default:
                Log.e(LOG_TAG, "Unhandled order status detected: " + String.valueOf(getOrder().getStatus()));
        }
    }


    protected void initializeMarkers(){
        super.initializeMarkers();
        //Add listener for courier position
        FirebaseUser currentUser = m_Auth.getCurrentUser();
        if (currentUser != null) {
            if (!(currentUser.getUid().equals(getOrder().getCourierID()))) { // set this to false in production
                if(m_CourierUserCEL == null) {
                    initializeMarkerCourierListener();
                }
            }
        }
        else{
            onNotLoggedIn();
        }
    }

    private void initializeMarkerCourierListener() {
        if (getOrder().getCourierID() != null) {
            m_CourierUserCEL = getUserRef().child(getOrder().getCourierID())
                    .child(LOCATION_FIELD).addValueEventListener(new ValueEventListener() {
                        // this listener listens to any changes in the courier's location
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                double latitude = dataSnapshot.child(LATITUDE_FIELD).getValue(double.class);
                                double longitude = dataSnapshot.child(LONGITUDE_FIELD).getValue(double.class);
                                LatLng courierPos = new LatLng(latitude, longitude);
                                updateCourierMarker(courierPos);
                                Log.d("FirebaseLocation", "Updating courier position...");
                            } else {
                                Log.e(LOG_TAG, "FIREBASE: User data is missing for " + getOrder().getCourierID());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    public void updateCourierMarker(LatLng courierPos){
        // method updates the courier's marker's location on the map
        if(m_CourierLocationMarker == null){
            m_CourierLocationMarker = getGoogleMap().addMarker(new MarkerOptions()
                    .position(courierPos)
                    .title("Courier Location")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_local_shipping_black_24dp)));
        }
        else{
            m_CourierLocationMarker.setPosition(courierPos);
        }
    }
}
