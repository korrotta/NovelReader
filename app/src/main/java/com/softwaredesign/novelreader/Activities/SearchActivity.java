package com.softwaredesign.novelreader.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.softwaredesign.novelreader.Adapters.NovelSearchAdapter;
import com.softwaredesign.novelreader.BackgroundTask;
import com.softwaredesign.novelreader.Global.ReusableFunction;
import com.softwaredesign.novelreader.Models.NovelModel;
import com.softwaredesign.novelreader.R;
import com.softwaredesign.novelreader.Scrapers.TruyenfullScraper;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchSearchView;
    private ListView novelSearchListView;
    private NovelSearchAdapter novelSearchAdapter;

    private ProgressBar searchProgressBar;
    private final TruyenfullScraper truyenfullScraper = new TruyenfullScraper();
    private String searchQuery;
    private LinearLayout searchPageControlLayout;
    private ImageView prevSearchPage, nextSearchPage;
    private TextView searchPageTextView;
    private static volatile int numberOfPages, currentPage, maxPage;
    private static ArrayList<NovelModel> novelList;

    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchSearchView = findViewById(R.id.searchSearchView);
        novelSearchListView = findViewById(R.id.searchListView);
        searchProgressBar = findViewById(R.id.searchProgressBar);
        searchPageControlLayout = findViewById(R.id.searchPageControlLayout);
        prevSearchPage = findViewById(R.id.previousSearchPage);
        nextSearchPage = findViewById(R.id.nextSearchPage);
        searchPageTextView = findViewById(R.id.searchPageTextView);

        // Handle fetch Novel from query
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            searchQuery = bundle.getString("searchQuery");
        }

        // Handle Search View
        searchView();

        if (novelList == null){
            novelList = new ArrayList<>();
            NovelModel novelModel = null;
            novelModel = new NovelModel("Cuộc Đấu Tình Yêu Tàn Khốc","https://truyenfull.vn/cuoc-dau-tinh-yeu-tan-khoc/","Mai Tử","https://static.8cache.com/cover/o/eJzLyTDV13X39Eyv8LZ0LDOL1A8LDvAOMg0xMizz1HeEgtwwE_1ECyOjFD_d0vxCR_1yQwsD3QwLAwAlvxFj/cuoc-dau-tinh-yeu-tan-khoc.jpg");
            novelList.add(novelModel);
            novelModel = new NovelModel("Duyên Tình Một Đêm","https://truyenfull.vn/duyen-tinh-mot-dem/","Đậu Toa","https://static.8cache.com/cover/o/eJzLyTDW1y108sorKfXzi_Ap1w8LDvD2MfYrr4z01HeEgtzQdH3dQOcco3KTtPiScv1yQwsD3QwLAwBWGRLG/duyen-tinh-mot-dem.jpg");
            novelList.add(novelModel);
            novelModel = new NovelModel("Yêu Phu Quân Keo Kiệt","https://truyenfull.vn/yeu-phu-quan-keo-kiet/","Đậu Toa","https://static.8cache.com/cover/o/eJzLyTDV13U2rPJKcjMwLs9z1Q8LDjWOMnfKD_Dw1HeEgty0bH0X3Wwv38ggv1KndP1yQwsD3QwLAwArchHA/yeu-phu-quan-keo-kiet.jpg");
            novelList.add(novelModel);
            novelModel = new NovelModel("Yêu Em Từ Cái Nhìn Đầu Tiên","https://truyenfull.vn/yeu-em-tu-cai-nhin-dau-tien/","Cố Mạn","https://static.8cache.com/cover/o/eJzLyTDT1zWL8EtMLi8Nskwu1g8LDjX2DgjzdS301HeEgtwkX_00zxwPd1dDD92Mcv1yQwsD3QwLAwBFOhIj/yeu-em-tu-cai-nhin-dau-tien.jpg");
            novelList.add(novelModel);
            novelModel = new NovelModel("Cô Dâu Của Trung Tá","https://truyenfull.vn/co-dau-cua-trung-ta/","Hồ Ly","https://static.8cache.com/cover/o/eJzLyTDW1_XOLqrIKnevDPDJ1w8L9vZ0CY0sM_D21HeEgtwgC_0C94x0C3e_-DTnfP1yQwsD3QwLAwBafBKr/co-dau-cua-trung-ta.jpg");
            novelList.add(novelModel);
            novelModel = new NovelModel("Quay Lại Mỉm Cười, Bắt Đầu JQ","https://truyenfull.vn/quay-lai-mim-cuoi-bat-dau-jq/","Đông Bôn Tây Cố","https://static.8cache.com/cover/o/eJzLyTDW1zXQLc2rSPQoTwr31Q8LDjVOzHdJc9L11HeEgty0cn2DCDPPCmeDeN_Kcv1yQwsD3QwLAwBEihJO/quay-lai-mim-cuoi-bat-dau-jq.jpg");
            novelList.add(novelModel);
            novelModel = new NovelModel("Hào Môn Kinh Mộng: 99 Ngày Làm Cô Dâu","https://truyenfull.vn/hao-mon-kinh-mong-99-ngay-lam-co-dau/","Ân Tầm","https://static.8cache.com/cover/o/eJzLyTDT1zUtzszKLsipqrRM1w8LDjUOcsqN9PP01HeEgtwUT_3ASue0LEfTjCAfA_1yQwsD3QwLAwBc9RKb/hao-mon-kinh-mong-99-ngay-lam-co-dau.jpg");
            novelList.add(novelModel);
            novelModel = new NovelModel("Vợ Cũ Của Tổng Tài Lạnh Lùng","https://truyenfull.vn/vo-cu-cua-tong-tai-lanh-lung/","Đậu Đậu Thiền","https://static.8cache.com/cover/o/eJzLyTDW1zVzC9ItynBNLckK1A8L9vb0DIv0Ccj01HeEgtxgC_1A_8BcP93ckAq3UP1yQwsD3QwLAwA6ZxIL/vo-cu-cua-tong-tai-lanh-lung.jpg");
            novelList.add(novelModel);
            novelModel = new NovelModel("Minh Hôn Cái Đầu Anh Á","https://truyenfull.vn/minh-hon-cai-dau-anh-a/","Mị Tinh Nhân","https://static.8cache.com/cover/o/eJzLyTDV1w0pNcn28nUOC0kt1w8L9vb0qvQJyXbz1HeEgtyQSP2QcPfSiNLMpIC0UP1yQwsD3QwLAwBNUhK9/minh-hon-cai-dau-anh-a.jpg");
            novelList.add(novelModel);
            novelModel = new NovelModel("Đấu Thần","https://truyenfull.vn/dau-than/","Yêu Yêu","https://static.8cache.com/cover/o/eJzLyTDW1y2LL3MOiK-IdK301A_zKsuvLDNOKdH11HeEgpzMUP20MOfKgJLIwCLTfP1yQwsD3QwLAwBteRNl/dau-than.jpg");
            novelList.add(novelModel);
        }

        // Set Adapter and ListView
        novelSearchAdapter = new NovelSearchAdapter(SearchActivity.this, novelList);
        novelSearchListView.setAdapter(novelSearchAdapter);
        novelSearchListView.setClickable(true);

        getTotalPagesThenResult.execute();

        // Handle Item Click
        novelSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create an intent to start DetailActivity
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);

                // Pass the novel URL to the DetailActivity
                intent.putExtra("NovelUrl", novelList.get(position).getUrl());

                // Start the DetailActivity
                startActivity(intent);
            }
        });

        // Setup search pagination
        setupPageControls();

        // Handle Paginagtion
        handlePagination();
    }

    private void handlePagination() {
        // Set click listener to show a popup menu for page selection
        searchPageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create a PopupMenu
                PopupMenu popupMenu = new PopupMenu(SearchActivity.this, searchPageTextView);

                // Add pages to the PopupMenu
                for (int i = 1; i <= numberOfPages; i++) {
                    popupMenu.getMenu().add(0, i, i, "Page " + i);
                }

                // Set a click listener for PopupMenu items
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Handle page selection
                        loadPage(item.getItemId());
                        return true;
                    }
                });

                // Set the gravity of the PopupMenu
                popupMenu.setGravity(Gravity.START);

                // Show the PopupMenu
                popupMenu.show();

            }
        });

        // Handle Previous Page Button
        prevSearchPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage <= 1) return;
                currentPage--;
                loadPage(currentPage);

            }
        });

        // Handle Next Page Button
        nextSearchPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage >= numberOfPages) return;
                currentPage++;
                loadPage(currentPage);

            }
        });
    }

    private void setupPageControls() {
        currentPage = 1;

        SearchActivity.this.searchPageControlLayout.setVisibility(View.VISIBLE);
        SearchActivity.this.searchPageTextView.setText("Page 1 of " + numberOfPages);
    }

    // Method to load a specific page of search results
    private void loadPage(int page) {
        currentPage = page;
        getSearchResult();

    }

    private void searchView() {
        searchSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                getTotalPagesThenResult.execute();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // No action on query text change
                return false;
            }
        });
    }


    //Pre-execute needed, renew instance every load
    private void getSearchResult(){
        new BackgroundTask(SearchActivity.this) {
            @Override
            public void onPreExecute () {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        searchProgressBar.setVisibility(View.VISIBLE);
                        searchProgressBar.startAnimation(AnimationUtils.loadAnimation(SearchActivity.this, android.R.anim.fade_in));
                    }
                });
            }

            @Override
            public void doInBackground () {
                //get data from source
                ReusableFunction.ReplaceList(novelList, truyenfullScraper.getSearchPageFromKeywordAndPageNumber(searchQuery, currentPage));
            }

            @Override
            public void onPostExecute () {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        searchProgressBar.setVisibility(View.GONE);
                        searchProgressBar.startAnimation(AnimationUtils.loadAnimation(SearchActivity.this, android.R.anim.fade_out));
                    }
                });
                novelSearchAdapter.notifyDataSetChanged();
                novelSearchListView.smoothScrollToPosition(0);
                SearchActivity.this.searchPageTextView.setText("Page " + currentPage + " of " + numberOfPages);
            }
        }.execute();
    }




    //No pre-execute needed
    private final BackgroundTask getTotalPagesThenResult = new BackgroundTask(SearchActivity.this) {
        @Override
        public void onPreExecute() {
            //Do nothing
        }

        @Override
        public void doInBackground() {
            numberOfPages = truyenfullScraper.getNumberOfSearchResultPage(searchQuery);
        }

        @Override
        public void onPostExecute() {
            setupPageControls();
            if (numberOfPages > 0) loadPage(currentPage);
            else {
                novelList.clear();
                novelSearchAdapter.notifyDataSetChanged();
            }
        }
    };
}