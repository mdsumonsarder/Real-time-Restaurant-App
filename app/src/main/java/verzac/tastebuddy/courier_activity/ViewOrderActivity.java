package verzac.tastebuddy.courier_activity;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import verzac.tastebuddy.AuthAbstractActivity;
import verzac.tastebuddy.DeliveryStatusMapActivity;
import verzac.tastebuddy.R;
import verzac.tastebuddy.helper.FirebaseKeylist;
import verzac.tastebuddy.models.Order;

public class ViewOrderActivity extends AuthAbstractActivity implements View.OnClickListener{
    private TextView m_restaurantView;
    private TextView m_menuView;
    private TextView m_destinationView;
    private TextView m_priceView;
    private Button m_acceptButton;
    private Toolbar m_toolbar;
    private Order m_order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order);
        //assign views
        m_restaurantView = (TextView) findViewById(R.id.restaurantTextView);
        m_menuView = (TextView) findViewById(R.id.menuListTextView);
        m_destinationView = (TextView) findViewById(R.id.destinationAddrTextView);
        m_priceView = (TextView) findViewById(R.id.priceTextView);
        m_acceptButton = (Button) findViewById(R.id.acceptButton);
        m_toolbar = (Toolbar) findViewById(R.id.toolbar);

        //initialize intent/parcelables
        m_order = getIntent().getParcelableExtra(Order.ORDER_KEY);
        Log.e("ViewOrderA", m_order.getFbKey());

        //init everything else
        m_toolbar.setTitle(m_order.toString());
        m_restaurantView.setText(m_order.getRestaurant().toString());
        m_menuView.setText(m_order.itemListToString());
        m_destinationView.setText(m_order.getDestinationAddress());
        m_priceView.setText(m_order.getPriceAsString());
        Log.d("ViewOrder", "Price is " + m_order.getPriceAsString());
        m_acceptButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final FirebaseUser courierUser = FirebaseAuth.getInstance().getCurrentUser();
        if(courierUser == null){
            onNotLoggedIn();
        }
        else {
            m_order.setCourierID(courierUser.getUid());
            m_order.setStatus(Order.STATUS_PICKUP);
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            db.getReference(Order.ORDER_KEY).child(m_order.getFbKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        // if the order has been successfuly removed from the database
                        String courierID = m_Auth.getCurrentUser().getUid();
                        //put the order in the ACTIVE_ORDER database
                        DatabaseReference activeOrderRef = FirebaseDatabase.getInstance().getReference(Order.ORDER_ACTIVE_KEY);
                        activeOrderRef.child(m_order.getFbKey()).setValue(m_order);
                        //update the user profile for having an active order
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(FirebaseKeylist.USER_PROFILE);
                        userRef.child(courierID).child(FirebaseKeylist.USER_PROFILE_ACTIVE_ORDER).setValue(m_order.getFbKey());
                        //launch activity
                        Intent i = new Intent(getApplicationContext(), CourierDeliveryStatusMapActivity.class);
                        i.putExtra(Order.ORDER_KEY, m_order);
                        startActivity(i);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), getString(R.string.err_order_not_found), Toast.LENGTH_SHORT)
                        .show();
                        finish();
                    }
                }
            });
        }


    }
}
