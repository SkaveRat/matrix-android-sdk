package org.matrix.matrixandroidsdk.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.matrix.androidsdk.data.RoomSummary;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.matrixandroidsdk.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyRoomsAdapter extends RecyclerView.Adapter<MyRoomsAdapter.ViewHolder>{

    private final List<RoomSummary> roomSummaries;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM d HH:mm", Locale.getDefault());

    public void addRoomSummary(RoomSummary summary) {
        Log.i("Foobar", summary.getRoomId());
        roomSummaries.add(summary);
    }

    public void removeRoomSummary(RoomSummary summary) {
        roomSummaries.remove(summary);
    }

    public RoomSummary getSummaryByRoomId(String roomId) {
        for (RoomSummary summary: roomSummaries) {
            if(summary.getRoomId() == roomId) {
                return summary;
            }
        }

        return null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout listItemLayout;
        public ViewHolder(LinearLayout listItemLayout) {
            super(listItemLayout);
            this.listItemLayout = listItemLayout;
        }
    }

    public MyRoomsAdapter() {
        this.roomSummaries = new ArrayList<RoomSummary>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_item_my_rooms, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RoomSummary summary = roomSummaries.get(position);

        TextView roomName = (TextView) holder.listItemLayout.findViewById(R.id.roomSummaryAdapter_roomName);
        TextView message = (TextView) holder.listItemLayout.findViewById(R.id.roomSummaryAdapter_message);
        TextView timestamp = (TextView) holder.listItemLayout.findViewById(R.id.roomSummaryAdapter_ts);

        roomName.setText(summary.getRoomName());
        Event latestEvent = summary.getLatestEvent();
        message.setText("");
        timestamp.setText(mDateFormat.format(new Date(latestEvent.originServerTs)));


    }

    @Override
    public int getItemCount() {
        return roomSummaries.size();
    }

}
