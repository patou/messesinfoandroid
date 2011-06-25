package cef.messesinfo;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

public class ExpandableView extends ExpandableListView {

    public ExpandableView(Context context) {
	super(context);
    }
    
    public ExpandableView(Context context, AttributeSet attrs) {
	super(context, attrs);
    }

    public ExpandableView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
    }

    public int getScrollPosition() {
	int height = computeVerticalScrollRange();
	int pos = computeVerticalScrollOffset() + computeVerticalScrollExtent();
	if (height != 0)
	    return pos * 100 / height;
	return 0;
    }
}
