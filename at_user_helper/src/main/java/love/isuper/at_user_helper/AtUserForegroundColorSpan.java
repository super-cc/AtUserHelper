package love.isuper.at_user_helper;

import android.text.style.ForegroundColorSpan;

/**
 * Created by guoshichao on 2021/6/29
 * AtUser的Span
 */
public class AtUserForegroundColorSpan extends ForegroundColorSpan {

    public String name;
    public String uid;
    public String atContent;

    public AtUserForegroundColorSpan(int color) {
        super(color);
    }

}
