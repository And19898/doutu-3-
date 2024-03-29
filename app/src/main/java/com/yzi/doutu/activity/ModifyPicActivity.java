package com.yzi.doutu.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.yzi.doutu.R;
import com.yzi.doutu.bean.DataBean;
import com.yzi.doutu.db.DBTools;
import com.yzi.doutu.interfaces.CommInterface;
import com.yzi.doutu.utils.CommUtil;
import com.yzi.doutu.utils.ImageUtils;
import com.yzi.doutu.utils.SharedUtils;
import com.yzi.doutu.view.ColorTagImageView;
import com.yzi.doutu.view.MyRelativeLayout;
import com.yzi.doutu.view.SoftKeyboardStateHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static com.yzi.doutu.utils.CommUtil.closeWaitDialog;
import static com.yzi.doutu.utils.CommUtil.showWaitDialog;


/**
 * 图片添加文字activity
 * Created by yzh-t105 on 2016/10/25.
 */
@Deprecated //新界面 ModifyImgActivity 代替
public class ModifyPicActivity extends BaseActivity implements  View.OnClickListener{

    private TextView tvRight;
    private MyRelativeLayout mainLayout;
    private com.yzi.doutu.view.ColorTagImageView colortag;
    private android.widget.EditText edWords;
    private TextView tvcc;
    private ImageView addTextImg;
    private TextView tvMul;
     ScrollView mScrollView;
    DataBean dataBean;


    Bitmap bitmap;
    Bitmap showBitmap = null;
    Bitmap resizeBmp;
    private Context context;
    private TextView typeface_a,typeface_b,typeface_c;

    Bundle bundle;
    Uri fileUri;
    String formWhere;

    /**mainLayout里当前显示图片的地址**/
    String showPath;
    //保存图片的宽高
    private int HEIGHT=280;
    private int WIDTH=280;

    String words="请输入文字";
    int wordsColor=Color.WHITE;//文字颜色
    Typeface typeface;
    SoftKeyboardStateHelper stateHelper;

