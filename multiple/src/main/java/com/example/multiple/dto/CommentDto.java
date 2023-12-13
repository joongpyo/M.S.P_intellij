package com.example.multiple.dto;

import java.time.LocalDate;

public class CommentDto {
    private int cId;
    private int bId;
    private String cSubject;
    private String cWriter;
    private String cComment;
    private int cVisit;
    private LocalDate cRegdate;
    private String configCode;

    public String getConfigCode() {
        return configCode;
    }

    public void setConfigCode(String configCode) {
        this.configCode = configCode;
    }

    public int getcId() {
        return cId;
    }

    public void setcId(int cId) {
        this.cId = cId;
    }

    public int getbId() {
        return bId;
    }

    public void setbId(int bId) {
        this.bId = bId;
    }

    public String getcSubject() {
        return cSubject;
    }

    public void setcSubject(String cSubject) {
        this.cSubject = cSubject;
    }

    public String getcWriter() {
        return cWriter;
    }

    public void setcWriter(String cWriter) {
        this.cWriter = cWriter;
    }

    public String getcComment() {
        return cComment;
    }

    public void setcComment(String cComment) {
        this.cComment = cComment;
    }

    public int getcVisit() {
        return cVisit;
    }

    public void setcVisit(int cVisit) {
        this.cVisit = cVisit;
    }

    public LocalDate getcRegdate() {
        return cRegdate;
    }

    public void setcRegdate(LocalDate cRegdate) {
        this.cRegdate = cRegdate;
    }

    @Override
    public String toString() {
        return "CommentDto{" +
                "cId=" + cId +
                ", bId=" + bId +
                ", cSubject='" + cSubject + '\'' +
                ", cWriter='" + cWriter + '\'' +
                ", cComment='" + cComment + '\'' +
                ", cVisit=" + cVisit +
                ", cRegdate=" + cRegdate +
                '}';
    }
}
