package br.com.nivo.volume;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Exposes ONE already-verified APK to Android's package installer. No file
 * manager, media permission, external storage access, or dependency required.
 */
public final class UpdateApkProvider extends ContentProvider {
    static final String FILENAME = "nivo-update.apk";
    static final String MIME = "application/vnd.android.package-archive";

    static File apkFile(android.content.Context context) {
        return new File(new File(context.getCacheDir(), "nivo-updates"), FILENAME);
    }

    private void validate(Uri uri) throws FileNotFoundException {
        if (getContext() == null ||
            !uri.getAuthority().equals(getContext().getPackageName() + ".updatefiles") ||
            !uri.getPath().equals("/" + FILENAME)) {
            throw new FileNotFoundException("Unrecognized update URI");
        }
    }

    @Override public boolean onCreate() { return true; }

    @Override public String getType(Uri uri) {
        try { validate(uri); } catch (FileNotFoundException ignored) { return null; }
        return MIME;
    }

    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        validate(uri);
        if (!"r".equals(mode)) throw new FileNotFoundException("Read-only");
        File file = apkFile(getContext());
        if (!file.isFile()) throw new FileNotFoundException("No verified update");
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try { validate(uri); } catch (FileNotFoundException e) { return null; }
        File file=apkFile(getContext());
        String[] columns=(projection==null || projection.length==0)?
            new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE}:projection;
        MatrixCursor cursor=new MatrixCursor(columns);
        Object[] values=new Object[columns.length];
        for(int i=0;i<columns.length;i++) {
            if(OpenableColumns.DISPLAY_NAME.equals(columns[i])) values[i]=FILENAME;
            else if(OpenableColumns.SIZE.equals(columns[i]))values[i]=file.isFile()?file.length():0;
        }
        cursor.addRow(values);
        return cursor;
    }
    @Override public Uri insert(Uri uri,ContentValues values){throw new UnsupportedOperationException();}
    @Override public int delete(Uri uri,String where,String[] args){throw new UnsupportedOperationException();}
    @Override public int update(Uri uri,ContentValues values,String where,String[] args){throw new UnsupportedOperationException();}
}