    Handler myHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == 1)
            {
                if (mainLayout.getWidth() != 0)
                {
                    Log.i("LinearLayoutW", mainLayout.getWidth() + "");
                    Log.i("LinearLayoutH", mainLayout.getHeight() + "");
                    // 取消定时器
                    if(!TextUtils.isEmpty(showPath)){
                        closeWaitDialog();
                        timer.cancel();
                        fillContent();
                    }
                   // mainLayout.getLayoutParams().height=mainLayout.getWidth();
                }
            }
        }
    };
    Timer timer = new Timer();
    TimerTask task = new TimerTask()
    {
        public void run()
        {
            Message message = new Message();
            message.what = 1;
            myHandler.sendMessage(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        context=this;
        application.addActivity(this);
        setContentView(R.layout.activity_modify_pic);
        bundle=getIntent().getExtras();
        formWhere=bundle.getString("tag");

        initView();
        initData();


    }


    private void initView() {

        ((TextView)findViewById(R.id.tvtitle)).setText("制作表情");
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
        typeface_a= (TextView) findViewById(R.id.typeface_a);
        typeface_b= (TextView) findViewById(R.id.typeface_b);
        typeface_c= (TextView) findViewById(R.id.typeface_c);
        addTextImg= (ImageView) findViewById(R.id.addTextImg);
        this.edWords = (EditText) findViewById(R.id.edWords);
        tvcc= (TextView) findViewById(R.id.tvcc);
        tvMul= (TextView) findViewById(R.id.tvMul);
        this.colortag = (ColorTagImageView) findViewById(R.id.color_tag);
        this.mainLayout = (MyRelativeLayout) findViewById(R.id.mainLayout);
        tvRight= (TextView) findViewById(R.id.tvRight);

        mScrollView= (ScrollView) findViewById(R.id.mScrollView);
        tvRight.setOnClickListener(this);
        typeface_a.setOnClickListener(this);
        typeface_b.setOnClickListener(this);
        typeface_c.setOnClickListener(this);
        addTextImg.setOnClickListener(this);
        tvMul.setOnClickListener(this);
        int width= CommUtil.getScreenWidth();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width,-1);
        mainLayout.setLayoutParams(lp);
        colortag.setListener(new ColorTagImageView.OnColorTagChanges() {
            @Override
            public void onColorChange(int colors) {

            tvcc.setTextColor(colors);
            wordsColor=colors;
            updateTextObj(true);


            }
        });

        edWords.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {

                if(s.length()>0){
                    words=edWords.getText().toString();
                    updateTextObj(false);
                }else{
                    words="请输入文字";
                    mainLayout.setMessage(words);
                }
            }
        });

        typeface_b.setTypeface(CommUtil.getTypeface("新蒂小丸子体",Typeface.NORMAL));
        typeface_c.setTypeface(CommUtil.getTypeface("华康少女",Typeface.NORMAL));
        setTypeface(SharedUtils.getInt(null,"typeface_"));


        mainLayout.setTouchCallBack(new MyRelativeLayout.MyRelativeTouchCallBack() {
            @Override
            public void touchMoveCallBack(int direction) {}
            @Override
            public void onTextViewMoving(TextView textView) {}
            @Override
            public void onTextViewMovingDone() {}
            @Override
            public void onTextViewClick(TextView textView) {
                updateTextObj(true);
            }

        });

        stateHelper=new SoftKeyboardStateHelper(mainLayout);
        stateHelper.addSoftKeyboardStateListener(new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                mainLayout.getLayoutParams().height=height;
                //mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
            @Override
            public void onSoftKeyboardClosed() {

            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setSimulateClick(mainLayout);
            }
        },1200);

    }

    /**
     * 更新文字
     * @param isColorChange
     */
    private void updateTextObj(boolean isColorChange) {

        mainLayout.setColor(wordsColor);
        mainLayout.setMessage(words);
        mainLayout.setTypeface(typeface);
        if(mainLayout.getTextView()!=null&&isColorChange){
            //自定义描边的textview是用反射设置的颜色， 重新执行ondraw（）也无法刷新颜色。移除一下在重新添加颜色就能改变了
            mainLayout.updateTv();
        }
        mainLayout.addTv(false);

    }

    void initData(){

        //是否从相册选图过来的
        if(formWhere!=null){

            fileUri=bundle.getParcelable("fileUri");
            try {
                showBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(),fileUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            words="来自相册的图片~";

            //先保存一下未添加文字之前的图片作为原图
            showPath = ImageUtils.saveBitmapToFiles(showBitmap, null);
            Log.v("","相册选来的原图已保存至:" + showPath);

            //从相册选图过来的 需要new一个DataBean
                dataBean=new DataBean();
                dataBean.setGifPath(showPath);// GifPath该字段只有在从相册选图时，该地址会保存截图的本地文件路径
                dataBean.setName(words);
                dataBean.setId((int)System.currentTimeMillis());
                WIDTH=HEIGHT=showBitmap.getWidth();//相册选来的图片保存我稍微设置大一点

        }else{

            showWaitDialog(context,null,true);

            dataBean= (DataBean) getIntent().getSerializableExtra("dataBean");
            //如果不是从我的制作界面进来的,dataBean改为从数据库读取
//            if(!"DIY".equals(dataBean.getFormWhere())){
//                DataBean bean=DBTools.getInstance().madeById(String.valueOf(dataBean.getId()));
//                if(bean!=null){
//                    dataBean=bean;
//                }
//            }

            //showPath=dataBean.getGifPath();//这个地址是网络图片地址是不行de (需要先保存本地)
            showPath=null;//没下载好之前先置空
            dataBean.setFormWhere("DIY");//为了保存到临时temp文件下随时清空，才改为的DIY
            CommUtil.onDownLoad(dataBean, context, new CommInterface.setListener() {
                @Override
                public void onResult(String picpath) {
                    showPath=picpath;
                    dataBean.setFormWhere(null);

                    if(TextUtils.isEmpty(picpath)){
                        CommUtil.showDialog(context, showPath + "\n表情原图不存在或已被删除!", "知道了"
                                , null, new CommInterface.setClickListener() {
                            @Override
                            public void onResult() {
                                setResult(RESULT_CANCELED);
                                finish();
                            }
                        }, null);

                    }
                }
            });


            if(!TextUtils.isEmpty(dataBean.getName())){
                words=dataBean.getName();
            }
        }

        edWords.setText(words);
        edWords.setSelection(words.length());

        // 延迟每次延迟10 毫秒 隔1秒执行一次
        timer.schedule(task, 10, 1000);

    }

    public void setTypeface(int type){


        wordsColor=SharedUtils.getInt(null,"wordsColor");
        tvcc.setTextColor(wordsColor);
        String  name=null;
        if(type==0){
            edWords.setTypeface(CommUtil.getTypeface(name,Typeface.BOLD));
            typeface_a.setTextColor(Color.parseColor("#00afec"));
            typeface_b.setTextColor(Color.parseColor("#6D6D6D"));
            typeface_c.setTextColor(Color.parseColor("#6D6D6D"));
        }else if(type==1){
            name="新蒂小丸子体";
            edWords.setTypeface(CommUtil.getTypeface(name,Typeface.BOLD));
            typeface_a.setTextColor(Color.parseColor("#6D6D6D"));
            typeface_b.setTextColor(Color.parseColor("#00afec"));
            typeface_c.setTextColor(Color.parseColor("#6D6D6D"));
        }else if(type==2){
            name="华康少女";
            edWords.setTypeface(CommUtil.getTypeface(name,Typeface.BOLD));
            typeface_a.setTextColor(Color.parseColor("#6D6D6D"));
            typeface_b.setTextColor(Color.parseColor("#6D6D6D"));
            typeface_c.setTextColor(Color.parseColor("#00afec"));
        }

        typeface=CommUtil.getTypeface(name,Typeface.NORMAL);
        updateTextObj(true);
        SharedUtils.putString(null,"typeface",name);
        SharedUtils.putInt(null,"typeface_",type);
    }

    int height=0;
    private void fillContent()
    {
        //showPath 是有可能被 用户跑到文件夹里自己删掉 。
        resizeBmp = BitmapFactory.decodeFile(showPath);
        if(resizeBmp!=null){
            //缩放一下bitmap保证bitmap宽高适应view
           // resizeBmp = ImageUtils.scaleWithWH(resizeBmp,mainLayout.getWidth(),mainLayout.getHeight());
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(
                    mainLayout.getWidth()-CommUtil.dip2px(30),mainLayout.getHeight()-CommUtil.dip2px(30));
            lp.gravity= Gravity.CENTER;
            mainLayout.setLayoutParams(lp);
            mainLayout.setBackGroundBitmap(resizeBmp);
           // mainLayout.setBackgroundColor(0x80FF4081);
            height=mainLayout.getHeight();
        }else{

            CommUtil.showDialog(context,showPath+"\n表情原图不存在或已被删除!", "知道了", null,null,null);
            setResult(RESULT_CANCELED);
            finish();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedUtils.putInt(null,"wordsColor",wordsColor);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.tvRight:
                save();
                break;
            case R.id.typeface_a:
                setTypeface(0);
                break;
            case R.id.typeface_b:
                setTypeface(1);
                break;
            case R.id.typeface_c:
                setTypeface(2);
                break;
            case R.id.addTextImg:
                mainLayout.removeAllThings();
                //edWords.setText("");
                break;


        }
    }



    private void save() {

        bitmap = ImageUtils.createViewBitmap(mainLayout,1);
        bitmap=ImageUtils.scaleWithWH(bitmap,HEIGHT,WIDTH);
     //   bitmap = ImageUtils.createViewBitmap(operateView,HEIGHT,WIDTH);

        String filePath = ImageUtils.saveBitmapToFiles(bitmap, dataBean);
        Log.v("","已保存至:" + filePath);

        dataBean.setName(words);
        dataBean.setMadeUrl(filePath);

        if (CommUtil.isWeiBaopen()) {
            CommUtil.getInstance().showSharePop(context,dataBean,null);
        } else {
            CommUtil.onDownLoad(dataBean,context,0);
        }

        DBTools.getInstance().addMades(dataBean);

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bitmap!=null){
            bitmap.recycle();
        }
        if(showBitmap!=null){
            showBitmap.recycle();
        }
        if(resizeBmp!=null){
            resizeBmp.recycle();
        }
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_MENU:
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_BACK:
                setResult(RESULT_OK);
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    private void setSimulateClick(View view) {
        float x=100;
        float y=height-getResources().getDimensionPixelOffset(R.dimen.dp80);
        long downTime = SystemClock.uptimeMillis();
        final MotionEvent downEvent = MotionEvent.obtain(downTime, downTime,
                MotionEvent.ACTION_DOWN, x, y, 0);
        downTime += 1000;
        final MotionEvent upEvent = MotionEvent.obtain(downTime, downTime,
                MotionEvent.ACTION_UP, x, y, 0);
        view.onTouchEvent(downEvent);
        view.onTouchEvent(upEvent);
        downEvent.recycle();
        upEvent.recycle();
    }




}
