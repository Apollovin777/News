package com.example.yurko.news;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface NewsDAO {

    @Query("SELECT * FROM NewsItem ORDER BY date")
    List<NewsEntry> loadAllNews();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertNewsEntry(NewsEntry newsEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateNewsEntry(NewsEntry newsEntry);

    @Delete
    void deleteNewsEntry(NewsEntry newsEntry);

    @Query("DELETE FROM NewsItem")
    void deleteAllNews();

    @Query("SELECT url FROM NewsItem WHERE url = :newsUrl LIMIT 1")
    String getNewsItemURL(String newsUrl);

}
