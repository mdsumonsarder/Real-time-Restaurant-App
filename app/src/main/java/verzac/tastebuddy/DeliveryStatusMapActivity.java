package verzac.tastebuddy;

import android.location.Location;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import verzac.tastebuddy.helper.FirebaseKeylist;
import verzac.tastebuddy.models.Order;

public abstract class DeliveryStatusMapActivity extends AuthAbstractActivity
        implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener, View.OnClickListener{
    // class which is the focal point of the app; tracks the delivery status of an order
    private static final int MAX_UPDATE_INTERVAL = 10000;
    private static final int MIN_UPDATE_INTERVAL = 1000;
    protected static final String LOCATION_FIELD = "LOCATION";
    protected static final String LATITUDE_FIELD = "LATITUDE";
    protected static final String LONGITUDE_FIELD = "LONGITUDE";
    private static final String LOG_TAG = "DeliveryStatusMap";


    private GoogleMap m_GoogleMap;
    private Marker m_DeliveryLocationMarker;
    private Order m_Order;
    private DatabaseReference m_ActiveOrderRef;
    private DatabaseReference m_UserRef;
    private GoogleApiClient m_APIClient;
    private TextView m_StatusView;
    private TextView m_DeliveredToView;
    private TextView m_DeliveredByView;
    private LinearLayout m_ExtraDeliveryInfoLayout;
    private Button m_ExtraDeliveryInfoExpandButton;
    private ImageView m_ExtraDeliveryInfoExpandIcon;
    private Button m_ContextButton;
    private DatabaseReference m_OrderRef;
    private TextView m_MenuItemTextView;
    private TextView m_PriceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_status_map);
        // initialize Google API client for location services
        if(m_APIClient == null){
            m_APIClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        //initialize database references
        m_ActiveOrderRef = FirebaseDatabase.getInstance().getReference(Order.ORDER_ACTIVE_KEY);
        m_UserRef = FirebaseDatabase.getInstance().getReference(FirebaseKeylist.USER_PROFILE);
        m_OrderRef = FirebaseDatabase.getInstance().getReference(Order.ORDER_KEY);

        //init order
        m_Order = getIntent().getParcelableExtra(Order.ORDER_KEY);
        Log.e("DSMA", m_Order.getFbKey());

        //init activity views
        m_StatusView = (TextView) findViewById(R.id.deliveryStatusTextView);
        m_ExtraDeliveryInfoLayout = (LinearLayout) findViewById(R.id.extraDeliveryInfoLayout);
        m_ExtraDeliveryInfoExpandButton = (Button) findViewById(R.id.expandDeliveryInfoButton);
        m_ExtraDeliveryInfoExpandButton.setOnClickListener(this);
        m_ExtraDeliveryInfoExpandIcon = (ImageView) findViewById(R.id.expandDeliveryInfoIcon);
        m_ContextButton = (Button) findViewById(R.id.deliveryStatusContextButton);
        m_ContextButton.setOnClickListener(this);
        m_MenuItemTextView = (TextView) findViewById(R.id.orderMenuItemsTextView);
        m_PriceTextView = (TextView) findViewById(R.id.orderPriceTextView);

        // initialize the map fragment
        MapFragment mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFrag.getMapAsync(this);
    }

    @Override
    public void onLogin(){
        // empty body because the default onLogin leads to this activity (so
        // overriding this prevents an infinite loop from happening)
    }


    @CallSuper
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.expandDeliveryInfoButton:
                // this button is to expand the delivery info menu on the top screen (just below the status view)
                Log.e(LOG_TAG, "Expand button hit!");
                if(m_ExtraDeliveryInfoLayout.getVisibility() == View.GONE) {
                    m_ExtraDeliveryInfoLayout.setVisibility(View.VISIBLE);
                    m_ExtraDeliveryInfoExpandIcon.setImageResource(R.drawable.ic_keyboard_arrow_up_black_18dp);
                }
                else if(m_ExtraDeliveryInfoLayout.getVisibility() == View.VISIBLE) {
                    m_ExtraDeliveryInfoLayout.setVisibility(View.GONE);
                    m_ExtraDeliveryInfoExpandIcon.setImageResource(R.drawable.ic_keyboard_arrow_down_black_18dp);
                }
                else Log.e(LOG_TAG, "UNHANDLED CASE IN EXPAND BUTTON HIT");
                break;
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        // called when the map has been synchronized with the activity
        m_GoogleMap = googleMap;
        try {
            m_GoogleMap.setMyLocationEnabled(true); // enable Google's blue my location tracker thing
        }
        catch (SecurityException secex){
            Log.e("Location", "Location not enabled.");
        }

        m_GoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_Order.getDestinationPos(), 15));
        initializeOrderListener();
        updateUI();
    }

    private void initializeOrderListener(){
        // initialize firebase so that we can get the order when it comes in into ACTIVE_ORDER
        // also this listener will be used to detect any changes to the order's status
        ValueEventListener orderListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    m_Order = dataSnapshot.getValue(Order.class);
                    if (m_Order == null) {
                        Log.e("DeliveryStatMap", "ERROR: ORDER IS NULL!");
                    } else {
                        Log.d("DeliveryStatMap", "Receiving fresh order from Firebase.");
                    }
                    onSuccessfulRetrieval();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        m_ActiveOrderRef.child(m_Order.getFbKey()).addValueEventListener(orderListener);
    }

    private void onSuccessfulRetrieval(){
        // called when an order has been successfuly retrieved
        updateUI();
    }

    //abstract function which updates the statusView (e.g. delivering and such
    protected abstract void updateStatusText(TextView statusView);

    //abstract function which updates the button's presentation
    protected abstract void updateConfirmButton(Button button);

    @CallSuper
    private void updateUI(){
        // notifies various UI views to update themselves
        initializeMarkers();
        updateStatusText(m_StatusView);
        updateConfirmButton(m_ContextButton);
        updateExtraInfo();
    }

    private void updateExtraInfo(){
        // updates the extra info menu on the top screen, just below the delivery status view
        updateCustomerInfoView();
        updateCourierInfoView();
        updateMenuItemView();
        updatePriceView();
    }

    private void updateCustomerInfoView(){
        // updates the customer info
        if(m_DeliveredToView == null){
            m_DeliveredToView = (TextView) findViewById(R.id.deliverToTextView);
        }
        m_UserRef.child(m_Order.getUserID())
                .child(FirebaseKeylist.USER_PROFILE_NAME)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String customerName = dataSnapshot.getValue(String.class);
                    m_DeliveredToView.setText(getString(R.string.order_ordered_by, customerName));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateCourierInfoView(){
        // updates the courier info
        if(m_DeliveredByView == null){
            m_DeliveredByView = (TextView) findViewById(R.id.deliveredByTextView);
        }
        if(m_Order.getCourierID() != null) {
            m_UserRef.child(m_Order.getCourierID())
                    .child(FirebaseKeylist.USER_PROFILE_NAME)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String courierName = dataSnapshot.getValue(String.class);
                                m_DeliveredByView.setText(getString(R.string.order_delivered_by, courierName));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
        else{
            m_DeliveredByView.setText(getString(R.string.order_delivered_by, "Pending."));
        }
    }

    private void updateMenuItemView(){
        // updates the list of order menu items
        m_MenuItemTextView.setText(getString(R.string.order_menuitem_list, m_Order.itemListToString()));
    }

    private void updatePriceView(){
        // updates the price
        m_PriceTextView.setText(getString(R.string.order_price, m_Order.getPriceAsString()));
    }

    @CallSuper
    protected void initializeMarkers(){
        // initialize all relevant markers
        //Add destination marker
        if (m_DeliveryLocationMarker == null) {
            m_DeliveryLocationMarker = m_GoogleMap.addMarker(new MarkerOptions()
                    .position(m_Order.getDestinationPos())
                    .title("Delivery Destination"));
        }

        if(!(m_DeliveryLocationMarker.getPosition().equals(m_Order.getDestinationPos()))){
            Log.e("DeliveryStatusMap", "Marker position and destination position mismatch!");
        }
    }

    protected void onDeliveryCompleted(){
        //executed when the delivery is completed
        // remove from ACTIVE_ORDER
        String key = m_Order.getFbKey();
        m_OrderRef.child(key).removeValue();
        m_ActiveOrderRef.child(key).removeValue();

        // remove from USER_PROFILE
        m_UserRef.child(m_Order.getUserID())
                .child(FirebaseKeylist.USER_PROFILE_ACTIVE_ORDER)
                .removeValue();
        if(m_Order.getCourierID() != null) {
            m_UserRef.child(m_Order.getCourierID())
                    .child(FirebaseKeylist.USER_PROFILE_ACTIVE_ORDER)
                    .removeValue();
        }
        finish();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try{
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setInterval(MAX_UPDATE_INTERVAL)
                    .setFastestInterval(MIN_UPDATE_INTERVAL);
            LocationServices.FusedLocationApi.requestLocationUpdates(m_APIClient, locationRequest, this);
        }
        catch (SecurityException secEx){
            Toast.makeText(this, "ERROR: Please enable location services",
                    Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "ERROR: Failed to connect to Map Service.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        // fired whenever there is an update on the user's location
        saveLocationToFirebase(location);
    }

    private void saveLocationToFirebase(Location location){
        // save the location specified by location to the firebase database
        // this location data will be presented to the customer
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(FirebaseKeylist.USER_PROFILE);
        FirebaseUser user = m_Auth.getCurrentUser();
        if (user != null) {
            userRef.child(user.getUid())
                    .child(LOCATION_FIELD)
                    .child(LATITUDE_FIELD)
                    .setValue(location.getLatitude()); // save latitude
            userRef.child(user.getUid())
                    .child(LOCATION_FIELD)
                    .child(LONGITUDE_FIELD)
                    .setValue(location.getLongitude()); // save longitude
        }
        else{
            Log.e("DeliveryStatusMap", "USER NOT LOGGED IN!");
            onNotLoggedIn();
        }
    }

    @Override
    protected void onStart(){
        m_APIClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop(){
        m_APIClient.disconnect();
        super.onStop();
    }

    // auxilliary getters to be used by subclasses
    protected GoogleMap getGoogleMap(){
        return m_GoogleMap;
    }

    protected Order getOrder(){
        return m_Order;
    }

    protected DatabaseReference getActiveOrderRef(){
        return m_ActiveOrderRef;
    }

    protected DatabaseReference getUserRef(){
        return m_UserRef;
    }

    protected Button getContextButton(){
        return m_ContextButton;
    }
}
