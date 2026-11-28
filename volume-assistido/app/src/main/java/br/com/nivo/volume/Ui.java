package br.com.nivo.volume;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

final class Ui {
    static int dp(Context c, float n) { return (int)(n * c.getResources().getDisplayMetrics().density + 0.5f); }

    static GradientDrawable rounded(int color, float radius, int borderColor, int border) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        if (border > 0) d.setStroke(border, borderColor);
        return d;
    }

    static GradientDrawable gradient(int[] colors, float radius, int stroke) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colors);
        d.setCornerRadius(radius);
        if (stroke != 0) d.setStroke(1, stroke);
        return d;
    }

    static TextView text(Context c, String value, int size, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setFontFeatureSettings("kern");
        if (bold) t.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
        else t.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL));
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    static LinearLayout column(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }
    static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }
    static View gap(Context c, int height) {
        View v = new View(c);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(c, height)));
        return v;
    }

    static class Demo extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF r = new RectF();
        private final Context context;
        Demo(Context c) { super(c); context=c; setLayerType(View.LAYER_TYPE_SOFTWARE,null); }
        private void rect(Canvas c, float l,float t,float rr,float b,float rad,int color) {
            p.setShader(null); p.setStyle(Paint.Style.FILL); p.setColor(color); r.set(l,t,rr,b); c.drawRoundRect(r,rad,rad,p);
        }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w=getWidth(), h=getHeight(), scale=getResources().getDisplayMetrics().density;
            int accent = Prefs.accent(context);
            int style = Prefs.integer(context,"style",1);
            int theme = ThemeStyles.highlight(style,accent);
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(0,0,w,h,new int[]{0xFF17243B,0xFF101927,0xFF0B1523},null,Shader.TileMode.CLAMP));
            r.set(0,0,w,h); c.drawRoundRect(r,dp(context,22),dp(context,22),p); p.setShader(null);
            p.setColor(theme); p.setAlpha(28);
            c.drawCircle(w*0.71f,h*0.41f,dp(context,115),p);
            p.setAlpha(13); c.drawCircle(w*0.71f,h*0.41f,dp(context,155),p); p.setAlpha(255);
            rect(c,dp(context,22),dp(context,18),w-dp(context,22),dp(context,47),dp(context,12),0x1CFFFFFF);
            p.setTypeface(android.graphics.Typeface.create("sans-serif-medium",0));
            p.setTextSize(dp(context,11));p.setColor(0xFFABB9CF);
            c.drawText("PRÉVIA DO SEU CONTROLE",dp(context,36),dp(context,37),p);
            boolean separate=Prefs.flag(context,"separate_buttons",true);
            boolean slider=Prefs.flag(context,"quick_slider",true);
            boolean right=Prefs.flag(context,"quick_slider_right",false);
            float ratio=Math.min(1f,(h-dp(context,96))/dp(context,Prefs.integer(context,"button_size",52)*2+Prefs.integer(context,"button_gap",32)+10));
            float btn=dp(context,Prefs.integer(context,"button_size",52))*ratio;
            float gap=dp(context,Prefs.integer(context,"button_gap",32))*ratio;
            float railWidth=dp(context,32),space=dp(context,8);
            float width=btn+(slider?railWidth+space:0);
            float ph=btn*2+gap,y=dp(context,54),x=(w-width)/2;
            float buttonsX=slider&&!right?x+railWidth+space:x;
            if(!separate)rect(c,buttonsX-space,y-space,buttonsX+btn+space,y+ph+space,dp(context,20),ThemeStyles.tileBg(style));
            if(slider){
                float railX=right?buttonsX+btn+space:x;
                rect(c,railX,y,railX+railWidth,y+ph,dp(context,15),0xFF101B29);
                float railCenter=railX+railWidth/2;
                float top=y+dp(context,12),bottom=y+ph-dp(context,12),thumb=bottom-(bottom-top)*.63f;
                rect(c,railCenter-dp(context,2),top,railCenter+dp(context,2),bottom,dp(context,2),0xFF455368);
                rect(c,railCenter-dp(context,2),thumb,railCenter+dp(context,2),bottom,dp(context,2),theme);
                p.setColor(theme);c.drawCircle(railCenter,thumb,dp(context,5),p);
            }
            boolean neon=style==2||style==4||style==8||style>=12;
            float minusY=y+btn+gap;
            rect(c,buttonsX,y,buttonsX+btn,y+btn,dp(context,14),neon?ThemeStyles.tileBg(style):theme);
            rect(c,buttonsX,minusY,buttonsX+btn,minusY+btn,dp(context,14),ThemeStyles.tileBg(style));
            if(neon){
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(context,1.5f));p.setColor(theme);
                for(float keyY:new float[]{y,minusY}){
                    r.set(buttonsX,keyY,buttonsX+btn,keyY+btn);
                    c.drawRoundRect(r,dp(context,14),dp(context,14),p);
                }
                p.setStyle(Paint.Style.FILL);
            }
            p.setTypeface(android.graphics.Typeface.create("sans-serif-medium",1));p.setTextSize(dp(context,24));
            p.setTextAlign(Paint.Align.CENTER);p.setColor(neon?theme:0xFF08101A);
            c.drawText("+",buttonsX+btn/2,y+btn/2+dp(context,8),p);
            p.setColor(neon?theme:0xFFD8F7FF);
            c.drawText("−",buttonsX+btn/2,minusY+btn/2+dp(context,8),p);
            p.setColor(theme);p.setTextSize(dp(context,12));
            c.drawText("63%",buttonsX+btn/2,y+btn+gap/2+dp(context,4),p);
            p.setTextAlign(Paint.Align.LEFT);
            p.setColor(0xFFB5C1D2);p.setTextSize(dp(context,10));p.setTypeface(android.graphics.Typeface.create("sans-serif",0));
            c.drawText("ARRASTE • AJUSTE • RECOLHA",dp(context,30),h-dp(context,20),p);
        }
    }
}
