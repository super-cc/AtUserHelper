package love.isuper.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import love.isuper.at_user_helper.AtUserHelper;
import love.isuper.at_user_helper.AtUserLinkOnClickListener;
import love.isuper.at_user_helper.SelectionEditText;

public class MainActivity extends AppCompatActivity {

    private SelectionEditText edt;
    private TextView tv;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edt = findViewById(R.id.edt);

        edt.addTextChangedListener(mTextWatcher);
        AtUserHelper.addSelectionChangeListener(edt);


        edt.setFocusable(true);
        edt.setFocusableInTouchMode(true);
        edt.requestFocus();



        tv = findViewById(R.id.tv);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //这里是发布的时候要用的串
                Editable editable = Editable.Factory.getInstance().newEditable(edt.getText());
                editable = AtUserHelper.toAtUser(editable);
                //editable.toString()就是发布用的串

                CharSequence content = AtUserHelper.parseAtUserLink(editable, getResources().getColor(R.color.blue),
                        new AtUserLinkOnClickListener() {
                            @Override
                            public void onClick(String uid) {
                                ToastUtils.show(MainActivity.this, "点击了At，" + "uid：" + uid, Toast.LENGTH_LONG);
                            }
                        });
                tv.setText(content);
            }
        });
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        private int beforeEditStart;
        private int beforeEditEnd;
        private SpannableStringBuilder beforeText, afterText;

        public void afterTextChanged(Editable s) {
            // 先去掉监听器，否则会出现栈溢出
            edt.removeTextChangedListener(mTextWatcher);

            if (AtUserHelper.isInputAt(beforeText.toString(), afterText.toString(), edt.getSelectionEnd())) {
                //这里正常的代码应该是跳到@好友的页面，然后回来之后做添加@内容，所以做个延迟的操作
                tv.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AtUserHelper.appendChooseUser(edt, "一个有故事的程序员", "1234",
                                mTextWatcher, getResources().getColor(R.color.blue));
                    }
                }, 300);
            }

            AtUserHelper.judgeLastEdit(edt, beforeText, afterText, s, beforeEditStart, beforeEditEnd);

            // 恢复监听器
            edt.addTextChangedListener(mTextWatcher);
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            beforeText = new SpannableStringBuilder(s);
            beforeEditStart = edt.getSelectionStart();
            beforeEditEnd = edt.getSelectionEnd();
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            afterText = new SpannableStringBuilder(s);
        }
    };

}