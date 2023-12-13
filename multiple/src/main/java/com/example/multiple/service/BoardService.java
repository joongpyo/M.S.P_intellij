package com.example.multiple.service;

import com.example.multiple.dto.BoardDto;
import com.example.multiple.dto.FileDto;
import com.example.multiple.dto.PageDto;
import com.example.multiple.mappers.BoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BoardService {

    @Autowired
    BoardMapper boardMapper;

    public int getGrpMaxCnt(String configCode) { //controller
        return boardMapper.getGrpMaxCnt(configCode);
    }

    public void setBoard(BoardDto boardDto) {
        boardMapper.setBoard(boardDto);
    }

    public void setFiles(FileDto fileDto) {
        boardMapper.setFiles(fileDto);
    }

    public PageDto PageInfo(String configCode,
                            int page,
                            String searchType, String words) {
        PageDto pageDto = new PageDto();

        String searchQuery = getSearch(searchType, words);

        int totalCount = boardMapper.getBoardCount(configCode, searchQuery);

        int totalPage = (int) Math.ceil((double) totalCount / pageDto.getPageCount());
        int startPage =  ((int) (Math.ceil((double) page / pageDto.getBlockCount())) - 1) * pageDto.getBlockCount() + 1;
        int endPage = startPage + pageDto.getBlockCount() - 1;

        if( endPage > totalPage ) {
            endPage = totalPage;
        }

        pageDto.setStartNum( (page - 1) * pageDto.getPageCount()  );
        pageDto.setTotalPage(totalPage);
        pageDto.setStartPage(startPage);
        pageDto.setEndPage(endPage);
        pageDto.setPage(page);

        return pageDto;
    }

    public String getSearch(String searchType, String words) {

        String searchQuery = "";
        if( searchType.equals("writer") ) {
            searchQuery = " WHERE writer = '"+words+"'";

        }else if( searchType.equals("content"))  {
            searchQuery = " WHERE content LIKE '%"+words+"%'";

        }else{
            searchQuery = "";
        }

        return searchQuery;
    }

    public List<BoardDto> getBoardList(String configCode,
                                       int page,
                                       String searchType, String words) {

        PageDto pd = PageInfo(configCode, page, searchType, words);
        String searchQuery = getSearch(searchType, words);

        Map<String, Object> map = new HashMap<>();
        map.put("configCode", configCode);
        map.put("startNum", pd.getStartNum());
        map.put("offset", pd.getPageCount());
        map.put("searchQuery", searchQuery);


        return boardMapper.getBoardList(map);
    }

    public BoardDto getBoard(String configCode, int id) {
        return boardMapper.getBoard(configCode, id);
    }

    public List<FileDto> getFiles(String configCode, int id) {
        return boardMapper.getFiles(configCode, id);
    }

    public void getBoardDelete(BoardDto boardDto) {
        boardMapper.getBoardDelete(boardDto);
    }

    public void setFilesDelete(BoardDto boardDto) {
        boardMapper.setFilesDelete(boardDto);
    }


}
