package com.softwaredesign.novelreader.Activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exporter_library.IChapterExportHandler;
import com.example.scraper_library.INovelScraper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.softwaredesign.novelreader.Adapters.NovelAdapter;
import com.softwaredesign.novelreader.Adapters.ServerSpinnerAdapter;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.ExportHandlers.EpubExportHandler;
import com.softwaredesign.novelreader.ExportHandlers.PdfExportHandler;
import com.softwaredesign.novelreader.Global.GlobalConfig;
import com.softwaredesign.novelreader.Global.ReusableFunction;
import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.R;
import com.softwaredesign.novelreader.Scrapers.TangthuvienScraper;
import com.softwaredesign.novelreader.Scrapers.TruyenfullScraper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_READ_STORAGE = 100;
    private static final int REQUEST_WRITE_PERMISSION = 111;
    private static final int PERMISSION_REQUEST_CODE = 1;

    private static final String CHANNEL_ID = "9999";

    // URL of the website to scrape novels from
    private SearchView searchView; // SearchView for searching novels
    private RecyclerView recyclerView; // RecyclerView for displaying novels
    private List<NovelModel> novelList = new ArrayList<>(); // List to hold novel data
    private NovelAdapter novelAdapter; // Adapter for the RecyclerView
    private ProgressBar progressBar; // ProgressBar to indicate loading
    private AppCompatSpinner serverSpinner; // Spinner for server sources
    private AppCompatButton pluginButton, continueButton; // Button for download pluins

    private String lastrunName, lastrunChapterName, lastrunServer, lastrunChapterUrl;

    private String[] pluginList = new String[]{"Source: Truyencv", "Format: Html"};
    final boolean[] pluginChecked = new boolean[]{false, false};

    private Handler handler = new Handler(Looper.getMainLooper());
    private final String TRUYENCV_PLUGIN
            = "https://raw.githubusercontent.com/nohiup/NovelReaderPluginHost/main/scraper_truyencvtest_TruyencvScraper.apk";
    private final String HTML_PLUGIN = "https://raw.githubusercontent.com/nohiup/NovelReaderPluginHost/main/exporter_htmlexporter_HtmlExportHandler.apk";

    private final FirebaseStorage storage = FirebaseStorage.getInstance("gs://readerpluginst.appspot.com");
    private final StorageReference ref = storage.getReference();

    private static File downloadDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //Set the layout for this activity

        downloadDir = MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        //Initialize views
        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        serverSpinner = findViewById(R.id.serverSpinner);
        pluginButton = findViewById(R.id.pluginButton);
        continueButton = findViewById(R.id.continueButton);

        //Init server adapter
        ServerSpinnerAdapter serverAdapter = new ServerSpinnerAdapter(this, android.R.layout.simple_spinner_item, GlobalConfig.Global_Source_List);
        serverSpinner.setAdapter(serverAdapter);

        String downloadDirPath = downloadDir.getAbsolutePath();

        //create document directory for exporting:
        File exportDir = MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        File newFolder = ReusableFunction.MakeDirectory(exportDir.getAbsolutePath(), "Export");
        Log.d("created", newFolder.getAbsolutePath());

        //generate directory
        makeDirectory(downloadDirPath);

        //Get last read Novel
        readLastRunLog();

        //Scraper add:
        INovelScraper truyenfull = new TruyenfullScraper();
        INovelScraper tangthuvien = new TangthuvienScraper();
        GlobalConfig.Global_Source_List.add(truyenfull);
        GlobalConfig.Global_Source_List.add(tangthuvien);

        //File format add:
        IChapterExportHandler pdfExport = new PdfExportHandler();
        IChapterExportHandler epubExport = new EpubExportHandler();

        //Add epubExport and pdfExport to the global exporter list
        GlobalConfig.Global_Exporter_List.add(epubExport);
        GlobalConfig.Global_Exporter_List.add(pdfExport);

        // Load all plugins using the specified download directory
        loadAllPlugins(downloadDir);

        // Notify the server adapter that the data set has changed
        serverAdapter.notifyDataSetChanged();
        // Handle the initialization or configuration of the search view
        handleSearchView();

        // Initialize the grid view with the created GridLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 2);
        gridViewInit(gridLayoutManager);

        //Note:Resource getter here
        getMainPageTask();

        // Handle Server Spinner
        serverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                INovelScraper scraperInstance = (INovelScraper) parent.getItemAtPosition(position);
                GlobalConfig.Global_Current_Scraper = scraperInstance;
                getMainPageTask(); //Get the main page task
                Log.d("Source check: ", GlobalConfig.Global_Current_Scraper.getSourceName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastrunChapterName == null) {
                    showNoPreviousDataDialogBox();
                } else {
                    showConfirmationDialogBox();
                }
            }
        });

        // Variables for plugins
        // Initialise the list items for the alert dialog
        final List<String> selectedItems = Arrays.asList(pluginList);

        // Handle Plugin Button
        pluginButton.setOnClickListener(v -> {
            // Initialise the alert dialog builder
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            // Set the title for the alert dialog
            builder.setTitle("Chọn Plugin");

            // Set the icon for the alert dialog
            builder.setIcon(R.drawable.logo);

            // Sets the alert dialog for multiple item selection
            builder.setMultiChoiceItems(pluginList, pluginChecked, (dialog, which, isChecked) -> {
                pluginChecked[which] = isChecked;
                String currentItem = selectedItems.get(which);
            });

            builder.setCancelable(false);

            // Handle the positive button of the dialog
            builder.setPositiveButton("Xác nhận", (dialog, which) -> {
                for (int i = 0; i < pluginChecked.length; i++) {
                    File truyencvPluginFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "scraper_truyencvtest_TruyencvScraper.apk");
                    File htmlPluginFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), getNameFromUrl(HTML_PLUGIN));
                    if (pluginChecked[i]) {
                        if (i == 0) {
                            //Download truyencv.jar
                            Log.d("Download", "truyencv.jar");
                            if (truyencvPluginFile.exists()) return;
                            StorageReference fileRef = ref.child("scraper_truyencvtest_TruyencvScraper.txt");
                            downloadProcessing(fileRef, truyencvPluginFile);
                        }
                        if (i == 1) {
                            //Download http.jar
                            Log.d("Download", "http.jar");

                            if (htmlPluginFile.exists()) return;
                            StorageReference fileRef = ref.child("exporter_htmlexporter_HtmlExportHandler.txt");
                            downloadProcessing(fileRef, htmlPluginFile);
                        }
                    } else if (!pluginChecked[i]) {
                        if (i == 0) {
                            //Download truyencv.jar
                            deletePluginFile(truyencvPluginFile);

                            int index = -1;
                            for (INovelScraper scraper : GlobalConfig.Global_Source_List) {
                                if (scraper.getSourceName().equalsIgnoreCase("Truyencv")) {
                                    index = GlobalConfig.Global_Source_List.indexOf(scraper);
                                }
                            }
                            if (index == -1) return;
                            GlobalConfig.Global_Source_List.remove(index);
                            serverAdapter.notifyDataSetChanged();

                        }
                        if (i == 1) {
                            //Download http.jar
                            deletePluginFile(htmlPluginFile);

                            int index = -1;
                            for (IChapterExportHandler exporter : GlobalConfig.Global_Exporter_List) {
                                if (exporter.getExporterName().equalsIgnoreCase("html")) {
                                    index = GlobalConfig.Global_Exporter_List.indexOf(exporter);
                                }
                            }
                            if (index == -1) return;
                            GlobalConfig.Global_Exporter_List.remove(index);
                            serverAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });

            // Handle the negative button of the alert dialog
            builder.setNegativeButton("Hủy", (dialog, which) -> {
            });

            // Handle the neutral button of the dialog to clear the selected items boolean checkedItem
            builder.setNeutralButton("Bỏ chọn", (dialog, which) -> {
                Arrays.fill(pluginChecked, false);
            });

            // Create the builder
            builder.create();

            // Create the alert dialog with the alert dialog builder instance
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

    private void showConfirmationDialogBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the title and message
        builder.setTitle("Tiếp tục đọc");
        builder.setMessage("Lần trước bạn đang đọc " + lastrunName + ", " + lastrunChapterName + ". \n \nBạn xác nhận muốn tiếp tục đọc?");

        // Set the "OK" button
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle when the user selects "OK"
                // Note: Action implementation required
                for (INovelScraper scraper : GlobalConfig.Global_Source_List) {
                    if (scraper.getSourceName().equals(lastrunServer)) {
                        GlobalConfig.Global_Current_Scraper = scraper;
                        serverSpinner.setSelection(GlobalConfig.Global_Source_List.indexOf(scraper));
                        break;
                    }
                }
                //Note: switch intent to reader mode
                ReusableFunction.ChangeActivityWithString(MainActivity.this, ReadActivity.class,
                        "ChapterUrl", lastrunChapterUrl);
            }
        });

        // Set the "Cancel" button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Create and display an AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void showNoPreviousDataDialogBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the title and message
        builder.setTitle("Tiếp tục đọc");
        builder.setMessage("Không có dữ liệu từ lần đọc trước. Có vẻ như bạn chưa từng sử dụng app.");
        // Set up the "OK" button
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        // Create and display the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void gridViewInit(GridLayoutManager gridLayoutManager) {
        // Set the layout manager for the recyclerView to gridLayoutManager
        recyclerView.setLayoutManager(gridLayoutManager);
        // Create a new instance of NovelAdapter, passing MainActivity context and novelList as parameters
        novelAdapter = new NovelAdapter(MainActivity.this, novelList);
        // Set the adapter for the recyclerView to novelAdapter
        recyclerView.setAdapter(novelAdapter);
    }


    private void handleSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.setQuery("", false);
                ReusableFunction.ChangeActivityWithString(MainActivity.this, SearchActivity.class, "searchQuery", query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // No action on query text change
                return false;
            }
        });
    }

    //task with Pre-execute - need to renew instance every call
    private void getMainPageTask() {
        new BackgroundTask(MainActivity.this) {
            @Override
            public void onPreExecute() {
                // Show and animate the progress bar before starting the task
                progressBar.setVisibility(View.VISIBLE);
                progressBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
            }

            @Override
            public void doInBackground() {
                // Clear the current list and fetch new data
                List<Object> list = GlobalConfig.Global_Current_Scraper.getHomePage();

                List<NovelModel> novels = identifyingList(list);
                ReusableFunction.ReplaceList(novelList, novels);
            }

            @Override
            public void onPostExecute() {
                // Hide and animate the progress bar after the task is completed
                progressBar.setVisibility(View.GONE);
                progressBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_out));

                novelAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private void makeDirectory(String downloadDirPath) {
        // Check if the device is running Android 6.0 (API level 23) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if the app has permission to read external storage
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Request permission to read external storage if it hasn't been granted
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_STORAGE);
            }
        }
        try {
            // Define the path for the plugin file within the download directory
            final String libPath = downloadDirPath + "/myPlugin1.apk";
            // Get or create a directory named "dex" in the app's private storage area
            final File tmpDir = getDir("dex", 0);
            // Note: tmpDir can be used for temporary file storage or other purposes
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Note: on developing
    private void loadAllPlugins(File downloadDir) {
        if (!downloadDir.isDirectory()) return;
        File[] files = downloadDir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) return;
            //note: file is file now.
            //check if it's an apk file
            if (!file.getName().toLowerCase().endsWith(".apk")) return;

            String fileName = file.getName().split("\\.")[0]; //Name Without Extension
            String[] nameHolder = fileName.split("_");

            switch (nameHolder[0]) {
                case "scraper": {
                    String scraperPath = file.getAbsolutePath();
                    Log.d("ScraperPathCheck", scraperPath);
                    loadScraperPlugin(scraperPath, nameHolder[1], nameHolder[2]);
                    break;
                }
                case "exporter": {
                    String exporterPath = file.getAbsolutePath();
                    Log.d("ExporterPathCheck", exporterPath);
                    loadExporterPlugin(exporterPath, nameHolder[1], nameHolder[2]);
                }
                default:
                    break;
            }

        }
    }

    private void loadScraperPlugin(String pluginPath, String classPackage, String className) {
        try {
            // Get or create a directory named "dex" in the app's private storage area
            final File tmpDir = getDir("dex", 0);
            // Create a DexClassLoader to load the plugin's .apk file
            final DexClassLoader classloader = new DexClassLoader(pluginPath, tmpDir.getAbsolutePath(), null, this.getClass().getClassLoader());
            // Load the specified class from the plugin
            // "com.example."+classPackage+"."+className: Fully qualified class name
            Class<?> classToLoad = classloader.loadClass("com.example." + classPackage + "." + className);
            // Instantiate the loaded class and cast it to INovelScraper
            INovelScraper addedScraperPlugin = (INovelScraper) classToLoad.newInstance();
            for (INovelScraper scraper : GlobalConfig.Global_Source_List) {
                if (scraper.getSourceName().equals(addedScraperPlugin.getSourceName())) {
                    Log.d("Add plugin status", "Failed, source exists");
                    return;
                }
            }
            // Add the new scraper plugin to the global source list
            GlobalConfig.Global_Source_List.add(addedScraperPlugin);
            pluginChecked[0] = true;
            Log.d("Added plugin: ", addedScraperPlugin.getSourceName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadExporterPlugin(String pluginPath, String classPackage, String className) {
        try {
            // Create or get a directory named "dex" in the app's private storage area
            final File tmpDir = getDir("dex", 0);
            // Create a DexClassLoader to load the plugin's .apk file
            final DexClassLoader classloader = new DexClassLoader(pluginPath, tmpDir.getAbsolutePath(), null, this.getClass().getClassLoader());
            // Load the specified class from the plugin
            // "com.example."+classPackage+"."+className: Fully qualified class name
            Class<?> classToLoad = classloader.loadClass("com.example." + classPackage + "." + className);
            // Instantiate the loaded class and cast it to IChapterExportHandler
            IChapterExportHandler addedExporterPlugin = (IChapterExportHandler) classToLoad.newInstance();
            for (IChapterExportHandler exporter : GlobalConfig.Global_Exporter_List) {
                if (exporter.getExporterName().equals(addedExporterPlugin.getExporterName())) {
                    Log.d("Add plugin status", "Failed, source exists");
                    return;
                }
            }
            // Add the new exporter plugin to the global exporter list
            GlobalConfig.Global_Exporter_List.add(addedExporterPlugin);
            pluginChecked[1] = true;
            Log.d("Added plugin: ", addedExporterPlugin.getExporterName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<NovelModel> identifyingList(List<Object> list) {
        List<NovelModel> novels = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof NovelModel) {
                novels.add((NovelModel) item);
            } else {
                String[] novelHolder = (String[]) item;
                NovelModel novel = new NovelModel(novelHolder[0], novelHolder[1], novelHolder[2], novelHolder[3]);
                novels.add(novel);
            }
        }
        return novels;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can call your export methods here
            } else {
                // Permission denied, handle the case
            }
        }
        if (requestCode == REQUEST_WRITE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission granted
            } else {
                //permission denied
            }
        }
    }

    //Return to last read Novel
    private void readLastRunLog() {
        //Create a File object representing the "lastrun.log" file in the documents directory within the app's external storage
        File file = new File(MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "lastrun.log");
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                ObjectInputStream in = new ObjectInputStream(fis);
                String[] data = (String[]) in.readObject();

                lastrunServer = data[0];
                lastrunName = data[1];
                lastrunChapterName = data[2];
                lastrunChapterUrl = data[3];

                Log.d("Last run", lastrunServer + " " + lastrunName + " " + lastrunChapterName + " " + lastrunChapterUrl);

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private String getNameFromUrl(String url) {
        String[] holder = url.split("/");
        return holder[holder.length - 1];
    }

    private void deletePluginFile(File file) {
        if (file.exists()) {
            if (file.delete()) {
                Toast.makeText(this, "File deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("No file", "No file");
        }
    }

    private void downloadProcessing(StorageReference reference, File file) {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
        notificationProgressBarInit();
        reference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                //success
                Log.d("SuccessNotif", file.toString());
                // Hide and animate the progress bar after the task is completed
                progressBar.setVisibility(View.GONE);
                progressBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_out));

                notificationFinishInit();

                loadAllPlugins(downloadDir);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Download Error", e.toString());
            }
        });
    }

    private void notificationProgressBarInit() {
        createNotificationChannel();
        int id = 1;
        Log.w("Method check", "reach here");
        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);

        mBuilder.setChannelId(CHANNEL_ID);
        mBuilder.setSmallIcon(R.drawable.ic_launcher_foreground);
        mBuilder.setContentTitle("My app");
        mBuilder.setContentText("Download in progress");
        mBuilder.setSmallIcon(R.drawable.ic_launcher_foreground);
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setAutoCancel(true);
        mBuilder.setProgress(0, 100, true);

        // Issues the notification
        mNotifyManager.notify(id, mBuilder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.novelreader_download);
            String description = "Plugin downloading";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void notificationFinishInit() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);

        builder.setContentTitle("My app").setContentText("Finished");
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        notificationManager.notify(1, builder.build());
    }


}