package verzac.tastebuddy.courier_activity;

import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import verzac.tastebuddy.AuthAbstractActivity;
import verzac.tastebuddy.R;
import verzac.tastebuddy.adapters.OrderRecyclerAdapter;
import verzac.tastebuddy.helper.TestDataInit;
import verzac.tastebuddy.models.MenuItem;
import verzac.tastebuddy.models.Order;
import verzac.tastebuddy.models.Restaurant;

public class OrderListActivity extends AuthAbstractActivity {

    private ArrayList<Order> m_OrderList; /*this holds the list of Monsters which will be displayed*/
    private RecyclerView m_RecyclerView; // our RecyclerView instance
    private LinearLayoutManager m_LinearLayoutManager; //the LayoutManager used by the RecyclerView
    private OrderRecyclerAdapter m_Adapter; // our custom RecyclerAdapter for Order objects
    private DatabaseReference m_OrderRef;
    private HashMap<String, Integer> m_OrderKeyMap; //neat little hack to keep track of where our order resides in the orderlist
    private ChildEventListener m_OrderCEL;
    private ProgressBar m_ProgressBar;
    private LinearLayout m_LinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Available Order");
        setContentView(R.layout.activity_order_list);

        //init vars and views
        m_RecyclerView = (RecyclerView) findViewById(R.id.orderRecyclerView);
        m_RecyclerView.setHasFixedSize(true);
        m_LinearLayoutManager = new LinearLayoutManager(this);
        m_LinearLayout = (LinearLayout) findViewById(R.id.orderListLinearLayout);
        m_RecyclerView.setLayoutManager(m_LinearLayoutManager);
        m_ProgressBar = (ProgressBar) findViewById(R.id.loadingBar);
        m_ProgressBar.setIndeterminate(true);
        m_OrderList = new ArrayList<Order>();
        m_OrderKeyMap = new HashMap<>();
        m_Adapter = new OrderRecyclerAdapter(m_OrderList);

        //Firebase thingy majiggies
        m_OrderRef = FirebaseDatabase.getInstance().getReference(Order.ORDER_KEY);
        m_OrderCEL = new ChildEventListener() {
            // child event listener to detect when a child has been added/changed/removed
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Order theOrder = dataSnapshot.getValue(Order.class);
                theOrder.setFbKey(dataSnapshot.getKey());
                Log.e("OrderListActivity", theOrder.getFbKey());
                m_OrderList.add(theOrder);
                m_OrderKeyMap.put(dataSnapshot.getKey(), m_OrderList.size() - 1);
                m_Adapter.notifyDataSetChanged();
                m_ProgressBar.setVisibility(View.GONE);
                m_LinearLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // orders shouldn't be able to change at this stage
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // remove the order from the list
                Integer index = m_OrderKeyMap.get(dataSnapshot.getKey());
                m_OrderList.remove(index.intValue());
                m_OrderKeyMap.remove(dataSnapshot.getKey());
                m_Adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorString = getString(R.string.err_firebase_unavailable);
                Toast toast2 = Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT);
                toast2.show();
                Log.e("Create Order:", errorString);
            }
        };
        m_OrderRef.addChildEventListener(m_OrderCEL);

        //Adapter
        m_Adapter = new OrderRecyclerAdapter(m_OrderList);
        m_RecyclerView.setAdapter(m_Adapter);
        Log.d("OrderListActivity", "Activity started.");
    }
}
