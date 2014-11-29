package dk.too.timetable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class TimeTableArrayAdapter extends ArrayAdapter<String> {

    public TimeTableArrayAdapter(Context context, String[] objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
    }
    
    private String getItem(int col, int row)
    {
        String ret = null;
        if (row == 0) {
            switch (col) {
            case 0:
                ret = "";
                break;
            case 1:
                ret = "월";
                break;
            case 2:
                ret = "화";
                break;
            case 3:
                ret = "수";
                break;
            case 4:
                ret = "목";
                break;
            case 5:
                ret = "금";
                break;
            case 6:
                ret = "토";
                break;
            }

        } else if (col == 0) {
            ret = row + "\n" + getTimeStr(row + 8);

        } else {
            ret = super.getItem(row * 6 + col);
        }

        return ret == null ? "" : ret;        
        
    }

    private String getTimeStr(int hour){
        
        int _min = hour * 60;
        
        if( hour > 18){
            _min -= 10 * (hour-18);
        }
        
        int min = _min%60;
        return _min/60 + ":" + (min < 10 ? "0"+min : min) ;
        
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // layout을 바꿔주고 있기 때문에 convertView를 사용할 수 없다.
        int col = position % 6;
        int row = position / 6;
        
        String data = getItem(col, row);
        
        View v = null;
        if(row == 0){
            
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = (TextView)vi.inflate(R.layout.item_row0, parent, false);
            
        } else if(col == 0){
            
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = (TextView)vi.inflate(R.layout.item_col0, parent, false);
            
        } else {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.item, parent, false);
        }
        
        if (row == 0 || col == 0) {
            TextView tv = (TextView) v;
            
            tv.setText(data);
            
        } else {
            
            TextView label = (TextView)v.findViewById(R.id.label);
            TextView room = (TextView)v.findViewById(R.id.room);
            
            String[] str = data.split("\n");
            if(str.length > 1){
                
                String upData = getItem(col, row-1);
                if(data.equals(upData))
                {
//                    View upView = getView(position-6, convertView, parent);
                    v.setLayoutParams(new GridView.LayoutParams(80, 200));
                    
//                    v.setVisibility(View.GONE);
                }           
                else
                {
//                    v.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
//                    v.setMeasuredDimension(v.getMeasuredWidth()*2, v.getMeasuredWidth());
                }
                    
                
                if(str[0].length() > 8){
                    label.setText(str[0].substring(0, 8));
                } else {
                    label.setText(str[0]);
                }
                
                room.setText(str[1]);
            }
            
        }
        
        return v;
    }
    
    
    private ViewWrapper item_row0 = null;
    private ViewWrapper item_col0 = null;
    private ViewWrapper item = null;

    //
    // Holder Pattern을 구현하는 ViewWrapper 클래스
    //
    class ViewWrapper {
        View base;
        TextView label = null;
        TextView room = null;
        
        ViewWrapper(View base) {
            this.base = base;
        }
        
        public View getBase() {
            return base;
        }
        
        // label 멤버 변수가 null일때만 findViewById를 호출
        // null이 아니면 저장된 instance 리턴 -> Overhaed 줄임
        TextView getLabel() {
            if(label == null) {
                label = (TextView)base.findViewById(R.id.label);
            }           
            return label;           
        }
        
        TextView getRoom() {
            if(room == null) {
                room = (TextView)base.findViewById(R.id.room);
            }           
            return room;
        }
    }
}
