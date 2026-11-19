package br.com.nivo.volume;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.PowerManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.media.AudioManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class FloatingService extends Service {
    public static final String ACTION_STOP = "br.com.nivo.volume.STOP";
    public static final String ACTION_REFRESH = "br.com.nivo.volume.REFRESH";
    private static final String CHANNEL = "nivo_volume";
    private static final int NOTIFICATION_ID = 481;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable autoCollapse = () -> { if(expanded) { expanded=false; reposition(false); render(); } };
    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private FrameLayout frame;
    private AudioManager audio;
    private boolean expanded;
    private int selected = 0;
    private int collapsedX, collapsedY;
    private float touchX, touchY;
    private int initialX, initialY;
    private boolean moved;
    private android.graphics.drawable.GradientDrawable rgbDockBackground;
    private TextView rgbPlus;
    private TextView rgbMinus;
    private final Runnable rgbTicker = new Runnable(){
        @Override public void run(){
            if(frame==null || expanded || Prefs.integer(FloatingService.this,"style",1)!=4
                || !Prefs.flag(FloatingService.this,"rgb_animate",true))return;
            PowerManager power=(PowerManager)getSystemService(POWER_SERVICE);
            if(power==null || power.isInteractive()){
                int seconds=Math.max(3,Prefs.integer(FloatingService.this,"rgb_speed",8));
                float hue=((android.os.SystemClock.uptimeMillis()%(seconds*1000L))*360f)/(seconds*1000f);
                int color=Color.HSVToColor(new float[]{hue,.90f,1f});
                if(rgbDockBackground!=null)rgbDockBackground.setStroke(dp(2),color);
                if(rgbPlus!=null)rgbPlus.setBackgroundTintList(ColorStateList.valueOf(color));
                if(rgbMinus!=null)rgbMinus.setTextColor(color);
            }
            handler.postDelayed(this,(power!=null&&!power.isInteractive())?1200:100);
        }
    };
    private final int[] STREAMS = {AudioManager.STREAM_MUSIC,AudioManager.STREAM_RING,
            AudioManager.STREAM_NOTIFICATION,AudioManager.STREAM_ALARM};
    private final String[] NAMES = {"MÍDIA","TOQUE","NOTIFIC.","ALARME"};
    private final String[] ICONS = {"♫","◖","✧","◷"};

    private int dp(float value) {return Ui.dp(this,value);}
    private float scale(){return Prefs.integer(this,"size",100)/100f;}
    private int s(float value){return dp(value*scale());}
    private int a(){return Prefs.accent(this);}
    private int buttonDp(){return Math.max(38,Math.min(92,Prefs.integer(this,"button_size",52)));}
    private int gapDp(){return Math.max(28,Math.min(116,Prefs.integer(this,"button_gap",32)));}
    private int dockWidth(){return s(buttonDp()+10);}
    private int dockHeight(){return s(buttonDp()*2+gapDp()+10);}
    private void clampDock(){
        if(params==null)return;
        int width=expanded?Math.min(s(288),screenWidth()-dp(18)):dockWidth();
        int height=expanded?s(410):dockHeight();
        params.x=Math.max(0,Math.min(params.x,Math.max(0,screenWidth()-width)));
        params.y=Math.max(dp(24),Math.min(params.y,Math.max(dp(24),screenHeight()-height-dp(28))));
    }
    private void restartRGB(){
        handler.removeCallbacks(rgbTicker);
        if(frame!=null&&!expanded&&Prefs.integer(this,"style",1)==4&&Prefs.flag(this,"rgb_animate",true)){
            handler.post(rgbTicker);
        }
    }

    @Override public void onCreate() {
        super.onCreate();
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        audio=(AudioManager)getSystemService(AUDIO_SERVICE);
        selected=Math.max(0,Math.min(3,Prefs.integer(this,"stream",0)));
        createNotificationChannel();
    }

    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        String action=intent==null?null:intent.getAction();
        if(ACTION_STOP.equals(action)){Prefs.put(this,"enabled",false);stopSelf();return START_NOT_STICKY;}
        if(!Settings.canDrawOverlays(this)){
            Prefs.put(this,"enabled",false);stopSelf();return START_NOT_STICKY;
        }
        try{
            startForeground(NOTIFICATION_ID, notification());
            if(frame==null) addOverlay();
            else if(ACTION_REFRESH.equals(action)) {
                if(expanded) {
                    params.x=Math.max(0,Math.min(params.x,screenWidth()-Math.min(s(288),screenWidth()-dp(18))));
                    params.y=Math.max(dp(24),Math.min(params.y,screenHeight()-s(410)));
                }
                clampDock();
                render();
            }
            return START_STICKY;
        }catch(Exception e){
            Prefs.put(this,"enabled",false);
            Toast.makeText(this,"NIVO: não foi possível abrir o controle",Toast.LENGTH_LONG).show();
            stopSelf();return START_NOT_STICKY;
        }
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel ch=new NotificationChannel(CHANNEL,"NIVO Volume",NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Controle de volume flutuante em execução");
            ch.setSound(null,null);
            NotificationManager manager=getSystemService(NotificationManager.class);
            if(manager!=null)manager.createNotificationChannel(ch);
        }
    }
    private Notification notification(){
        Intent open=new Intent(this,MainActivity.class);
        PendingIntent toSettings=PendingIntent.getActivity(this,0,open,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Intent stop=new Intent(this,FloatingService.class).setAction(ACTION_STOP);
        PendingIntent toStop=PendingIntent.getService(this,1,stop,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this,CHANNEL)
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                .setContentTitle("NIVO Volume ativo")
                .setContentText("Toque para personalizar • painel flutuante")
                .setContentIntent(toSettings)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,"Parar",toStop)
                .build();
    }
    private int screenWidth(){
        DisplayMetrics m=new DisplayMetrics();wm.getDefaultDisplay().getRealMetrics(m);return m.widthPixels;
    }
    private int screenHeight(){
        DisplayMetrics m=new DisplayMetrics();wm.getDefaultDisplay().getRealMetrics(m);return m.heightPixels;
    }
    private void addOverlay(){
        collapsedX=Prefs.integer(this,"position_x",screenWidth()-dp(76));
        collapsedY=Prefs.integer(this,"position_y",screenHeight()/3);
        params=new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.LEFT;
        params.x=collapsedX;
        params.y=collapsedY;
        clampDock();
        params.alpha=Prefs.integer(this,"opacity",92)/100f;
        frame=new FrameLayout(this);
        wm.addView(frame,params);
        render();
    }
    private void reposition(boolean expansion){
        if(frame==null)return;
        params.alpha=Prefs.integer(this,"opacity",92)/100f;
        if(expansion){
            collapsedX=params.x;collapsedY=params.y;
            params.x=Math.min(params.x,Math.max(0,screenWidth()-s(296)-dp(10)));
            params.y=Math.min(params.y,Math.max(dp(24),screenHeight()-s(410)));
        }else{
            params.x=collapsedX;
            params.y=collapsedY;
            clampDock();
        }
        wm.updateViewLayout(frame,params);
    }
    private void render(){
        if(frame==null)return;
        handler.removeCallbacks(autoCollapse);
        handler.removeCallbacks(rgbTicker);
        rgbDockBackground=null;rgbPlus=null;rgbMinus=null;
        params.alpha=Prefs.integer(this,"opacity",92)/100f;
        frame.removeAllViews();
        if(expanded) showExpanded();
        else showCollapsed();
        wm.updateViewLayout(frame,params);
        frame.setAlpha(0.86f);frame.setScaleX(0.96f);frame.setScaleY(0.96f);
        frame.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(180L).start();
        scheduleCollapse();
        restartRGB();
    }
    private void showCollapsed(){
        // Os dois botões ficam sempre acessíveis, com tamanho e espaçamento próprios.
        int style=Prefs.integer(this,"style",1);
        int size=s(buttonDp());
        LinearLayout dock=Ui.column(this);
        dock.setGravity(Gravity.CENTER);
        dock.setPadding(s(5),s(5),s(5),s(5));
        int bg=(style==0)?0xFF111824:(style==1)?0xEE263447:
                (style==2)?0xFF090D19:(style==3)?0xFF211934:0xFF11111D;
        int stroke=(style==0)?Prefs.OUTLINE:(style==1)?0xFF54718D:(style==3)?0xFFB39AFF:a();
        rgbDockBackground=Ui.rounded(bg,s(style==0?13:23),stroke,dp(style>=2?2:1));
        dock.setBackground(rgbDockBackground);
        if(style==3)dock.setElevation(dp(9));

        TextView plus=Ui.text(this,"+",Math.max(20,(int)(buttonDp()*.60f)),0xFF07131D,true);
        plus.setGravity(Gravity.CENTER);
        plus.setBackground(Ui.rounded(style==3?0xFFBD99FF:a(),s(style==0?11:18),0,0));
        plus.setContentDescription("Aumentar volume de "+NAMES[selected]);
        dock.addView(plus,new LinearLayout.LayoutParams(size,size));

        TextView percentage=Ui.text(this,"",12,style==3?0xFFC7B5FF:a(),true);
        percentage.setGravity(Gravity.CENTER);
        percentage.setContentDescription("Arraste para mover o controle; toque para abrir o painel completo");
        dock.addView(percentage,new LinearLayout.LayoutParams(size,s(gapDp())));

        TextView minus=Ui.text(this,"−",Math.max(20,(int)(buttonDp()*.60f)),style==3?0xFFBBA5FA:Prefs.TEXT,true);
        minus.setGravity(Gravity.CENTER);
        int minusBg=(style==0)?0xFF222D3C:(style==3)?0xFF34284C:0xFF273A50;
        minus.setBackground(Ui.rounded(minusBg,s(style==0?11:18),0,0));
        minus.setContentDescription("Diminuir volume de "+NAMES[selected]);
        dock.addView(minus,new LinearLayout.LayoutParams(size,size));

        frame.addView(dock);
        if(style==4){rgbPlus=plus;rgbMinus=minus;}
        updateQuickPercentage(percentage);
        plus.setOnClickListener(v->{
            v.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
            quickStep(1,percentage);
        });
        minus.setOnClickListener(v->{
            v.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
            quickStep(-1,percentage);
        });
        percentage.setOnTouchListener(this::dragOrOpen);
    }
    private void updateQuickPercentage(TextView label){
        int stream=STREAMS[selected];
        int max=Math.max(1,audio.getStreamMaxVolume(stream));
        int actual=Math.max(0,audio.getStreamVolume(stream));
        label.setText((int)Math.round(100.0*actual/max)+"%");
    }
    private void quickStep(int delta,TextView label){
        int stream=STREAMS[selected];
        int next=Math.max(0,Math.min(audio.getStreamMaxVolume(stream),
                audio.getStreamVolume(stream)+delta));
        changeVolume(stream,next);
        updateQuickPercentage(label);
    }
        private boolean dragOrOpen(View view, MotionEvent event) {
        switch(event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                touchX=event.getRawX();touchY=event.getRawY();
                initialX=params.x;initialY=params.y;
                moved=false;
                handler.removeCallbacks(autoCollapse);
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx=event.getRawX()-touchX,dy=event.getRawY()-touchY;
                if(Math.abs(dx)>dp(9)||Math.abs(dy)>dp(9))moved=true;
                if(moved) {
                    params.x=initialX+(int)dx;
                    params.y=initialY+(int)dy;
                    clampDock();
                    wm.updateViewLayout(frame,params);
                }
                return true;
            case MotionEvent.ACTION_UP:
                if(moved){
                    collapsedX=params.x;collapsedY=params.y;
                    Prefs.put(this,"position_x",collapsedX);
                    Prefs.put(this,"position_y",collapsedY);
                }else{
                    expanded=true;reposition(true);render();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:return true;
            default:return false;
        }
    }
    private TextView pill(String txt,int color,int textColor,int width,int height){
        TextView t=Ui.text(this,txt,14,textColor,true);
        t.setGravity(Gravity.CENTER);
        t.setBackground(Ui.rounded(color,s(16),0,0));
        t.setLayoutParams(new LinearLayout.LayoutParams(s(width),s(height)));
        return t;
    }
    private void showExpanded(){
        int panelW=Math.min(s(288),screenWidth()-dp(18));
        LinearLayout box=Ui.column(this);
        box.setPadding(s(17),s(15),s(17),s(18));
        int style=Prefs.integer(this,"style",1);
        int background=style==0?0xFF101723:style==1?0xF0223041:style==3?0xFF211934:0xFF090F1B;
        box.setBackground(Ui.rounded(background,s(25),style==2?a():0xFF3D5367,dp(style==2?2:1)));
        frame.addView(box,new FrameLayout.LayoutParams(panelW,-2));
        LinearLayout heading=Ui.row(this);
        TextView label=Ui.text(this,"◖   N I V O",15,a(),true);
        label.setLetterSpacing(.09f);
        heading.addView(label,new LinearLayout.LayoutParams(0,s(42),1));
        TextView minimize=pill("–",0xFF253347,Prefs.TEXT,40,36);
        minimize.setContentDescription("Recolher controle");
        minimize.setOnClickListener(v->{expanded=false;reposition(false);render();});
        heading.addView(minimize);box.addView(heading);
        box.addView(Ui.gap(this,14));

        LinearLayout categories=Ui.row(this);
        for(int i=0;i<4;i++){
            final int index=i;
            LinearLayout cell=Ui.column(this);
            cell.setGravity(Gravity.CENTER);
            int bg=i==selected?0xFF2A4861:0xFF182535;
            cell.setBackground(Ui.rounded(bg,s(13),i==selected?a():0,s(i==selected?1:0)));
            TextView symbol=Ui.text(this,ICONS[i],18,i==selected?a():Prefs.MUTED,true);
            symbol.setGravity(Gravity.CENTER);
            cell.addView(symbol);
            TextView name=Ui.text(this,NAMES[i],8,i==selected?Prefs.TEXT:Prefs.MUTED,true);
            name.setGravity(Gravity.CENTER);cell.addView(name);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,s(62),1);
            if(i>0)cp.leftMargin=s(5);
            categories.addView(cell,cp);
            cell.setOnClickListener(v->{selected=index;Prefs.put(this,"stream",selected);render();});
        }
        box.addView(categories);
        box.addView(Ui.gap(this,18));
        LinearLayout captions=Ui.row(this);
        captions.addView(Ui.text(this,NAMES[selected],11,Prefs.MUTED,true),new LinearLayout.LayoutParams(0,-2,1));
        TextView percentage=Ui.text(this,"",26,Prefs.TEXT,true);
        captions.addView(percentage);box.addView(captions);
        box.addView(Ui.gap(this,9));

        int stream=STREAMS[selected];
        int max=Math.max(1,audio.getStreamMaxVolume(stream));
        int current=Math.max(0,audio.getStreamVolume(stream));
        percentage.setText((int)Math.round(100.0*current/max)+"%");
        SeekBar seek=new SeekBar(this);
        seek.setMax(max);
        seek.setProgress(current);
        seek.setProgressTintList(ColorStateList.valueOf(a()));
        seek.setProgressBackgroundTintList(ColorStateList.valueOf(0xFF40526A));
        seek.setThumbTintList(ColorStateList.valueOf(a()));
        box.addView(seek,new LinearLayout.LayoutParams(-1,s(43)));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar bar,int val,boolean fromUser){
                if(fromUser){
                    changeVolume(stream,val);
                    int actual=Math.max(0,audio.getStreamVolume(stream));
                    percentage.setText((int)Math.round(100.0*actual/max)+"%");
                    if(actual!=val)bar.setProgress(actual);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar bar){handler.removeCallbacks(autoCollapse);}
            @Override public void onStopTrackingTouch(SeekBar bar){scheduleCollapse();}
        });

        box.addView(Ui.gap(this,10));
        LinearLayout buttons=Ui.row(this);
        TextView minus=pill("−",0xFF2A374A,Prefs.TEXT,77,50);
        buttons.addView(minus,new LinearLayout.LayoutParams(0,s(50),1));
        TextView volumeIcon=Ui.text(this,"◖",25,a(),true);
        volumeIcon.setGravity(Gravity.CENTER);
        buttons.addView(volumeIcon,new LinearLayout.LayoutParams(s(55),s(50)));
        TextView plus=pill("+",a(),0xFF06111D,77,50);
        buttons.addView(plus,new LinearLayout.LayoutParams(0,s(50),1));
        minus.setOnClickListener(v->step(stream,-1,seek,percentage,max));
        plus.setOnClickListener(v->step(stream,1,seek,percentage,max));
        box.addView(buttons);
        box.addView(Ui.gap(this,15));
        TextView foot=Ui.text(this,"ARRASTE O LOGO PARA MOVER",10,Prefs.MUTED,true);
        foot.setGravity(Gravity.CENTER);foot.setLetterSpacing(.1f);
        box.addView(foot);
        heading.setOnTouchListener((v,e)->{
            if(e.getActionMasked()==MotionEvent.ACTION_DOWN){
                touchX=e.getRawX();touchY=e.getRawY();
                initialX=params.x;initialY=params.y;return true;
            }
            if(e.getActionMasked()==MotionEvent.ACTION_MOVE){
                params.x=Math.max(0,Math.min(screenWidth()-panelW,initialX+(int)(e.getRawX()-touchX)));
                params.y=Math.max(dp(24),Math.min(screenHeight()-s(360),initialY+(int)(e.getRawY()-touchY)));
                wm.updateViewLayout(frame,params);return true;
            }
            if(e.getActionMasked()==MotionEvent.ACTION_UP){
                collapsedX=params.x;collapsedY=params.y;
                Prefs.put(this,"position_x",collapsedX);
                Prefs.put(this,"position_y",collapsedY);
                scheduleCollapse();return true;
            }
            return true;
        });
    }
    private void changeVolume(int stream,int value){
        try{ audio.setStreamVolume(stream,value,0); }
        catch(SecurityException e){ Toast.makeText(this,"Permissão de volume indisponível neste modo",Toast.LENGTH_SHORT).show(); }
        catch(Exception ignored){}
        scheduleCollapse();
    }
    private void step(int stream,int delta,SeekBar bar,TextView percentage,int max){
        int next=Math.max(0,Math.min(max,audio.getStreamVolume(stream)+delta));
        changeVolume(stream,next);
        int actual=audio.getStreamVolume(stream);
        bar.setProgress(actual);
        percentage.setText((int)Math.round(actual*100.0/max)+"%");
    }
    private void scheduleCollapse(){
        handler.removeCallbacks(autoCollapse);
        if(expanded && Prefs.flag(this,"auto",true)){
            int seconds=Math.max(2,Prefs.integer(this,"collapse",6));
            handler.postDelayed(autoCollapse,seconds*1000L);
        }
    }
    @Override public void onDestroy() {
        handler.removeCallbacks(autoCollapse);
        handler.removeCallbacks(rgbTicker);
        if(frame!=null) {
            try{wm.removeView(frame);}catch(Exception ignored){}
            frame=null;
        }
        // Preserve a escolha do usuário ao encerrar por iniciativa do sistema.
        // Parar explicitamente e desligar no app já desativam a preferência.
        super.onDestroy();
    }
    @Override public void onConfigurationChanged(android.content.res.Configuration config) {
        super.onConfigurationChanged(config);
        if(frame==null)return;
        clampDock();
        collapsedX=Math.max(0,Math.min(collapsedX,Math.max(0,screenWidth()-dockWidth())));
        collapsedY=Math.max(dp(24),Math.min(collapsedY,Math.max(dp(24),screenHeight()-dockHeight()-dp(28))));
        wm.updateViewLayout(frame,params);
        render();
    }
    @Override public IBinder onBind(Intent i) {return null;}
}
