package com.mediatek.camera;

import com.mediatek.camera.R;
import android.util.Log;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL;
import android.provider.Settings;
import com.mediatek.camera.common.utils.CameraUtil;


/**
 * Created by huangfei on 17-6-23.
 */
public class BvUtils {
    private static final String TAG = "BvUtils";
    private BvUtils() {
    }
    public static int[][] bright_map = new int[][] {
        {-999,   3080,   0}, //too dark begin.
        {-100,   3080,   0},
        {-50,    3080,   1},
        {-40,    3080,   5},
        {-30,    3080,   10},
        {-25,    3080,   14},//5
        {-20,    3080,   18}, 
        {-15,    3080,   25},
        {-10,    3080,   32},
        {-5,     3080,   64},
        {0,      3080,   81},//10
        {5,      3080,   105}, 
        {10,     3080,   123},
        {15,     2618,   157},
        {20,     1848,   245},
        {25,     1078,   255},//15
        {30,     770,    255},
        {35,     616,    255},
        {40,     308,    255},
        {45,     154,    255},
        {50,     154,    255},//20
        {55,     141,    255}, 
        {60,     97,     255}, 
        {65,     70,     255},
        {70,     64,     255},
        {75,     32,     255},//25
        {80,     22,     255},        
        {85,     14,     255},
        {90,     8,      255},
        {100,    6,      255},
        {110,    4,      255},//30
        {120,    4,      255},
        {130,    4,      255},
        {140,    4,      255},
    };
    
    public static boolean is_now_subcam2_covered(int level, int reg1, int reg2) {
        boolean ret = false;
        if(level >= 28){ 
            ret = (reg1 >= bright_map[26][1]);
        } else if(level >= 22){
            ret = (reg1 >= bright_map[level-5][1])||(reg2 <= bright_map[14][2]);
        } else if(level >= 16){
            ret = (reg1 >= bright_map[level-6][1])&&(reg2 <= bright_map[13][2]);
        } else if(level >= 10){
            ret = (reg1 >= bright_map[2][1])&&(reg2 <= bright_map[5][2]);
        } else if(level >= 3){
            ret = (reg1 >= bright_map[2][1])&&(reg2 <= bright_map[2][2]);
        } else{ //too dark. we set as covered.
            ret = true;
        }
        Log.d(TAG, "is_now_subcam2_covered. ret:" + ret);
        if((reg1>3080)||(reg1==0)&&(reg2==0)){
            ret = false;
        }
        return ret;
    }

    public static int[][] bright_map1 = new int[][] {
        {-100,   3080,   0},
        {-50,    3080,   0},
        {-40,    3080,   1},
        {-30,    3080,   5},
        {-25,    3080,   6},//5
        {-20,    3080,   7}, 
        {-15,    3080,   12},
        {-10,    3080,   16},
        {-5,     3080,   24},
        {0,      3080,   30},//10
        {5,      3080,   47}, 
        {10,     3080,   80},
        {15,     3080,   107},
        {20,     3080,   128},
        {25,     2668,   170},//15
        {30,     1540,   238},
        {35,     1078,   255},
        {40,     770,    255},
        {45,     616,    255},
        {50,     462,    255},//20
        {55,     308,    255}, 
        {60,     154,    255}, 
        {65,     154,    255},
        {70,     147,    255},
        {75,     120,    255},//25
        {80,     69,     255},        
        {85,     34,     255},
        {90,     21,     255},
        {100,    12,     255},
        {110,    6,      255},//30
        {120,    4,      255},
        {130,    4,      255},
    };

    public static boolean is_now_subcam2_covered1(int level, int reg1, int reg2) {
        boolean ret = false;
        if(level >= 22){ //bright light, we use reg1 to judge.
            ret = reg1 >= bright_map1[level-6][1];
        } else if(level >= 12){ 
            ret = reg2 <= bright_map1[level-7][2];
        } else if(level >= 7){ //dark, we need use reg2 to judge.
                ret = reg2 <= bright_map1[level-5][2];//if is darker than -3 level. is is covered..
        } else{ //too dark. we set as covered.
            ret = true;
        }
        if(reg1>3080){
            ret = false;
        }
        return ret;    
    }
    
    public static int[][] getBrightMap(){
        return bright_map;
    }
    public static int[][] getBrightMap1(){
        return bright_map1;
    }

    //bv wuyonglin add for setting ui 20200923 start
    public static AlertDialog generateNormalDialog(Context context, String title, String message,
                                                   String posTitle, View.OnClickListener listener, DialogInterface.OnDismissListener dismissListener){
        return generateNormalDialog(context, title, message,
                posTitle, listener, dismissListener, false);
    }

    public static AlertDialog generateNormalDialog(Context context, String title, String message,
                       String posTitle, View.OnClickListener listener, DialogInterface.OnDismissListener dismissListener, boolean isSingleButton){
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        final Window window = alertDialog.getWindow();
        assert window != null;
        View decorView = window.getDecorView();
        /* modify by liangchangwei for new API changed begin */
        decorView.setBackground(context.getDrawable(R.drawable.bv_dialog_background));
        /* modify by liangchangwei for new API changed end */
        //bv xiaoye modify for bug-2216 20200919 start
        decorView.setPadding(0, 0, 0, 0);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        Settings.Global.putInt(context.getContentResolver(), "dk.light.navigation", 1);
        //bv xiaoye modify for bug-2216 20200919 end

        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.BvDialogStyleAnimation);
        window.setAttributes(lp);

        //bv xiaoye modify for bug-2216 20200919 start
        View view;
        if (CameraUtil.isEdgeToEdgeEnabled(context)){
            view = LayoutInflater.from(context).inflate(R.layout.bv_dialog_normal_gest, null);
        }else {
            view = LayoutInflater.from(context).inflate(R.layout.bv_dialog_normal, null);
        }
        //bv xiaoye modify for bug-2216 20200919 end
        TextView tvTitle = view.findViewById(R.id.bv_dialog_title);
        if (tvTitle != null){
            tvTitle.setText(title);
        }

        TextView tvMessage = view.findViewById(R.id.bv_dialog_message);
        if (tvMessage != null){
            tvMessage.setText(message);
        }

        TextView tvCalcel = view.findViewById(R.id.bv_dialog_cancel);
        if (tvCalcel != null/* && listener != null*/){
            tvCalcel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
        }

        TextView tvOk = view.findViewById(R.id.bv_dialog_ok);
        View tvDivider = view.findViewById(R.id.bv_dialog_divider);
        if (tvOk != null){
            if (isSingleButton){
                tvOk.setVisibility(View.GONE);
                tvDivider.setVisibility(View.GONE);
            }else {
                if (!TextUtils.isEmpty(posTitle)){
                    tvOk.setText(posTitle);
                }
                tvOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        if (listener != null){
                            listener.onClick(v);
                        }
                    }
                });
            }
        }

        //bv xiaoye modify for bug-2216 20200919 start
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Settings.Global.putInt(context.getContentResolver(), "dk.light.navigation", 0);
                if (dismissListener != null){
                    dismissListener.onDismiss(dialog);
                }
            }
        });
        //bv xiaoye modify for bug-2216 20200919 end
        //bv xiaoye modify for bug-2218 20200919 end

        alertDialog.setView(view);

        //alertDialog.setCanceledOnTouchOutside(false);
        //alertDialog.setCancelable(false);

        return alertDialog;
    }
    //bv wuyonglin add for setting ui 20200923 end
}
