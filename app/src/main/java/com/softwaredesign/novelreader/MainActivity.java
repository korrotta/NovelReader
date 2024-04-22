package com.softwaredesign.novelreader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private List<Novel> novelList;
    private NovelAdapter novelAdapter;
    private Novel novel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);

        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return false;
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Initialize novel List
        novelList = new ArrayList<>();

        // TODO: Add novel parsing from web here

        // Example for testing purposes
        novel = new Novel("A", "Author A", R.drawable.logo, "1234");
        novelList.add(novel);
        novel = new Novel("B", "Author B", R.drawable.logo, "5678");
        novelList.add(novel);
        novel = new Novel("C", "Author C", R.drawable.logo, "91011");
        novelList.add(novel);
        novel = new Novel("D", "Author D", R.drawable.logo, "121314");
        novelList.add(novel);
        novel = new Novel("E", "Author E", R.drawable.logo, "151617");
        novelList.add(novel);

        novelAdapter = new NovelAdapter(MainActivity.this, novelList);
        recyclerView.setAdapter(novelAdapter);
    }

    private void searchList(String text) {
        List<Novel> novelSearchList = new ArrayList<>();
        for (Novel n : novelList) {
            if (n.getName().toLowerCase().contains(text.toLowerCase())) {
                novelSearchList.add(n);
            }
        }

        if (novelSearchList.isEmpty()) {
            Toast.makeText(this, "Not Found", Toast.LENGTH_SHORT).show();
        }
        else {
            novelAdapter.setSearchList(novelSearchList);
        }
    }
}