package br.com.nivo.volume;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manually initiated, non-silent self-update from the project's own GitHub Releases.
 *
 * Flow: check manifest -> request consent -> download into private cache ->
 * SHA-256 verification -> package/version/signature verification -> Android
 * package installer with explicit user confirmation.
 *
 * Updating an installed package requires the SAME app signing certificate.
 * This class intentionally rejects a differently signed package rather than
 * recommending a destructive uninstall.
 */
final class UpdateManager {
    private static final String MANIFEST_URL =
        "https://raw.githubusercontent.com/helbermoura2/cena-downloads/main/volume-assistido/update.json";
    private static final String TRUSTED_DOWNLOAD_PREFIX =
        "https://github.com/helbermoura2/cena-downloads/releases/download/";
    private static final long MAX_APK_SIZE=100L*1024*1024;
    private static final int SIGNATURE_FLAGS_NEW=PackageManager.GET_SIGNING_CERTIFICATES;
    private static final int SIGNATURE_FLAGS_OLD=PackageManager.GET_SIGNATURES;

    private final Activity activity;
    private final AtomicBoolean busy=new AtomicBoolean(false);
    private AlertDialog progressDialog;
    private boolean pendingInstallPermission;
    private boolean readyToInstall;

    UpdateManager(Activity activity){this.activity=activity;}

    private void ui(Runnable action){
        activity.runOnUiThread(()->{
            if(!activity.isFinishing() && !activity.isDestroyed())action.run();
        });
    }

