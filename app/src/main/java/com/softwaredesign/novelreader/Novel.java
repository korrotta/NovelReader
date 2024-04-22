package com.softwaredesign.novelreader;

public class Novel {
    private String Name, Author, Description;
    private int Image;

    public Novel(String name, String author, int image, String description) {
        Name = name;
        Author = author;
        Image = image;
        Description = description;
    }

    public String getName() {
        return Name;
    }

    public String getAuthor() {
        return Author;
    }

    public int getImage() {
        return Image;
    }

    public String getDescription() {
        return Description;
    }

}
