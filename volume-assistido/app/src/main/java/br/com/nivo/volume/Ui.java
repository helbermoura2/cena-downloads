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
            int btn=Prefs.integer(context,"button_size",52);
            int gap=Prefs.integer(context,"button_gap",32);
            float pw=dp(context,Math.max(92,btn+42)),ph=dp(context,Math.max(144,btn*2+gap+20));
            float x=(w-pw)/2,y=dp(context,60);
            if(y+ph>h-dp(context,36)) y=Math.max(dp(context,52),h-ph-dp(context,36));
            p.setShadowLayer(dp(context,28),0,dp(context,12),style==2||style==8?theme:0xAA000000);
            rect(c,x,y,x+pw,y+ph,dp(context,28),ThemeStyles.tileBg(style));
            p.clearShadowLayer();
            if(style>=2) {
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(context,1.5f));p.setColor(theme);r.set(x,y,x+pw,y+ph);c.drawRoundRect(r,dp(context,28),dp(context,28),p);p.setStyle(Paint.Style.FILL);
            }
            p.setColor(theme);
            c.drawCircle(x+pw*0.5f,y+dp(context,38),dp(context,23),p);
            p.setColor(0xFF08101A);p.setTypeface(android.graphics.Typeface.create("sans-serif-medium",1));p.setTextSize(dp(context,27));
            float first=y+dp(context,btn*.65f);
            c.drawText("+",x+pw*.5f-dp(context,8),first,p);
            float mid=y+dp(context,btn+gap/2f+9);
            p.setColor(theme);p.setTextSize(dp(context,12));
            c.drawText("63%",x+pw*.5f-dp(context,13),mid,p);
            p.setColor(0xFFD8F7FF);p.setTextSize(dp(context,25));
            c.drawText("−",x+pw*.5f-dp(context,9),y+dp(context,btn+gap+btn*.65f),p);
            p.setColor(0xFFB5C1D2);p.setTextSize(dp(context,10));p.setTypeface(android.graphics.Typeface.create("sans-serif",0));
            c.drawText("ARRASTE • AJUSTE • RECOLHA",dp(context,30),h-dp(context,20),p);
        }
    }
}
