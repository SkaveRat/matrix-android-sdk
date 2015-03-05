package org.matrix.matrixandroidsdk.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.RoomSummary;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.PublicRoom;
import org.matrix.matrixandroidsdk.Matrix;
import org.matrix.matrixandroidsdk.R;
import org.matrix.matrixandroidsdk.activity.HomeActivity;
import org.matrix.matrixandroidsdk.activity.RoomActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;

public class RoomSummaryAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private int mLayoutResourceId;

    private int mOddColourResId;
    private int mEvenColourResId;
    private int mUnreadColor;
    private int mHighlightColor;
    private int mPublicHighlightColor;

    private List<RoomSummary>mRecentsSummariesList;

    private List<RoomSummary>mFilteredRecentsSummariesList;
    private List<PublicRoom>mFilteredPublicRoomsList;

    private String mSearchedPattern = "";

    private DateFormat mDateFormat;

    private String mMyUserId = null;

    private ArrayList<String> mHighLightedRooms = new ArrayList<String>();
    private HashMap<String, Integer> mUnreadCountMap = new HashMap<String, Integer>();

    /**
     * Construct an adapter which will display a list of rooms.
     * @param context Activity context
     * @param layoutResourceId The resource ID of the layout for each item. Must have TextViews with
     *                         the IDs: roomsAdapter_roomName, roomsAdapter_roomTopic
     */
    public RoomSummaryAdapter(Context context, int layoutResourceId) {
        //super(context, layoutResourceId);
        mContext = context;
        mLayoutResourceId = layoutResourceId;
        mLayoutInflater = LayoutInflater.from(mContext);
        mDateFormat = new SimpleDateFormat("MMM d HH:mm", Locale.getDefault());
        //setNotifyOnChange(false);

        mRecentsSummariesList = new ArrayList<RoomSummary>();
        mUnreadColor = context.getResources().getColor(R.color.room_summary_unread_background);
        mHighlightColor = context.getResources().getColor(R.color.room_summary_highlight_background);
        mPublicHighlightColor = context.getResources().getColor(R.color.room_summary_public_highlight_background);


        mMyUserId = Matrix.getInstance(context.getApplicationContext()).getDefaultSession().getCredentials().userId;
    }

    /**
     *  unread messages map
     */
    public  HashMap<String, Integer> getUnreadCountMap() {
        return mUnreadCountMap;
    }

    public void setUnreadCountMap(HashMap<String, Integer> map) {
        mUnreadCountMap = map;
    }

    /**
     *  search management
     */

    public void setSearchedPattern(String pattern) {
        if (null == pattern) {
            pattern = "";
        }

        if (!pattern.equals(mSearchedPattern)) {
            mSearchedPattern = pattern.toLowerCase();
            this.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        mFilteredRecentsSummariesList = new ArrayList<RoomSummary>();
        mFilteredPublicRoomsList = new ArrayList<PublicRoom>();

        // there is a pattern to search
        if (mSearchedPattern.length() > 0) {

            // search in the recent rooms
            for (RoomSummary summary : mRecentsSummariesList) {
                String roomName = summary.getRoomName();

                if (!TextUtils.isEmpty(roomName) && (roomName.toLowerCase().indexOf(mSearchedPattern) >= 0)) {
                    mFilteredRecentsSummariesList.add(summary);
                } else {
                    String topic = summary.getRoomTopic();

                    if (!TextUtils.isEmpty(topic) && (topic.toLowerCase().indexOf(mSearchedPattern) >= 0)) {
                        mFilteredRecentsSummariesList.add(summary);
                    }
                }
            }
        }

        super.notifyDataSetChanged();
    }

    /**
     * recents list management
     */

    public List<RoomSummary> getRecentsSummariesList() {
        return mRecentsSummariesList;
    }

    public void addRoomSummary(RoomSummary roomSummary) {
        mRecentsSummariesList.add(roomSummary);
    }

    public RoomSummary getRoomSummaryAt(int index) {
        if (mSearchedPattern.length() > 0) {
            return mFilteredRecentsSummariesList.get(index);
        } else {
            return mRecentsSummariesList.get(index);
        }
    }

    public void removeRoomSummary(RoomSummary roomSummary) {
        mRecentsSummariesList.remove(roomSummary);
    }

    public RoomSummary getSummaryByRoomId(String roomId) {
        for (int i=0; i<mRecentsSummariesList.size(); i++) {
            RoomSummary summary = mRecentsSummariesList.get(i);
            if (roomId.equals(summary.getRoomId())) {
                return summary;
            }
        }
        return null;
    }

    /**
     * Set the latest event for a room summary.
     * @param event The latest event
     */
    public void setLatestEvent(Event event, RoomState roomState) {
        RoomSummary summary = getSummaryByRoomId(event.roomId);
        if (summary != null) {
            summary.setLatestEvent(event);
            summary.setLatestRoomState(roomState);
            sortSummaries();
            notifyDataSetChanged();
        }
    }

    public void incrementUnreadCount(String roomId) {
        Integer count = mUnreadCountMap.get(roomId);
        if (count == null) {
            count = 0;
        }
        mUnreadCountMap.put(roomId, count + 1);
    }

    public void highlightRoom(String roomId) {
        if (mHighLightedRooms.indexOf(roomId) < 0) {
            mHighLightedRooms.add(roomId);
        }
    }

    public void resetUnreadCount(String roomId) {
        mUnreadCountMap.put(roomId, 0);
        mHighLightedRooms.remove(roomId);
    }

    public void resetUnreadCounts() {
        Set<String> roomIds = mUnreadCountMap.keySet();

        for(String roomId : roomIds) {
            resetUnreadCount(roomId);
        }
    }

    public void setAlternatingColours(int oddResId, int evenResId) {
        mOddColourResId = oddResId;
        mEvenColourResId = evenResId;
    }

    public void sortSummaries() {
        Collections.sort(mRecentsSummariesList, new Comparator<RoomSummary>() {
            @Override
            public int compare(RoomSummary lhs, RoomSummary rhs) {
                if (lhs == null || lhs.getLatestEvent() == null) {
                    return 1;
                } else if (rhs == null || rhs.getLatestEvent() == null) {
                    return -1;
                }

                if (lhs.getLatestEvent().originServerTs > rhs.getLatestEvent().originServerTs) {
                    return -1;
                } else if (lhs.getLatestEvent().originServerTs < rhs.getLatestEvent().originServerTs) {
                    return 1;
                }
                return 0;
            }
        });
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(mLayoutResourceId, parent, false);
        }

        if (groupPosition == HomeActivity.recentsGroupIndex) {
            List<RoomSummary> summariesList = (mSearchedPattern.length() > 0) ? mFilteredRecentsSummariesList : mRecentsSummariesList;

            RoomSummary summary = summariesList.get(childPosition);

            Integer unreadCount = mUnreadCountMap.get(summary.getRoomId());

            if ((unreadCount == null) || (unreadCount == 0)) {
                convertView.setBackgroundColor(0);
            } else if (mHighLightedRooms.indexOf(summary.getRoomId()) >= 0) {
                convertView.setBackgroundColor(mHighlightColor);
            } else {
                convertView.setBackgroundColor(mUnreadColor);
            }

            CharSequence message = summary.getRoomTopic();
            String timestamp = null;

            TextView textView = (TextView) convertView.findViewById(R.id.roomSummaryAdapter_roomName);

            RoomState latestRoomState = summary.getLatestRoomState();
            if (null == latestRoomState) {
                Room room = Matrix.getInstance(mContext.getApplicationContext()).getDefaultSession().getDataHandler().getStore().getRoom(summary.getRoomId());

                if (null != room.getLiveState()) {
                    latestRoomState = room.getLiveState().deepCopy();
                }
            }

            // the public rooms are displayed with bold fonts
            if ((null != latestRoomState) && (null != latestRoomState.visibility) && latestRoomState.visibility.equals(RoomState.VISIBILITY_PUBLIC)) {
                textView.setTypeface(null, Typeface.BOLD);
            } else {
                textView.setTypeface(null, Typeface.NORMAL);
            }

            // display the unread messages count
            String roomNameMessage = (latestRoomState != null) ? latestRoomState.getDisplayName(mMyUserId) : summary.getRoomName();

            if (null != roomNameMessage) {
                if ((null != unreadCount) && (unreadCount > 0)) {
                    roomNameMessage += " (" + unreadCount + ")";
                }
            }

            textView.setText(roomNameMessage);

            if (summary.getLatestEvent() != null) {
                AdapterUtils.EventDisplay display = new AdapterUtils.EventDisplay(mContext, summary.getLatestEvent(), latestRoomState);
                display.setPrependMessagesWithAuthor(true);
                message = display.getTextualDisplay();

                timestamp = mDateFormat.format(new Date(summary.getLatestEvent().originServerTs));
            }

            // check if this is an invite
            if (summary.isInvited()) {
                String memberName = summary.getInviterUserId();

                if (null != latestRoomState) {
                    memberName = latestRoomState.getMemberName(memberName);
                }

                message = mContext.getString(R.string.summary_user_invitation, memberName);
            }

            textView = (TextView) convertView.findViewById(R.id.roomSummaryAdapter_message);
            textView.setText(message);
            textView = (TextView) convertView.findViewById(R.id.roomSummaryAdapter_ts);
            textView.setVisibility(View.VISIBLE);
            textView.setText(timestamp);

            if (mOddColourResId != 0 && mEvenColourResId != 0) {
                convertView.setBackgroundColor(childPosition % 2 == 0 ? mEvenColourResId : mOddColourResId);
            }

        }

        return convertView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.adapter_room_section_header, null);
        }

        TextView heading = (TextView) convertView.findViewById(R.id.heading);

        if (groupPosition == HomeActivity.recentsGroupIndex) {

            int unreadCount = 0;

            for(Integer i : mUnreadCountMap.values()) {
                unreadCount += i;
            }

            String header = mContext.getResources().getString(R.string.my_rooms);

            if (unreadCount > 0) {
                header += " ("  + unreadCount + ")";
            }

            heading.setText(header);
        }

        return convertView;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return (mSearchedPattern.length() > 0) ? mFilteredRecentsSummariesList.size() : mRecentsSummariesList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public int getGroupCount() {
        return 2;
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


}
