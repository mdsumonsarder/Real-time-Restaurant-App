package verzac.tastebuddy.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.provider.Settings.Secure;
import android.util.Log;

/**
 * Created by verzac on 5/9/2017.
 */

@IgnoreExtraProperties
public class Order implements Parcelable{
    //Attributes
    // Might need extra attrs later on, but this should suffice as a minimum;
    public static final String ORDER_KEY = "ORDER";
    public static final String ORDER_ACTIVE_KEY = "ACTIVE_ORDER";
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PICKUP = 1;
    public static final int STATUS_DELIVERING = 2;
    public static final int STATUS_DELIVERED = 3;
    public String fb_fbKey; //firebase key, may be a bit redundant but helpful to know nonetheless
    public ArrayList<MenuItem> m_ItemList;
    public Restaurant m_Restaurant;
    public String m_DestinationAddress;
    public double m_DestinationLat;
    public double m_DestinationLong;
    public int m_Price;
    public String m_UserID;
    public String m_CourierID;
    public int m_StatusID;
    
    public Order(){

    }

    public Order(Restaurant restaurant, String destinationAddress, String userID,
                 double destinationLat, double destinationLong){
        m_ItemList = new ArrayList<>();
        m_Restaurant = restaurant;
        m_Price = 0;
        m_DestinationAddress = destinationAddress;
        m_UserID = userID;
        m_DestinationLong = destinationLong;
        m_DestinationLat = destinationLat;
        m_StatusID = 0;
    }

    @Exclude
    public int getStatus(){
        return m_StatusID;
    }

    @Exclude
    public void setStatus(int statusID){
        m_StatusID = statusID;
    }

    @Exclude
    public LatLng getDestinationPos(){
        return new LatLng(m_DestinationLat, m_DestinationLong);
    }


    @Exclude
    public Restaurant getRestaurant(){
        return m_Restaurant;
    }

    @Exclude
    public void setFbKey(String fbKey){
        fb_fbKey = fbKey;
    }

    @Exclude
    public String getFbKey(){
        return fb_fbKey;
    }

    @Exclude
    public String getCourierID(){
        return m_CourierID;
    }

    @Exclude
    public void setCourierID(String courID){
        m_CourierID = courID;
    }

    @Exclude
    public String getUserID(){
        return m_UserID;
    }

    @Exclude
    public String getDestinationAddress(){
        return m_DestinationAddress;
    }

    @Exclude
    public void setDestinationAddress(String address){
        m_DestinationAddress = address;
    }

    @Exclude
    public String getPriceAsString(){
        return "$" + String.valueOf(m_Price);
    }

    @Exclude
    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    @Exclude
    public String itemListToString(){
        ArrayList<MenuItem> itemList = getItemList();
        if(itemList.size() == 0){
            return "None.";
        }
        String output = "";
        Collections.sort(itemList, new Comparator<MenuItem>() {
            @Override
            public int compare(MenuItem o1, MenuItem o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        for(MenuItem m : itemList){
            output += m.toString() + '\n';
        }
        return output;
    }

    @Exclude
    public String toString(){
        if (m_Restaurant == null){
            Log.e("ORDER.toString", "Restaurant is null!");
        }
        if (m_UserID == null){
            Log.e("ORDER.userID", "UserID is null!");
        }
        if (getDestinationAddress() == null){
            return getRestaurant().toString();
        }
        return getRestaurant().toString() + " --> " + getDestinationAddress();
    }

    @Exclude
    public ArrayList<MenuItem> getItemList(){
        return m_ItemList;
    }

    @Exclude
    public void addItem(MenuItem item){
        m_ItemList.add(item);
        m_Price += item.getPrice();
    }

    @Exclude
    @Override
    public int describeContents() {
        return 0;
    }

    @Exclude
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(m_ItemList);
        dest.writeParcelable(m_Restaurant, flags);
        dest.writeString(m_DestinationAddress);
        dest.writeInt(m_Price);
        dest.writeString(m_UserID);
        dest.writeDouble(m_DestinationLat);
        dest.writeDouble(m_DestinationLong);
        dest.writeString(fb_fbKey);
        dest.writeString(m_CourierID);
        dest.writeInt(m_StatusID);
    }


    protected Order(Parcel in) {
        m_ItemList = new ArrayList<MenuItem>();
        in.readTypedList(m_ItemList, MenuItem.CREATOR);
        //m_ItemList = new ArrayList<>();
        m_Restaurant = in.readParcelable(Restaurant.class.getClassLoader());
        m_DestinationAddress = in.readString();
        m_Price = in.readInt();
        m_UserID = in.readString();
        m_DestinationLat = in.readDouble();
        m_DestinationLong = in.readDouble();
        fb_fbKey = in.readString();
        m_CourierID = in.readString();
        m_StatusID = in.readInt();
    }
}
