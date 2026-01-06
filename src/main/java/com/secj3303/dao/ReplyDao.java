package com.secj3303.dao;

import java.util.List;

import com.secj3303.model.Reply;

public interface ReplyDao {
    List<Reply> findByPostId(int postId);
    Reply findById(int id);
    int save(Reply reply);
    void update(Reply reply);
    void delete(int id);
}