package com.example.multiple.controller;

import com.example.multiple.dto.BoardDto;
import com.example.multiple.dto.FileDto;
import com.example.multiple.mappers.BoardMapper;
import com.example.multiple.mappers.ConfigMapper;
import com.example.multiple.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

@Controller
public class BoardController {

    @Value("${fileDir}")
    String fileDir;

    @Autowired
    BoardService boardService;

    @Autowired
    ConfigMapper configMapper;

    @Autowired
    BoardMapper boardMapper;

    @GetMapping("/board/boardList")
    public String getBoardList(@RequestParam String configCode, Model model,
                               @RequestParam(value="page", defaultValue = "1") int page,
                               @RequestParam(value="searchType", defaultValue = "") String searchType,
                               @RequestParam(value="words", defaultValue = "") String words) {
        model.addAttribute("configCode", configCode);
        model.addAttribute("board", boardService.getBoardList(configCode, page, searchType, words));
        model.addAttribute("config", configMapper.getBoardConfig(configCode));
        model.addAttribute("page", boardService.PageInfo(configCode, page, searchType, words));

        String searchQuery = boardService.getSearch(searchType, words);
        model.addAttribute("total", boardMapper.getBoardCount(configCode, searchQuery));

        return "board/boardList";
    }

    @GetMapping("/board/boardWrite")
    public String getBoardWrite(@RequestParam String configCode, Model model) {
        model.addAttribute("configCode", configCode);
        return "board/boardWrite";
    }

    @PostMapping("/board/boardWrite")
    public String setBoardWrite(
            @RequestParam("files") List<MultipartFile> files, @ModelAttribute BoardDto boardDto, Model model) throws IOException {


        int grp = boardService.getGrpMaxCnt(boardDto.getConfigCode());
        /* 답변형 게시판 처리를 위한 부분 */
        boardDto.setGrp(grp);
        boardDto.setSeq(boardDto.getSeq() + 1);


        /* 답변형 게시판 처리를 위한 부분 */

        if( !files.get(0).isEmpty() ) {
            boardDto.setIsFiles("Y");
            boardService.setBoard(boardDto);
            int fileID = boardDto.getId();

            String folderName = new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis());

            File makeFolder = new File(fileDir + folderName);
            if( !makeFolder.exists() ) {
                makeFolder.mkdir();
            }

            FileDto fileDto = new FileDto();
            for(MultipartFile mf : files) {
                String savedPathName = fileDir + folderName;

                String orgName = mf.getOriginalFilename();
                String ext = orgName.substring(orgName.lastIndexOf("."));
                String uuid = UUID.randomUUID().toString();
                String savedFileName = uuid + ext;

                mf.transferTo(new File(savedPathName + "/" + savedFileName));

                fileDto.setConfigCode(boardDto.getConfigCode());
                fileDto.setId(fileID);
                fileDto.setOrgName(orgName);
                fileDto.setSavedFileName(savedFileName);
                fileDto.setSavedPathName(savedPathName);
                fileDto.setFolderName(folderName);
                fileDto.setExt(ext);

                boardService.setFiles(fileDto);
            }

        }else{
            boardService.setBoard(boardDto);
        }

        return "redirect:/board/boardList?configCode="+boardDto.getConfigCode();
    }

    @GetMapping("/board/boardView")
    public String getBoardView(
            @RequestParam String configCode,
            @RequestParam int id, Model model) {

        model.addAttribute("configCode", configCode);
        model.addAttribute("board", boardService.getBoard(configCode, id));
        model.addAttribute("files", boardService.getFiles(configCode, id));

        return "board/boardView";
    }

    @GetMapping("/board/boardDelete")
    public String getBoardDelete(@ModelAttribute BoardDto boardDto) {

        if( !boardDto.getConfigCode().isEmpty() && boardDto.getId() > 0 ) {
            boardService.getBoardDelete(boardDto); //내용 디비
            boardMapper.setCommentDelete(boardDto);

            //파일 삭제
            List<FileDto> files = boardService.getFiles(boardDto.getConfigCode(), boardDto.getId());
            for(FileDto fd: files) {
                File file = new File(fd.getSavedPathName() + "/" + fd.getSavedFileName());
                file.delete();
            }
            boardService.setFilesDelete(boardDto);
        }

        
        return "redirect:/board/boardList?configCode="+boardDto.getConfigCode();
    }

    @GetMapping("/board/download")
    public ResponseEntity<Resource> getDownload(
            @RequestParam String configCode,
            @RequestParam String savedFileName) throws MalformedURLException {

        FileDto fd = boardMapper.getFile(configCode, savedFileName);

        String fileName = fd.getOrgName().substring(0, fd.getOrgName().indexOf("."));
        String extName = savedFileName.substring(savedFileName.lastIndexOf("."));
        String downloadFile = fileName + extName;

        UrlResource resource = new UrlResource("file:" + fd.getSavedPathName() + "/" + fd.getSavedFileName());
        String encodeFileName = UriUtils.encode(downloadFile, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodeFileName + "\"" ;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    @GetMapping("/board/boardReply")
    public String getBoardReply(@RequestParam String configCode,
                                @RequestParam int id,
                                Model model) {
        BoardDto bd = boardMapper.getBoard(configCode, id);
        model.addAttribute("reply", bd);
        model.addAttribute("configCode", configCode);
        return "board/boardReply";
    }

    @PostMapping("/board/boardReply")
    public String setBoardReply(@RequestParam("files") List<MultipartFile> files,
                                @ModelAttribute BoardDto boardDto,
                                Model model) throws IOException {

        /* 계층형 답글 알고리즘 코드 */

        boardMapper.setReplyUpdate(boardDto);
        boardDto.setGrp(boardDto.getGrp());
        boardDto.setSeq(boardDto.getSeq() + 1);
        boardDto.setDepth(boardDto.getDepth() + 1);

        /* 계층형 답글 알고리즘 코드 */

        if( !files.get(0).isEmpty() ) {
            boardDto.setIsFiles("Y");
            boardService.setBoard(boardDto);
            int fileID = boardDto.getId();

            String folderName = new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis());

            File makeFolder = new File(fileDir + folderName);
            if( !makeFolder.exists() ) {
                makeFolder.mkdir();
            }

            FileDto fileDto = new FileDto();
            for(MultipartFile mf : files) {
                String savedPathName = fileDir + folderName;

                String orgName = mf.getOriginalFilename();
                String ext = orgName.substring(orgName.lastIndexOf("."));
                String uuid = UUID.randomUUID().toString();
                String savedFileName = uuid + ext;

                mf.transferTo(new File(savedPathName + "/" + savedFileName));

                fileDto.setConfigCode(boardDto.getConfigCode());
                fileDto.setId(fileID);
                fileDto.setOrgName(orgName);
                fileDto.setSavedFileName(savedFileName);
                fileDto.setSavedPathName(savedPathName);
                fileDto.setFolderName(folderName);
                fileDto.setExt(ext);

                boardService.setFiles(fileDto);
            }

        }else{
            boardService.setBoard(boardDto);
        }

        return "redirect:/board/boardList?configCode="+boardDto.getConfigCode();
    }

}
