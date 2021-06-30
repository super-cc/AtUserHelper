package love.isuper.at_user_helper;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guoshichao on 2021/5/17
 */
public class SelectionEditText extends AppCompatEditText {

    private List<OnSelectionChangeListener> onSelectionChangeListeners;

    private OnSelectionChangeListener onSelectionChangeListener;

    public SelectionEditText(Context context) {
        super(context);
    }

    public SelectionEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectionEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (onSelectionChangeListener != null) {
            onSelectionChangeListener.onSelectionChange(selStart, selEnd);
        }
        if (onSelectionChangeListeners != null) {
            for (int i = 0; i < onSelectionChangeListeners.size(); i++) {
                onSelectionChangeListeners.get(i).onSelectionChange(selStart, selEnd);
            }
        }
    }

    public void addOnSelectionChangeListener(OnSelectionChangeListener onSelectionChangeListener) {
        if (onSelectionChangeListeners == null) {
            onSelectionChangeListeners = new ArrayList<>();
        }
        onSelectionChangeListeners.add(onSelectionChangeListener);
    }

    public void removeOnSelectionChangedListener(OnSelectionChangeListener onSelectionChangeListener) {
        if (onSelectionChangeListeners != null) {
            onSelectionChangeListeners.remove(onSelectionChangeListener);
        }
    }

    public void clearOnSelectionChangedListener() {
        if (onSelectionChangeListeners != null) {
            onSelectionChangeListeners.clear();
        }
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener onSelectionChangeListener) {
        this.onSelectionChangeListener = onSelectionChangeListener;
    }

    public interface OnSelectionChangeListener {
        void onSelectionChange(int selStart, int selEnd);
    }

}

