package love.isuper.at_user_helper;

import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by guoshichao on 2021/6/29
 * AtUserHelper
 */
public class AtUserHelper {

    //多端定好的正则
//        public static final String AT_PATTERN = "@\\(name:([^\\n\\r`~\\!@#\\$%\\^&\\*\\(\\)\\+=\\|'\\:;'\\,\\[\\]\\.\\<\\>/\\?！@#￥%……（）——\\{\\}【】‘；：”“’。，、？]+),id:([A-Za-z0-9]+)\\)";
    public static final String AT_PATTERN = "@\\(name:([\\s\\S]*?),id:([A-Za-z0-9]+)\\)";

    public static CharSequence parseAtUserLink(CharSequence text) {
        return parseAtUserLink(text, 0);
    }

    public static CharSequence parseAtUserLink(CharSequence text, @ColorInt int color) {
        return parseAtUserLink(text, color, null);
    }

    /**
     * @return 解析AtUser
     */
    public static CharSequence parseAtUserLink(CharSequence text, @ColorInt int color, AtUserLinkOnClickListener clickListener) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }

        // 进行正则匹配[文字](链接)
        SpannableStringBuilder spannableString = new SpannableStringBuilder(text);

        Matcher matcher = Pattern.compile(AT_PATTERN).matcher(text);
        int replaceOffset = 0; //每次替换之后matcher的偏移量
        while (matcher.find()) {
            // 解析链接  格式是[文字](链接)
            final String name = matcher.group(1);
            final String uid = matcher.group(2);

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(uid)) {
                continue;
            }

            // 把匹配成功的串append进结果串中, 并设置点击效果
            String atName = "@" + name + "";
            int clickSpanStart = matcher.start() - replaceOffset;
            int clickSpanEnd = clickSpanStart + atName.length();
            spannableString.replace(matcher.start() - replaceOffset, matcher.end() - replaceOffset, atName);
            replaceOffset += matcher.end() - matcher.start() - atName.length();

            if (color != 0) {
                AtUserForegroundColorSpan atUserLinkSpan = new AtUserForegroundColorSpan(color);
                atUserLinkSpan.name = name;
                atUserLinkSpan.uid = uid;
                atUserLinkSpan.atContent = matcher.group();
                spannableString.setSpan(atUserLinkSpan, clickSpanStart, clickSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            //是否加超链接：
            if (clickListener != null) {
                spannableString.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View v) {
                        //取消选择
                        Spannable spannable = (Spannable) ((TextView) v).getText();
                        Selection.removeSelection(spannable);

                        // 对id进行解密
                        String atUserId = uid;
                        if (!TextUtils.isEmpty(uid)) {
                            atUserId = EncryptTool.hashIdsDecode(uid);
                        }
                        //外面传进来点击监听：
                        clickListener.onClick(atUserId);
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(color);//设置文字颜色
                        ds.setUnderlineText(false);      //下划线设置
                        ds.setFakeBoldText(false);      //加粗设置
                    }
                }, clickSpanStart, clickSpanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return spannableString;
    }

    /**
     * 是否输入了At
     */
    public static boolean isInputAt(String beforeStr, String afterStr, int editSelectionEnd) {
        if (!TextUtils.isEmpty(afterStr)) {
            if (TextUtils.isEmpty(beforeStr) || afterStr.length() > beforeStr.length()) {//输入内容的操作
                if (afterStr.length() >= 1 && editSelectionEnd - 1 >= 0 && (afterStr.subSequence(editSelectionEnd - 1, editSelectionEnd)).equals("@")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return 是否删除AtUser整体
     */
    public static boolean judgeLastEdit(EditText editText, CharSequence beforeStr, CharSequence afterStr, Editable s, int editSelectionStart, int editSelectionEnd) {
        if (TextUtils.isEmpty(afterStr) || TextUtils.isEmpty(beforeStr)
                || !(afterStr instanceof SpannableStringBuilder)
                || !(beforeStr instanceof SpannableStringBuilder)) {
            return false;
        }
        if (afterStr.length() < beforeStr.length()) {//删除内容的操作
            SpannableStringBuilder beforeSp = (SpannableStringBuilder) beforeStr;
            AtUserForegroundColorSpan[] beforeSpans = beforeSp.getSpans(0, beforeSp.length(), AtUserForegroundColorSpan.class);
            boolean mReturn = false;
            for (AtUserForegroundColorSpan span : beforeSpans) {
                int start = beforeSp.getSpanStart(span);
                int end = beforeSp.getSpanEnd(span);

                boolean isRemove = false;
                if (editSelectionStart == editSelectionEnd && editSelectionEnd == end) {
                    //如果刚后在后面，先选中，下次点击才删除
                    editText.setText(beforeStr);
                    editText.setSelection(start, end);

                    //方案二是直接删除
//                    isRemove = true;
//                    s.delete(start, end - 1);
                } else if (editSelectionStart <= start && editSelectionEnd >= end) {
                    return false;
                } else if (editSelectionStart <= start && editSelectionEnd > start) {
                    isRemove = true;
                    s.delete(editSelectionStart, end - editSelectionEnd);
                } else if (editSelectionStart < end && editSelectionEnd >= end) {
                    isRemove = true;
                    s.delete(start, editSelectionStart);
                }

                if (isRemove) {
                    mReturn = true;
                    beforeSp.removeSpan(span);
                }
            }
            return mReturn;
        }
        return false;
    }

    public static void appendChooseUser(EditText editText, String name, String uid, TextWatcher watcher) {
        appendChooseUser(editText, name, uid, watcher, 0);
    }

    /**
     * 将User添加到At之后
     */
    public static void appendChooseUser(EditText editText, String name, String uid, TextWatcher watcher, @ColorInt int color) {
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(uid)) {
            editText.removeTextChangedListener(watcher);
            //@(name:xxxxx,id:XOVo9x)
            String atUserId = EncryptTool.hashIdsEncode(uid);
            //和服务端商量好的拼接规则
            String result = "@(name:" + name + ",id:" + atUserId + ")";
            int beforeTextLength = editText.length();
            int selectionEnd = editText.getSelectionEnd();
            editText.getText().replace(selectionEnd - 1, selectionEnd, result);
            editText.setText(parseAtUserLink(editText.getText(), color));
            int afterTextLength = editText.length();
            editText.setSelection(afterTextLength - beforeTextLength + selectionEnd);
            editText.addTextChangedListener(watcher);
        }
    }

    /**
     * 给EditText添加选择监听，使AtUser成为一个整体
     */
    public static void addSelectionChangeListener(SelectionEditText editText) {
        editText.addOnSelectionChangeListener(new SelectionEditText.OnSelectionChangeListener() {
            @Override
            public void onSelectionChange(int selStart, int selEnd) {
                Editable editable = editText.getText();
                if (editable instanceof SpannableStringBuilder) {
                    SpannableStringBuilder spanStr = (SpannableStringBuilder) editable;
                    AtUserForegroundColorSpan[] beforeSpans = spanStr.getSpans(0, spanStr.length(), AtUserForegroundColorSpan.class);
                    for (AtUserForegroundColorSpan span : beforeSpans) {
                        int start = spanStr.getSpanStart(span);
                        int end = spanStr.getSpanEnd(span);

                        boolean isChange = false;
                        if (selStart > start && selStart < end) {
                            selStart = start;
                            isChange = true;
                        }
                        if (selEnd < end && selEnd > start) {
                            selEnd = end;
                            isChange = true;
                        }

                        if (isChange) {
                            editText.setSelection(selStart, selEnd);
                        }
                    }
                }
            }
        });
    }

    /**
     * AtUser解析
     */
    public static Editable toAtUser(final Editable editable) {
        if (TextUtils.isEmpty(editable)) {
            return null;
        }
        Editable result = editable;
        if (editable instanceof SpannableStringBuilder) {
            SpannableStringBuilder spanStr = (SpannableStringBuilder) editable;
            AtUserForegroundColorSpan[] beforeSpans = spanStr.getSpans(0, spanStr.length(), AtUserForegroundColorSpan.class);
            for (AtUserForegroundColorSpan span : beforeSpans) {
                int start = spanStr.getSpanStart(span);
                int end = spanStr.getSpanEnd(span);
                result.replace(start, end, span.atContent);
            }
        }
        return result;
    }

    /**
     * 判断String中是否有At
     */
    public static boolean hasAt(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }

        Matcher matcher = Pattern.compile(AT_PATTERN).matcher(text);
        while (matcher.find()) {
            // 解析链接
            final String name = matcher.group(1);
            final String uid = matcher.group(2);

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(uid)) {
                continue;
            }
            return true;
        }
        return false;
    }


}
