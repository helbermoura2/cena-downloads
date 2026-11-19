package br.com.nivo.volume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        if (!Prefs.flag(context,"boot",false)) return;
        if (!Prefs.flag(context,"enabled",false)) return;
        if (!Settings.canDrawOverlays(context)) return;
        try {
            Intent service = new Intent(context,FloatingService.class);
            if (Build.VERSION.SDK_INT>=26) context.startForegroundService(service);
            else context.startService(service);
        } catch (RuntimeException blocked) {
            // Em alguns aparelhos o Android limita início de serviço no boot.
            Log.w("NivoVolume","Início automático restringido pelo Android",blocked);
        }
    }
}
