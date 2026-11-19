package br.com.nivo.volume;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PixelFormat;
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
    private final int[] STREAMS = {AudioManager.STREAM_MUSIC,AudioManager.STREAM_RING,
            AudioManager.STREAM_NOTIFICATION,AudioManager.STREAM_ALARM};
    private final String[] NAMES = {"MÍDIA","TOQUE","NOTIFIC.","ALARME"};
    private final String[] ICONS = {"♫","◖","✧","◷"};

    private int dp(float value) {return Ui.dp(this,value);}
    private float scale(){return Prefs.integer(this,"size",100)/100f;}
    private int s(float value){return dp(value*scale());}
    private int a(){return Prefs.accent(this);}

    @Override public void onCreate() {
        super.onCreate();
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        audio=(AudioManager)getSystemService(AUDIO_SERVICE);
        selected=Math.max(0,Math.min(3,Prefs.integer(this,"stream",0)));
        createNotificationChannel();
    }

    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        String action=intent==null?null:intent.getAction();
        if(ACTION_STOP.equals(action)){stopSelf();return START_NOT_STICKY;}
        if(!Settings.canDrawOverlays(this)){
            Prefs.put(this,"enabled",false);stopSelf();return START_NOT_STICKY;
        }
        try{
            startForeground(NOTIFICATION_ID, notification());
            if(frame==null) addOverlay();
            else if(ACTION_REFRESH.equals(action)) render();
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
        params.x=Math.max(0,Math.min(screenWidth()-dp(64),collapsedX));
        params.y=Math.max(dp(40),Math.min(screenHeight()-dp(180),collapsedY));
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
            params.x=Math.max(0,Math.min(screenWidth()-dp(64),collapsedX));
            params.y=collapsedY;
        }
        wm.updateViewLayout(frame,params);
    }
    private void render(){
        if(frame==null)return;
        handler.removeCallbacks(autoCollapse);
        params.alpha=Prefs.integer(this,"opacity",92)/100f;
        frame.removeAllViews();
        if(expanded) showExpanded();
        else showCollapsed();
        wm.updateViewLayout(frame,params);
        frame.setAlpha(0.86f);frame.setScaleX(0.96f);frame.setScaleY(0.96f);
        frame.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(180L).start();
        scheduleCollapse();
    }
    private void showCollapsed(){
        LinearLayout tab=Ui.column(this);
        tab.setGravity(Gravity.CENTER);
        int style=Prefs.integer(this,"style",1);
        int bg=style==0?0xFF101724:style==1?0xED1B2C3E:0xFF090E1A;
        tab.setBackground(Ui.rounded(bg,s(20),style==2?a():0xFF41617C,dp(style==2?2:1)));
        TextView icon=Ui.text(this,"◖",26,a(),true);
        // O ícone é dimensionado junto com o botão.
        icon.setTextSize(26*scale());
        icon.setGravity(Gravity.CENTER);
        tab.addView(icon,new LinearLayout.LayoutParams(s(60),s(61)));
        TextView bar=Ui.text(this,"•••",11,a(),true);
        bar.setGravity(Gravity.CENTER);tab.addView(bar,new LinearLayout.LayoutParams(s(60),s(16)));
        frame.addView(tab);
        tab.setOnTouchListener(this::dragOrOpen);
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
                    params.x=Math.max(0,Math.min(screenWidth()-dp(58),initialX+(int)dx));
                    params.y=Math.max(dp(24),Math.min(screenHeight()-dp(140),initialY+(int)dy));
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
        int background=style==0?0xFF101723:style==1?0xF0223041:0xFF090F1B;
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
                    percentage.setText((int)Math.round(100.0*val/max)+"%");
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
        if(frame!=null) {
            try{wm.removeView(frame);}catch(Exception ignored){}
            frame=null;
        }
        Prefs.put(this,"enabled",false);
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent i) {return null;}
}
