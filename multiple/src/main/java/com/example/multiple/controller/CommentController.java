package com.example.multiple.controller;


import com.example.multiple.dto.CommentDto;
import com.example.multiple.mappers.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class CommentController {

    @Autowired
    CommentMapper commentMapper;

    @GetMapping("/comment/commentWrite")
    @ResponseBody
    public Map<String, Object> setCommentWrite(@ModelAttribute CommentDto commentDto) {
        commentMapper.setComment(commentDto);
        return Map.of("msg", "success");
    }

    @GetMapping("/comment/commentList")
    @ResponseBody
    public Map<String, Object> getCommentList(@ModelAttribute CommentDto commentDto) {
        List<CommentDto> cList = commentMapper.getCommentList(commentDto);
        return Map.of("cList", cList);
    }

}
