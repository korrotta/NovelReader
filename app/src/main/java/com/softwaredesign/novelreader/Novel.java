package com.softwaredesign.novelreader;

import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

public class Novel {
    private String Name, Author, Description, ImageUrl, NovelUrl;

    public Novel(String name, String author, String imageUrl, String novelUrl) {
        Name = name;
        Author = author;
        ImageUrl = imageUrl;
        NovelUrl = novelUrl;
        Description = "";
    }

    public Novel(String name, String author, String description, String imageUrl, String novelUrl) {
        Name = name;
        Author = author;
        Description = description;
        ImageUrl = imageUrl;
        NovelUrl = novelUrl;
    }

    public String getName() {
        return Name;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public String getNovelUrl() {
        return NovelUrl;
    }

}