    private static HttpURLConnection connect(String address) throws IOException{
        URL url=new URL(address);
        if(!"https".equalsIgnoreCase(url.getProtocol()))
            throw new IOException("URL não segura");
        HttpURLConnection connection=(HttpURLConnection)url.openConnection();
        connection.setConnectTimeout(12000);
        connection.setReadTimeout(18000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent","NIVO-Volume-Updater/1.0");
        if(connection.getResponseCode()!=200){
            int code=connection.getResponseCode();
            connection.disconnect();
            throw new IOException("Servidor respondeu HTTP "+code);
        }
        return connection;
    }

    private static String readManifest() throws Exception{
        HttpURLConnection connection=connect(MANIFEST_URL);
        try(InputStream stream=connection.getInputStream();
            BufferedReader reader=new BufferedReader(new InputStreamReader(stream,"UTF-8"))){
            StringBuilder builder=new StringBuilder();
            String line;
            while((line=reader.readLine())!=null){
                builder.append(line);
                if(builder.length()>32000)throw new IOException("Manifesto muito grande");
            }
            return builder.toString();
        }finally{connection.disconnect();}
    }

    private long currentVersion(){
        try{
            PackageInfo app=activity.getPackageManager().getPackageInfo(activity.getPackageName(),0);
            if(Build.VERSION.SDK_INT>=28)return app.getLongVersionCode();
            return app.versionCode;
        }catch(Exception ignored){return 0;}
    }

    void checkForUpdates(){
        if(!busy.compareAndSet(false,true))return;
        new Thread(()->{
            try{
                JSONObject json=new JSONObject(readManifest());
                long code=json.getLong("versionCode");
                String version=json.optString("versionName","nova");
                String notes=json.optString("notes","");
                String url=json.optString("apkUrl","");
                String hash=json.optString("sha256","").toLowerCase(Locale.ROOT);
                if(code<=currentVersion()){
                    ui(()->notice("Seu NIVO está atualizado",
                        "Você já está usando a versão mais recente publicada."));
                    return;
                }
                if(!url.startsWith(TRUSTED_DOWNLOAD_PREFIX)||!url.endsWith(".apk"))
                    throw new IOException("A nova versão ainda não possui um APK oficial publicado.");
                if(!hash.matches("[a-f0-9]{64}"))
                    throw new IOException("A nova versão não tem hash SHA-256 válido. Download cancelado.");
                ui(()->new AlertDialog.Builder(activity)
                    .setTitle("Atualização " + version)
                    .setMessage(notes + "\n\nBaixar o APK dentro do NIVO e abrir a instalação do Android? Seus dados serão preservados somente se a assinatura for compatível.")
                    .setNegativeButton("Agora não",null)
                    .setPositiveButton("Atualizar",(d,w)->download(url,hash,code))
                    .show());
            }catch(Exception e){
                ui(()->notice("Não foi possível verificar",e.getMessage()));
            }finally{busy.set(false);}
        },"nivo-update-check").start();
    }

    private void download(String url,String expectedHash,long expectedVersion){
        if(!busy.compareAndSet(false,true))return;
        ProgressBar bar=new ProgressBar(activity);
        bar.setIndeterminate(true);
        progressDialog=new AlertDialog.Builder(activity)
            .setTitle("Baixando atualização")
            .setMessage("Baixando e verificando o APK de forma segura…")
            .setView(bar)
            .setCancelable(false)
            .create();
        progressDialog.show();
        new Thread(()->{
            File folder=new File(activity.getCacheDir(),"nivo-updates");
            File part=new File(folder,"nivo-update-partial.apk");
            File dest=UpdateApkProvider.apkFile(activity);
            HttpURLConnection connection=null;
            try{
                if(!folder.isDirectory()&&!folder.mkdirs())
                    throw new IOException("Não foi possível preparar a pasta de atualização");
                // Strictly use the known official Github release URL.
                connection=connect(url);
                long declared=connection.getContentLengthLong();
                if(declared>MAX_APK_SIZE)
                    throw new IOException("APK maior que o limite permitido");
                MessageDigest md=MessageDigest.getInstance("SHA-256");
                long total=0;
                try(InputStream input=connection.getInputStream();
                    FileOutputStream output=new FileOutputStream(part,false)){
                    byte[] buffer=new byte[16*1024];
                    int bytes;
                    while((bytes=input.read(buffer))!=-1){
                        total+=bytes;
                        if(total>MAX_APK_SIZE)
                            throw new IOException("APK maior que o limite permitido");
                        output.write(buffer,0,bytes);
                        md.update(buffer,0,bytes);
                    }
                    output.flush();
                    output.getFD().sync();
                }
                if(total<=0)throw new IOException("Download vazio");
                StringBuilder hex=new StringBuilder(64);
                for(byte b:md.digest()) hex.append(String.format(Locale.ROOT,"%02x",b & 255));
                if(!expectedHash.equals(hex.toString()))
                    throw new SecurityException("O SHA-256 do APK não corresponde ao publicado. Instalação bloqueada.");
                verifyArchive(part,expectedVersion);
                if(dest.exists()&&!dest.delete())
                    throw new IOException("Não foi possível substituir o APK temporário");
                if(!part.renameTo(dest))
                    throw new IOException("Não foi possível finalizar o download");
                readyToInstall=true;
                ui(()->{
                    dismissProgress();
                    new AlertDialog.Builder(activity)
                        .setTitle("Download concluído")
                        .setMessage("O APK foi verificado. Agora o Android solicitará sua confirmação para atualizar o aplicativo.")
                        .setNegativeButton("Depois",null)
                        .setPositiveButton("Instalar", (d,w)->install())
                        .show();
                });
            }catch(Exception e){
                part.delete();
                readyToInstall=false;
                ui(()->{
                    dismissProgress();
                    notice("Atualização não instalada",e.getMessage());
                });
            }finally{
                if(connection!=null)connection.disconnect();
                busy.set(false);
            }
        },"nivo-apk-download").start();
    }

    private void verifyArchive(File file,long expectedVersion) throws Exception{
        PackageManager pm=activity.getPackageManager();
        int flags=Build.VERSION.SDK_INT>=28?SIGNATURE_FLAGS_NEW:SIGNATURE_FLAGS_OLD;
        PackageInfo installed=pm.getPackageInfo(activity.getPackageName(),flags);
        PackageInfo incoming=pm.getPackageArchiveInfo(file.getAbsolutePath(),flags);
        if(incoming==null || !activity.getPackageName().equals(incoming.packageName))
            throw new SecurityException("O pacote baixado não é o aplicativo NIVO.");
        long newVersion=Build.VERSION.SDK_INT>=28?incoming.getLongVersionCode():incoming.versionCode;
        if(newVersion!=expectedVersion || newVersion<=currentVersion())
            throw new SecurityException("A versão do APK não corresponde à atualização anunciada.");
        Signature[] current;
        Signature[] proposed;
        if(Build.VERSION.SDK_INT>=28){
            if(installed.signingInfo==null || incoming.signingInfo==null)
                throw new SecurityException("Não foi possível validar a assinatura do APK.");
            current=installed.signingInfo.getApkContentsSigners();
            proposed=incoming.signingInfo.getApkContentsSigners();
        }else{
            current=installed.signatures;
            proposed=incoming.signatures;
        }
        if(current==null || proposed==null || current.length==0 || !Arrays.equals(current,proposed)){
            throw new SecurityException(
                "A nova versão foi assinada com uma chave diferente. O Android não pode atualizar por cima da versão instalada. O APK foi bloqueado para preservar seus dados.");
        }
    }

    private void install(){
        File apk=UpdateApkProvider.apkFile(activity);
        if(!readyToInstall || !apk.isFile()){
            notice("Arquivo indisponível","Use Atualizar aplicativo para baixar novamente.");
            return;
        }
        if(Build.VERSION.SDK_INT>=26 && !activity.getPackageManager().canRequestPackageInstalls()){
            pendingInstallPermission=true;
            new AlertDialog.Builder(activity)
                .setTitle("Permissão de instalação")
                .setMessage("O Android precisa permitir instalações iniciadas pelo NIVO. Isso é solicitado uma única vez. Você sempre confirmará cada atualização.")
                .setNegativeButton("Cancelar",(d,w)->pendingInstallPermission=false)
                .setPositiveButton("Abrir configurações",(d,w)->{
                    Intent intent=new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:"+activity.getPackageName()));
                    try{activity.startActivity(intent);}
                    catch(Exception e){pendingInstallPermission=false;notice("Não foi possível abrir",e.getMessage());}
                }).show();
            return;
        }
        try{
            pendingInstallPermission=false;
            Uri uri=Uri.parse("content://"+activity.getPackageName()+".updatefiles/"+UpdateApkProvider.FILENAME);
            Intent intent=new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri,UpdateApkProvider.MIME);
            intent.setClipData(ClipData.newRawUri("NIVO Update",uri));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(intent);
        }catch(Exception e){
            notice("Instalador indisponível","Não foi possível abrir a instalação do Android: "+e.getMessage());
        }
    }

    void onResume(){
        if(!pendingInstallPermission || !readyToInstall)return;
        if(Build.VERSION.SDK_INT<26 || activity.getPackageManager().canRequestPackageInstalls()){
            pendingInstallPermission=false;
            install();
        }
    }

    private void dismissProgress(){
        if(progressDialog!=null){
            progressDialog.dismiss();
            progressDialog=null;
        }
    }

    private void notice(String title,String details){
        new AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(details==null?"Tente novamente.":details)
            .setPositiveButton("Entendi",null)
            .show();
    }
}
