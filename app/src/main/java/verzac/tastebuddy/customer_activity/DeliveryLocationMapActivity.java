package verzac.tastebuddy.customer_activity;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import verzac.tastebuddy.AuthAbstractActivity;
import verzac.tastebuddy.R;

public class DeliveryLocationMapActivity extends AuthAbstractActivity
        implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, View.OnClickListener, GoogleMap.OnMapClickListener{
    // this activity is for when a customer wants to choose his delivery location
    private static final LatLng LOCATION_CLAYTON = new LatLng(-37.9150, 145.1300);

    private GoogleMap m_GoogleMap;
    private GoogleApiClient m_APIClient;
    private Marker m_DeliveryLocationMarker;
    private FloatingActionButton m_AcceptLocButton;
    private CoordinatorLayout m_CoordinatorLayout;
    private int m_ClickCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_location_map);
        m_ClickCount = 0;
        if(m_APIClient == null){
            m_APIClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        MapFragment mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFrag.getMapAsync(this);
        m_AcceptLocButton = (FloatingActionButton) findViewById(R.id.acceptLocationButton);
        m_AcceptLocButton.setOnClickListener(this);
        m_CoordinatorLayout = (CoordinatorLayout) findViewById(R.id.deliveryLocMapCoordinatorLayout);
        //snackbar to inform the user on what to do
        Snackbar.make(m_CoordinatorLayout, getString(R.string.prompt_choose_location1), Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }

    @Override
    public void onClick(View v) {
        //when the tick button is clicked
        if(m_DeliveryLocationMarker != null) {
            Intent i = new Intent(this, RestaurantListActivity.class);
            i.putExtra(LatLng.class.toString(), m_DeliveryLocationMarker.getPosition());
            startActivity(i);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        m_GoogleMap = googleMap;
        m_GoogleMap.setOnMapClickListener(this);
        m_GoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LOCATION_CLAYTON, 15));

        m_AcceptLocButton.setEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        /*
        * this is executed once when the app has succcessfully connected to GMaps*/
        try{
            m_GoogleMap.setMyLocationEnabled(true); // enable google's blue tracker thingy
            if (m_DeliveryLocationMarker == null) { // if a delivery location marker doesn't exist
                // get the last known location of the device
                Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(m_APIClient);
                LatLng lastKnownLocationLatLng = new LatLng(lastKnownLocation.getLatitude(),
                        lastKnownLocation.getLongitude());
                // create a new marker
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(lastKnownLocationLatLng)
                        .title("Delivery Destination")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                m_DeliveryLocationMarker = m_GoogleMap.addMarker(markerOptions);
                m_GoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_DeliveryLocationMarker.getPosition(), 15));
            }
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
    protected void onStart(){
        m_APIClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop(){
        m_APIClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        // this method is run when the map is clicked
        if (m_DeliveryLocationMarker != null) {
            m_DeliveryLocationMarker.setPosition(latLng);
            if(m_ClickCount == 0) {
                Snackbar.make(m_CoordinatorLayout, getString(R.string.prompt_choose_location2), Snackbar.LENGTH_LONG)
                        .setAction("OK", null).show();
            }
            m_ClickCount += 1;
        }
    }
}
