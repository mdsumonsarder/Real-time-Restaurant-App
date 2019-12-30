package verzac.tastebuddy.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import verzac.tastebuddy.R;
import verzac.tastebuddy.courier_activity.ViewOrderActivity;
import verzac.tastebuddy.models.Order;

/**
 * Created by Ben on 09-Apr-17.
 */

public class OrderRecyclerAdapter
        extends RecyclerView.Adapter<OrderRecyclerAdapter.OrderHolder>
        implements Filterable{
    // adapter class for displaying Order objects, this will specify how our data is going to be
    // presented in the RecyclerView

    private ArrayList<Order> m_OrderList; // the complete data set of Orders
    private ArrayList<Order> m_cDisplayedOrderList; // the data set which will be displayed to the users

    public static class OrderHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView m_cOrderNameView;
        private Order m_cOrder;

        public OrderHolder(View v){
            super(v);
            m_cOrderNameView = (TextView) v.findViewById(R.id.recyclerOrderShortTextView);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // when a OrderHolder (e.g. a row or a cell in the RecyclerView) is clicked
            // view that Order's detail through ViewOrderActivity
            Context context = itemView.getContext();
            Intent viewOrderIntent = new Intent(context, ViewOrderActivity.class);
            viewOrderIntent.putExtra(Order.ORDER_KEY, m_cOrder);
            context.startActivity(viewOrderIntent);
        }

        public void bindOrder(Order Order){
            // bind a Order object (and thus its data) into the Order holder
            // the binded Order's detail will be displayed in the cell/row of the RecyclerView
            // :param Order: a Order which will be displayed on the RecyclerView
            m_cOrder = Order;
            m_cOrderNameView.setText(Order.toString());
        }
    }

    public OrderRecyclerAdapter(ArrayList<Order> OrderList){
        // initialize the adapter with its data set
        // :param OrderList: the data source for the adapter
        m_OrderList = OrderList;
        m_cDisplayedOrderList = OrderList;
    }

    public Filter getFilter(){
        /* this method is where the filtering of the Order objects based on a criteria will
        occur */
        return new Filter(){
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                /* performs the filtering on the results/data set
                * :param constraint: the text query that is entered into the Search Bar. this will
                 * be used to filter the data */
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0){
                    results.values = m_OrderList;
                    results.count = m_OrderList.size();
                }
                else{
                    ArrayList<Order> filterResultsData = new ArrayList<Order>();
                    for(int i = 0; i < m_OrderList.size(); i++){
                        //this is where we filter stuffs
                        if (m_OrderList.get(i).toString().toLowerCase()
                                .contains(constraint.toString().toLowerCase())){
                            filterResultsData.add(m_OrderList.get(i));
                        }
                    }
                    results.values = filterResultsData;
                    results.count = filterResultsData.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                // publish the results or do whatever we want to do with it once we obtain the
                // filtered data set
                m_cDisplayedOrderList = (ArrayList<Order>)results.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public OrderRecyclerAdapter.OrderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the recyclerview_item_row layout and pass it onto the OrderHolder
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_order_row, parent, false);
        return new OrderHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(OrderRecyclerAdapter.OrderHolder holder, int position) {
        // called when the system wants to bind a Order to a particular view
        Order Order = m_cDisplayedOrderList.get(position);
        holder.bindOrder(Order);
    }

    @Override
    public int getItemCount() {
        // indicates how many items will be displayed on the RecyclerView
        return m_cDisplayedOrderList.size();
    }
}
