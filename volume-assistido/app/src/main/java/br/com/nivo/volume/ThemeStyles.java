package br.com.nivo.volume;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Temas nativos do dock flutuante. Gradientes e formas são desenhados com
 * Android GradientDrawable: sem WebView, bibliotecas extras ou rastreamento.
 * O estilo "Liquid Glass" simula vidro translúcido; não lê pixels dos apps de fundo.
 */
final class ThemeStyles {
    static final String[] NAMES = {
        "Minimal", "Glass", "Neon", "Aurora", "RGB ∞",
        "Liquid Glass", "Titanium", "Carbon", "Synthwave",
        "Pearl", "Pixel", "Candy"
    };
    static final String[] SYMBOLS = {
        "●","◌","✦","◈","∞","◉","⬡","▨","☾","◍","▣","♥"
    };
    private ThemeStyles(){}

    static int highlight(int style, int accent) {
        switch(style) {
            case 3: return 0xFFBD9FFF;
            case 5: return 0xFFB6F1FF;
            case 6: return 0xFFCCD7E4;
            case 7: return 0xFFB8FA77;
            case 8: return 0xFFFF5BCB;
            case 9: return 0xFF7789A4;
            case 10: return 0xFF9AFF74;
            case 11: return 0xFFFF91B9;
            default: return accent;
        }
    }
    static int tileBg(int style){
        switch(style){
            case 5:return 0xFF30465D;
            case 6:return 0xFF3D4653;
            case 7:return 0xFF182021;
            case 8:return 0xFF3B204D;
            case 9:return 0xFFE8EFF5;
            case 10:return 0xFF14291D;
            case 11:return 0xFF55364C;
            default:return 0xFF152334;
        }
    }
    private static int d(Context c,float dp){return Ui.dp(c,dp);}
    private static GradientDrawable grad(Context c,int[] colors,float corner,int strokeColor,int strokeDp){
        GradientDrawable g=Ui.gradient(colors,d(c,corner),0);
        if(strokeDp>0)g.setStroke(d(c,strokeDp),strokeColor);
        return g;
    }
    private static GradientDrawable solid(Context c,int color,float corner,int strokeColor,int strokeDp){
        return Ui.rounded(color,d(c,corner),strokeColor,d(c,strokeDp));
    }
    static void applyDock(Context c,int style,int accent,float scale,
                          LinearLayout dock,TextView plus,TextView minus,TextView mid){
        int buttonSize=Math.max(38,Math.min(92,Prefs.integer(c,"button_size",52)));
        int effective=(int)(buttonSize*scale);
        float pill=buttonSize*.40f*scale;
        float rounded=buttonSize*.57f*scale;
        int ink=0xFF07131D;
        plus.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));
        minus.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));
        dock.setElevation(0);

        switch(style){
            case 0: // Minimal: geométrico, sem efeitos ou brilho
                dock.setBackground(solid(c,0xFF141920,12*scale,0xFF384655,1));
                plus.setBackground(solid(c,accent,11*scale,0,0));
                minus.setBackground(solid(c,0xFF222D39,11*scale,0,0));
                plus.setTextColor(ink);minus.setTextColor(0xFFF2F5F9);mid.setTextColor(accent);
                break;
            case 1: // Glass: painel escuro, borda suave
                dock.setBackground(grad(c,new int[]{0xEE30475A,0xEF172434},23*scale,0xFF617A92,1));
                plus.setBackground(solid(c,accent,pill,0,0));
                minus.setBackground(solid(c,0xFF273A50,pill,0,0));
                plus.setTextColor(ink);minus.setTextColor(0xFFEEF8FF);mid.setTextColor(accent);
                break;
            case 2: // Neon: brilho colorido ao redor do corpo
                dock.setBackground(solid(c,0xFF090E19,23*scale,accent,2));
                dock.setElevation(d(c,9*scale));
                plus.setBackground(grad(c,new int[]{accent,0xFF48A7C9},pill,0,0));
                minus.setBackground(solid(c,0xFF132231,pill,accent,1));
                plus.setTextColor(ink);minus.setTextColor(accent);mid.setTextColor(accent);
                break;
            case 3: // Aurora: roxo profundo e tons aquáticos
                dock.setBackground(grad(c,new int[]{0xFF39305E,0xFF102E43,0xFF1A2942},26*scale,0xFFB89DFF,1));
                plus.setBackground(grad(c,new int[]{0xFFBFA5FF,0xFF75D4E8},rounded,0,0));
                minus.setBackground(solid(c,0xFF2D2D51,rounded,0xFF6D7DBD,1));
                plus.setTextColor(ink);minus.setTextColor(0xFFD5C9FF);mid.setTextColor(0xFFCFB9FF);
                break;
            case 4: // RGB ∞: cores atualizadas pelo serviço, quando habilitado
                dock.setBackground(solid(c,0xFF11111D,23*scale,accent,2));
                plus.setBackground(solid(c,accent,pill,0,0));
                minus.setBackground(solid(c,0xFF273A50,pill,0,0));
                plus.setTextColor(ink);minus.setTextColor(accent);mid.setTextColor(accent);
                break;
            case 5: // Liquid Glass: vidro translúcido com superfície acetinada
                dock.setBackground(grad(c,new int[]{0xC785A5BD,0xBF394B66,0xDA172536},30*scale,0xFFE2F7FF,1));
                plus.setBackground(grad(c,new int[]{0xD9FFFFFF,0x78A7E8FB},rounded,0xC9FFFFFF,1));
                minus.setBackground(grad(c,new int[]{0xAA6E8EA9,0x7B293E50},rounded,0xA1E8F6FF,1));
                plus.setTextColor(0xFF123046);minus.setTextColor(0xFFF7FCFF);mid.setTextColor(0xFFF6FDFF);
                dock.setElevation(d(c,10*scale));
                break;
            case 6: // Titanium: metal escovado por gradientes, sem textura de imagem
                dock.setBackground(grad(c,new int[]{0xFF596472,0xFF2E3744,0xFF151C26},17*scale,0xFFB0C1D1,1));
                plus.setBackground(grad(c,new int[]{0xFFDAE5F0,0xFF879BAE,0xFF5C7287},13*scale,0xFFE8F4FC,1));
                minus.setBackground(grad(c,new int[]{0xFF586879,0xFF242F3B},13*scale,0xFF73889A,1));
                plus.setTextColor(0xFF18232D);minus.setTextColor(0xFFF3F7FC);mid.setTextColor(0xFFD6DFE7);
                break;
            case 7: // Carbon: utilitário e discreto, sem exigir imagens externas
                dock.setBackground(solid(c,0xFF141A1B,8*scale,0xFF586660,1));
                plus.setBackground(solid(c,0xFF26312D,5*scale,0xFFB8FA77,1));
                minus.setBackground(solid(c,0xFF101919,5*scale,0xFF45584B,1));
                plus.setTypeface(Typeface.create("monospace",Typeface.BOLD));
                minus.setTypeface(Typeface.create("monospace",Typeface.BOLD));
                plus.setTextColor(0xFFB8FA77);minus.setTextColor(0xFFB8FA77);mid.setTextColor(0xFFB8FA77);
                break;
            case 8: // Synthwave: neon rosa + roxo, aspecto noturno retro-futurista
                dock.setBackground(grad(c,new int[]{0xFF4A255C,0xFF171329,0xFF261844},25*scale,0xFFFF62C8,2));
                dock.setElevation(d(c,12*scale));
                plus.setBackground(grad(c,new int[]{0xFFFF60C3,0xFF9E6CFF},rounded,0,0));
                minus.setBackground(solid(c,0xFF301943,rounded,0xFFAB7CFF,1));
                plus.setTextColor(0xFF230B37);minus.setTextColor(0xFFFFA6E1);mid.setTextColor(0xFFFF7CD6);
                break;
            case 9: // Pearl: claro e macio, alto contraste em superfícies claras
                dock.setBackground(grad(c,new int[]{0xFFF6FAFF,0xFFD7E1ED},28*scale,0xFFFFFFFF,1));
                plus.setBackground(grad(c,new int[]{0xFFE6D7FE,0xFFBCCFF5},rounded,0xFFFFFFFF,1));
                minus.setBackground(grad(c,new int[]{0xFFF4F7FB,0xFFCEDAE7},rounded,0xFFFFFFFF,1));
                plus.setTextColor(0xFF273856);minus.setTextColor(0xFF425775);mid.setTextColor(0xFF425775);
                dock.setElevation(d(c,7*scale));
                break;
            case 10: // Pixel: quinas quadradas e verde terminal
                dock.setBackground(solid(c,0xFF0A1711,0,0xFF83EE70,2));
                plus.setBackground(solid(c,0xFF9AFF74,0,0,0));
                minus.setBackground(solid(c,0xFF173021,0,0xFF9AFF74,1));
                plus.setTypeface(Typeface.create("monospace",Typeface.BOLD));
                minus.setTypeface(Typeface.create("monospace",Typeface.BOLD));
                plus.setTextColor(0xFF0A1811);minus.setTextColor(0xFFB4FFA0);mid.setTextColor(0xFF9AFF74);
                break;
            case 11: // Candy: botões doces de aspecto 3D e fundo vinho
                dock.setBackground(grad(c,new int[]{0xFF5A3D54,0xFF30273E},28*scale,0xFFF6A2C6,1));
                plus.setBackground(grad(c,new int[]{0xFFFFD2C3,0xFFFF91B9,0xFFD96ACB},rounded,0xFFFFE9EE,1));
                minus.setBackground(grad(c,new int[]{0xFF715271,0xFF473251},rounded,0xFFCC8EC4,1));
                plus.setTextColor(0xFF43213E);minus.setTextColor(0xFFFFE5F5);mid.setTextColor(0xFFFFB3D9);
                dock.setElevation(d(c,7*scale));
                break;
            default: applyDock(c,1,accent,scale,dock,plus,minus,mid);
        }
    }
}
