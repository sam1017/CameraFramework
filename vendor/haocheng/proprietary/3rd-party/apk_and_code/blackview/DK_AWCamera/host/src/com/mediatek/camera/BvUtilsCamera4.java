package com.mediatek.camera;

import com.mediatek.camera.R;
import android.util.Log;

/**
 * Created by huangfei on 17-6-23.
 */
public class BvUtilsCamera4 {
    private static final String TAG = "BvUtilsCamera4";
    private BvUtilsCamera4() {
    }
    public static int[][] bright_map = new int[][] {
        {-999,  3080,   0}, //too dark begin.
        {-100,  3080,   0},
        {-50,   3080,   1},
        {-40,   3080,   5},
        {-30,   3080,   10},
        {-25,   3080,   14},//5
        {-20,   3080,   18}, 
        {-15,   3080,   25},
        {-10,   3080,   32},
        {-5,    3080,   64},
        {0,     3080,   81},//10
        {5,     3080,   105}, 
        {10,    3080,   123},
        {15,    2618,   157},
        {20,    1848,   245},
        {25,    1078,   255},//15
        {30,    770,    255},
        {35,    616,    255},
        {40,    308,    255},
        {45,    154,    255},
        {50,    154,    255},//20
        {55,    141,    255}, 
        {60,    97,     255}, 
        {65,    70,     255},
        {70,    64,     255},
        {75,    32,     255},//25
        {80,    22,     255},        
        {85,    14,     255},
        {90,    8,      255},
        {100,   6,      255},
        {110,   4,      255},//30
        {120,   4,      255},
        {130,   4,      255},
        {140,   4,      255},
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


    public static int[][] getBrightMap(){
        return bright_map;
    }

}
