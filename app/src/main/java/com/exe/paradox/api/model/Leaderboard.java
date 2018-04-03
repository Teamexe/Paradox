package com.exe.paradox.api.model;

import com.google.gson.annotations.SerializedName;

public class Leaderboard {
    @SerializedName("name")
    String name;

    @SerializedName("picture")
    String picture;

    @SerializedName("score")
    int score;

    @SerializedName("level")
    int level;

    public String getName() {
        return name;
    }

    public String getPicture() {
        return picture;
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }
}
