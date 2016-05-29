/*
* Food Scanner: A free and open Food Analyzer with superb accuracy!
*
* Food Scanner is a first-of-a-kind food analyzer offering valuable
* information about foods such as nutritional facts, allergens and
* chemicals, using ordinary smartphones.
*
* Authors: D. Stefanidis
*
* Supervisor: Demetrios Zeinalipour-Yazti
*
* URL: http://foodscanner.cs.ucy.ac.cy
* Contact: foodscanner@cs.ucy.ac.cy
*
* Copyright (c) 2016, Data Management Systems Lab (DMSL), University of Cyprus.
* All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of
* this software and associated documentation files (the "Software"), to deal in the
* Software without restriction, including without limitation the rights to use, copy,
* modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
* and to permit persons to whom the Software is furnished to do so, subject to the
* following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
* DEALINGS IN THE SOFTWARE.
*
*/

package com.ds.FoodScanner.help;

import com.ds.util.Util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class OCRLanguageInstallService extends IntentService {

    protected static final String ACTION_INSTALL_COMPLETED = "com.ds.FoodScanner.ACTION_OCR_LANGUAGE_INSTALLED";
    protected static final String ACTION_INSTALL_FAILED = "com.ds.FoodScanner.ACTION_INSTALL_FAILED";
    protected static final String EXTRA_OCR_LANGUAGE = "ocr_language";
    protected static final String EXTRA_OCR_LANGUAGE_DISPLAY = "ocr_language_display";
    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_FILE_NAME = "file_name";

    public OCRLanguageInstallService() {
        this(OCRLanguageInstallService.class.getSimpleName());
    }

    public OCRLanguageInstallService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!intent.hasExtra(DownloadManager.EXTRA_DOWNLOAD_ID)) {
            return;
        }
        final long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        if (downloadId != 0) {

            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            ParcelFileDescriptor file;
            GzipCompressorInputStream gzIn = null;
            BufferedInputStream in = null;
            FileInputStream fin = null;
            try {
                String fileName = intent.getStringExtra(EXTRA_FILE_NAME);
                String langName = extractLanguageNameFromUri(fileName);
                File tessDir = Util.getTrainingDataDir(this);
                tessDir.mkdirs();

                file = dm.openDownloadedFile(downloadId);
                fin = new FileInputStream(file.getFileDescriptor());
                in = new BufferedInputStream(fin);

                final byte[] buffer = new byte[4096 * 8];
                if (fileName.endsWith("traineddata")) {
                    copyInputStream(in, langName, tessDir, buffer);
                    return;
                }
                gzIn = new GzipCompressorInputStream(in);
                if (fileName.endsWith("traineddata.gz")) {
                    copyInputStream(gzIn, langName, tessDir, buffer);
                    return;
                }

                TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn);


                if ("ara.traineddata".equalsIgnoreCase(langName) || "hin.traineddata".equalsIgnoreCase(langName)) {
                    //extract also cube data as otherwise tesseract will crash
                    TarArchiveEntry entry = null;
                    String lang = null;
                    while ((entry = tarIn.getNextTarEntry()) != null) {
                        final String currentFileName = entry.getName().substring("tesseract-ocr/tessdata/".length());
                        File trainedData = new File(tessDir, currentFileName);
                        FileOutputStream fout = new FileOutputStream(trainedData);
                        int len;
                        while ((len = tarIn.read(buffer)) != -1) {
                            fout.write(buffer, 0, len);
                        }
                        fout.close();
                        if (entryIsNotLanguageFile(entry, langName)) {
                            lang = currentFileName.substring(0, currentFileName.length() - ".traineddata".length());
                        }

                    }
                    notifyReceivers(lang);
                    return;
                }

                TarArchiveEntry entry = tarIn.getNextTarEntry();

                while (!entryIsNotLanguageFile(entry, langName)) {
                    if (entry == null) {
                        break;
                    }
                    entry = tarIn.getNextTarEntry();
                }
                if (entry != null) {
                    final String currentLangName = entry.getName().substring("tesseract-ocr/tessdata/".length());
                    File trainedData = new File(tessDir, currentLangName);
                    if (trainedData.isDirectory()) {
                        trainedData.delete();
                    }
                    FileOutputStream fout = new FileOutputStream(trainedData);
                    int len;
                    while ((len = tarIn.read(buffer)) != -1) {
                        fout.write(buffer, 0, len);
                    }
                    fout.close();
                    String lang = currentLangName.substring(0, currentLangName.length() - ".traineddata".length());
                    notifyReceivers(lang);
                    return;
                }
                tarIn.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                String tessDir = Util.getDownloadTempDir(this);
                File targetFile = new File(tessDir, OCRLanguageActivity.DOWNLOADED_TRAINING_DATA);
                if (targetFile.exists()) {
                    targetFile.delete();
                }
                closeInputStream(gzIn);
                closeInputStream(in);
                closeInputStream(fin);
            }

        }
    }

    private void closeInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void copyInputStream(InputStream inputStream, String langName, File tessDir, byte[] buffer) throws IOException {
        File trainedData = new File(tessDir, langName);
        FileOutputStream out = new FileOutputStream(trainedData);
        int n;
        while (-1 != (n = inputStream.read(buffer))) {
            out.write(buffer, 0, n);
        }
        out.close();
        String lang = langName.substring(0, langName.length() - ".traineddata".length());
        notifyReceivers(lang);
    }

    private boolean entryIsNotLanguageFile(TarArchiveEntry entry, String langName) {
        if (entry == null) {
            return false;
        }
        if (!entry.getName().endsWith(".traineddata")) {
            return false;
        }
        if (entry.getName().endsWith("_old.traineddata")) {
            return false;
        }
        if (langName != null) {
            String entryFileName = entry.getName().substring("tesseract-ocr/tessdata/".length());
            if (entryFileName.equals(langName)) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    private String extractLanguageNameFromUri(String fileName) {
        int end = fileName.indexOf(".tar.gz");
        if (end > -1 && end > 2) {
            String sub = fileName.substring(end - 7, end);
            if (sub.startsWith("chi_")) {
                return sub + ".traineddata";
            }
            return fileName.substring(end - 3, end) + ".traineddata";
        }
        end = fileName.indexOf(".gz");
        int start = fileName.lastIndexOf("/");
        if (end > -1 && end > 2 && start >= -1) {
            return fileName.substring(start + 1, end);
        }
        if (fileName.endsWith(".traineddata")) {
            final int length = fileName.length();
            return fileName.substring(length -15, length);
        }
        return null;
    }

    private void notifyReceivers(String lang) {
        // notify language activity
        Log.i(OCRLanguageInstallService.class.getSimpleName(), "Installing " + lang);
        Intent resultIntent = new Intent(ACTION_INSTALL_COMPLETED);
        resultIntent.putExtra(EXTRA_OCR_LANGUAGE, lang);
        resultIntent.putExtra(EXTRA_STATUS, DownloadManager.STATUS_SUCCESSFUL);
        sendBroadcast(resultIntent);
    }
}
