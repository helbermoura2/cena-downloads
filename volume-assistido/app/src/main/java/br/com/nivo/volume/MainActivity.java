package br.com.nivo.volume;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    private ScrollView scroll;
    private boolean hasShownPermissionHint;
    private final int[] cards = {0xFF111824, 0xFF141E2D};
    private int a() { return Prefs.accent(this); }
    private int d(float n) { return Ui.dp(this,n); }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Prefs.BG);
        getWindow().setNavigationBarColor(Prefs.BG);
        rebuild(0);
    }
    @Override protected void onResume() {
        super.onResume();
        if (scroll != null && hasShownPermissionHint) rebuild(scroll.getScrollY());
        hasShownPermissionHint = true;
    }

    private LinearLayout card(int padding) {
        LinearLayout c=Ui.column(this);
        c.setPadding(d(padding),d(padding),d(padding),d(padding));
        c.setBackground(Ui.rounded(Prefs.SURFACE,d(20),Prefs.OUTLINE,d(1)));
        return c;
    }
    private LinearLayout.LayoutParams lp(int width, int height) {
        return new LinearLayout.LayoutParams(width<0?width:d(width),height<0?height:d(height));
    }
    private void h(LinearLayout l, String text) {
        TextView t=Ui.text(this,text,20,Prefs.TEXT,true);
        LinearLayout.LayoutParams p=lp(-1,-2);p.topMargin=d(22);p.bottomMargin=d(12);
        l.addView(t,p);
    }
    private void sectionLabel(LinearLayout l, String title, String detail) {
        l.addView(Ui.text(this,title,16,Prefs.TEXT,true),lp(-1,-2));
        if(detail!=null){TextView s=Ui.text(this,detail,12,Prefs.MUTED,false);
            LinearLayout.LayoutParams sp=lp(-1,-2);sp.topMargin=d(5);l.addView(s,sp);}
    }
    private void refreshService() {
        if (!Prefs.flag(this,"enabled",false)) return;
        Intent i=new Intent(this,FloatingService.class);
        i.setAction(FloatingService.ACTION_REFRESH);
        try { startService(i); } catch (Exception ignored) { }
    }
    private void rebuild(int saveY) {
        scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Prefs.BG);
        scroll.setClipToPadding(false);
        scroll.setFitsSystemWindows(true);
        LinearLayout root=Ui.column(this);
        root.setPadding(d(18),d(20),d(18),d(44));
        scroll.addView(root);
        setContentView(scroll);

        LinearLayout brand=Ui.row(this);
        TextView glyph=Ui.text(this,"◖",26,a(),true);
        glyph.setGravity(Gravity.CENTER);glyph.setBackground(Ui.rounded(0xFF1B2A36,d(13),0,0));
        brand.addView(glyph,lp(44,44));
        LinearLayout names=Ui.column(this); names.setPadding(d(12),0,0,0);
        TextView logo=Ui.text(this,"N I V O",17,Prefs.TEXT,true);logo.setLetterSpacing(.14f);
        names.addView(logo);names.addView(Ui.text(this,"CONTROLE DE VOLUME",10,Prefs.MUTED,true));
        brand.addView(names,new LinearLayout.LayoutParams(0,-2,1));
        TextView tag=Ui.text(this,"v0.1",11,a(),true);
        tag.setPadding(d(12),d(7),d(12),d(7));tag.setBackground(Ui.rounded(0xFF182D3B,d(40),0,0));
        brand.addView(tag);
        root.addView(brand);
        root.addView(Ui.gap(this,29));

        TextView eyebrow=Ui.text(this,"MAIS CONFORTO. MENOS BOTÕES.",11,a(),true);
        eyebrow.setLetterSpacing(.10f);root.addView(eyebrow);
        root.addView(Ui.gap(this,10));
        TextView hero=Ui.text(this,"O volume na\nponta do dedo.",32,Prefs.TEXT,true);
        hero.setLineSpacing(d(2),1f);root.addView(hero);
        root.addView(Ui.gap(this,11));
        TextView desc=Ui.text(this,"Controle o som de qualquer tela com uma interface leve, fluida e feita para você.",14,Prefs.MUTED,false);
        desc.setLineSpacing(d(3),1f);root.addView(desc);
        root.addView(Ui.gap(this,20));
        Ui.Demo demo=new Ui.Demo(this);
        root.addView(demo,lp(-1,282));

        h(root,"Começar");
        LinearLayout start=card(17);
        LinearLayout swRow=Ui.row(this);
        LinearLayout left=Ui.column(this);
        left.addView(Ui.text(this,"Botão flutuante",16,Prefs.TEXT,true));
        left.addView(Ui.text(this,"Aparece por cima de outros apps",12,Prefs.MUTED,false));
        swRow.addView(left,new LinearLayout.LayoutParams(0,-2,1));
        Switch enabled=new Switch(this); enabled.setThumbTintList(ColorStateList.valueOf(a()));
        enabled.setChecked(Prefs.flag(this,"enabled",false));
        enabled.setOnCheckedChangeListener((button, checked) -> toggle(checked,button));
        swRow.addView(enabled);start.addView(swRow);
        start.addView(Ui.gap(this,14));
        View line=new View(this);line.setBackgroundColor(0xFF263244);start.addView(line,lp(-1,1));
        start.addView(Ui.gap(this,14));
        LinearLayout permRow=Ui.row(this);
        LinearLayout permText=Ui.column(this);
        permText.addView(Ui.text(this,"Permissão de sobreposição",14,Prefs.TEXT,true));
        boolean permitted=Settings.canDrawOverlays(this);
        permText.addView(Ui.text(this,permitted?"Permitido • pronto para uso":"Toque para liberar no Android",12,permitted?0xFF75E7B6:0xFFFFBF7B,false));
        permRow.addView(permText,new LinearLayout.LayoutParams(0,-2,1));
        permRow.addView(Ui.text(this,"›",26,a(),true));
        permRow.setOnClickListener(v->openPermission());
        start.addView(permRow);root.addView(start);

        h(root,"Escolha seu estilo");
        LinearLayout styles=Ui.row(this);
        String[] labels={"Minimal","Glass","Neon"};
        String[] marks={"●","◌","✦"};
        for(int n=0;n<3;n++){
            final int style=n;
            boolean selected=Prefs.integer(this,"style",1)==n;
            LinearLayout item=Ui.column(this);
            item.setPadding(d(9),d(14),d(9),d(12));item.setGravity(Gravity.CENTER_HORIZONTAL);
            int bg= selected?0xFF1C2F41:Prefs.SURFACE;
            item.setBackground(Ui.rounded(bg,d(18),selected?a():Prefs.OUTLINE,d(1)));
            TextView mark=Ui.text(this,marks[n],29,selected?a():0xFF8FA6BE,true);mark.setGravity(Gravity.CENTER);
            item.addView(mark,lp(-1,40));
            item.addView(Ui.gap(this,8));
            TextView name=Ui.text(this,labels[n],12,selected?Prefs.TEXT:Prefs.MUTED,true);
            name.setGravity(Gravity.CENTER);item.addView(name);
            LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(0,d(100),1);
            if(n>0)ip.leftMargin=d(9);styles.addView(item,ip);
            item.setOnClickListener(v->{Prefs.put(this,"style",style);refreshService();rebuild(scroll.getScrollY());});
        }
        root.addView(styles);

        h(root,"Sua paleta");
        LinearLayout palette=card(17);
        sectionLabel(palette,"Cor de destaque","Escolha a luz que combina com você.");
        palette.addView(Ui.gap(this,15));
        LinearLayout dots=Ui.row(this);dots.setGravity(Gravity.CENTER_VERTICAL);
        int chosen=Prefs.integer(this,"accent",0);
        for(int n=0;n<Prefs.COLORS.length;n++){
            final int colorIndex=n; final boolean selected=chosen==n;
            TextView dot=Ui.text(this,selected?"✓":"",17,Prefs.BG,true); dot.setGravity(Gravity.CENTER);
            dot.setBackground(Ui.rounded(Prefs.COLORS[n],d(40),selected?0xFFF5FAFF:0,d(selected?2:0)));
            LinearLayout.LayoutParams ip=lp(42,42); if(n>0)ip.leftMargin=d(12);dots.addView(dot,ip);
            dot.setOnClickListener(v->{Prefs.put(this,"accent",colorIndex);refreshService();rebuild(scroll.getScrollY());});
        }
        palette.addView(dots);root.addView(palette);

        h(root,"Ajuste os detalhes");
        LinearLayout settings=card(17);
        addSlider(settings,"Tamanho do controle","size",70,150,100,"%",v->refreshService());
        settings.addView(Ui.gap(this,12));
        addSlider(settings,"Opacidade","opacity",40,100,92,"%",v->refreshService());
        settings.addView(Ui.gap(this,10));
        addSwitch(settings,"Recolher automaticamente","O controle fica discreto quando não usado","auto",true);
        settings.addView(Ui.gap(this,12));
        addSlider(settings,"Tempo para recolher","collapse",2,20,6," s",v->refreshService());
        settings.addView(Ui.gap(this,9));
        addSwitch(settings,"Iniciar com o celular","Pode depender das restrições de bateria do sistema","boot",false);
        root.addView(settings);

        h(root,"Aplicativo");
        LinearLayout footer=card(17);
        LinearLayout update=Ui.row(this);
        LinearLayout updateL=Ui.column(this);
        updateL.addView(Ui.text(this,"Verificar atualização",15,Prefs.TEXT,true));
        updateL.addView(Ui.text(this,"Versões publicadas no seu GitHub",12,Prefs.MUTED,false));
        update.addView(updateL,new LinearLayout.LayoutParams(0,-2,1));
        update.addView(Ui.text(this,"↗",23,a(),true));
        update.setOnClickListener(v->checkUpdate());footer.addView(update);
        footer.addView(Ui.gap(this,12));
        footer.addView(Ui.text(this,"NIVO Volume • Feito para Android • Sem anúncios",11,Prefs.MUTED,false));
        root.addView(footer);
        scroll.post(()->scroll.scrollTo(0,saveY));
    }

    private void addSwitch(LinearLayout parent,String title,String sub,String key,boolean fallback){
        LinearLayout row=Ui.row(this);
        LinearLayout labels=Ui.column(this);
        labels.addView(Ui.text(this,title,14,Prefs.TEXT,true));
        TextView description=Ui.text(this,sub,11,Prefs.MUTED,false);
        LinearLayout.LayoutParams p=lp(-1,-2);p.topMargin=d(4);labels.addView(description,p);
        row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
        Switch sw=new Switch(this);sw.setThumbTintList(ColorStateList.valueOf(a()));
        sw.setChecked(Prefs.flag(this,key,fallback));sw.setOnCheckedChangeListener((b,checked)->{
            Prefs.put(this,key,checked);refreshService();
        });row.addView(sw);parent.addView(row);
    }
    interface IntChange {void accept(int value);}
    private void addSlider(LinearLayout parent,String title,String key,int min,int max,int fallback,String unit,IntChange listener){
        LinearLayout top=Ui.row(this);
        top.addView(Ui.text(this,title,14,Prefs.TEXT,true),new LinearLayout.LayoutParams(0,-2,1));
        int current=Prefs.integer(this,key,fallback);
        TextView amount=Ui.text(this,current+unit,13,a(),true);top.addView(amount);parent.addView(top);
        SeekBar bar=new SeekBar(this);bar.setMax(max-min);bar.setProgress(Math.max(0,Math.min(max-min,current-min)));
        bar.setProgressTintList(ColorStateList.valueOf(a()));
        bar.setThumbTintList(ColorStateList.valueOf(a()));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(0xFF314054));
        LinearLayout.LayoutParams bp=lp(-1,34);bp.topMargin=d(3);parent.addView(bar,bp);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar s,int p,boolean fromUser){
                if(fromUser){int val=min+p;Prefs.put(MainActivity.this,key,val);amount.setText(val+unit);listener.accept(val);}
            }
            @Override public void onStartTrackingTouch(SeekBar s){}
            @Override public void onStopTrackingTouch(SeekBar s){}
        });
    }

    private void toggle(boolean checked,CompoundButton button) {
        if(checked && !Settings.canDrawOverlays(this)){
            button.setChecked(false);
            new AlertDialog.Builder(this).setTitle("Permitir botão flutuante")
                .setMessage("Para mostrar o controle em outros aplicativos, autorize 'Aparecer sobre outros apps' para NIVO.")
                .setNegativeButton("Agora não",null)
                .setPositiveButton("Abrir configurações",(d,w)->openPermission()).show();
            return;
        }
        if(checked){
            try {
                if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},42);
                Prefs.put(this,"enabled",true);
                Intent i=new Intent(this,FloatingService.class);
                if(Build.VERSION.SDK_INT>=26) startForegroundService(i); else startService(i);
            }catch(Exception ex){
                Prefs.put(this,"enabled",false);button.setChecked(false);
                new AlertDialog.Builder(this).setTitle("Não foi possível iniciar").setMessage(ex.getMessage()).setPositiveButton("OK",null).show();
            }
        }else{
            Prefs.put(this,"enabled",false);stopService(new Intent(this,FloatingService.class));
        }
    }
    private void openPermission(){
        Intent i=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:"+getPackageName()));
        try{startActivity(i);}catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}
    }

    private void checkUpdate(){
        new Thread(()->{
            HttpURLConnection conn=null;
            try {
                URL url=new URL("https://raw.githubusercontent.com/helbermoura2/cena-downloads/main/volume-assistido/update.json");
                conn=(HttpURLConnection)url.openConnection();
                conn.setConnectTimeout(8500);conn.setReadTimeout(8500);
                conn.setRequestProperty("User-Agent","NIVO-Volume/0.1");
                if(conn.getResponseCode()!=200) throw new Exception("HTTP "+conn.getResponseCode());
                StringBuilder b=new StringBuilder();
                try(BufferedReader reader=new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"))){
                    String line;while((line=reader.readLine())!=null && b.length()<30000)b.append(line);
                }
                JSONObject json=new JSONObject(b.toString());
                int newer=json.optInt("versionCode",1);
                String version=json.optString("versionName","?");
                String apk=json.optString("apkUrl","");
                runOnUiThread(()->{
                    if(newer>1 && apk.startsWith("https://github.com/helbermoura2/")){
                        new AlertDialog.Builder(this).setTitle("Nova versão "+version)
                            .setMessage("Abrir a página de download? A instalação exigirá sua confirmação no Android.")
                            .setNegativeButton("Cancelar",null)
                            .setPositiveButton("Baixar",(d,w)->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(apk)))).show();
                    }else{
                        new AlertDialog.Builder(this).setTitle("Nenhum APK novo")
                            .setMessage("Você está com a versão inicial ou ainda não há um APK mais recente publicado no GitHub.")
                            .setPositiveButton("Entendi",null).show();
                    }
                });
            }catch(Exception e){runOnUiThread(()->new AlertDialog.Builder(this)
                .setTitle("Não foi possível verificar")
                .setMessage("Confira sua conexão e tente novamente.\n"+e.getClass().getSimpleName())
                .setPositiveButton("OK",null).show());
            }finally{if(conn!=null)conn.disconnect();}
        }).start();
    }
}
